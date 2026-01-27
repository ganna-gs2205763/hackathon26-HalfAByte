# BirthLink  
**SMS-Based Safe Childbirth Support in Crisis Settings**

---

## Overview
BirthLink is an SMS-first coordination and support system designed for crisis and displacement settings where internet access, smartphones, and stable infrastructure cannot be assumed.

The system enables pregnant women or accompanying family members to request help through simple, menu-driven SMS interactions. Requests are processed by a backend coordination system that prioritizes cases by urgency and approximate location, allowing community volunteers and coordinators to respond more effectively during critical moments of labor and childbirth.

This project addresses **Health – HPS#2: Digital Tools for Safe Childbirth in Crisis Settings**.

---

## Key Features
- SMS-based help requests using basic mobile phones  
- Menu-driven request flows suitable for low literacy contexts  
- Backend case creation, prioritization, and tracking  
- Volunteer coordination logic with structured case states  
- Offline-resilient, low-bandwidth system design  
- Human-in-the-loop approach (no automated medical diagnosis)

---

## Live Demo
- **SMS Simulator:** https://safebirth-api-748784316537.me-central1.run.app/simulator/index.html

This submission can be fully explored using the SMS simulator, which demonstrates the end-to-end flow of help requests, backend processing, and coordination logic without requiring local setup or live telecom infrastructure. For demo purposes, it currently works in English, Arabic support will be added.

---

## Architecture Overview
BirthLink follows a **backend-first REST API architecture** that supports multiple clients (SMS, web dashboards, and mobile applications).

Core components include:
- Spring Boot REST API (deployed on Google Cloud Run)
- SMS processing layer with simulator support
- Case management and prioritization services
- Volunteer coordination and analytics endpoints

---

## Current Scope (PoC Submission)

Implemented in this phase:
- SMS request handling
- Backend logic for case creation, urgency tagging, and location grouping  
- Volunteer and case management APIs  
- SMS simulator for testing flows without external dependencies  

This phase focuses on validating the **core coordination infrastructure** needed to reduce delays during childbirth emergencies in low-connectivity environments.

---

## Planned Features (Next Submission)

To be implemented in the next phase:
- **Guidance Module (using Decision Trees)**
  - Structured, non-diagnostic guidance flows through the mobile app
  - Decision-tree logic to support mothers or accompanying caregivers during labor or emergencies
  - Step-by-step prompts to reduce uncertainty while waiting for assistance
  - Designed to support safer decisions, not replace professional medical care
- Flutter mobile application connected to the live backend  
- APK distribution 

---

## Mobile Application Status
The Flutter mobile application is currently implemented using **static mock data** to show an example of what the heatmaps would look like.

---

## Design & Ethical Boundaries
All guidance and coordination features are intentionally designed to be:
- Non-diagnostic  
- Rule-based (decision trees)  
- Human-in-the-loop

BirthLink does not replace professional medical care and is intended to strengthen communication, coordination, and early response in crisis settings.

---

## Team
**Team Name:** HalfAByte  

**Team Members:**
- Bsmalla Mostafa  
- Carmela Chavez  
- Ahya AlWattar  
- Ganna Soltan  

---

## Credits
This project was developed as part of LifeLines 2026 Hackathon.

---

## License
This project is released under the MIT License.
