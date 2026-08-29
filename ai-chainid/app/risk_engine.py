"""
FEATURE 2 — AI-Based Risk & Anomaly Detection

Answers: "Does this access request look suspicious?"

Design (hybrid ML + rule-based, per the spec):
  1. A deterministic, weighted rule-based scorer acts as the explainability
     backbone and as the label generator for synthetic training data. It
     directly encodes "valid credentials != valid access": credential
     validity is only one of many signals.
  2. A RandomForestRegressor is trained on the synthetic access-log dataset
     to predict a continuous 0-100 risk score, so the system generalizes
     beyond the exact rule thresholds and exposes feature_importances_.
  3. An IsolationForest is trained ONLY on low-risk ("normal") traffic to
     catch novel anomalies that don't match any known rule pattern
     (unsupervised outlier detection), and blended into the final score.

This demonstrates continuous monitoring: every access event (not just
login) can be scored independently and compared against the user's own
established baseline behavior.
"""

from __future__ import annotations

import random
from dataclasses import dataclass
from typing import List, Optional

import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestRegressor, IsolationForest
from sklearn.metrics import mean_absolute_error, r2_score

from app.domain import RESOURCE_SENSITIVITY, RESOURCE_DEPARTMENT_AFFINITY, ROLES, DEPARTMENTS, RESOURCES, Sensitivity

FEATURE_COLUMNS = [
    "resource_sensitivity",
    "is_unusual_time",
    "known_device",
    "location_match",
    "role_resource_match",
    "failed_attempts",
    "request_frequency",
    "credential_valid",
    "previous_behavior_score",
]

RULE_WEIGHTS = {
    "resource_sensitivity": 10,      # per sensitivity level (1-4) -> up to 40
    "is_unusual_time": 15,
    "unknown_device": 20,
    "location_mismatch": 20,
    "role_resource_mismatch": 20,
    "failed_attempts": 8,            # per attempt, capped
    "request_frequency": 5,          # per unit over baseline, capped
    "invalid_credential": 15,
    "bad_previous_behavior": 10,
}


def _is_unusual_hour(hour: float) -> bool:
    # Normal working hours assumed 7:00 - 20:00
    return hour < 7 or hour > 20


def rule_based_score(
    resource_sensitivity: int,
    hour: float,
    known_device: bool,
    location_match: bool,
    role_resource_match: bool,
    failed_attempts: int,
    request_frequency: float,
    credential_valid: bool,
    previous_behavior_score: float,  # 0 (bad/erratic) - 1 (great/consistent)
) -> (float, List[str]):
    """Deterministic, explainable rule-based risk score in [0, 100]."""
    score = 0.0
    reasons = []

    score += resource_sensitivity * RULE_WEIGHTS["resource_sensitivity"] / 4  # normalize to /10 per level roughly
    if resource_sensitivity >= Sensitivity.HIGH:
        reasons.append("Sensitive resource")

    if _is_unusual_hour(hour):
        score += RULE_WEIGHTS["is_unusual_time"]
        reasons.append("Unusual access time")

    if not known_device:
        score += RULE_WEIGHTS["unknown_device"]
        reasons.append("Unknown device")

    if not location_match:
        score += RULE_WEIGHTS["location_mismatch"]
        reasons.append("Location mismatch")

    if not role_resource_match:
        score += RULE_WEIGHTS["role_resource_mismatch"]
        reasons.append("Resource not associated with user's role")

    score += min(failed_attempts, 5) * RULE_WEIGHTS["failed_attempts"]
    if failed_attempts >= 2:
        reasons.append("Multiple failed access attempts")
    elif failed_attempts == 1:
        reasons.append("One recent failed attempt")

    over_baseline = max(0.0, request_frequency - 5)
    score += min(over_baseline, 6) * RULE_WEIGHTS["request_frequency"]
    if over_baseline > 0:
        reasons.append("Unusually high request frequency")

    if not credential_valid:
        score += RULE_WEIGHTS["invalid_credential"]
        reasons.append("Invalid credential")

    score += (1 - previous_behavior_score) * RULE_WEIGHTS["bad_previous_behavior"]
    if previous_behavior_score < 0.4:
        reasons.append("Deviates from user's historical behavior pattern")

    score = float(np.clip(score, 0, 100))
    if not reasons:
        reasons = ["Normal working time", "Known device", "Expected location",
                    "Role matches resource", "Valid credential", "Normal access behavior"]
    return score, reasons


