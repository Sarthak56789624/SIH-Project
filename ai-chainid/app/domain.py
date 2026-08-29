"""
Domain model for AI-ChainID.

This module centralizes the "world knowledge" that both AI features are
grounded in: what roles exist, what resources exist, how sensitive each
resource is, and which roles are normally associated with which resources.

This is intentionally NOT hidden inside the ML models. Keeping it explicit
here means:
  1. We can generate realistic, internally-consistent synthetic training data.
  2. The rule layer can be used as an explainability backbone (a human can
     always see *why* a resource is considered sensitive or role-appropriate).
  3. Admins/devs can tune organizational policy without retraining a model
     from scratch (though retraining is still recommended after big changes).
"""

from enum import IntEnum
from typing import Dict, List, Set


class Sensitivity(IntEnum):
    LOW = 1
    MEDIUM = 2
    HIGH = 3
    CRITICAL = 4


ROLES = [
    "Software Developer",
    "DevOps Engineer",
    "QA Engineer",
    "Product Manager",
    "HR Specialist",
    "Finance Analyst",
    "System Administrator",
    "Contractor",
]

DEPARTMENTS = [
    "Engineering",
    "Product",
    "HR",
    "Finance",
    "IT",
]

EMPLOYMENT_TYPES = ["Employee", "Contractor", "Intern"]

RESOURCES = [
    "GitHub Project Alpha",
    "Development Server",
    "Project Management System",
    "Production Database",
    "HR Database",
    "Finance System",
    "Admin Panel",
]

RESOURCE_SENSITIVITY: Dict[str, Sensitivity] = {
    "GitHub Project Alpha": Sensitivity.LOW,
    "Development Server": Sensitivity.MEDIUM,
    "Project Management System": Sensitivity.LOW,
    "Production Database": Sensitivity.CRITICAL,
    "HR Database": Sensitivity.HIGH,
    "Finance System": Sensitivity.HIGH,
    "Admin Panel": Sensitivity.CRITICAL,
}

# Which departments are normally associated with which resources.
# This is the backbone of "role_resource_match" and of least-privilege logic.
RESOURCE_DEPARTMENT_AFFINITY: Dict[str, Set[str]] = {
    "GitHub Project Alpha": {"Engineering", "Product"},
    "Development Server": {"Engineering"},
    "Project Management System": {"Engineering", "Product", "HR", "Finance", "IT"},
    "Production Database": {"Engineering", "IT"},
    "HR Database": {"HR"},
    "Finance System": {"Finance"},
    "Admin Panel": {"IT"},
}

# Roles that are allowed to touch CRITICAL resources at all, even if their
# department matches. E.g. a junior developer's department matches
# "Engineering" but shouldn't get Production DB / Admin Panel by default.
CRITICAL_RESOURCE_ELIGIBLE_ROLES: Dict[str, Set[str]] = {
    "Production Database": {"DevOps Engineer", "System Administrator"},
    "Admin Panel": {"System Administrator"},
}

# Default max permission duration (days) by sensitivity — used as a
# least-privilege ceiling regardless of what the model suggests.
MAX_DURATION_DAYS_BY_SENSITIVITY: Dict[Sensitivity, int] = {
    Sensitivity.LOW: 90,
    Sensitivity.MEDIUM: 30,
    Sensitivity.HIGH: 14,
    Sensitivity.CRITICAL: 7,
}

CERTIFICATIONS = [
    "Backend Development Certification",
    "Cloud Infrastructure Certification",
    "Database Administration Certification",
    "Security+ Certification",
    "HR Compliance Certification",
    "Financial Systems Certification",
    "None",
]

PROJECTS = ["Project Alpha", "Project Beta", "Project Gamma", "Unassigned"]
