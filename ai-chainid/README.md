# AI-ChainID — AI Components

Implements exactly the two AI components requested:

1. **AI-Based Permission Recommendation** — "What access should this person have?"
2. **AI-Based Risk & Anomaly Detection** — "Does this access request look suspicious?"

Core principle: **a valid identity does not automatically mean valid access.**

## Project layout

```
ai-chainid/
├── app/
│   ├── domain.py            # roles, resources, sensitivity, org policy rules
│   ├── permission_engine.py # Feature 1: RandomForest classifier + explainability
│   ├── risk_engine.py       # Feature 2: hybrid rule-based + RandomForest + IsolationForest
│   └── main.py               # FastAPI app: POST /ai/recommend-permissions, POST /ai/risk-score
├── scripts/
│   ├── train_models.py      # generates synthetic data, trains + saves both models
│   ├── evaluate.py           # precision/recall/F1/confusion matrix/feature importance report
│   └── demo.py                # runs the two required end-to-end demo scenarios
├── data/                      # generated synthetic datasets (CSV)
├── models/                    # trained model artifacts (.joblib) + eval metrics (.json)
└── requirements.txt
```

## Setup

```bash
pip install -r requirements.txt
```

## Step-by-step usage

### 1. Generate data + train both models

```bash
python scripts/train_models.py
```

This will:
- Generate a synthetic, policy-grounded **permission dataset** (6,000 rows) and
  **access-log dataset** (8,000 rows) into `data/`.
- Train the permission recommendation `RandomForestClassifier`.
- Train the risk `RandomForestRegressor` + an `IsolationForest` anomaly detector
  trained only on normal (LOW-risk) traffic.
- Save both models to `models/` and print feature importances + evaluation metrics.

### 2. See the full evaluation report

```bash
python scripts/evaluate.py
```

Prints precision/recall/F1 per risk class, the confusion matrix, and feature
importances for both models.

### 3. Run the required demo scenarios

```bash
python scripts/demo.py
```

Runs:
- **Feature 1 demo**: a Software Developer profile evaluated against all 7
  resources — shows ALLOW/DENY/REVIEW + confidence + reason + suggested duration
  for each.
- **Feature 2, Scenario 1 (Normal Employee)**: same developer, Development
  Server, 11:00 AM, known device, expected location → **LOW risk, ALLOW**.
- **Feature 2, Scenario 2 (Compromised Credential)**: same developer, same
  *valid* credential, but Production Database, 2:30 AM, unknown device, wrong
  location, 4 failed attempts → **HIGH risk, BLOCK + admin alert**.

### 4. Run the API

```bash
uvicorn app.main:app --reload --port 8000
```

Open `http://localhost:8000/docs` for interactive Swagger UI, or call directly:

```bash
curl -X POST http://localhost:8000/ai/recommend-permissions \
  -H "Content-Type: application/json" \
  -d '{
    "role": "Software Developer",
    "department": "Engineering",
    "credentials": ["Backend Development Certification"],
    "projects": ["Project Alpha"],
    "resources": ["GitHub Project Alpha", "Development Server", "HR Database"]
  }'

curl -X POST http://localhost:8000/ai/risk-score \
  -H "Content-Type: application/json" \
  -d '{
    "did": "did:aichainid:8F72A91",
    "role": "Software Developer",
    "department": "Engineering",
    "resource": "Production Database",
    "time": "02:30",
    "deviceKnown": false,
    "locationMatch": false,
    "failedAttempts": 4,
    "credentialValid": true
  }'
```

## Design notes

### Feature 1 — Permission Recommendation
- `app/domain.py` encodes an explicit least-privilege policy: which
  departments/roles are normally eligible for which resources, and how
  sensitive each resource is. This is **not** hardcoded into the model — it's
  used to *generate realistic labeled training data*, so the classifier learns
  to generalize the policy (e.g. to role/department/project combinations it
  wasn't literally shown) rather than memorizing a lookup table.
- Returns ALLOW / DENY / REVIEW + confidence (`predict_proba`) + a
  human-readable reason assembled from the same policy signals + a suggested
  duration capped by resource sensitivity.
- **Never auto-grants access.** The API response always includes a note that
  this is a recommendation requiring admin/manager approval, and there is no
  code path anywhere that writes/grants a permission.

### Feature 2 — Risk & Anomaly Detection
Hybrid ML + rule-based, as the spec calls for:
1. A deterministic **rule-based scorer** (`rule_based_score`) is the
   explainability backbone — it's how "Unknown device", "Unusual access time",
   etc. reasons are generated, and it directly encodes "valid credential is
   only one signal among many."
2. A **RandomForestRegressor** trained on the synthetic access-log dataset
   learns to predict the continuous 0–100 score, generalizing beyond exact
   rule thresholds. Exposes `feature_importances_`.
3. An **IsolationForest** trained only on LOW-risk traffic adds an
   unsupervised layer that can flag novel anomaly patterns that don't match
   any coded rule.
4. Final score = weighted blend (55% rule-based / 35% regressor / 10%
   isolation-forest boost), bucketed into LOW (0–30) / MEDIUM (31–70) /
   HIGH (71–100), mapped to ALLOW / ADMIN_REVIEW / BLOCK.

### Honest limitations (hackathon-scale, not production claims)
- Datasets are **synthetic**, generated from the explicit policy/rule
  functions in `domain.py` (plus noise) — not real access logs. Metrics
  (precision/recall/F1, R²) reflect how well the models learned that
  synthetic policy, not real-world accuracy.
- In the current synthetic generator, `is_unusual_time` correlates strongly
  with the other anomaly signals, so it dominates feature importance for the
  risk regressor (0.78). If you extend this, decorrelate the synthetic
  scenarios more (e.g. unusual-time-but-otherwise-normal cases) so the model
  is forced to weigh multiple signals more evenly.
- `IsolationForest` contamination (15%) and the rule/ML blend weights (55/35/10)
  are reasonable hackathon defaults, not tuned against real incident data.
- Swap in real historical access logs and re-run `train_models.py` before any
  real deployment decision relies on this.
