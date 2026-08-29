"""
FEATURE 1 — AI-Based Permission Recommendation

Answers: "What access should this person have?"

Design:
  - A rule-grounded synthetic-data generator produces (profile, resource) ->
    label examples that reflect real least-privilege policy (see domain.py).
  - A RandomForestClassifier learns to generalize this policy across
    combinations it wasn't explicitly given, and gives a confidence score
    via predict_proba.
  - The model NEVER auto-grants access. It only returns a recommendation
    object; an admin/manager must approve, modify, or reject it.
"""

from __future__ import annotations

import random
from dataclasses import dataclass, field
from typing import List, Optional

import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import LabelEncoder

from app.domain import (
    CERTIFICATIONS,
    CRITICAL_RESOURCE_ELIGIBLE_ROLES,
    DEPARTMENTS,
    EMPLOYMENT_TYPES,
    MAX_DURATION_DAYS_BY_SENSITIVITY,
    PROJECTS,
    RESOURCE_DEPARTMENT_AFFINITY,
    RESOURCE_SENSITIVITY,
    RESOURCES,
    ROLES,
    Sensitivity,
)

RECOMMENDATION_LABELS = ["DENY", "REVIEW", "ALLOW"]  # ordinal-ish order for readability

FEATURE_COLUMNS = [
    "dept_matches_resource",
    "role_eligible_for_critical",
    "resource_sensitivity",
    "has_relevant_cert",
    "project_assigned",
    "employment_is_contractor",
    "employment_is_intern",
    "contract_duration_days",
    "previously_approved_similar",
]


# --------------------------------------------------------------------------
# Ground-truth policy function (used to LABEL synthetic training examples).
# This encodes the least-privilege principle explicitly.
# --------------------------------------------------------------------------
def _ground_truth_label(
    department: str,
    role: str,
    resource: str,
    has_relevant_cert: bool,
    project_assigned: bool,
    employment_type: str,
    contract_duration_days: int,
    previously_approved_similar: bool,
) -> str:
    sensitivity = RESOURCE_SENSITIVITY[resource]
    dept_ok = department in RESOURCE_DEPARTMENT_AFFINITY[resource]
    critical_eligible_roles = CRITICAL_RESOURCE_ELIGIBLE_ROLES.get(resource)
    role_ok_for_critical = (critical_eligible_roles is None) or (role in critical_eligible_roles)

    # Hard denies: wrong department entirely
    if not dept_ok:
        return "DENY"

    # Critical resources: department match alone is not enough
    if sensitivity == Sensitivity.CRITICAL:
        if not role_ok_for_critical:
            return "DENY"
        if not project_assigned or not has_relevant_cert:
            return "REVIEW"
        if employment_type in ("Contractor", "Intern") and not previously_approved_similar:
            return "REVIEW"
        return "ALLOW" if previously_approved_similar or has_relevant_cert else "REVIEW"

    if sensitivity == Sensitivity.HIGH:
        if not project_assigned and not previously_approved_similar:
            return "REVIEW"
        if employment_type == "Intern":
            return "REVIEW"
        return "ALLOW" if (has_relevant_cert or previously_approved_similar) else "REVIEW"

    if sensitivity == Sensitivity.MEDIUM:
        if project_assigned or previously_approved_similar:
            return "ALLOW"
        return "REVIEW"

    # LOW sensitivity + department match -> generally fine
    if employment_type == "Intern" and not project_assigned:
        return "REVIEW"
    return "ALLOW"


def _suggested_duration(resource: str, employment_type: str, contract_duration_days: int) -> Optional[int]:
    sensitivity = RESOURCE_SENSITIVITY[resource]
    ceiling = MAX_DURATION_DAYS_BY_SENSITIVITY[sensitivity]
    if employment_type in ("Contractor", "Intern"):
        return max(1, min(ceiling, contract_duration_days))
    return ceiling


# --------------------------------------------------------------------------
# Synthetic dataset generation
# --------------------------------------------------------------------------
def generate_permission_dataset(n_samples: int = 6000, seed: int = 42) -> pd.DataFrame:
    rng = random.Random(seed)
    rows = []
    for _ in range(n_samples):
        role = rng.choice(ROLES)
        department = rng.choice(DEPARTMENTS)
        employment_type = rng.choice(EMPLOYMENT_TYPES)
        resource = rng.choice(RESOURCES)
        has_relevant_cert = rng.random() < 0.5
        project_assigned = rng.random() < 0.55
        contract_duration_days = rng.choice([7, 14, 30, 60, 90, 180, 365])
        previously_approved_similar = rng.random() < 0.3

        label = _ground_truth_label(
            department, role, resource, has_relevant_cert, project_assigned,
            employment_type, contract_duration_days, previously_approved_similar,
        )

        sensitivity = RESOURCE_SENSITIVITY[resource]
        dept_matches = department in RESOURCE_DEPARTMENT_AFFINITY[resource]
        critical_eligible = CRITICAL_RESOURCE_ELIGIBLE_ROLES.get(resource)
        role_eligible = (critical_eligible is None) or (role in critical_eligible)

        rows.append({
            "role": role,
            "department": department,
            "employment_type": employment_type,
            "resource": resource,
            "dept_matches_resource": int(dept_matches),
            "role_eligible_for_critical": int(role_eligible),
            "resource_sensitivity": int(sensitivity),
            "has_relevant_cert": int(has_relevant_cert),
            "project_assigned": int(project_assigned),
            "employment_is_contractor": int(employment_type == "Contractor"),
            "employment_is_intern": int(employment_type == "Intern"),
            "contract_duration_days": contract_duration_days,
            "previously_approved_similar": int(previously_approved_similar),
            "label": label,
        })
    return pd.DataFrame(rows)


