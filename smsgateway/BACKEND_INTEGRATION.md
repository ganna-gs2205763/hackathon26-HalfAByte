# SMS Gateway - Backend Integration Guide

This document describes how to integrate your backend WebSocket server with the SMS Gateway Android app.

---

## Overview

The SMS Gateway app connects to your backend via WebSocket. When an SMS is received on the device, it's forwarded to your backend. Your backend can then send commands to the app to send SMS messages.

```
┌─────────────┐         WebSocket         ┌─────────────┐
│   Android   │ ◄──────────────────────►  │   Backend   │
│  SMS Gateway│                           │   Server    │
└─────────────┘                           └─────────────┘
      │                                          │
      │ Receives SMS                             │ Processes & Responds
      │ Sends SMS                                │ Decides recipients
      ▼                                          ▼
  ┌─────────┐                              ┌─────────┐
  │ Carrier │                              │ Database│
  └─────────┘                              └─────────┘
```

---

## WebSocket Connection

### Endpoint
Your server should expose a WebSocket endpoint (e.g., `wss://your-server.com/ws/sms-gateway`).

### Connection Parameters
The app connects with a standard WebSocket handshake. No special headers are required, but you may add authentication:

```
# Example with token authentication via query param
wss://your-server.com/ws/sms-gateway?token=YOUR_AUTH_TOKEN
```

### Keep-Alive
The app sends WebSocket pings every 30 seconds. Ensure your server responds to pings.

---

## Message Protocol

All messages are JSON objects with a `type` field.

### App → Backend Messages

#### 1. Incoming SMS Notification

Sent when the app receives an SMS.

```json
{
  "type": "incoming_sms",
  "sender": "+1234567890",
  "message": "Hello, this is a test message",
  "timestamp": 1706554800000
}
```

| Field | Type | Description |
|-------|------|-------------|
| `type` | string | Always `"incoming_sms"` |
| `sender` | string | Phone number of the SMS sender |
| `message` | string | Full SMS content (may be concatenated from multi-part) |
| `timestamp` | number | Unix timestamp in milliseconds |

---

#### 2. SMS Sent Confirmation

Sent after the app attempts to send SMS messages.

```json
{
  "type": "sms_sent",
  "request_id": "abc123-uuid",
  "recipients": ["+1111111111", "+2222222222"],
  "success_count": 2,
  "failure_count": 0,
  "status": "success"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `type` | string | Always `"sms_sent"` |
| `request_id` | string | The ID you provided in the send command |
| `recipients` | array | List of phone numbers targeted |
| `success_count` | number | How many SMS were sent successfully |
| `failure_count` | number | How many failed |
| `status` | string | `"success"`, `"partial"`, or `"failed"` |

---

### Backend → App Messages

#### 1. Send SMS Command

Tell the app to send an SMS to one or more recipients.

```json
{
  "type": "send_sms",
  "request_id": "unique-id-for-tracking",
  "recipients": ["+1111111111", "+2222222222"],
  "message": "Alert: New notification received!"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `type` | string | Must be `"send_sms"` |
| `request_id` | string | Unique ID to track this request (returned in confirmation) |
| `recipients` | array | List of phone numbers to send to |
| `message` | string | The SMS body (max ~160 chars for single SMS, longer messages are auto-split) |

---

## Example Flow

```
1. User sends SMS to the gateway phone
   Phone → App: SMS received

2. App notifies backend
   App → Backend: {"type": "incoming_sms", "sender": "+1234567890", ...}

3. Backend processes and decides to alert team members
   Backend → App: {"type": "send_sms", "recipients": ["+1111", "+2222"], ...}

4. App sends SMS to each recipient
   App → Carrier: SMS to +1111
   App → Carrier: SMS to +2222

5. App confirms to backend
   App → Backend: {"type": "sms_sent", "status": "success", ...}
```

---

## Backend Implementation Example

### Node.js (with `ws` library)

```javascript
const WebSocket = require('ws');

const wss = new WebSocket.Server({ port: 8080 });

wss.on('connection', (ws) => {
  console.log('SMS Gateway connected');

  ws.on('message', (data) => {
    const message = JSON.parse(data);
    
    if (message.type === 'incoming_sms') {
      console.log(`SMS from ${message.sender}: ${message.message}`);
      
      // Process the SMS and decide response
      // For example, forward to team members
      const alertRecipients = ['+1111111111', '+2222222222'];
      
      ws.send(JSON.stringify({
        type: 'send_sms',
        request_id: generateUUID(),
        recipients: alertRecipients,
        message: `Alert from ${message.sender}: ${message.message}`
      }));
    }
    
    if (message.type === 'sms_sent') {
      console.log(`SMS delivery: ${message.status} (${message.success_count}/${message.recipients.length})`);
    }
  });

  ws.on('close', () => {
    console.log('SMS Gateway disconnected');
  });
});

function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = Math.random() * 16 | 0;
    return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
  });
}
```

### Python (with `websockets` library)

```python
import asyncio
import json
import uuid
import websockets

async def handle_connection(websocket):
    print("SMS Gateway connected")
    
    async for message in websocket:
        data = json.loads(message)
        
        if data['type'] == 'incoming_sms':
            print(f"SMS from {data['sender']}: {data['message']}")
            
            # Process and respond
            response = {
                'type': 'send_sms',
                'request_id': str(uuid.uuid4()),
                'recipients': ['+1111111111', '+2222222222'],
                'message': f"Alert from {data['sender']}: {data['message']}"
            }
            await websocket.send(json.dumps(response))
        
        elif data['type'] == 'sms_sent':
            print(f"SMS delivery: {data['status']}")

async def main():
    async with websockets.serve(handle_connection, "0.0.0.0", 8080):
        await asyncio.Future()

asyncio.run(main())
```

---

## Testing

### Using the Mock Mode

1. Install the app on your Android device
2. Enable **Mock Mode** toggle
3. Tap **Connect**
4. Send an SMS to the device from another phone
5. The mock server will automatically echo back a send command
6. The device will send an SMS reply to the original sender

### Testing with Your Backend

1. Deploy your WebSocket server
2. In the app, disable Mock Mode
3. Enter your WebSocket URL (e.g., `wss://your-server.com/ws`)
4. Tap **Connect**
5. Send an SMS to the device and verify your backend receives it
6. Send a `send_sms` command from your backend and verify the SMS is sent

---

## Error Handling

### Connection Errors
The app automatically reconnects with exponential backoff (1s → 2s → 4s → ... → max 30s).

### SMS Send Failures
Check the `failure_count` in the `sms_sent` confirmation. Common causes:
- Invalid phone number format
- No cellular signal
- SMS permissions revoked

---

## Security Recommendations

1. **Use WSS (TLS)**: Always use `wss://` in production
2. **Authentication**: Add token-based auth via query param or first message
3. **Rate Limiting**: Limit SMS sends to prevent abuse
4. **Input Validation**: Validate phone numbers before sending

---

## Support

For questions about the app, refer to the source code or contact the development team.
