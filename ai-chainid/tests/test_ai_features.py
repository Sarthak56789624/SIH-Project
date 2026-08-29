"""
Unit and Integration Tests for AI-ChainID AI features.
Run:
    python -m unittest tests/test_ai_features.py
"""
import unittest
from pathlib import Path
import joblib

ROOT = Path(__file__).resolve().parents[1]
MODELS_DIR = ROOT / "models"


class TestAIEngines(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.perm_engine = joblib.load(MODELS_DIR / "permission_engine.joblib")
        cls.risk_engine = joblib.load(MODELS_DIR / "risk_engine.joblib")

    def test_permission_dev_normal(self):
        profile = {
            "role": "Software Developer",
            "department": "Engineering",
            "employment_type": "Employee",
            "credentials": ["Backend Development Certification"],
            "projects": ["Project Alpha"],
            "contract_duration_days": 90,
            "previously_approved_similar": True,
        }
        recs = self.perm_engine.recommend(profile, ["GitHub Project Alpha", "Development Server"])
        self.assertEqual(len(recs), 2)
        for r in recs:
            self.assertEqual(r.recommendation, "ALLOW")
            self.assertGreater(r.confidence, 0.7)

    def test_permission_intern_restriction(self):
        profile = {
            "role": "Software Developer",
            "department": "Engineering",
            "employment_type": "Intern",
            "credentials": [],
            "projects": ["Project Alpha"],
            "contract_duration_days": 30,
            "previously_approved_similar": False,
        }
        recs = self.perm_engine.recommend(profile, ["Production Database", "Admin Panel"])
        self.assertEqual(len(recs), 2)
        for r in recs:
            self.assertEqual(r.recommendation, "DENY")

    def test_risk_normal_access(self):
        req = {
            "did": "did:aichainid:8F72A91",
            "role": "Software Developer",
            "department": "Engineering",
            "resource": "Development Server",
            "hour": 11.0,
            "device_known": True,
            "location_match": True,
            "failed_attempts": 0,
            "request_frequency": 2.0,
            "credential_valid": True,
            "previous_behavior_score": 0.9,
        }
        res = self.risk_engine.assess(req)
        self.assertEqual(res.risk_level, "LOW")
        self.assertLess(res.risk_score, 30.0)
        self.assertEqual(res.decision, "ALLOW")

    def test_risk_night_attack(self):
        req = {
            "did": "did:aichainid:8F72A91",
            "role": "Software Developer",
            "department": "Engineering",
            "resource": "Production Database",
            "hour": 2.5,
            "device_known": False,
            "location_match": False,
            "failed_attempts": 4,
            "request_frequency": 9.5,
            "credential_valid": True,
            "previous_behavior_score": 0.2,
        }
        res = self.risk_engine.assess(req)
        self.assertEqual(res.risk_level, "HIGH")
        self.assertGreater(res.risk_score, 70.0)
        self.assertIn("Unusual access time", res.reasons)


if __name__ == "__main__":
    unittest.main()