# --------------------------------------------------------------------------
# Model wrapper
# --------------------------------------------------------------------------
@dataclass
class PermissionRecommendation:
    resource: str
    recommendation: str
    confidence: float
    reason: str
    suggested_duration_days: Optional[int]


class PermissionEngine:
    def __init__(self):
        self.model = RandomForestClassifier(
            n_estimators=300, max_depth=8, random_state=42, class_weight="balanced"
        )
        self.label_encoder = LabelEncoder()
        self.label_encoder.fit(RECOMMENDATION_LABELS)
        self._fitted = False

    def fit(self, df: pd.DataFrame):
        X = df[FEATURE_COLUMNS]
        y = self.label_encoder.transform(df["label"])
        self.model.fit(X, y)
        self._fitted = True
        return self

    def _row_to_features(self, profile: dict, resource: str) -> dict:
        sensitivity = RESOURCE_SENSITIVITY[resource]
        dept_matches = profile["department"] in RESOURCE_DEPARTMENT_AFFINITY[resource]
        critical_eligible = CRITICAL_RESOURCE_ELIGIBLE_ROLES.get(resource)
        role_eligible = (critical_eligible is None) or (profile["role"] in critical_eligible)
        has_cert = len(profile.get("credentials", [])) > 0
        project_assigned = bool(set(profile.get("projects", [])) & set(PROJECTS)) and len(profile.get("projects", [])) > 0
        employment_type = profile.get("employment_type", "Employee")

        return {
            "dept_matches_resource": int(dept_matches),
            "role_eligible_for_critical": int(role_eligible),
            "resource_sensitivity": int(sensitivity),
            "has_relevant_cert": int(has_cert),
            "project_assigned": int(project_assigned),
            "employment_is_contractor": int(employment_type == "Contractor"),
            "employment_is_intern": int(employment_type == "Intern"),
            "contract_duration_days": profile.get("contract_duration_days", 90),
            "previously_approved_similar": int(profile.get("previously_approved_similar", False)),
        }, dept_matches, role_eligible, sensitivity, has_cert, project_assigned

    def _explain(self, dept_matches, role_eligible, sensitivity, has_cert, project_assigned, recommendation) -> str:
        parts = []
        if not dept_matches:
            parts.append("the user's department is not associated with this resource")
        else:
            parts.append("the user's department is associated with this resource")
        if sensitivity >= Sensitivity.HIGH:
            parts.append("the resource is highly sensitive")
            if not role_eligible:
                parts.append("the user's role is not on the eligible list for this critical resource")
            if not has_cert:
                parts.append("no relevant certification/credential was found")
            if not project_assigned:
                parts.append("the user is not assigned to a project requiring this resource")
        elif sensitivity == Sensitivity.MEDIUM:
            if project_assigned:
                parts.append("the user is assigned to a relevant project")
        else:
            parts.append("resource sensitivity is low")

        joined = "; ".join(parts)
        return f"{recommendation} because {joined}."

    def recommend(self, profile: dict, resources: List[str]) -> List[PermissionRecommendation]:
        if not self._fitted:
            raise RuntimeError("Model not fitted. Call fit() or load a trained model first.")

        results = []
        for resource in resources:
            if resource not in RESOURCE_SENSITIVITY:
                # Unknown resource -> conservative REVIEW, can't assess without policy data
                results.append(PermissionRecommendation(
                    resource=resource,
                    recommendation="REVIEW",
                    confidence=0.5,
                    reason="Resource is not in the known policy catalog; manual review required.",
                    suggested_duration_days=None,
                ))
                continue

            feats, dept_matches, role_eligible, sensitivity, has_cert, project_assigned = \
                self._row_to_features(profile, resource)
            X = pd.DataFrame([feats])[FEATURE_COLUMNS]
            proba = self.model.predict_proba(X)[0]
            pred_idx = int(np.argmax(proba))
            label = self.label_encoder.inverse_transform([pred_idx])[0]
            confidence = float(proba[pred_idx])

            duration = None
            if label == "ALLOW":
                duration = _suggested_duration(
                    resource, profile.get("employment_type", "Employee"),
                    profile.get("contract_duration_days", 90),
                )
            elif label == "REVIEW":
                # Reviewed permissions, if later approved, still shouldn't exceed ceiling
                duration = _suggested_duration(
                    resource, profile.get("employment_type", "Employee"),
                    profile.get("contract_duration_days", 90),
                )

            reason = self._explain(dept_matches, role_eligible, sensitivity, has_cert, project_assigned, label)

            results.append(PermissionRecommendation(
                resource=resource,
                recommendation=label,
                confidence=round(confidence, 3),
                reason=reason,
                suggested_duration_days=duration if label == "ALLOW" else None,
            ))
        return results
