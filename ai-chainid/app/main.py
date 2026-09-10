"""
AI-ChainID — Unified Platform Backend
======================================
Single FastAPI application that:
  1. Serves the complete frontend SPA (GET /)
  2. Exposes all 10 workflow steps as REST endpoints
  3. Runs both AI engines (Permission RF + Risk RF + IsolationForest)

Workflow endpoints:
  POST /api/onboard                       Step 1-3: Onboard employee → DID → PII stored
  POST /api/employees/{did}/verify        Step 4:   Credential verification
  POST /api/access-requests               Step 5:   AI access recommendation + create request
  POST /api/access-requests/{id}/approve  Step 6:   Admin approval
  POST /api/access-requests/{id}/reject   Step 6:   Admin rejection
  POST /api/employees/{did}/change-role   Step 9:   Role change → AI re-analysis
  POST /api/employees/{did}/offboard      Step 10:  Offboarding

Data endpoints:
  GET  /api/stats
  GET  /api/employees
  GET  /api/employees/{did}
  GET  /api/access-requests
  GET  /api/blockchain
  GET  /api/audit-log
  GET  /api/risk-alerts

AI direct endpoints:
  POST /ai/recommend-permissions
  POST /ai/risk-score
  GET  /health
"""

from pathlib import Path
from typing import List, Optional

import joblib
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse, RedirectResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel, Field

from .store import get_store

ROOT = Path(__file__).resolve().parents[1]
MODELS_DIR = ROOT / "models"
STATIC_DIR = Path(__file__).resolve().parent / "static"

