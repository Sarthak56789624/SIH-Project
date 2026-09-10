"""
AI-ChainID — In-Memory Data Store
Holds all runtime state: employees, DIDs, access requests, blockchain records,
audit log, and risk alerts. Thread-safe using a simple lock.
"""

import hashlib
import random
import string
import threading
import time
from dataclasses import dataclass, field
from datetime import datetime, timezone
from typing import Dict, List, Optional

_lock = threading.Lock()

# ─────────────────────────────────────────────────────────────────────────────
# Helpers
# ─────────────────────────────────────────────────────────────────────────────

def _now_iso() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="seconds").replace("+00:00", "Z")


def _gen_did(name: str) -> str:
    """Generate a DID from name + random suffix."""
    suffix = "".join(random.choices(string.digits + "ABCDEF", k=8))
    return f"did:aichainid:{suffix}"


def _gen_block_hash() -> str:
    """Simulate a blockchain TX hash."""
    return "0x" + hashlib.sha256(str(time.time()).encode()).hexdigest()[:40]


def _gen_id() -> str:
    return "".join(random.choices(string.ascii_lowercase + string.digits, k=10))


# ─────────────────────────────────────────────────────────────────────────────
# Data Models
# ─────────────────────────────────────────────────────────────────────────────

@dataclass
class Employee:
    id: str
    did: str
    first_name: str
    last_name: str
    email: str
    role: str
    department: str
    employment_type: str  # Employee | Contractor | Intern
    credentials: List[str]
    projects: List[str]
    contract_duration_days: int
    status: str  # active | offboarded
    created_at: str
    pii_encrypted: bool = True
    pii_stored_offchain: bool = True
    credential_verified: bool = False
    assets_assigned: List[str] = field(default_factory=list)

    def to_dict(self):
        return {
            "id": self.id,
            "did": self.did,
            "firstName": self.first_name,
            "lastName": self.last_name,
            "fullName": f"{self.first_name} {self.last_name}",
            "email": self.email,
            "role": self.role,
            "department": self.department,
            "employmentType": self.employment_type,
            "credentials": self.credentials,
            "projects": self.projects,
            "contractDurationDays": self.contract_duration_days,
            "status": self.status,
            "createdAt": self.created_at,
            "piiEncrypted": self.pii_encrypted,
            "piiStoredOffchain": self.pii_stored_offchain,
            "credentialVerified": self.credential_verified,
            "assetsAssigned": self.assets_assigned,
        }


@dataclass
class AccessRequest:
    id: str
    employee_did: str
    employee_name: str
    role: str
    department: str
    resources: List[str]
    ai_recommendations: List[dict]
    status: str  # pending | approved | rejected
    created_at: str
    decided_at: Optional[str] = None
    decided_by: str = "admin@aichainid.org"
    blockchain_tx: Optional[str] = None
    note: str = ""

    def to_dict(self):
        return {
            "id": self.id,
            "employeeDid": self.employee_did,
            "employeeName": self.employee_name,
            "role": self.role,
            "department": self.department,
            "resources": self.resources,
            "aiRecommendations": self.ai_recommendations,
            "status": self.status,
            "createdAt": self.created_at,
            "decidedAt": self.decided_at,
            "decidedBy": self.decided_by,
            "blockchainTx": self.blockchain_tx,
            "note": self.note,
        }


@dataclass
class BlockchainRecord:
    tx_hash: str
    record_type: str  # access_granted | access_revoked | role_change | offboarding
    subject_did: str
    subject_name: str
    details: dict
    timestamp: str
    block_number: int
    previous_hash: str = "0x0000000000000000000000000000000000000000000000000000000000000000"
    is_break_glass: bool = False
    policy_signature: str = "SIG_SMART_POLICY_VALIDATED"

    def to_dict(self):
        return {
            "txHash": self.tx_hash,
            "recordType": self.record_type,
            "subjectDid": self.subject_did,
            "subjectName": self.subject_name,
            "details": self.details,
            "timestamp": self.timestamp,
            "blockNumber": self.block_number,
            "previousHash": self.previous_hash,
            "isBreakGlass": self.is_break_glass,
            "policySignature": self.policy_signature,
        }


