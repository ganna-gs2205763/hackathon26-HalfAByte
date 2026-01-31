# AI SMS Integration Walkthrough

## Summary
Successfully resolved Spring Boot startup issues and enabled AI-powered SMS conversations using OpenAI's GPT-4o-mini.

## Changes Made

### 1. OpenAI Configuration Bean
Created `OpenAiConfig.java` to expose `OpenAiService` as a Spring bean:

```java
@Bean
@ConditionalOnProperty(name = "openai.enabled", havingValue = "true", matchIfMissing = true)
public OpenAiService openAiService() {
    if (apiKey == null || apiKey.isBlank()) {
        return null; // Will use fallback responses
    }
    return new OpenAiService(apiKey, Duration.ofSeconds(timeoutSeconds));
}
```

**File:** `src/main/java/com/safebirth/config/OpenAiConfig.java`

### 2. DotEnvLoader for .env File Support
Created `DotEnvLoader.java` to load environment variables from `.env` file:

- Reads `.env` file at application startup
- Adds properties to Spring Environment as highest priority source
- Enables local development without setting system environment variables

**File:** `src/main/java/com/safebirth/config/DotEnvLoader.java`

### 3. Bean Disambiguation
Resolved Spring bean conflicts between two `ConversationService` classes:

| Package | Bean Name |
|---------|-----------|
| `com.safebirth.sms.conversation` | `aiConversationManager` |
| `com.safebirth.sms.simulator` | `simulatorConversationService` |

### 4. Webhook Endpoint
Added new `/api/sms/webhook` endpoint in `SmsWebhookController.java`:

```java
@PostMapping("/webhook")
public ResponseEntity<WebhookResponse> aiWebhook(@RequestBody WebhookRequest request) {
    String response = conversationService.processMessage(request.from(), request.body());
    return ResponseEntity.ok(new WebhookResponse(request.from(), response, true, null));
}
```

**File:** `src/main/java/com/safebirth/sms/handler/SmsWebhookController.java`

## Testing Results

### Health Check
```
GET /api/sms/health
→ SMS service is running, gatewayAvailable: True, mode: ready
```

### AI Conversation (English)
```
POST /api/sms/webhook
{"from": "+962791234567", "body": "Hello, I need help"}
→ "Hello! Are you a pregnant mother or a volunteer? Please let me know..."
```

```
POST /api/sms/webhook  
{"from": "+962791234567", "body": "I am a mother"}
→ "Thank you for reaching out! Can you please provide..."
```

### Emergency Response
```
POST /api/sms/webhook
{"from": "+962799999999", "body": "Help! My water broke"}
→ "I'm here to help you! Can you please share your..."
```

## Configuration

### Environment Variables (.env)
```
OPENAI_API_KEY=sk-proj-...
```

### Application Properties (optional)
```properties
openai.model=gpt-4o-mini
openai.max-tokens=300
openai.temperature=0.7
openai.timeout-seconds=60
```

## Server Startup
```bash
mvn spring-boot:run
# Server runs on http://localhost:8080
# API endpoints:
#   GET  /api/sms/health    - Health check
#   POST /api/sms/webhook   - AI-powered SMS endpoint
#   POST /api/sms/simulate  - Legacy command-based simulation
```

## Files Modified/Created

| File | Description |
|------|-------------|
| `src/main/java/com/safebirth/config/OpenAiConfig.java` | OpenAI service bean configuration |
| `src/main/java/com/safebirth/config/DotEnvLoader.java` | Loads .env file into Spring properties |
| `src/main/java/com/safebirth/SafeBirthApplication.java` | Added DotEnvLoader initializer |
| `src/main/java/com/safebirth/sms/handler/SmsWebhookController.java` | Added /webhook endpoint |
| `src/main/java/com/safebirth/sms/conversation/ConversationService.java` | Added bean name |
| `src/main/java/com/safebirth/sms/conversation/AiConversationService.java` | Made OpenAiService optional |
| `src/main/java/com/safebirth/sms/simulator/ConversationService.java` | Added bean name |
| `scripts/simulate-sms.ps1` | PowerShell script to test SMS flows |
