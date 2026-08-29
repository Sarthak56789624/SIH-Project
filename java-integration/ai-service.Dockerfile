# Place this at ai-chainid/Dockerfile (the Python project from the earlier response)
FROM python:3.11-slim

WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Train models at build time so the image is ready to serve immediately.
# (For frequently-changing policy, you may prefer to mount models/ as a
# volume and run scripts/train_models.py as a separate step instead.)
RUN python scripts/train_models.py

EXPOSE 8000
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
