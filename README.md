# WhatsApp Spring Boot Application - Infobip Integration

## Overview

This Spring Boot application integrates with Infobip's WhatsApp API to send and receive WhatsApp messages. The application has been successfully migrated from Twilio to Infobip.

## Configuration

### Infobip Credentials (Already Configured)

Your application is configured with:
- **API Key**: `05e2528d857d11fc3bb326170342ded4-4098c481-33a1-43e3-9d0a-d195776ce9aa`
- **Base URL**: `https://e5v55q.api.infobip.com`
- **WhatsApp Sender**: `447860088970`

## Getting Started

### 1. Activate Your Test Sender (First Time Only)

According to Infobip documentation:

1. Add `447860099299` to your WhatsApp contacts (or scan QR code from Infobip portal)
2. Send your Infobip account username in the conversation
3. Wait for auto-reply confirming sender activation

**Note**: Your sender number is `447860088970` - this is what will appear as the sender when you send messages.

### 2. Start the Application

```bash
cd c:\repository\personal\twilio-whatsapp-springboot
mvn spring-boot:run
```

Wait for the message: `Started DemoApplication in X.XXX seconds`

### 3. Send Your First WhatsApp Message

#### Option 1: Quick Test (Send Template to Tebogo)

```powershell
Invoke-RestMethod -Uri "http://localhost:8082/api/whatsapp/send-test" -Method POST
```

#### Option 2: Send Custom Text Message

```powershell
$headers = @{
    "Content-Type" = "application/json"
}

$body = @{
    to = "27662035457"
    message = "Hello from Infobip! This is a test message from Spring Boot."
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8082/api/whatsapp/send-text" `
    -Method POST `
    -Headers $headers `
    -Body $body
```

#### Option 3: Send Template Message

```powershell
$headers = @{
    "Content-Type" = "application/json"
}

$body = @{
    to = "27662035457"
    templateName = "test_whatsapp_template_en"
    language = "en"
    placeholders = @("Tebogo")
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8082/api/whatsapp/send-template" `
    -Method POST `
    -Headers $headers `
    -Body $body
```

## API Endpoints

### WhatsApp Messaging Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/whatsapp/send-test` | POST | Quick test - sends template message to 27662035457 |
| `/api/whatsapp/send-text` | POST | Send a custom text message |
| `/api/whatsapp/send-template` | POST | Send a custom template message |

### Webhook Endpoint

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/infobip/webhook` | POST | Receives incoming WhatsApp messages from Infobip |

### Other Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/swagger-ui/index.html` | GET | Swagger API Documentation |
| `/h2-console` | GET | H2 Database Console |

## Webhook Configuration

To receive messages from your customers:

1. Go to Infobip Portal → WhatsApp → Webhooks
2. Set your webhook URL to: `https://your-domain.com/infobip/webhook`
3. Make sure your application is publicly accessible (use ngrok for testing)

### Testing with ngrok

```bash
# Install ngrok if not already installed
# Then run:
ngrok http 8082

# Copy the HTTPS URL (e.g., https://abc123.ngrok.io)
# Configure in Infobip portal: https://abc123.ngrok.io/infobip/webhook
```

## Message Types

### 1. Template Messages (Session Start)
- **Required** when initiating conversation with customers
- Must be pre-approved by WhatsApp
- Used for notifications and conversation starters
- Example: `test_whatsapp_template_en`

### 2. Text Messages (In-Session)
- Can be sent within 24-hour window after customer responds
- No template approval needed
- More flexible content

### 3. Rich Media Messages
- Images, videos, documents
- Location sharing
- Interactive buttons
- Contact cards

## Project Structure

```
src/main/java/com/example/demo/
├── DemoApplication.java                    # Main Spring Boot application
├── InfobipWhatsAppService.java            # WhatsApp messaging service
├── InfobipWebhookController.java          # Webhook for incoming messages
├── config/
│   └── SwaggerConfig.java                 # API documentation config
├── controller/
│   ├── AdminController.java               # Admin endpoints
│   └── WhatsAppTestController.java        # WhatsApp test endpoints
├── service/
│   ├── ConversationService.java           # Conversation management
│   └── DataInitializationService.java     # Sample data setup
├── entity/
│   ├── User.java                          # User entity
│   ├── Agent.java                         # Agent entity
│   ├── Message.java                       # Message entity
│   ├── Query.java                         # Query entity
│   └── SecurityQuestion.java              # Security question entity
├── repository/
│   └── [All JPA repositories]
└── dto/
    └── [Data Transfer Objects]
```

## Key Features

✅ Send template messages (for initiating conversations)
✅ Send text messages (within 24-hour session window)
✅ Receive incoming messages via webhook
✅ Conversation state management
✅ User authentication flow
✅ Agent assignment system
✅ H2 in-memory database
✅ Swagger API documentation

## Important Notes

### WhatsApp Business Rules

1. **24-Hour Window**: After a customer sends you a message, you have 24 hours to respond with any content
2. **Template Messages**: Outside the 24-hour window, you can only send pre-approved templates
3. **Sender Registration**: Your sender number must be registered and activated with Infobip
4. **Message Approval**: Template messages must be approved by WhatsApp before use

### Testing Tips

1. **Use Test Sender**: `447860099299` (Infobip's test number)
2. **Your Sender**: `447860088970` (appears as sender)
3. **Test Recipient**: `27662035457` (Tebogo's number from your example)
4. **Template**: `test_whatsapp_template_en` (must be approved in Infobip portal)

## Troubleshooting

### Application won't start
```bash
# Check if port 8082 is already in use
netstat -ano | findstr :8082

# Check logs for errors
mvn spring-boot:run
```

### Message sending fails
- Verify Infobip API key is correct
- Ensure sender number is activated
- Check if template is approved (for template messages)
- Verify recipient number format (include country code without +)

### Webhook not receiving messages
- Ensure webhook URL is publicly accessible
- Check Infobip portal webhook configuration
- Verify webhook URL format: `https://your-domain.com/infobip/webhook`

## Next Steps

1. ✅ **Activate your sender** (add 447860099299 to WhatsApp)
2. ✅ **Test sending messages** (use the test endpoints)
3. ⬜ **Configure webhook** (for receiving messages)
4. ⬜ **Register production sender** (for production use)
5. ⬜ **Create message templates** (in Infobip portal)
6. ⬜ **Upgrade account** (if using trial)

## Resources

- [Infobip WhatsApp API Documentation](https://www.infobip.com/docs/api#channels/whatsapp)
- [Infobip Portal](https://portal.infobip.com/)
- [WhatsApp Business Policy](https://www.whatsapp.com/legal/business-policy)

## Support

For Infobip-specific issues:
- Check Infobip Documentation
- Contact Infobip Support
- Visit Infobip Developer Portal

For application issues:
- Check application logs
- Review Swagger documentation at `http://localhost:8082/swagger-ui/index.html`
- Verify database state at `http://localhost:8082/h2-console`
