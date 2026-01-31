# SafeBirth Connect

**SMS-first maternal support coordination system for crisis settings**

SafeBirth Connect enables maternal healthcare coordination in refugee camps and disaster zones where internet access is unreliable but SMS remains available. Mothers can register and request emergency help via SMS, while volunteers receive alerts and coordinate responses.

## Features

- **SMS-Based Registration**: Mothers and volunteers register via SMS
- **Emergency Alerts**: Instant SMS alerts to nearby certified volunteers
- **Bilingual Support**: Full Arabic and English support for all SMS commands
- **Smart Matching**: Priority-based volunteer matching (certified midwives first)
- **NGO Dashboard**: Flutter app for coordinators to monitor cases
- **Offline-Ready**: Local SQLite storage for volunteer app during connectivity issues
- **RTL Support**: Full Arabic right-to-left layout support

## Quick Start

### Prerequisites

- **Java 21** (JDK 21+)
- **Flutter 3.x** with Dart SDK
- **Maven** (optional - wrapper included)

### Backend

**Windows (PowerShell):**
```powershell
cd backend

# Run with Maven
.\mvnw.cmd spring-boot:run

# Or build and run JAR
.\mvnw.cmd clean package -DskipTests
java -jar target\safebirth-connect-0.0.1-SNAPSHOT.jar
```

**Linux/macOS:**
```bash
cd backend
./mvnw spring-boot:run
```

**Access Points:**
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:file:./data/safebirth`, user: `sa`, no password)

### Flutter App

```powershell
cd mobile

# Get dependencies
flutter pub get

# Generate code (Freezed, Riverpod)
flutter pub run build_runner build --delete-conflicting-outputs

# Run the app
flutter run
```

### Test SMS Locally (No Twilio Required)

**Windows (PowerShell):**
```powershell
# Register a mother
Invoke-RestMethod -Uri "http://localhost:8080/api/sms/simulate" `
  -Method POST -ContentType "application/json" `
  -Body '{"from": "+201234567890", "body": "REG MOTHER CAMP A ZONE 3 DUE 15-02 RISK HIGH"}'

# Register a volunteer
Invoke-RestMethod -Uri "http://localhost:8080/api/sms/simulate" `
  -Method POST -ContentType "application/json" `
  -Body '{"from": "+201234567891", "body": "REG VOLUNTEER NAME Sarah CAMP A ZONE 3 SKILL MIDWIFE"}'

# Send emergency
Invoke-RestMethod -Uri "http://localhost:8080/api/sms/simulate" `
  -Method POST -ContentType "application/json" `
  -Body '{"from": "+201234567890", "body": "EMERGENCY"}'
```

**curl:**
```bash
curl -X POST http://localhost:8080/api/sms/simulate \
  -H "Content-Type: application/json" \
  -d '{"from": "+201234567890", "body": "REG MOTHER CAMP A ZONE 3"}'
```

---

## SMS Commands

| Action | English | Arabic |
|--------|---------|--------|
| Register Mother | `REG MOTHER CAMP A ZONE 3 DUE 15-02 RISK HIGH` | `تسجيل ام مخيم أ منطقة 3 موعد 15-02 خطورة عالية` |
| Register Volunteer | `REG VOLUNTEER NAME [name] CAMP A ZONE 3 SKILL MIDWIFE` | `تسجيل متطوع الاسم [اسم] مخيم أ منطقة 3 مهارة قابلة` |
| Emergency | `EMERGENCY` or `SOS` | `طوارئ` |
| Support Request | `SUPPORT` | `مساعدة` |
| Accept Case | `ACCEPT HR-0001` | `قبول HR-0001` |
| Complete Case | `COMPLETE HR-0001` | `انهاء HR-0001` |
| Cancel Case | `CANCEL HR-0001` | `الغاء HR-0001` |
| Set Available | `AVAILABLE` | `متاح` |
| Set Busy | `BUSY` | `مشغول` |
| Check Status | `STATUS` | `حالة` |
| Get Help | `HELP` | `مساعدة` |

**Skill Types:** `MIDWIFE` (قابلة), `NURSE` (ممرضة), `TRAINED` (مدربة), `COMMUNITY` (متطوع)

---

## API Endpoints

### SMS (Twilio Webhook)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/sms/incoming` | Twilio webhook (TwiML response) |
| POST | `/api/sms/simulate` | Test SMS locally (JSON response) |
| GET | `/api/sms/health` | Health check |

### Dashboard (NGO App)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dashboard/stats` | Overview statistics |
| GET | `/api/dashboard/cases` | List cases (filterable) |
| GET | `/api/dashboard/cases/{caseId}` | Case details |
| GET | `/api/dashboard/volunteers` | List volunteers |
| GET | `/api/dashboard/zones` | Zone statistics |

