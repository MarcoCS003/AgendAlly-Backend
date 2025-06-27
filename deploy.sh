#!/bin/bash

# Variables - CAMBIAR ESTOS VALORES
PROJECT_ID="causal-jigsaw-463005-m7"  # ← CAMBIAR AQUÍ
REGION="us-central1"
SERVICE_NAME="academic-ally-backend"
IMAGE_NAME="gcr.io/$PROJECT_ID/$SERVICE_NAME"

echo "🚀 Iniciando deployment..."

# Configurar proyecto
gcloud config set project $PROJECT_ID

# Habilitar APIs
gcloud services enable cloudbuild.googleapis.com
gcloud services enable run.googleapis.com

# Construir imagen
echo "🏗��� Construyendo imagen..."
docker build -t $IMAGE_NAME .

# Subir imagen
echo "⬆��� Subiendo imagen..."
docker push $IMAGE_NAME

# Deployar
echo "🌐 Deploying a Cloud Run..."
gcloud run deploy $SERVICE_NAME \
    --image $IMAGE_NAME \
    --platform managed \
    --region $REGION \
    --allow-unauthenticated \
    --port 8080 \
    --memory 512Mi

echo "✅ ¡Deployment completado!"
