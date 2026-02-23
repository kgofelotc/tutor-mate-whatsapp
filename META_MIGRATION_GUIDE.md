# Meta WhatsApp Business Platform Migration Guide

## 🎯 Migration Complete!

Your Spring Boot application has been successfully migrated from Twilio to **Meta's WhatsApp Business Platform**.

## 📋 What Changed

### ✅ New Files Created
1. **MetaWhatsAppService.java** - Handles all WhatsApp messaging via Meta's API
2. **MetaWebhookController.java** - Receives and processes incoming messages from Meta
3. This guide

### ✅ Modified Files
1. **application.properties** - Updated with Meta configuration
2. **ConversationService.java** - Now uses MetaWhatsAppService instead of TwilioService

### 🗑️ Files Kept for Reference
- TwilioService.java (can be deleted once migration is verified)
- TwilioWebhookController.java (can be deleted once migration is verified)

---

## 🚀 Setup Instructions

### Step 1: Get Meta WhatsApp Credentials

1. **Create a Meta Developer Account**
   - Visit https://developers.facebook.com/
   - Click "Get Started" to create a developer account

2. **Create a Meta App**
   - Go to https://developers.facebook.com/apps
   - Click "Create App"
   - Choose "Business" as app type
   - Fill in app details

3. **Add WhatsApp Product**
   
   **Option A: If you see "Add a Product" section:**
   - In your app dashboard, find "WhatsApp" under "Add a Product"
   - Click "Set Up"
   
   **Option B: If you see "Connect with customers through WhatsApp":**
   - Click "Customize the Connect with customers through WhatsApp use case"
   - This means WhatsApp is already added to your app
   
   **Option C: If WhatsApp is already in your left sidebar:**
   - Just click on "WhatsApp" in the left navigation menu
   - Skip to step 4 to get your credentials

4. **Get Your Credentials**
   You need these 4 values:

   a) **Access Token** (Temporary for testing)
      - In App Dashboard, click "WhatsApp" in left sidebar (or your use case)
      - Go to "API Setup" or "Getting Started" 
      - Look for "Temporary access token" - copy this
      - **Note**: For production, you'll need a permanent System User token
      - To create permanent token:
        1. Go to Meta Business Suite (business.facebook.com)
        2. Settings → Business Settings → Users → System Users
        3. Create system user → Generate token
        4. **Permissions needed**: `whatsapp_business_messaging`, `whatsapp_business_management`

   b) **Phone Number ID**
      - Same page as access token (API Setup)
      - Look for "Phone number ID" 
      - It's a numeric ID (e.g., `123456789012345`)
      - **Important**: This is NOT your phone number, it's the ID of your WhatsApp Business Phone Number

   c) **Business Account ID (WABA ID)**
      - On the same API Setup page, look for "WhatsApp Business Account ID"
      - Or go to Meta Business Suite → WhatsApp Accounts
      - Copy the WhatsApp Business Account ID (numeric ID)

   d) **Webhook Verify Token**
      - Create your own secure random string (e.g., "my_super_secret_verify_token_12345")
      - You'll use this when setting up the webhook
      - Keep this safe - you'll need it in both your app and Meta dashboard

### Step 2: Configure Your Application

Update your `application.properties` or set environment variables:

```properties
# Option 1: Direct configuration (development only)
meta.whatsapp.access.token=YOUR_ACCESS_TOKEN_HERE
meta.whatsapp.phone.number.id=YOUR_PHONE_NUMBER_ID_HERE
meta.whatsapp.business.account.id=YOUR_WABA_ID_HERE
meta.webhook.verify.token=YOUR_CUSTOM_VERIFY_TOKEN_HERE
```

**OR** use environment variables (recommended for production):

```bash
# Windows PowerShell
$env:META_WHATSAPP_ACCESS_TOKEN="your_access_token"
$env:META_WHATSAPP_PHONE_NUMBER_ID="your_phone_number_id"
$env:META_WHATSAPP_BUSINESS_ACCOUNT_ID="your_waba_id"
$env:META_WEBHOOK_VERIFY_TOKEN="your_custom_verify_token"

# Run your app
mvn spring-boot:run
```

### Step 3: Expose Your Webhook (Development)

Meta needs to send webhooks to your application. Use **ngrok** or similar:

```powershell
# Install ngrok if you haven't
# Download from: https://ngrok.com/download

# Start your Spring Boot app (default port 8082)
mvn spring-boot:run

# In a new terminal, start ngrok
ngrok http 8082
```

Copy the HTTPS URL (e.g., `https://abc123.ngrok.io`)

### Step 4: Configure Meta Webhook

1. **Go to App Dashboard → WhatsApp → Configuration**

2. **Set Webhook URL**
   - Callback URL: `https://YOUR_NGROK_URL/meta/webhook`
   - Verify Token: Use the same token from your application.properties
   - Click "Verify and Save"

3. **Subscribe to Webhook Fields**
   - Check the box for `messages`
   - This ensures you receive incoming message events

### Step 5: Test Your Setup

1. **Start your application**
   ```bash
   mvn spring-boot:run
   ```

2. **Send a test message**
   - Go to App Dashboard → WhatsApp → API Setup
   - Use the "Send and receive messages" section
   - Send a message to your test number

3. **Or create a test endpoint** (optional):
   ```java
   @GetMapping("/test-meta")
   public String testMeta() {
       metaWhatsAppService.sendTextMessage("27XXXXXXXXX", "Hello from Meta!");
       return "Message sent!";
   }
   ```

---

