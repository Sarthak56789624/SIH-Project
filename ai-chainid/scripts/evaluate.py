"""
Prints a full evaluation report for the Risk & Anomaly Detection model:
precision/recall/F1 per class, confusion matrix, and feature importances.

(The Permission Recommendation model is a policy-following classifier, not
an anomaly-style scorer; its "accuracy" is really "agreement with the
least-privilege policy encoded in domain.py". We check that here too via
a held-out split.)

Run:
    python scripts/evaluate.py
"""
import sys
from pathlib import Path

import joblib
import pandas as pd
from sklearn.metrics import classification_report

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

from app.permission_engine import PermissionEngine, generate_permission_dataset, FEATURE_COLUMNS


def eval_risk_model():
    risk_engine = joblib.load(ROOT / "models" / "risk_engine.joblib")
    print("=" * 70)
    print("RISK & ANOMALY DETECTION MODEL — Evaluation")
    print("=" * 70)
    m = risk_engine.eval_metrics_
    print(f"Regressor MAE:  {m['mae']} (avg error in risk-score points, 0-100 scale)")
    print(f"Regressor R^2:  {m['r2']}")
    print("\nPer-class metrics (after bucketing predicted score into LOW/MEDIUM/HIGH):")
    for cls in ["LOW", "MEDIUM", "HIGH"]:
        print(f"  {cls:7s} precision={m['precision_per_class'][cls]:.3f}  "
              f"recall={m['recall_per_class'][cls]:.3f}  f1={m['f1_per_class'][cls]:.3f}")
    print("\nConfusion matrix (rows=actual, cols=predicted), order LOW/MEDIUM/HIGH:")
    for row in m["confusion_matrix"]:
        print("  ", row)
    print("\nFeature importances:")
    print(risk_engine.feature_importances_.round(3).to_string())


def eval_permission_model():
    print("\n" + "=" * 70)
    print("PERMISSION RECOMMENDATION MODEL — Evaluation")
    print("=" * 70)
    from sklearn.model_selection import train_test_split

    df = generate_permission_dataset(n_samples=6000, seed=99)  # fresh held-out-style set
    engine = joblib.load(ROOT / "models" / "permission_engine.joblib")

    X = df[FEATURE_COLUMNS]
    y_true = df["label"]
    y_pred_idx = engine.model.predict(X)
    y_pred = engine.label_encoder.inverse_transform(y_pred_idx)

    print(classification_report(y_true, y_pred, labels=["DENY", "REVIEW", "ALLOW"]))


if __name__ == "__main__":
    eval_risk_model()
    eval_permission_model()
    print("\nNote: metrics are computed on SYNTHETIC data generated from an explicit")
    print("policy/rule model (see app/domain.py). They demonstrate the models learn")
    print("the intended least-privilege / anomaly patterns correctly — they are NOT")
    print("a claim of production-grade accuracy on real organizational data.")
