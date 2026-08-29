"""
AI-ChainID backend — exposes the two AI features as clean REST endpoints.

    POST /ai/recommend-permissions
    POST /ai/risk-score

Run:
    uvicorn app.main:app --reload --port 8000

Then try:
    http://localhost:8000/docs   (interactive Swagger UI)
"""
from pathlib import Path
from typing import List, Optional

import joblib
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse, RedirectResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel, Field

ROOT = Path(__file__).resolve().parents[1]
MODELS_DIR = ROOT / "models"
STATIC_DIR = Path(__file__).resolve().parent / "static"

app = FastAPI(
    title="AI-ChainID AI Services",
    description="AI-Based Permission Recommendation + AI-Based Risk & Anomaly Detection. "
                "A valid identity does not automatically mean valid access.",
    version="1.0.0",
)

# Enable CORS for all origins so frontend testing works seamlessly
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


_perm_engine = None
_risk_engine = None


@app.on_event("startup")
def load_models():
    global _perm_engine, _risk_engine
    perm_path = MODELS_DIR / "permission_engine.joblib"
    risk_path = MODELS_DIR / "risk_engine.joblib"
    if not perm_path.exists() or not risk_path.exists():
        raise RuntimeError(
            "Trained models not found. Run `python scripts/train_models.py` first."
        )
    _perm_engine = joblib.load(perm_path)
    _risk_engine = joblib.load(risk_path)


# --------------------------------------------------------------------------
# Feature 1: Permission Recommendation
# --------------------------------------------------------------------------
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
    note: str = "These are AI-generated recommendations only. Final access must be approved by an administrator or manager."


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


# --------------------------------------------------------------------------
# Feature 2: Risk & Anomaly Detection
# --------------------------------------------------------------------------
class RiskRequest(BaseModel):
    did: str
    role: str
    department: Optional[str] = None
    resource: str
    time: str = Field(..., description="24h time as HH:MM, e.g. '02:30'")
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


def _parse_hour(time_str: str) -> float:
    try:
        h, m = time_str.split(":")
        return int(h) + int(m) / 60
    except Exception:
        raise HTTPException(status_code=400, detail="`time` must be in HH:MM 24h format, e.g. '02:30'")


@app.post("/ai/risk-score", response_model=RiskResponse)
def risk_score(req: RiskRequest):
    hour = _parse_hour(req.time)
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
    return RiskResponse(
        riskScore=result.risk_score,
        riskLevel=result.risk_level,
        decision=result.decision,
        reasons=result.reasons,
        modelConfidence=result.model_confidence,
    )


@app.get("/health")
def health():
    return {"status": "ok", "models_loaded": _perm_engine is not None and _risk_engine is not None}
