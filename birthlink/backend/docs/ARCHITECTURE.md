# SafeBirth Connect - Architecture & Deployment Guide

## Table of Contents
1. [System Overview](#system-overview)
2. [Backend Architecture](#backend-architecture)
3. [API vs Server-Side Rendering](#api-vs-server-side-rendering)
4. [Docker & Containerization](#docker--containerization)
5. [Cloud Run Hosting](#cloud-run-hosting)
6. [How Components Interact](#how-components-interact)
7. [Cost Analysis](#cost-analysis)

---

## System Overview

SafeBirth Connect is an **SMS-first maternal support coordination system** designed for crisis settings (refugee camps, disaster zones) where internet access is unreliable but SMS remains available.

### Technology Stack

| Component | Technology |
|-----------|------------|
| Backend Framework | Spring Boot 3.2.5 |
| Language | Java 17 |
| Database | H2 (in-memory for Cloud Run) |
| SMS Gateway | Twilio SDK 9.14.0 |
| API Documentation | SpringDoc OpenAPI (Swagger) |
| Container Runtime | Docker (Alpine-based) |
| Cloud Platform | Google Cloud Run |

---

## Backend Architecture

### Package Structure

```
com.safebirth/
├── api/                    # REST Controllers & DTOs
│   ├── DashboardController # NGO coordinator dashboard
│   ├── VolunteerController # Mobile app endpoints
│   └── DashboardService    # Statistics aggregation
│
├── domain/                 # Core Business Logic (DDD-style)
│   ├── mother/            # Mother entity, repository, service
│   ├── volunteer/         # Volunteer entity, repository, service
│   └── helprequest/       # HelpRequest entity, repository, service
│
├── matching/              # Volunteer-to-request matching algorithm
│   └── MatchingService
│
├── sms/                   # SMS Processing Layer
│   ├── handler/           # Webhook & command routing
│   ├── parser/            # Bilingual SMS parsing (EN/AR)
│   ├── gateway/           # Twilio & Mock implementations
│   └── simulator/         # SMS testing simulator
│
├── config/                # Configuration classes
│   ├── TwilioConfig
│   ├── WebConfig (CORS)
│   └── OpenApiConfig
│
└── exception/             # Global error handling
```

### Layered Architecture

```
┌─────────────────────────────────────────────────────┐
│                   API Layer                         │
│  (Controllers: REST endpoints, request/response)    │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                Service Layer                        │
│  (Business logic, matching algorithm, SMS parsing)  │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│              Repository Layer                       │
│  (Data access via Spring Data JPA)                  │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                Entity Layer                         │
│  (Domain models: Mother, Volunteer, HelpRequest)    │
└─────────────────────────────────────────────────────┘
```

### Key Entities

| Entity | Purpose | Key Fields |
|--------|---------|------------|
| **Mother** | Registered pregnant women | phoneNumber, name, camp, zone, dueDate, riskLevel |
| **Volunteer** | Healthcare workers | phoneNumber, skillType, zones[], availability |
| **HelpRequest** | Emergency/support cases | caseId, mother, acceptedBy, status, requestType |

### API Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/dashboard/stats` | GET | Dashboard statistics |
| `/api/dashboard/cases` | GET | List cases with filtering |
| `/api/dashboard/volunteers` | GET | List volunteers |
| `/api/dashboard/zones` | GET | Zone-level stats |
| `/api/volunteer/me` | GET | Current volunteer profile |
| `/api/volunteer/me/cases` | GET | Volunteer's active cases |
| `/api/sms/incoming` | POST | Twilio webhook |
| `/swagger-ui.html` | GET | API documentation |

---

## API vs Server-Side Rendering

### What We Use: REST API (Backend-Only)

SafeBirth Connect uses a **REST API architecture**, not server-side rendering:

```
┌─────────────┐     HTTP/JSON      ┌─────────────────┐
│   Mobile    │ ◄─────────────────► │   Spring Boot   │
│   App       │                     │   REST API      │
│  (Flutter)  │                     │   (Backend)     │
└─────────────┘                     └─────────────────┘
                                           │
┌─────────────┐     HTTP/JSON              │
│   Web       │ ◄──────────────────────────┘
│  Dashboard  │
│  (React)    │
└─────────────┘
```

### Key Differences

| Aspect | Server-Side Rendering (SSR) | REST API (Our Approach) |
|--------|----------------------------|-------------------------|
| **HTML Generation** | Server generates HTML | Server returns JSON data |
| **Rendering** | Server renders UI | Client (mobile/web) renders UI |
| **Data Transfer** | Full HTML pages | JSON payloads only |
| **Client Responsibility** | Display pre-rendered content | Parse JSON & render UI |
| **Use Case** | Web pages with SEO needs | Mobile apps, SPAs, microservices |

### Why REST API for SafeBirth?

1. **Mobile-First**: Flutter app needs JSON, not HTML
2. **Separation of Concerns**: Backend focuses on business logic
3. **Multiple Clients**: Same API serves mobile app, web dashboard, and SMS gateway
4. **Lightweight**: JSON payloads are smaller than HTML
5. **Flexibility**: Frontend teams can iterate independently

---

## Docker & Containerization

### What is Docker?

Docker packages applications with all dependencies into **containers** - lightweight, portable units that run consistently across environments.

```
┌────────────────────────────────────────────────────┐
│                 Docker Container                   │
│  ┌──────────────────────────────────────────────┐  │
│  │  Application (safebirth-api.jar)             │  │
│  ├──────────────────────────────────────────────┤  │
│  │  Java Runtime (Eclipse Temurin 17 JRE)       │  │
│  ├──────────────────────────────────────────────┤  │
│  │  Operating System (Alpine Linux)             │  │
│  └──────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────┘
```

### Our Dockerfile (Multi-Stage Build)

```dockerfile
# STAGE 1: Build (larger image with build tools)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B    # Cache dependencies
COPY src ./src
RUN mvn package -DskipTests -B      # Build JAR

# STAGE 2: Runtime (minimal image)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Security: Run as non-root user
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build /app/target/*.jar app.jar
RUN chown -R spring:spring /app
USER spring

# Cloud Run compatibility
ENV PORT=8080
EXPOSE 8080

# JVM optimizations for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT}"]
```

### Why Multi-Stage Build?

| Stage | Image Size | Contains |
|-------|------------|----------|
| Build | ~800MB | JDK, Maven, source code, dependencies |
| Runtime | ~200MB | JRE only, compiled JAR |

**Result**: Final image is 4x smaller, faster to deploy, smaller attack surface.

### Key Docker Concepts

| Concept | Explanation |
|---------|-------------|
| **Image** | Blueprint/template (like a class) |
| **Container** | Running instance (like an object) |
| **Layer** | Each Dockerfile instruction creates a cacheable layer |
| **Registry** | Storage for images (Artifact Registry) |
| **Multi-stage** | Use multiple FROM statements to reduce final image size |

---

## Cloud Run Hosting

### What is Cloud Run?

Google Cloud Run is a **serverless container platform** that:
- Runs Docker containers
- Auto-scales from 0 to N instances
- Charges only for actual usage (CPU/memory/requests)
- Handles HTTPS, load balancing, and SSL certificates

### Our Deployment Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    Google Cloud Platform                      │
│                                                              │
│  ┌─────────────────┐    ┌─────────────────────────────────┐  │
│  │ Artifact        │    │        Cloud Run                │  │
│  │ Registry        │───►│  ┌───────────────────────────┐  │  │
│  │ (Docker Images) │    │  │ safebirth-api container   │  │  │
│  └─────────────────┘    │  │ - Spring Boot app         │  │  │
│                         │  │ - H2 in-memory DB         │  │  │
│  ┌─────────────────┐    │  │ - Port 8080               │  │  │
│  │ Cloud Build     │    │  └───────────────────────────┘  │  │
│  │ (CI/CD)         │    │                                 │  │
│  └─────────────────┘    │  Region: me-central1 (Qatar)   │  │
│                         │  Instances: min=1, max=1        │  │
│                         │  Memory: 512Mi, CPU: 1          │  │
│                         └─────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
                              │
                              ▼
                    https://safebirth-api-xxx.run.app
```

### Configuration

| Setting | Value | Rationale |
|---------|-------|-----------|
| Region | `me-central1` | Qatar - lowest latency for target users |
| Min Instances | 1 | Always running, no cold starts |
| Max Instances | 1 | Cost control for hackathon |
| Memory | 512Mi | Minimum for Spring Boot |
| CPU | 1 | Sufficient for API workload |
| Port | 8080 | Standard HTTP port |

### Spring Profiles

| Profile | Database | Use Case |
|---------|----------|----------|
| `dev` | H2 file-based | Local development |
| `prod` | H2 file-based | Production with persistence |
| `cloudrun` | H2 in-memory | Cloud Run (stateless) |

**Note**: Cloud Run uses in-memory H2, so data resets on container restart. For production with data persistence, migrate to Cloud SQL.

---

## How Components Interact

### Request Flow: Mobile App to Backend

```
┌─────────────┐         ┌─────────────────┐         ┌──────────────┐
│   Flutter   │  HTTPS  │   Cloud Run     │   JPA   │    H2 DB     │
│   Mobile    │────────►│   Container     │────────►│  (in-memory) │
│   App       │◄────────│   Spring Boot   │◄────────│              │
└─────────────┘  JSON   └─────────────────┘         └──────────────┘
```

### Request Flow: SMS to Backend

```
┌─────────────┐         ┌─────────────┐         ┌─────────────────┐
│   Mother's  │   SMS   │   Twilio    │  HTTP   │   Cloud Run     │
│   Phone     │────────►│   Gateway   │────────►│   /api/sms/     │
│             │◄────────│             │◄────────│   incoming      │
└─────────────┘   SMS   └─────────────┘  TwiML  └─────────────────┘
```

### Complete System Interaction

```
                    ┌─────────────────────────────────────┐
                    │         Google Cloud Run            │
                    │    safebirth-api container          │
                    │                                     │
┌──────────┐        │  ┌─────────────────────────────┐   │
│ Flutter  │◄──────►│  │    REST API Layer           │   │
│ Mobile   │  JSON  │  │  /api/dashboard/*           │   │
│ App      │        │  │  /api/volunteer/*           │   │
└──────────┘        │  └──────────────┬──────────────┘   │
                    │                 │                   │
┌──────────┐        │  ┌──────────────▼──────────────┐   │
│ React    │◄──────►│  │    Service Layer            │   │
│ Web      │  JSON  │  │  MatchingService            │   │
│ Dashboard│        │  │  DashboardService           │   │
└──────────┘        │  └──────────────┬──────────────┘   │
                    │                 │                   │
┌──────────┐        │  ┌──────────────▼──────────────┐   │
│ Twilio   │◄──────►│  │    SMS Layer                │   │
│ (SMS)    │ TwiML  │  │  SmsParser (EN/AR)          │   │
└──────────┘        │  │  SmsCommandHandler          │   │
     ▲              │  └──────────────┬──────────────┘   │
     │              │                 │                   │
     │              │  ┌──────────────▼──────────────┐   │
┌────┴─────┐        │  │    Data Layer               │   │
│ Mother's │        │  │  H2 Database (in-memory)    │   │
│ Phone    │        │  │  Mother, Volunteer,         │   │
│ (SMS)    │        │  │  HelpRequest entities       │   │
└──────────┘        │  └─────────────────────────────┘   │
                    └─────────────────────────────────────┘
```

---

## Cost Analysis

### Current Setup (Hackathon/MVP)

| Resource | Configuration | Monthly Cost |
|----------|---------------|--------------|
| Cloud Run | 1 instance, 512Mi, 1 CPU, always-on | ~$25-35 |
| Artifact Registry | < 1GB storage | ~$0.10 |
| Cloud Build | < 120 min/month | Free tier |
| **Total** | | **~$25-35/month** |

#### Cost Breakdown

Cloud Run pricing (me-central1):
- CPU: $0.00002400/vCPU-second
- Memory: $0.00000250/GiB-second
- Requests: $0.40/million

**With min-instances=1 (always on):**
```
Monthly hours: 730 hours
CPU cost: 730 * 3600 * 0.00002400 = ~$63
Memory cost: 730 * 3600 * 0.5 * 0.00000250 = ~$3.3
Total: ~$66 (before free tier)

Free tier: 180,000 vCPU-seconds + 360,000 GiB-seconds
After free tier: ~$25-35/month
```

### Production Scaling Costs

| Scenario | Configuration | Monthly Cost |
|----------|---------------|--------------|
| **Low Traffic** | 1-2 instances, 512Mi | $25-50 |
| **Medium Traffic** | 2-5 instances, 1Gi | $80-150 |
| **High Traffic** | 5-10 instances, 2Gi | $200-400 |

### Adding Persistent Database (Production)

| Database Option | Configuration | Additional Cost |
|-----------------|---------------|-----------------|
| Cloud SQL (PostgreSQL) | db-f1-micro, 10GB | ~$7-10/month |
| Cloud SQL (PostgreSQL) | db-g1-small, 20GB | ~$25-30/month |
| Cloud SQL (PostgreSQL) | Production (HA) | ~$100+/month |

### Cost Optimization Tips

1. **Scale to Zero**: Set `min-instances=0` if cold starts are acceptable (~3-5s startup)
2. **Right-size Memory**: Start with 512Mi, increase only if needed
3. **Use Committed Use Discounts**: 1-3 year commitments save 17-52%
4. **Regional Selection**: Some regions are cheaper than others
5. **Request-based Billing**: Low traffic apps benefit from scale-to-zero

### Recommended Production Setup

| Component | Configuration | Cost |
|-----------|---------------|------|
| Cloud Run | min=1, max=10, 1Gi | ~$50-100 |
| Cloud SQL | db-g1-small, PostgreSQL | ~$25 |
| Secret Manager | Twilio credentials | ~$0.06 |
| Cloud Monitoring | Basic | Free |
| **Total** | | **~$75-125/month** |

---

## Quick Reference

### Deployment Commands

```bash
# Build image
gcloud builds submit --tag me-central1-docker.pkg.dev/PROJECT_ID/safebirth-repo/safebirth-api:latest

# Deploy to Cloud Run
gcloud run deploy safebirth-api \
    --image me-central1-docker.pkg.dev/PROJECT_ID/safebirth-repo/safebirth-api:latest \
    --region me-central1 \
    --min-instances 1 \
    --max-instances 1 \
    --memory 512Mi \
    --set-env-vars "SPRING_PROFILES_ACTIVE=cloudrun"
```

### Useful Commands

```bash
# Get service URL
gcloud run services describe safebirth-api --region me-central1 --format 'value(status.url)'

# View logs
gcloud run services logs read safebirth-api --region me-central1

# Update environment variables
gcloud run services update safebirth-api --set-env-vars "KEY=VALUE"
```

### Environment Variables

| Variable | Purpose |
|----------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile (cloudrun) |
| `TWILIO_ACCOUNT_SID` | Twilio account ID |
| `TWILIO_AUTH_TOKEN` | Twilio auth token |
| `TWILIO_PHONE_NUMBER` | Twilio phone number |
| `TWILIO_MOCK_ENABLED` | Enable/disable mock SMS |

---

## Summary

SafeBirth Connect is deployed as a **containerized Spring Boot REST API** on **Google Cloud Run**:

- **Backend**: Spring Boot 3.2.5 with layered architecture
- **Data**: H2 in-memory (Cloud Run) or file-based (local)
- **SMS**: Twilio integration with bilingual support (EN/AR)
- **Containerization**: Multi-stage Docker build (~200MB image)
- **Hosting**: Cloud Run with always-on instance
- **Cost**: ~$25-35/month (hackathon), ~$75-125/month (production with DB)

The REST API architecture separates frontend (Flutter mobile, React web) from backend, allowing independent development and deployment of each component.