def risk_level(score: float) -> str:
    if score <= 30:
        return "LOW"
    if score <= 70:
        return "MEDIUM"
    return "HIGH"


def decision_for_level(level: str) -> str:
    return {"LOW": "ALLOW", "MEDIUM": "ADMIN_REVIEW", "HIGH": "BLOCK"}[level]


# --------------------------------------------------------------------------
# Synthetic access-log dataset generation
# --------------------------------------------------------------------------
def generate_risk_dataset(n_samples: int = 8000, seed: int = 7) -> pd.DataFrame:
    rng = random.Random(seed)
    rows = []
    for _ in range(n_samples):
        role = rng.choice(ROLES)
        department = rng.choice(DEPARTMENTS)
        resource = rng.choice(RESOURCES)
        sensitivity = int(RESOURCE_SENSITIVITY[resource])
        role_resource_match = department in RESOURCE_DEPARTMENT_AFFINITY[resource]

        # ~70% normal events, ~30% anomalous-leaning events
        is_anomalous_scenario = rng.random() < 0.3

        if is_anomalous_scenario:
            hour = rng.choice([rng.uniform(0, 6), rng.uniform(21, 24)])
            known_device = rng.random() < 0.25
            location_match = rng.random() < 0.25
            failed_attempts = rng.choice([0, 1, 2, 3, 4, 5])
            request_frequency = rng.uniform(3, 15)
            credential_valid = rng.random() < 0.85  # compromised creds are often still "valid"
            previous_behavior_score = rng.uniform(0.0, 0.6)
        else:
            hour = rng.uniform(7, 20)
            known_device = rng.random() < 0.92
            location_match = rng.random() < 0.9
            failed_attempts = rng.choice([0, 0, 0, 0, 1])
            request_frequency = rng.uniform(0, 5)
            credential_valid = rng.random() < 0.98
            previous_behavior_score = rng.uniform(0.6, 1.0)

        score, _ = rule_based_score(
            sensitivity, hour, known_device, location_match, role_resource_match,
            failed_attempts, request_frequency, credential_valid, previous_behavior_score,
        )
        # small noise to avoid the ML model trivially memorizing the rule
        score = float(np.clip(score + rng.uniform(-4, 4), 0, 100))

        rows.append({
            "role": role,
            "department": department,
            "resource": resource,
            "resource_sensitivity": sensitivity,
            "hour": round(hour, 2),
            "is_unusual_time": int(_is_unusual_hour(hour)),
            "known_device": int(known_device),
            "location_match": int(location_match),
            "role_resource_match": int(role_resource_match),
            "failed_attempts": failed_attempts,
            "request_frequency": round(request_frequency, 2),
            "credential_valid": int(credential_valid),
            "previous_behavior_score": round(previous_behavior_score, 3),
            "risk_score": round(score, 1),
            "risk_label": risk_level(score),
        })
    return pd.DataFrame(rows)


# --------------------------------------------------------------------------
# Model wrapper
# --------------------------------------------------------------------------
@dataclass
class RiskAssessment:
    risk_score: float
    risk_level: str
    decision: str
    reasons: List[str]
    model_confidence: float