app = FastAPI(
    title="AI-ChainID Unified Platform",
    description=(
        "Zero-Trust Identity & Access Management — 10-step workflow "
        "powered by AI Permission Recommendation and AI Security Monitoring."
    ),
    version="2.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

if STATIC_DIR.exists():
    app.mount("/static", StaticFiles(directory=STATIC_DIR), name="static")


@app.get("/", include_in_schema=False)
@app.get("/ui", include_in_schema=False)
def root():
    index_file = STATIC_DIR / "index.html"
    if index_file.exists():
        return FileResponse(index_file)
    return RedirectResponse(url="/docs")


@app.get("/login", include_in_schema=False)
def login_page():
    login_file = STATIC_DIR / "login.html"
    if login_file.exists():
        return FileResponse(login_file)
    return RedirectResponse(url="/")


# ─────────────────────────────────────────────────────────────────────────────
# AI Model Loading
# ─────────────────────────────────────────────────────────────────────────────

_perm_engine = None
_risk_engine = None


@app.on_event("startup")
def load_models():
    global _perm_engine, _risk_engine
    perm_path = MODELS_DIR / "permission_engine.joblib"
    risk_path = MODELS_DIR / "risk_engine.joblib"
    if not perm_path.exists() or not risk_path.exists():
        raise RuntimeError("Trained models not found. Run `python scripts/train_models.py` first.")
    _perm_engine = joblib.load(perm_path)
    _risk_engine = joblib.load(risk_path)


# ─────────────────────────────────────────────────────────────────────────────
# Pydantic Request/Response Models
# ─────────────────────────────────────────────────────────────────────────────

class OnboardRequest(BaseModel):
    firstName: str
    lastName: str
    email: str
    role: str
    department: str
    employmentType: str = "Employee"
    credentials: List[str] = Field(default_factory=list)
    projects: List[str] = Field(default_factory=list)
    contractDurationDays: int = 90
    assets: List[str] = Field(default_factory=list)


class ChangeRoleRequest(BaseModel):
    newRole: str
    newDepartment: str
    resources: Optional[List[str]] = None


class AccessRequestCreate(BaseModel):
    employeeDid: str
    resources: List[str]


class ApproveRejectRequest(BaseModel):
    approver: str = "admin@aichainid.org"
    reason: str = ""


# AI Direct models
class PermissionRequest(BaseModel):
    role: str
    department: str
    employment_type: str = "Employee"
    credentials: List[str] = Field(default_factory=list)
    projects: List[str] = Field(default_factory=list)
    resources: List[str]
    contract_duration_days: int = 90
    previously_approved_similar: bool = False


class PermissionItem(BaseModel):
    resource: str
    recommendation: str
    confidence: float
    reason: str
    suggestedDuration: Optional[str] = None


class PermissionResponse(BaseModel):
    recommendations: List[PermissionItem]
    note: str = "AI-generated recommendations only. Final access must be approved by an administrator."


class RiskRequest(BaseModel):
    did: str
    role: str
    department: Optional[str] = None
    resource: str
    time: str = Field(..., description="24h time HH:MM e.g. '14:30'")
    deviceKnown: bool = True
    locationMatch: bool = True
    failedAttempts: int = 0
    requestFrequency: float = 1.0
    credentialValid: bool = True
    previousBehaviorScore: float = 0.8


class RiskResponse(BaseModel):
    riskScore: float
    riskLevel: str
    decision: str
    reasons: List[str]
    modelConfidence: float


# ─────────────────────────────────────────────────────────────────────────────
# Internal helpers
# ─────────────────────────────────────────────────────────────────────────────

def _parse_hour(time_str: str) -> float:
    try:
        h, m = time_str.split(":")
        return int(h) + int(m) / 60
    except Exception:
        raise HTTPException(status_code=400, detail="`time` must be HH:MM format e.g. '14:30'")


def _run_perm_ai(profile: dict, resources: List[str]) -> List[dict]:
    recs = _perm_engine.recommend(profile, resources)
    return [
        {
            "resource": r.resource,
            "recommendation": r.recommendation,
            "confidence": round(r.confidence, 4),
            "reason": r.reason,
            "suggestedDuration": f"{r.suggested_duration_days} days" if r.suggested_duration_days else None,
        }
        for r in recs
    ]


# ─────────────────────────────────────────────────────────────────────────────
# WORKFLOW STEP 1–3: Employee Onboarding + DID Creation + PII Storage
# ─────────────────────────────────────────────────────────────────────────────

@app.post("/api/onboard", summary="Step 1-3: Onboard employee, generate DID, store PII")
def onboard_employee(req: OnboardRequest):
    store = get_store()
    emp = store.create_employee(
        first_name=req.firstName,
        last_name=req.lastName,
        email=req.email,
        role=req.role,
        department=req.department,
        employment_type=req.employmentType,
        credentials=req.credentials,
        projects=req.projects,
        contract_duration_days=req.contractDurationDays,
        assets=req.assets,
    )
    return {
        "success": True,
        "message": f"Employee onboarded successfully. DID assigned: {emp.did}",
        "employee": emp.to_dict(),
    }


# ─────────────────────────────────────────────────────────────────────────────
# WORKFLOW STEP 4: Credential Verification
# ─────────────────────────────────────────────────────────────────────────────

@app.post("/api/employees/{did}/verify", summary="Step 4: Verify employee credentials")
def verify_credentials(did: str):
    store = get_store()
    emp = store.verify_credentials(did)
    if not emp:
        raise HTTPException(status_code=404, detail="Employee not found")
    return {
        "success": True,
        "message": "Credentials verified and linked to DID",
        "employee": emp.to_dict(),
    }


# ─────────────────────────────────────────────────────────────────────────────
# WORKFLOW STEP 5: AI Access Recommendation + Create Request
# ─────────────────────────────────────────────────────────────────────────────

@app.post("/api/access-requests", summary="Step 5: AI recommends access + creates request")
def create_access_request(req: AccessRequestCreate):
    store = get_store()
    emp = store.get_employee(req.employeeDid)
    if not emp:
        raise HTTPException(status_code=404, detail="Employee not found")
    if emp.status == "offboarded":
        raise HTTPException(status_code=400, detail="Cannot request access for an offboarded employee")

    profile = {
        "role": emp.role,
        "department": emp.department,
        "employment_type": emp.employment_type,
        "credentials": emp.credentials,
        "projects": emp.projects,
        "contract_duration_days": emp.contract_duration_days,
        "previously_approved_similar": False,
    }
    ai_recs = _run_perm_ai(profile, req.resources)

    access_req = store.create_access_request(
        employee_did=emp.did,
        employee_name=f"{emp.first_name} {emp.last_name}",
        role=emp.role,
        department=emp.department,
        resources=req.resources,
        ai_recommendations=ai_recs,
    )
    return {
        "success": True,
        "message": "Access request created with AI recommendations",
        "request": access_req.to_dict(),
    }


# ─────────────────────────────────────────────────────────────────────────────
# WORKFLOW STEP 6: Admin Approval / Rejection
# ─────────────────────────────────────────────────────────────────────────────

@app.post("/api/access-requests/{req_id}/approve", summary="Step 6: Admin approves access")
def approve_request(req_id: str, body: ApproveRejectRequest = ApproveRejectRequest()):
    store = get_store()
    req = store.approve_access_request(req_id, body.approver)
    if not req:
        raise HTTPException(status_code=404, detail="Request not found or not in pending state")
    return {
        "success": True,
        "message": "Access approved and recorded on blockchain",
        "request": req.to_dict(),
    }


@app.post("/api/access-requests/{req_id}/reject", summary="Step 6: Admin rejects access")
def reject_request(req_id: str, body: ApproveRejectRequest = ApproveRejectRequest()):
    store = get_store()
    req = store.reject_access_request(req_id, body.approver, body.reason)
    if not req:
        raise HTTPException(status_code=404, detail="Request not found or not in pending state")
    return {
        "success": True,
        "message": "Access request rejected",
        "request": req.to_dict(),
    }


# ─────────────────────────────────────────────────────────────────────────────
# WORKFLOW STEP 8: AI Security Monitoring (direct endpoint)
# ─────────────────────────────────────────────────────────────────────────────

@app.post("/ai/risk-score", response_model=RiskResponse, summary="Step 8: AI risk & anomaly detection")
def risk_score(req: RiskRequest):
    hour = _parse_hour(req.time)
    store = get_store()
    request = {
        "did": req.did,
        "role": req.role,
        "department": req.department,
        "resource": req.resource,
        "hour": hour,
        "device_known": req.deviceKnown,
        "location_match": req.locationMatch,
        "failed_attempts": req.failedAttempts,
        "request_frequency": req.requestFrequency,
        "credential_valid": req.credentialValid,
        "previous_behavior_score": req.previousBehaviorScore,
    }
    result = _risk_engine.assess(request)

    # Find employee name for alert
    emp = store.get_employee(req.did)
    emp_name = f"{emp.first_name} {emp.last_name}" if emp else req.did

    store.add_risk_alert(
        employee_did=req.did,
        employee_name=emp_name,
        risk_score=result.risk_score,
        risk_level=result.risk_level,
        resource=req.resource,
        reasons=result.reasons,
        decision=result.decision,
    )

    return RiskResponse(
        riskScore=result.risk_score,
        riskLevel=result.risk_level,
        decision=result.decision,
        reasons=result.reasons,
        modelConfidence=result.model_confidence,
    )


# ─────────────────────────────────────────────────────────────────────────────
# WORKFLOW STEP 9: Role Change + AI Re-analysis
# ─────────────────────────────────────────────────────────────────────────────

@app.post("/api/employees/{did}/change-role", summary="Step 9: Role change triggers AI re-analysis")
def change_role(did: str, req: ChangeRoleRequest):
    store = get_store()
    emp = store.change_role(did, req.newRole, req.newDepartment)
    if not emp:
        raise HTTPException(status_code=404, detail="Employee not found")

    # Auto-run AI re-analysis on all standard resources if not specified
    from .domain import RESOURCES
    resources = req.resources or RESOURCES
    profile = {
        "role": emp.role,
        "department": emp.department,
        "employment_type": emp.employment_type,
        "credentials": emp.credentials,
        "projects": emp.projects,
        "contract_duration_days": emp.contract_duration_days,
        "previously_approved_similar": False,
    }
    ai_recs = _run_perm_ai(profile, resources)

    return {
        "success": True,
        "message": f"Role updated to {emp.role}. AI re-analyzed access permissions.",
        "employee": emp.to_dict(),
        "aiRecommendations": ai_recs,
    }


# ─────────────────────────────────────────────────────────────────────────────
# WORKFLOW STEP 10: Offboarding
# ─────────────────────────────────────────────────────────────────────────────

@app.post("/api/employees/{did}/offboard", summary="Step 10: Offboard employee, revoke all access")
def offboard_employee(did: str):
    store = get_store()
    emp = store.offboard_employee(did)
    if not emp:
        raise HTTPException(status_code=404, detail="Employee not found")
    return {
        "success": True,
        "message": f"{emp.first_name} {emp.last_name} offboarded. All access revoked. Blockchain audit preserved.",
        "employee": emp.to_dict(),
    }


# ─────────────────────────────────────────────────────────────────────────────
# DATA LISTING ENDPOINTS
# ─────────────────────────────────────────────────────────────────────────────

@app.get("/api/stats", summary="Platform statistics")
def get_stats():
    return get_store().stats()


@app.get("/api/employees", summary="List all employees")
def list_employees():
    emps = get_store().list_employees()
    return {"employees": [e.to_dict() for e in emps], "total": len(emps)}


@app.get("/api/employees/{did}", summary="Get employee by DID")
def get_employee(did: str):
    emp = get_store().get_employee(did)
    if not emp:
        raise HTTPException(status_code=404, detail="Employee not found")
    return emp.to_dict()


@app.get("/api/access-requests", summary="List access requests")
def list_access_requests(status: Optional[str] = None):
    reqs = get_store().list_access_requests(status)
    return {"requests": [r.to_dict() for r in reqs], "total": len(reqs)}


@app.get("/api/blockchain", summary="Blockchain audit records")
def get_blockchain():
    records = get_store().blockchain_records
    return {"records": [r.to_dict() for r in records], "total": len(records)}


@app.get("/api/audit-log", summary="Full audit log")
def get_audit_log():
    entries = get_store().audit_log
    return {"entries": [e.to_dict() for e in entries], "total": len(entries)}


@app.get("/api/risk-alerts", summary="AI security risk alerts")
def get_risk_alerts():
    alerts = get_store().list_risk_alerts()
    return {"alerts": [a.to_dict() for a in alerts], "total": len(alerts)}


# ─────────────────────────────────────────────────────────────────────────────
# AI DIRECT ENDPOINT — Permission Recommendation (standalone)
# ─────────────────────────────────────────────────────────────────────────────

@app.post("/ai/recommend-permissions", response_model=PermissionResponse)
def recommend_permissions(req: PermissionRequest):
    profile = {
        "role": req.role,
        "department": req.department,
        "employment_type": req.employment_type,
        "credentials": req.credentials,
        "projects": req.projects,
        "contract_duration_days": req.contract_duration_days,
        "previously_approved_similar": req.previously_approved_similar,
    }
    recs = _perm_engine.recommend(profile, req.resources)
    items = [
        PermissionItem(
            resource=r.resource,
            recommendation=r.recommendation,
            confidence=r.confidence,
            reason=r.reason,
            suggestedDuration=f"{r.suggested_duration_days} days" if r.suggested_duration_days else None,
        )
        for r in recs
    ]
    return PermissionResponse(recommendations=items)


@app.get("/health")
def health():
    store = get_store()
    return {
        "status": "ok",
        "models_loaded": _perm_engine is not None and _risk_engine is not None,
        "platform": "AI-ChainID Unified v2.0",
        **store.stats(),
    }
