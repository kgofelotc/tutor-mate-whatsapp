# 🚀 TutorMate WhatsApp - Twilio Setup Guide

## Migration from Infobip to Twilio

✅ **Infobip implementation saved in branch:** `infobip-implementation`

---

## Step 1: Get Twilio Credentials

1. **Sign up for Twilio**
   - Go to https://www.twilio.com/try-twilio
   - Sign up for a free account

2. **Get Your Credentials**
   - Go to https://console.twilio.com
   - Find your **Account SID** and **Auth Token** on the dashboard
   - Copy these - you'll need them for Render configuration

3. **Enable WhatsApp Sandbox**
   - Go to https://console.twilio.com/us1/develop/sms/try-it-out/whatsapp-learn
   - Join the Twilio Sandbox for WhatsApp
   - Send the join code from your WhatsApp to the Twilio number
   - Your sandbox number will be: `+1 415 523 8886` (or similar)

---

## Step 2: Configure Render Environment Variables

1. **Go to Render Dashboard**
   - Visit https://dashboard.render.com
   - Click on your service: `tutormate-whatsapp`

2. **Go to Environment Tab**
   - Click **"Environment"** in the left menu

3. **Delete Old Infobip Variables**
   - Remove: `INFOBIP_API_KEY`
   - Remove: `INFOBIP_BASE_URL`
   - Remove: `INFOBIP_WHATSAPP_FROM`

4. **Add Twilio Variables**
   ```
   TWILIO_ACCOUNT_SID=<your_account_sid>
   TWILIO_AUTH_TOKEN=<your_auth_token>
   TWILIO_WHATSAPP_FROM=+14155238886
   ```

5. **Save Changes**
   - Render will automatically redeploy

---

## Step 3: Configure Twilio Webhook

Once deployment completes:

1. **Go to Twilio Console**
   - Visit https://console.twilio.com/us1/develop/sms/settings/whatsapp-sandbox

2. **Set Webhook URL**
   - Under **"When a message comes in"**
   - Set to: `https://tutormate-whatsapp.onrender.com/twilio/webhook`
   - Method: **HTTP POST**

3. **Save Configuration**

---

## Step 4: Test Your Application

### Test the Flow

1. **Join Sandbox** (if you haven't already)
   - Send the join code from your WhatsApp

2. **Start Conversation**
   - Send **"Hi"** to the Twilio WhatsApp number
   - You should receive the welcome message!

3. **Follow the Flow**
   - Select role (TUTOR or STUDENT)
   - Choose action (REGISTER, LOGIN, STATUS)
   - Complete registration or login

### Monitor Logs

- **Render Logs:** https://dashboard.render.com → Your Service → Logs
- **Twilio Logs:** https://console.twilio.com/us1/monitor/logs/sms

---

## Webhook URLs

- **Inbound Messages:** `https://tutormate-whatsapp.onrender.com/twilio/webhook`
- **Status Callbacks:** `https://tutormate-whatsapp.onrender.com/twilio/status`
- **Health Check:** `https://tutormate-whatsapp.onrender.com/twilio/webhook` (GET)

---

## Important Notes

### 📱 Phone Number Format

Twilio uses the format: `whatsapp:+1234567890`
- The app automatically handles this format
- Store numbers WITHOUT the `whatsapp:` prefix in the database

### 🆓 Twilio Free Trial

- **Sandbox is FREE** with no time limit
- You can test with any WhatsApp number after they join the sandbox
- Upgrade to get your own WhatsApp Business number

### 🔄 Infobip Version

To switch back to Infobip:
```bash
git checkout infobip-implementation
```

---

## Troubleshooting

### Issue: Webhook not receiving messages

**Solution:**
1. Check Twilio webhook configuration
2. Verify URL includes `/twilio/webhook`
3. Ensure service is running on Render
4. Check Render logs for errors

### Issue: Messages not sending

**Solution:**
1. Verify Twilio credentials in Render environment variables
2. Check Account SID and Auth Token are correct
3. Ensure phone numbers have correct format
4. Check Twilio logs for error details

### Issue: "Unable to create record" error

**Solution:**
1. Verify the recipient joined the WhatsApp Sandbox
2. Check phone number format (include country code)
3. Verify your Twilio account is active

---

## Cost Comparison

| Feature | Twilio | Infobip |
|---------|--------|---------|
| **Trial** | Free sandbox (unlimited testing) | 60-day trial, limited messages |
| **Webhook** | ✅ Available in trial | ❌ Not available in trial |
| **Setup** | Simple, 5 minutes | Complex, support required |
| **Pricing** | Pay-as-you-go, transparent | Enterprise pricing |

---

## Next Steps After Setup

1. ✅ Configure Twilio credentials in Render
2. ✅ Set webhook URL in Twilio Console
3. ✅ Join WhatsApp Sandbox
4. ✅ Test conversation flow
5. 📊 Monitor usage and performance
6. 🎨 Add more features (tutoring sessions, payments, etc.)

---

**Your TutorMate WhatsApp application is ready with Twilio! 🎉**