class RiskEngine:
    def __init__(self):
        self.regressor = RandomForestRegressor(n_estimators=300, max_depth=10, random_state=7)
        self.iso_forest = IsolationForest(n_estimators=200, contamination=0.15, random_state=7)
        self._fitted = False
        self.feature_importances_: Optional[pd.Series] = None
        self.eval_metrics_ = {}

    def fit(self, df: pd.DataFrame, test_size: float = 0.2):
        from sklearn.model_selection import train_test_split
        from sklearn.metrics import precision_recall_fscore_support, confusion_matrix

        X = df[FEATURE_COLUMNS]
        y = df["risk_score"]
        y_label = df["risk_label"]

        X_train, X_test, y_train, y_test, ylab_train, ylab_test = train_test_split(
            X, y, y_label, test_size=test_size, random_state=7, stratify=y_label
        )

        self.regressor.fit(X_train, y_train)

        # Isolation Forest trained only on normal (LOW risk) traffic
        normal_mask = ylab_train == "LOW"
        self.iso_forest.fit(X_train[normal_mask])

        # Evaluation
        pred_scores = self.regressor.predict(X_test)
        mae = mean_absolute_error(y_test, pred_scores)
        r2 = r2_score(y_test, pred_scores)

        pred_labels = [risk_level(s) for s in pred_scores]
        precision, recall, f1, _ = precision_recall_fscore_support(
            ylab_test, pred_labels, labels=["LOW", "MEDIUM", "HIGH"], average=None, zero_division=0
        )
        cm = confusion_matrix(ylab_test, pred_labels, labels=["LOW", "MEDIUM", "HIGH"])

        self.eval_metrics_ = {
            "mae": round(float(mae), 2),
            "r2": round(float(r2), 3),
            "precision_per_class": dict(zip(["LOW", "MEDIUM", "HIGH"], [round(p, 3) for p in precision])),
            "recall_per_class": dict(zip(["LOW", "MEDIUM", "HIGH"], [round(r, 3) for r in recall])),
            "f1_per_class": dict(zip(["LOW", "MEDIUM", "HIGH"], [round(f, 3) for f in f1])),
            "confusion_matrix": cm.tolist(),
            "confusion_matrix_labels": ["LOW", "MEDIUM", "HIGH"],
        }

        self.feature_importances_ = pd.Series(
            self.regressor.feature_importances_, index=FEATURE_COLUMNS
        ).sort_values(ascending=False)

        self._fitted = True
        return self

    def assess(self, request: dict) -> RiskAssessment:
        if not self._fitted:
            raise RuntimeError("Model not fitted. Call fit() or load a trained model first.")

        resource = request["resource"]
        sensitivity = int(RESOURCE_SENSITIVITY.get(resource, Sensitivity.MEDIUM))
        role = request.get("role", "")
        department = request.get("department")
        if department is None:
            # infer plausible department for role-resource match check if not given
            role_resource_match = request.get("role_resource_match")
            if role_resource_match is None:
                role_resource_match = True  # unknown -> don't penalize unfairly
        else:
            role_resource_match = department in RESOURCE_DEPARTMENT_AFFINITY.get(resource, set())

        hour = request["hour"]
        known_device = bool(request.get("device_known", request.get("deviceKnown", True)))
        location_match = bool(request.get("location_match", request.get("locationMatch", True)))
        failed_attempts = int(request.get("failed_attempts", request.get("failedAttempts", 0)))
        request_frequency = float(request.get("request_frequency", 1))
        credential_valid = bool(request.get("credential_valid", request.get("credentialValid", True)))
        previous_behavior_score = float(request.get("previous_behavior_score", 0.8))

        rb_score, reasons = rule_based_score(
            sensitivity, hour, known_device, location_match, role_resource_match,
            failed_attempts, request_frequency, credential_valid, previous_behavior_score,
        )

        feats = pd.DataFrame([{
            "resource_sensitivity": sensitivity,
            "is_unusual_time": int(_is_unusual_hour(hour)),
            "known_device": int(known_device),
            "location_match": int(location_match),
            "role_resource_match": int(role_resource_match),
            "failed_attempts": failed_attempts,
            "request_frequency": request_frequency,
            "credential_valid": int(credential_valid),
            "previous_behavior_score": previous_behavior_score,
        }])[FEATURE_COLUMNS]

        ml_score = float(np.clip(self.regressor.predict(feats)[0], 0, 100))

        # IsolationForest: -1 = anomaly, 1 = normal. decision_function: higher = more normal.
        iso_pred = self.iso_forest.predict(feats)[0]
        iso_anomaly_boost = 15 if iso_pred == -1 else 0
        if iso_pred == -1 and "Deviates from user's historical behavior pattern" not in reasons:
            reasons.append("Flagged as a novel behavioral outlier by anomaly-detection model")

        # Blend: 55% rule-based backbone (explainable, policy-controlled),
        # 35% ML regressor (learned generalization), 10% isolation-forest anomaly boost
        final_score = float(np.clip(0.55 * rb_score + 0.35 * ml_score + 0.10 * iso_anomaly_boost * 6.67, 0, 100))
        level = risk_level(final_score)
        decision = decision_for_level(level)

        # crude confidence proxy: agreement between rule-based and ML score
        agreement = 1 - (abs(rb_score - ml_score) / 100)
        confidence = round(float(np.clip(agreement, 0.5, 0.99)), 2)

        # sort reasons by rough severity heuristic (keep order stable, cap at top 6)
        return RiskAssessment(
            risk_score=round(final_score, 1),
            risk_level=level,
            decision=decision,
            reasons=reasons[:6],
            model_confidence=confidence,
        )
