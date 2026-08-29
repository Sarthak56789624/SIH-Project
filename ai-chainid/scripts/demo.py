"""
Demonstrates the two required end-to-end scenarios:

  Scenario 1 — Normal Employee:
      Developer -> AI Permission Recommendation -> normal access request
      -> LOW risk -> access can proceed

  Scenario 2 — Compromised Credential:
      Same developer, valid credential, but unknown device / wrong location
      / 2:30 AM / production DB -> HIGH risk -> BLOCKED + admin alert

Run:
    python scripts/demo.py
"""
import sys
from pathlib import Path

import joblib

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

MODELS_DIR = ROOT / "models"


def line():
    print("-" * 70)


def main():
    perm_engine = joblib.load(MODELS_DIR / "permission_engine.joblib")
    risk_engine = joblib.load(MODELS_DIR / "risk_engine.joblib")

    print("#" * 70)
    print("# AI-ChainID — Two AI Features Demo")
    print("# Principle: A valid identity does not automatically mean valid access.")
    print("#" * 70)

    # ----------------------------------------------------------------
    print("\n\n=== FEATURE 1: Permission Recommendation ===")
    print('"What access should this person have?"\n')

    profile = {
        "role": "Software Developer",
        "department": "Engineering",
        "employment_type": "Employee",
        "credentials": ["Backend Development Certification"],
        "projects": ["Project Alpha"],
        "contract_duration_days": 365,
        "previously_approved_similar": True,
    }
    resources = [
        "GitHub Project Alpha",
        "Development Server",
        "Project Management System",
        "Production Database",
        "HR Database",
        "Finance System",
        "Admin Panel",
    ]
    print(f"Profile: {profile}\n")
    recs = perm_engine.recommend(profile, resources)
    for r in recs:
        line()
        print(f"Resource:     {r.resource}")
        print(f"Recommend:    {r.recommendation}  (confidence {r.confidence})")
        print(f"Reason:       {r.reason}")
        print(f"Suggested duration: {r.suggested_duration_days} days" if r.suggested_duration_days else
              "Suggested duration: N/A")
    print("\n>> These are RECOMMENDATIONS ONLY. A manager/admin must approve, modify, or reject each one.")

    # ----------------------------------------------------------------
    print("\n\n=== FEATURE 2 — SCENARIO 1: Normal Employee Access ===")
    print('"Does this access request look suspicious?"\n')

    normal_request = {
        "did": "did:aichainid:8F72A91",
        "role": "Software Developer",
        "department": "Engineering",
        "resource": "Development Server",
        "hour": 11.0,
        "device_known": True,
        "location_match": True,
        "failed_attempts": 0,
        "request_frequency": 2,
        "credential_valid": True,
        "previous_behavior_score": 0.9,
    }
    print(f"Request: {normal_request}\n")
    result1 = risk_engine.assess(normal_request)
    line()
    print(f"Risk Score: {result1.risk_score}/100 — {result1.risk_level}")
    print(f"Decision:   {result1.decision}")
    print(f"Model confidence: {result1.model_confidence}")
    print("Reasons:")
    for r in result1.reasons:
        print(f"  - {r}")

    # ----------------------------------------------------------------
    print("\n\n=== FEATURE 2 — SCENARIO 2: Compromised Credential ===")
    print("Same developer. Valid credential. But everything else changed.\n")

    suspicious_request = {
        "did": "did:aichainid:8F72A91",
        "role": "Software Developer",
        "department": "Engineering",
        "resource": "Production Database",
        "hour": 2.5,
        "device_known": False,
        "location_match": False,
        "failed_attempts": 4,
        "request_frequency": 9,
        "credential_valid": True,
        "previous_behavior_score": 0.2,
    }
    print(f"Request: {suspicious_request}\n")
    result2 = risk_engine.assess(suspicious_request)
    line()
    print(f"*** HIGH RISK ACCESS ATTEMPT ***")
    print(f"Risk Score: {result2.risk_score}/100 — {result2.risk_level}")
    print(f"Decision:   {result2.decision}")
    print(f"Model confidence: {result2.model_confidence}")
    print("Primary factors:")
    for i, r in enumerate(result2.reasons, 1):
        print(f"  {i}. {r}")

    print("\n\n>> KEY TAKEAWAY: The credential was valid in BOTH requests.")
    print(">> Scenario 2 proves: valid credentials can still be compromised or misused.")
    print(">> The system caught it by looking at DEVICE, LOCATION, TIME, and BEHAVIOR —")
    print(">> not just identity.")


if __name__ == "__main__":
    main()
