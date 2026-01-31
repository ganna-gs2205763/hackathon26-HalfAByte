# SafeBirth Connect - Deployment Guide

## Quick Start

### Prerequisites
- Google Cloud SDK (`gcloud`) installed and authenticated
- Billing account linked to project
- Docker (optional, for local testing)

---

## Current Deployment

| Resource | Value |
|----------|-------|
| **Project ID** | `safebirth-hackathon26` |
| **Region** | `me-central1` (Qatar) |
| **Service URL** | https://safebirth-api-748784316537.me-central1.run.app |
| **Swagger UI** | https://safebirth-api-748784316537.me-central1.run.app/swagger-ui.html |

---

## Deploy Updates

### One-Command Redeploy

```bash
cd backend

# Build and deploy
gcloud builds submit --tag me-central1-docker.pkg.dev/safebirth-hackathon26/safebirth-repo/safebirth-api:latest && \
gcloud run deploy safebirth-api \
    --image me-central1-docker.pkg.dev/safebirth-hackathon26/safebirth-repo/safebirth-api:latest \
    --platform managed \
    --region me-central1 \
    --port 8080 \
    --memory 512Mi \
    --cpu 1 \
    --min-instances 1 \
    --max-instances 1 \
    --set-env-vars "SPRING_PROFILES_ACTIVE=cloudrun,TWILIO_MOCK_ENABLED=true" \
    --allow-unauthenticated
```

### Step-by-Step

```bash
# 1. Build container image
gcloud builds submit \
    --tag me-central1-docker.pkg.dev/safebirth-hackathon26/safebirth-repo/safebirth-api:latest

# 2. Deploy to Cloud Run
gcloud run deploy safebirth-api \
    --image me-central1-docker.pkg.dev/safebirth-hackathon26/safebirth-repo/safebirth-api:latest \
    --region me-central1
```

---

## Useful Commands

### View Logs
```bash
# Real-time logs
gcloud run services logs read safebirth-api --region me-central1 --limit 50

# Tail logs
gcloud run services logs tail safebirth-api --region me-central1
```

### Check Service Status
```bash
# Get service details
gcloud run services describe safebirth-api --region me-central1

# Get just the URL
gcloud run services describe safebirth-api --region me-central1 --format 'value(status.url)'
```

### Update Configuration
```bash
# Update environment variables
gcloud run services update safebirth-api \
    --region me-central1 \
    --set-env-vars "TWILIO_MOCK_ENABLED=false"

# Scale instances
gcloud run services update safebirth-api \
    --region me-central1 \
    --min-instances 0 \
    --max-instances 5
```

### View Revisions
```bash
gcloud run revisions list --service safebirth-api --region me-central1
```

---

## Local Development

### Run with Maven
```bash
cd backend
./mvnw spring-boot:run
# Access at http://localhost:8080
```

### Run with Docker
```bash
cd backend

# Build image
docker build -t safebirth-api .

# Run container
docker run -p 8080:8080 \
    -e SPRING_PROFILES_ACTIVE=dev \
    safebirth-api
```

---

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Spring profile | `dev` |
| `TWILIO_MOCK_ENABLED` | Use mock SMS | `true` |
| `TWILIO_ACCOUNT_SID` | Twilio account | - |
| `TWILIO_AUTH_TOKEN` | Twilio token | - |
| `TWILIO_PHONE_NUMBER` | Twilio number | - |
| `PORT` | Server port (set by Cloud Run) | `8080` |

---

## Verify Deployment

```bash
# Test API endpoint
curl https://safebirth-api-748784316537.me-central1.run.app/api/dashboard/stats

# Test Swagger UI (should return 200)
curl -s -o /dev/null -w "%{http_code}" \
    https://safebirth-api-748784316537.me-central1.run.app/swagger-ui/index.html
```

---

## Troubleshooting

### Container fails to start
```bash
# Check build logs
gcloud builds list --limit 5

# Check specific build
gcloud builds log BUILD_ID
```

### Application errors
```bash
# View recent logs
gcloud run services logs read safebirth-api --region me-central1 --limit 100
```

### Memory issues
```bash
# Increase memory
gcloud run services update safebirth-api \
    --region me-central1 \
    --memory 1Gi
```

---

## Cost Control

### Scale to Zero (saves ~$25/month)
```bash
gcloud run services update safebirth-api \
    --region me-central1 \
    --min-instances 0
```
**Note**: First request after idle will have ~3-5s cold start.

### Delete Service (stop all charges)
```bash
gcloud run services delete safebirth-api --region me-central1
```
