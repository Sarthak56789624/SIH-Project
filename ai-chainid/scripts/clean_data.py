"""
Data Cleaning Script — permission_dataset.csv
=============================================
Steps performed:
  1. Load raw permission dataset
  2. Report pre-cleaning stats
  3. Remove exact duplicate rows
  4. Validate column types and value ranges
  5. Check for null/NaN values
  6. Optionally top-up samples via regeneration to restore original row count
  7. Save cleaned CSV and print a final summary report

Run:
    python scripts/clean_data.py
"""

import sys
from pathlib import Path

import pandas as pd
import numpy as np

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

DATA_DIR = ROOT / "data"
INPUT_CSV = DATA_DIR / "permission_dataset.csv"
OUTPUT_CSV = DATA_DIR / "permission_dataset.csv"  # overwrite in-place
BACKUP_CSV = DATA_DIR / "permission_dataset_backup.csv"


# ── Expected schema ────────────────────────────────────────────────────────────
INT_BINARY_COLS = [
    "dept_matches_resource",
    "role_eligible_for_critical",
    "has_relevant_cert",
    "project_assigned",
    "employment_is_contractor",
    "employment_is_intern",
    "previously_approved_similar",
]

VALID_LABELS = {"DENY", "REVIEW", "ALLOW"}
VALID_SENSITIVITIES = {1, 2, 3, 4}
VALID_DURATIONS = {7, 14, 30, 60, 90, 180, 365}


def banner(title: str):
    print("\n" + "=" * 70)
    print(f"  {title}")
    print("=" * 70)


def report_stats(df: pd.DataFrame, label: str):
    banner(label)
    print(f"  Rows             : {len(df):,}")
    print(f"  Columns          : {df.shape[1]}")
    print(f"  Null values      : {df.isnull().sum().sum()}")
    print(f"  Exact duplicates : {df.duplicated().sum()}")
    print(f"\n  Label distribution:")
    vc = df["label"].value_counts()
    for lbl, cnt in vc.items():
        pct = cnt / len(df) * 100
        print(f"    {lbl:<8s} {cnt:>5,}  ({pct:.1f}%)")


def validate_schema(df: pd.DataFrame) -> list[str]:
    """Return list of validation error messages (empty = all good)."""
    errors = []

    # Binary columns must be 0 or 1
    for col in INT_BINARY_COLS:
        bad = df[col][~df[col].isin([0, 1])]
        if not bad.empty:
            errors.append(f"  Column '{col}' has non-binary values at rows: {bad.index.tolist()[:5]}")

    # Label must be one of the three valid values
    invalid_labels = df[~df["label"].isin(VALID_LABELS)]
    if not invalid_labels.empty:
        errors.append(f"  'label' has unexpected values: {invalid_labels['label'].unique().tolist()}")

    # Sensitivity must be 1-4
    invalid_sens = df[~df["resource_sensitivity"].isin(VALID_SENSITIVITIES)]
    if not invalid_sens.empty:
        errors.append(f"  'resource_sensitivity' has unexpected values: {invalid_sens['resource_sensitivity'].unique().tolist()}")

    # contract_duration_days must be from the allowed set
    invalid_dur = df[~df["contract_duration_days"].isin(VALID_DURATIONS)]
    if not invalid_dur.empty:
        errors.append(f"  'contract_duration_days' has unexpected values: {invalid_dur['contract_duration_days'].unique().tolist()}")

    return errors


def topup_samples(df_clean: pd.DataFrame, target_n: int = 6000) -> pd.DataFrame:
    """
    If rows were removed as duplicates, regenerate fresh rows (no duplicates)
    to restore the dataset to ~target_n rows, preserving label proportions.
    """
    if len(df_clean) >= target_n:
        return df_clean

    needed = target_n - len(df_clean)
    print(f"\n  Top-up: regenerating {needed} fresh rows to reach {target_n:,} total ...")

    from app.permission_engine import generate_permission_dataset

    # Generate a larger pool with a different seed to avoid re-introducing the
    # same duplicates, then drop any rows already present in df_clean.
    pool = generate_permission_dataset(n_samples=needed * 5, seed=123)

    # Remove rows that already exist in the cleaned dataset (exact match on all columns)
    merged_check = pd.merge(pool, df_clean, how="left", indicator=True)
    new_rows = merged_check[merged_check["_merge"] == "left_only"].drop(columns=["_merge"])

    if len(new_rows) < needed:
        # Still not enough unique rows — just take what we have
        print(f"  Warning: could only find {len(new_rows)} new unique rows; dataset will be smaller than {target_n:,}.")
        top_up = new_rows
    else:
        top_up = new_rows.sample(n=needed, random_state=42).reset_index(drop=True)

    combined = pd.concat([df_clean, top_up], ignore_index=True)
    print(f"  Top-up complete: {len(df_clean):,} + {len(top_up):,} = {len(combined):,} rows")
    return combined


def main():
    banner("LOADING DATA")
    df = pd.read_csv(INPUT_CSV)
    print(f"  Loaded: {INPUT_CSV}")

    report_stats(df, "PRE-CLEANING STATS")

    # ── Step 1: Backup original ────────────────────────────────────────────────
    df.to_csv(BACKUP_CSV, index=False)
    print(f"\n  Backup saved -> {BACKUP_CSV}")

    # ── Step 2: Remove null rows (sanity) ─────────────────────────────────────
    banner("STEP 1 - Null / NaN Removal")
    null_count = df.isnull().sum().sum()
    df.dropna(inplace=True)
    print(f"  Removed {null_count} null values. Rows remaining: {len(df):,}")

    # ── Step 3: Remove exact duplicate rows ───────────────────────────────────
    banner("STEP 2 - Duplicate Removal")
    before = len(df)
    df.drop_duplicates(inplace=True)
    df.reset_index(drop=True, inplace=True)
    removed = before - len(df)
    print(f"  Removed {removed} exact duplicate rows. Rows remaining: {len(df):,}")

    # -- Step 4: Schema validation ----------------------------------------------
    banner("STEP 3 - Schema & Value Validation")
    errors = validate_schema(df)
    if errors:
        print("  [!] Validation issues found:")
        for e in errors:
            print(e)
    else:
        print("  [OK] All columns pass schema validation (types, ranges, valid labels).")  

    # -- Step 5: Top-up to restore row count -----------------------------------
    banner("STEP 4 - Top-up to Restore Dataset Size")
    df = topup_samples(df, target_n=6000)
    # Final shuffle so new rows aren't all at the end
    df = df.sample(frac=1, random_state=42).reset_index(drop=True)

    # ── Step 6: Final validation & save ───────────────────────────────────────
    report_stats(df, "POST-CLEANING STATS")

    final_errors = validate_schema(df)
    if final_errors:
        print("\n  [!] Post-clean validation issues:")
        for e in final_errors:
            print(e)
    else:
        print("\n  [OK] Final dataset passes all validation checks.")

    df.to_csv(OUTPUT_CSV, index=False)
    print(f"\n  [OK] Cleaned dataset saved -> {OUTPUT_CSV}")
    print(f"  [OK] Original backed up   -> {BACKUP_CSV}")

    banner("CLEANING COMPLETE")
    print(f"  Original rows : 6,000")
    print(f"  Duplicates removed : {removed}")
    print(f"  After dedup   : {6000 - removed:,}")
    print(f"  Final rows    : {len(df):,}")


if __name__ == "__main__":
    main()