### Volunteer App
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/volunteer/me` | Current profile (X-Phone-Number header) |
| GET | `/api/volunteer/me/cases` | Assigned cases |
| PUT | `/api/volunteer/me/availability` | Update status |

---

## Architecture

See [plans/ARCHITECTURE.md](plans/ARCHITECTURE.md) for detailed system architecture.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           SAFEBIRTH CONNECT                              │
├─────────────────────────────────────────────────────────────────────────┤
│   ┌─────────────┐     ┌─────────────┐     ┌─────────────────────────┐  │
│   │   MOTHERS   │     │ VOLUNTEERS  │     │    NGO COORDINATORS     │  │
│   │  (Any Phone)│     │ (Any Phone) │     │     (Flutter App)       │  │
│   └──────┬──────┘     └──────┬──────┘     └───────────┬─────────────┘  │
│          │ SMS               │ SMS                    │ REST API        │
│          ▼                   ▼                        ▼                 │
│   ┌──────────────────────────────────────────────────────────────────┐ │
│   │                        TWILIO                                     │ │
│   └──────────────────────────┬───────────────────────────────────────┘ │
│                              │ Webhook                                  │
│                              ▼                                          │
│   ┌──────────────────────────────────────────────────────────────────┐ │
│   │                    SPRING BOOT BACKEND                            │ │
│   │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌────────────┐ │ │
│   │  │ SMS Parser  │ │  Matching   │ │ REST API    │ │  Services  │ │ │
│   │  │ (AR + EN)   │ │  Service    │ │ Controller  │ │            │ │ │
│   │  └─────────────┘ └─────────────┘ └─────────────┘ └────────────┘ │ │
│   │                          │                                        │ │
│   │                          ▼                                        │ │
│   │               ┌─────────────────────┐                            │ │
│   │               │    H2 Database      │                            │ │
│   │               └─────────────────────┘                            │ │
│   └──────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Tech Stack

### Backend
| Component | Technology |
|-----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| Database | H2 (embedded) |
| SMS Gateway | Twilio SDK 10.x |
| API Docs | Springdoc OpenAPI |
| Build Tool | Maven |

### Mobile App
| Component | Technology |
|-----------|------------|
| Framework | Flutter 3.x |
| State Management | Riverpod 2.x with code generation |
| HTTP Client | Dio |
| Local Database | sqflite |
| Routing | go_router |
| Data Classes | Freezed |
| Font | Cairo (Arabic-friendly) |

---

## Project Structure

```
safebirthconnect/
├── backend/                     # Spring Boot API
│   └── src/main/java/com/safebirth/
│       ├── api/                 # REST controllers & DTOs
│       ├── config/              # Configuration
│       ├── domain/              # Entities & services
│       │   ├── mother/
│       │   ├── volunteer/
│       │   └── helprequest/
│       ├── matching/            # Volunteer matching algorithm
│       ├── sms/                 # SMS parsing & handling
│       │   ├── gateway/
│       │   ├── handler/
│       │   └── parser/
│       └── exception/           # Error handling
├── mobile/                      # Flutter app
│   └── lib/
│       ├── core/                # Constants, network, localization
│       ├── features/            # Feature modules (inbox, dashboard, settings)
│       └── shared/              # Reusable widgets & providers
├── docs/                        # Documentation
│   ├── TWILIO_SETUP.md
│   ├── SMS_TEST_CHECKLIST.md
│   ├── LAUNCH_CHECKLIST.md
│   └── DEMO_SCRIPT.md
└── plans/                       # Development phase plans
    ├── ARCHITECTURE.md
    ├── PROGRESS.md
    └── PHASE-01.md ... PHASE-07.md
```

---

## Development

### Run Tests

**Backend:**
```powershell
cd backend
.\mvnw.cmd test
```

**Flutter:**
```powershell
cd mobile
flutter test
```

### Twilio Live SMS Setup

See [docs/TWILIO_SETUP.md](docs/TWILIO_SETUP.md) for detailed instructions.

Quick setup:
1. Create Twilio account at https://www.twilio.com
2. Get a phone number with SMS capability
3. Set environment variables:
   ```powershell
   $env:TWILIO_ACCOUNT_SID = "ACxxxxxxx"
   $env:TWILIO_AUTH_TOKEN = "your_token"
   $env:TWILIO_PHONE_NUMBER = "+1234567890"
   ```
4. Start backend with prod profile: `.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod`
5. Start ngrok: `ngrok http 8080`
6. Configure webhook URL in Twilio: `https://xxx.ngrok-free.app/api/sms/incoming`

---

## Development Phases

| Phase | Description | Status |
|-------|-------------|--------|
| 01 | Project Setup & Structure | 🟢 Complete |
| 02 | Core Entities & SMS Parsing | 🟢 Complete |
| 03 | Twilio Integration & Matching | 🟢 Complete |
| 04 | REST API for Flutter | 🟢 Complete |
| 05 | Flutter Project Setup | 🟢 Complete |
| 06 | Flutter Features | 🟢 Complete |
| 07 | Integration & Testing | 🟢 Complete |

---

## Documentation

- [Architecture Reference](plans/ARCHITECTURE.md)
- [Twilio Setup Guide](docs/TWILIO_SETUP.md)
- [SMS Test Checklist](docs/SMS_TEST_CHECKLIST.md)
- [Launch Checklist](docs/LAUNCH_CHECKLIST.md)
- [Demo Script](docs/DEMO_SCRIPT.md)

---

## License

MIT License - See LICENSE file for details.

## Contributing

This is a hackathon project for humanitarian purposes. Contributions welcome!

---

Built with ❤️ for maternal healthcare in crisis settings.
