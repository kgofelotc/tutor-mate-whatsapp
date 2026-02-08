# Quick Start Guide - Send Your First WhatsApp Message! 🚀

## Step 1: Start the Application

Open PowerShell and run:

```powershell
cd c:\repository\personal\twilio-whatsapp-springboot
mvn spring-boot:run
```

**Wait for this message**: `Started DemoApplication in X.XXX seconds`

## Step 2: Send a Test Message

### Option A: Use the PowerShell Test Script (Recommended)

```powershell
.\test-whatsapp.ps1
```

Select option 1 to send a quick test message!

### Option B: Manual Test

Open a **NEW PowerShell window** (keep the server running in the first one) and run:

```powershell
Invoke-RestMethod -Uri "http://localhost:8082/api/whatsapp/send-test" -Method POST
```

## What Happens?

The application will:
1. ✅ Connect to Infobip API using your credentials
2. ✅ Send a WhatsApp template message to `27662035457` (Tebogo)
3. ✅ Return the message status

## Expected Response

```json
{
  "success": true,
  "message": "Test message sent to Tebogo!",
  "bulkId": "xxx-xxx-xxx",
  "messages": [...]
}
```

## Important Notes

### Before Sending Messages:

1. **Activate Your Sender** (First Time Only):
   - Add `447860099299` to your WhatsApp contacts
   - Send your Infobip username to activate
   - Wait for confirmation

2. **Template Approval**:
   - Template `test_whatsapp_template_en` must be approved in Infobip portal
   - Check your Infobip dashboard for template status

3. **Recipient Number**:
   - Must be a valid WhatsApp number
   - Include country code (e.g., `27662035457` for South Africa)
   - No `+` or `whatsapp:` prefix needed

## Troubleshooting

### Error: "Unable to connect to the remote server"
- Check if the application is running
- Verify it's listening on port `8082`

### Error: "API Error" or "Failed to send message"
- Verify your Infobip API key is correct
- Check if your sender is activated
- Ensure the template is approved
- Check recipient number format

### Server Stops Immediately
- Check for port conflicts: `netstat -ano | findstr :8082`
- Review console errors
- Try: `mvn clean install` then `mvn spring-boot:run`

## Next Steps

Once you successfully send a message:

1. ✅ Send custom text messages
2. ✅ Set up webhook to receive messages
3. ✅ Explore other endpoints in Swagger: `http://localhost:8082/swagger-ui/index.html`
4. ✅ View database: `http://localhost:8082/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`, username: `sa`)

## Test Endpoints

| Action | Endpoint |
|--------|----------|
| Quick Test | `POST http://localhost:8082/api/whatsapp/send-test` |
| Send Text | `POST http://localhost:8082/api/whatsapp/send-text` |
| Send Template | `POST http://localhost:8082/api/whatsapp/send-template` |
| API Docs | `GET http://localhost:8082/swagger-ui/index.html` |
| Health Check | `GET http://localhost:8082/actuator/health` |

---

**Your Configuration:**
- 📱 Sender: `447860088970`
- 🔑 API Key: `05e25...e9aa` (configured)
- 🌐 Base URL: `https://e5v55q.api.infobip.com`
- 📨 Test Recipient: `27662035457`
- 📋 Template: `test_whatsapp_template_en`

Happy messaging! 💬