## 🔄 Key Differences: Twilio vs Meta

| Feature | Twilio | Meta |
|---------|--------|------|
| **Message Format** | Phone numbers with `whatsapp:` prefix | Just the phone number |
| **Webhook Format** | Form-encoded data | JSON payload |
| **Interactive Buttons** | Simulated with text + emojis | Native interactive buttons (up to 3) |
| **Lists** | Simulated with numbered text | Native interactive lists (up to 10 items) |
| **API Style** | Twilio SDK | REST API (Spring RestTemplate) |
| **Authentication** | Account SID + Auth Token | Access Token (Bearer) |

---

## 📱 Message Types Supported

### 1. Text Messages
```java
metaWhatsAppService.sendTextMessage(phoneNumber, "Hello!");
```

### 2. Interactive Buttons (up to 3)
```java
List<String> buttons = Arrays.asList("Option 1", "Option 2", "Option 3");
metaWhatsAppService.sendMessageWithButtons(phoneNumber, "Choose an option:", buttons);
```

### 3. Interactive Lists (up to 10 items)
```java
List<MetaWhatsAppService.ListOption> options = new ArrayList<>();
options.add(new MetaWhatsAppService.ListOption("1", "Math", "Learn mathematics"));
options.add(new MetaWhatsAppService.ListOption("2", "Science", "Explore science"));

metaWhatsAppService.sendListMessage(phoneNumber, "Subjects", "Choose a subject:", options);
```

### 4. Template Messages (requires pre-approval)
```java
metaWhatsAppService.sendTemplateMessage(
    phoneNumber, 
    "session_reminder", 
    "en", 
    Arrays.asList("John Doe", "Mathematics", "2026-02-15")
);
```

---

## 🔍 Webhook Events Received

Your `MetaWebhookController` automatically handles:

- ✅ **Text messages** - Regular text from users
- ✅ **Button clicks** - When users click interactive buttons
- ✅ **List selections** - When users select from lists
- ✅ **Quick reply buttons** - Quick response buttons
- ℹ️ **Media messages** - Images, documents, audio, video (logged but not processed)
- ℹ️ **Location** - Location sharing (logged)
- ℹ️ **Status updates** - Message delivery status (logged)

---

## ⚠️ Important Notes

### Rate Limits
- Meta has rate limits based on your Business verification level
- Tier 1 (default): 1,000 conversations per day
- Higher tiers require Business Verification

### Message Templates
- For **outbound messages** outside a 24-hour window, you MUST use approved templates
- Templates must be created in Meta Business Suite
- User-initiated conversations have a 24-hour window for free-form messages

### Phone Numbers
- Format: Country code + number (no + or spaces)
- Example: `27821234567` for South Africa
- Remove `whatsapp:` prefix - Meta doesn't use it

### Testing
- Meta provides a test number during development
- Add your test recipients in the App Dashboard
- Production requires Business Verification

---

## 🧪 Testing Checklist

- [ ] Application starts without errors
- [ ] Webhook verification succeeds in Meta dashboard
- [ ] Can receive incoming messages (check logs)
- [ ] Can send text messages
- [ ] Can send interactive buttons
- [ ] Can send interactive lists
- [ ] Button clicks are processed correctly
- [ ] List selections are processed correctly
- [ ] ConversationService flows work end-to-end

---

## 🐛 Troubleshooting

### Webhook Verification Fails
- ✅ Check that your verify token matches in both Meta and application.properties
- ✅ Ensure ngrok URL is correct and HTTPS
- ✅ Check that your app is running on the correct port

### Messages Not Sending
- ✅ Verify your access token is valid and has correct permissions
- ✅ Check phone number format (no + or spaces)
- ✅ Ensure recipient is added as a test user (for development)
- ✅ Check application logs for API errors

### Not Receiving Incoming Messages
- ✅ Verify webhook is subscribed to `messages` field
- ✅ Check webhook URL is publicly accessible
- ✅ Look for errors in application logs
- ✅ Test with Meta's "Send Test Message" feature

### Interactive Messages Not Working
- ✅ Button titles max 20 characters
- ✅ List titles max 24 characters
- ✅ Max 3 buttons per message
- ✅ Max 10 list items per message

---

## 📚 Additional Resources

- [Meta WhatsApp Business Platform Documentation](https://developers.facebook.com/docs/whatsapp/cloud-api)
- [Message Templates Guide](https://developers.facebook.com/docs/whatsapp/message-templates)
- [Business Verification](https://developers.facebook.com/docs/development/release/business-verification)
- [Interactive Messages Guide](https://developers.facebook.com/docs/whatsapp/cloud-api/guides/send-messages#interactive-messages)

---

## 🔄 Rollback (If Needed)

If you need to rollback to Twilio:

1. In `ConversationService.java`, change:
   ```java
   @Autowired
   private MetaWhatsAppService metaWhatsAppService;
   ```
   back to:
   ```java
   @Autowired
   private TwilioService twilioService;
   ```

2. Replace all `metaWhatsAppService` calls with `twilioService`

3. Update webhook endpoint in Twilio dashboard back to `/twilio/webhook`

---

## ✅ Next Steps

1. **Get your Meta credentials** (Step 1 above)
2. **Configure application.properties** (Step 2 above)
3. **Set up ngrok and webhook** (Steps 3-4 above)
4. **Test the integration** (Step 5 above)
5. **Create message templates** for notifications (if needed)
6. **Apply for Business Verification** (for production use)

**Need help?** Check the troubleshooting section or review the Meta documentation linked above.

Good luck with your migration! 🚀
