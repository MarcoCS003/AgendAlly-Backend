#!/bin/bash

# Variables - CAMBIAR ESTOS VALORES
PROJECT_ID="causal-jigsaw-463005-m7"  # â† CAMBIAR AQUÃ
REGION="us-central1"
SERVICE_NAME="academic-ally-backend"
IMAGE_NAME="gcr.io/$PROJECT_ID/$SERVICE_NAME"

echo "ğŸš€ Iniciando deployment..."

# Configurar proyecto
gcloud config set project $PROJECT_ID

# Habilitar APIs
gcloud services enable cloudbuild.googleapis.com
gcloud services enable run.googleapis.com

# Construir imagen
echo "ğŸ—ö¸ Construyendo imagen..."
docker build -t $IMAGE_NAME .

# Subir imagen
echo "â¬†ö¸ Subiendo imagen..."
docker push $IMAGE_NAME

# Deployar
echo "ğŸŒ Deploying a Cloud Run..."
gcloud run deploy $SERVICE_NAME \
    --image $IMAGE_NAME \
    --platform managed \
    --region $REGION \
    --allow-unauthenticated \
    --port 8080 \
    --memory 512Mi

echo "âœ… Â¡Deployment completado!"
