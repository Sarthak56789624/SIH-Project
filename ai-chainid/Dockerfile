FROM python:3.11-slim

WORKDIR /app

# Install curl for container health check
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Install python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application source and models
COPY . .

# Ensure trained models are present (train if missing)
RUN python -c "import os; from pathlib import Path; p = Path('models/risk_engine.joblib'); \
    exit(0) if p.exists() else os.system('python scripts/train_models.py')"

EXPOSE 8000

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
