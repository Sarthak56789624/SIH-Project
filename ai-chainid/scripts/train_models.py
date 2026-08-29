"""
Generates synthetic datasets, trains both AI models, evaluates the risk
model, and saves everything to disk (data/ and models/).

Run:
    python scripts/train_models.py
"""
import json
import sys
from pathlib import Path

import joblib

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

from app.permission_engine import PermissionEngine, generate_permission_dataset
from app.risk_engine import RiskEngine, generate_risk_dataset

DATA_DIR = ROOT / "data"
MODELS_DIR = ROOT / "models"
DATA_DIR.mkdir(exist_ok=True)
MODELS_DIR.mkdir(exist_ok=True)


def main():
    print("=" * 70)
    print("STEP A — Generating synthetic datasets")
    print("=" * 70)

    perm_df = generate_permission_dataset(n_samples=6000)
    perm_df.to_csv(DATA_DIR / "permission_dataset.csv", index=False)
    print(f"Permission dataset: {len(perm_df)} rows -> {DATA_DIR/'permission_dataset.csv'}")
    print(perm_df["label"].value_counts(), "\n")

    risk_df = generate_risk_dataset(n_samples=8000)
    risk_df.to_csv(DATA_DIR / "risk_dataset.csv", index=False)
    print(f"Risk dataset: {len(risk_df)} rows -> {DATA_DIR/'risk_dataset.csv'}")
    print(risk_df["risk_label"].value_counts(), "\n")

    print("=" * 70)
    print("STEP B — Training Feature 1: Permission Recommendation model")
    print("=" * 70)
    perm_engine = PermissionEngine().fit(perm_df)
    joblib.dump(perm_engine, MODELS_DIR / "permission_engine.joblib")
    print("Saved -> models/permission_engine.joblib")

    importances = dict(zip(
        perm_engine.model.feature_names_in_ if hasattr(perm_engine.model, "feature_names_in_") else [],
        perm_engine.model.feature_importances_,
    ))
    print("Feature importances:")
    for k, v in sorted(importances.items(), key=lambda x: -x[1]):
        print(f"  {k:35s} {v:.3f}")
    print()

    print("=" * 70)
    print("STEP C — Training Feature 2: Risk & Anomaly Detection model")
    print("=" * 70)
    risk_engine = RiskEngine().fit(risk_df)
    joblib.dump(risk_engine, MODELS_DIR / "risk_engine.joblib")
    print("Saved -> models/risk_engine.joblib\n")

    print("Evaluation on held-out test set:")
    print(json.dumps(risk_engine.eval_metrics_, indent=2))
    print("\nFeature importances (risk regressor):")
    print(risk_engine.feature_importances_.round(3).to_string())

    with open(MODELS_DIR / "risk_eval_metrics.json", "w") as f:
        json.dump(risk_engine.eval_metrics_, f, indent=2)
    print(f"\nSaved evaluation metrics -> models/risk_eval_metrics.json")

    print("\nDone. Both models trained and saved.")


if __name__ == "__main__":
    main()