@dataclass
class AuditEntry:
    id: str
    event_type: str
    actor: str
    subject: str
    details: str
    timestamp: str
    severity: str  # info | warning | critical

    def to_dict(self):
        return {
            "id": self.id,
            "eventType": self.event_type,
            "actor": self.actor,
            "subject": self.subject,
            "details": self.details,
            "timestamp": self.timestamp,
            "severity": self.severity,
        }


@dataclass
class RiskAlert:
    id: str
    employee_did: str
    employee_name: str
    risk_score: float
    risk_level: str
    resource: str
    reasons: List[str]
    decision: str
    status: str  # active | resolved
    created_at: str

    def to_dict(self):
        return {
            "id": self.id,
            "employeeDid": self.employee_did,
            "employeeName": self.employee_name,
            "riskScore": self.risk_score,
            "riskLevel": self.risk_level,
            "resource": self.resource,
            "reasons": self.reasons,
            "decision": self.decision,
            "status": self.status,
            "createdAt": self.created_at,
        }


# ─────────────────────────────────────────────────────────────────────────────
# Store Singleton
# ─────────────────────────────────────────────────────────────────────────────

class Store:
    def __init__(self):
        self.employees: Dict[str, Employee] = {}
        self.access_requests: Dict[str, AccessRequest] = {}
        self.blockchain_records: List[BlockchainRecord] = []
        self.audit_log: List[AuditEntry] = []
        self.risk_alerts: List[RiskAlert] = []
        self._block_number: int = 1000
        self._seed_demo_data()

    # ── Helpers ──────────────────────────────────────────────────────────────

    def _next_block(self) -> int:
        self._block_number += random.randint(1, 5)
        return self._block_number

    def _audit(self, event_type: str, actor: str, subject: str, details: str, severity: str = "info"):
        entry = AuditEntry(
            id=_gen_id(),
            event_type=event_type,
            actor=actor,
            subject=subject,
            details=details,
            timestamp=_now_iso(),
            severity=severity,
        )
        self.audit_log.insert(0, entry)

    def _blockchain_record(self, record_type: str, subject_did: str, subject_name: str, details: dict,
                           is_break_glass: bool = False, policy_signature: str = None) -> str:
        prev_hash = self.blockchain_records[0].tx_hash if self.blockchain_records else "0x0000000000000000000000000000000000000000000000000000000000000000"
        tx = _gen_block_hash()
        blk = self._next_block()
        sig = policy_signature or (f"EMERGENCY_OVERRIDE_ADMIN_BLOCK_{blk}" if is_break_glass else f"SIG_ZERO_TRUST_{blk}")
        record = BlockchainRecord(
            tx_hash=tx,
            record_type=record_type,
            subject_did=subject_did,
            subject_name=subject_name,
            details=details,
            timestamp=_now_iso(),
            block_number=blk,
            previous_hash=prev_hash,
            is_break_glass=is_break_glass,
            policy_signature=sig
        )
        self.blockchain_records.insert(0, record)
        return tx

    # ── Employees ────────────────────────────────────────────────────────────

    def create_employee(self, first_name: str, last_name: str, email: str,
                        role: str, department: str, employment_type: str,
                        credentials: List[str], projects: List[str],
                        contract_duration_days: int,
                        assets: List[str] = None) -> Employee:
        with _lock:
            emp_id = _gen_id()
            did = _gen_did(first_name)
            emp = Employee(
                id=emp_id,
                did=did,
                first_name=first_name,
                last_name=last_name,
                email=email,
                role=role,
                department=department,
                employment_type=employment_type,
                credentials=credentials,
                projects=projects,
                contract_duration_days=contract_duration_days,
                status="active",
                created_at=_now_iso(),
                assets_assigned=assets or [],
            )
            self.employees[did] = emp
            # Auto-audit: onboarding
            self._audit("employee_onboarded", "HR Admin", did,
                        f"{first_name} {last_name} onboarded as {role} in {department}", "info")
            # Blockchain record for DID creation
            self._blockchain_record("did_created", did, f"{first_name} {last_name}", {
                "role": role, "department": department, "employmentType": employment_type
            })
            return emp

    def get_employee(self, did: str) -> Optional[Employee]:
        return self.employees.get(did)

    def list_employees(self) -> List[Employee]:
        return list(self.employees.values())

    def verify_credentials(self, did: str) -> Optional[Employee]:
        with _lock:
            emp = self.employees.get(did)
            if not emp:
                return None
            emp.credential_verified = True
            self._audit("credential_verified", "System", did,
                        f"Credentials verified for {emp.first_name} {emp.last_name}", "info")
            return emp

    def change_role(self, did: str, new_role: str, new_department: str) -> Optional[Employee]:
        with _lock:
            emp = self.employees.get(did)
            if not emp:
                return None
            old_role = emp.role
            old_dept = emp.department
            emp.role = new_role
            emp.department = new_department
            self._audit("role_changed", "HR Admin", did,
                        f"{emp.first_name} {emp.last_name}: {old_role} → {new_role} ({old_dept} → {new_department})",
                        "warning")
            self._blockchain_record("role_change", did, f"{emp.first_name} {emp.last_name}", {
                "oldRole": old_role, "newRole": new_role,
                "oldDepartment": old_dept, "newDepartment": new_department,
            })
            return emp

    def offboard_employee(self, did: str) -> Optional[Employee]:
        with _lock:
            emp = self.employees.get(did)
            if not emp:
                return None
            emp.status = "offboarded"
            emp.assets_assigned = []
            # Revoke all pending access requests
            for req in self.access_requests.values():
                if req.employee_did == did and req.status in ("pending", "approved"):
                    req.status = "rejected"
                    req.decided_at = _now_iso()
                    req.note = "Auto-revoked due to offboarding"
            self._audit("employee_offboarded", "HR Admin", did,
                        f"{emp.first_name} {emp.last_name} offboarded — all access revoked", "critical")
            self._blockchain_record("offboarding", did, f"{emp.first_name} {emp.last_name}", {
                "reason": "Employee departure", "accessRevoked": True, "assetsReturned": True
            })
            return emp

    # ── Access Requests ───────────────────────────────────────────────────────

    def create_access_request(self, employee_did: str, employee_name: str,
                               role: str, department: str,
                               resources: List[str],
                               ai_recommendations: List[dict]) -> AccessRequest:
        with _lock:
            req = AccessRequest(
                id=_gen_id(),
                employee_did=employee_did,
                employee_name=employee_name,
                role=role,
                department=department,
                resources=resources,
                ai_recommendations=ai_recommendations,
                status="pending",
                created_at=_now_iso(),
            )
            self.access_requests[req.id] = req
            self._audit("access_request_submitted", employee_name, employee_did,
                        f"Access requested for: {', '.join(resources)}", "info")
            return req

    def approve_access_request(self, req_id: str, approver: str = "admin@aichainid.org") -> Optional[AccessRequest]:
        with _lock:
            req = self.access_requests.get(req_id)
            if not req or req.status != "pending":
                return None
            req.status = "approved"
            req.decided_at = _now_iso()
            req.decided_by = approver
            tx = self._blockchain_record("access_granted", req.employee_did, req.employee_name, {
                "resources": [r["resource"] for r in req.ai_recommendations if r.get("recommendation") == "GRANT"],
                "approvedBy": approver,
                "requestId": req_id,
            })
            req.blockchain_tx = tx
            self._audit("access_approved", approver, req.employee_did,
                        f"Access approved for: {', '.join(req.resources)}", "info")
            return req

    def reject_access_request(self, req_id: str, approver: str = "admin@aichainid.org", reason: str = "") -> Optional[AccessRequest]:
        with _lock:
            req = self.access_requests.get(req_id)
            if not req or req.status != "pending":
                return None
            req.status = "rejected"
            req.decided_at = _now_iso()
            req.decided_by = approver
            req.note = reason
            self._audit("access_rejected", approver, req.employee_did,
                        f"Access rejected for: {', '.join(req.resources)}. Reason: {reason}", "warning")
            return req

    def list_access_requests(self, status: Optional[str] = None) -> List[AccessRequest]:
        reqs = list(self.access_requests.values())
        if status:
            reqs = [r for r in reqs if r.status == status]
        return sorted(reqs, key=lambda r: r.created_at, reverse=True)

    # ── Risk Alerts ───────────────────────────────────────────────────────────

    def add_risk_alert(self, employee_did: str, employee_name: str,
                       risk_score: float, risk_level: str, resource: str,
                       reasons: List[str], decision: str) -> RiskAlert:
        with _lock:
            alert = RiskAlert(
                id=_gen_id(),
                employee_did=employee_did,
                employee_name=employee_name,
                risk_score=risk_score,
                risk_level=risk_level,
                resource=resource,
                reasons=reasons,
                decision=decision,
                status="active" if risk_level in ("HIGH", "CRITICAL") else "resolved",
                created_at=_now_iso(),
            )
            self.risk_alerts.insert(0, alert)
            severity = "critical" if risk_level in ("HIGH", "CRITICAL") else "warning"
            self._audit("risk_alert_generated", "AI Security Engine", employee_did,
                        f"Risk {risk_level} ({risk_score:.1f}) for {employee_name} on {resource}", severity)
            if risk_level in ("HIGH", "CRITICAL"):
                self._blockchain_record("security_alert", employee_did, employee_name, {
                    "riskScore": risk_score, "riskLevel": risk_level,
                    "resource": resource, "reasons": reasons,
                })
            return alert

    def list_risk_alerts(self) -> List[RiskAlert]:
        return self.risk_alerts[:50]

    # ── Statistics ────────────────────────────────────────────────────────────

    def stats(self) -> dict:
        emps = list(self.employees.values())
        reqs = list(self.access_requests.values())
        alerts = self.risk_alerts
        return {
            "totalEmployees": len(emps),
            "activeEmployees": sum(1 for e in emps if e.status == "active"),
            "offboardedEmployees": sum(1 for e in emps if e.status == "offboarded"),
            "totalAccessRequests": len(reqs),
            "pendingRequests": sum(1 for r in reqs if r.status == "pending"),
            "approvedRequests": sum(1 for r in reqs if r.status == "approved"),
            "rejectedRequests": sum(1 for r in reqs if r.status == "rejected"),
            "blockchainRecords": len(self.blockchain_records),
            "auditEvents": len(self.audit_log),
            "activeRiskAlerts": sum(1 for a in alerts if a.status == "active"),
            "totalRiskAlerts": len(alerts),
        }

    # ── Seed Demo Data ────────────────────────────────────────────────────────

    def _seed_demo_data(self):
        """Seed a few demo employees so the platform is not empty on start."""
        demo_employees = [
            ("Alex", "Rivera", "alex.rivera@aichainid.org", "Software Developer", "Engineering",
             "Employee", ["Backend Development Certification"], ["Project Alpha"], 90,
             ["Laptop", "ID Card"]),
            ("Priya", "Sharma", "priya.sharma@aichainid.org", "DevOps Engineer", "Engineering",
             "Employee", ["Cloud Infrastructure Certification"], ["Project Beta"], 90,
             ["Laptop", "ID Card"]),
            ("Marcus", "Chen", "marcus.chen@aichainid.org", "HR Specialist", "HR",
             "Employee", ["HR Compliance Certification"], ["Unassigned"], 90,
             ["Laptop", "ID Card"]),
            ("Sara", "Johnson", "sara.johnson@aichainid.org", "Finance Analyst", "Finance",
             "Employee", ["Financial Systems Certification"], ["Unassigned"], 90,
             ["Laptop", "ID Card"]),
            ("Dev", "Contractor", "dev.contractor@example.org", "Contractor", "Engineering",
             "Contractor", ["None"], ["Project Gamma"], 30,
             ["ID Card"]),
        ]
        for fn, ln, email, role, dept, emp_type, creds, projects, dur, assets in demo_employees:
            emp = self.create_employee(fn, ln, email, role, dept, emp_type, creds, projects, dur, assets)
            # Mark first 3 as credential-verified
            if emp_type == "Employee" and role != "Contractor":
                self.verify_credentials(emp.did)


# Module-level singleton
_store = Store()


def get_store() -> Store:
    return _store
