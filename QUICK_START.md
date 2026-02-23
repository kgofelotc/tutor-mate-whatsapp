# Quick Start Guide - Meta WhatsApp Integration

## ✅ Your Credentials (Already Configured!)

- **Access Token**: `EAATAmHy...` ✅
- **Phone Number ID**: `959729383897901` ✅
- **Business Account ID**: `941501588391536` ✅
- **Test Phone**: `+27 78 531 2360` ✅
- **From Number**: `+1 555 142 4698` (Meta's test number)

## 🚀 Start Your Application (3 Steps)

### Step 1: Set Environment Variables

```powershell
# Run this script to set credentials
.\setup-meta-credentials.ps1
```

### Step 2: Start the Application

```powershell
mvn spring-boot:run
```

Wait for: `Started DemoApplication in X.XXX seconds`

### Step 3: Test Sending a Message

```powershell
# Send a simple text message to yourself
Invoke-RestMethod -Uri "http://localhost:8082/api/meta/test/send?to=27785312360&message=Hello from TutorMate!" -Method GET
```

**You should receive a WhatsApp message on +27 78 531 2360!** 🎉

---

## 🧪 More Test Commands

### Test Interactive Buttons
```powershell
Invoke-RestMethod -Uri "http://localhost:8082/api/meta/test/buttons?to=27785312360" -Method GET
```

### Test Interactive List
```powershell
Invoke-RestMethod -Uri "http://localhost:8082/api/meta/test/list?to=27785312360" -Method GET
```

### Test the Full Conversation Flow
Send **"hi"** or **"menu"** from WhatsApp to the Meta test number to start the tutoring bot conversation.

---

## 🔌 Setting Up Webhooks (For Receiving Messages)

To receive incoming messages, you need to expose your webhook to the internet.

### Option 1: Using ngrok (Recommended for Testing)

1. **Download and install ngrok**: https://ngrok.com/download

2. **Start your app** (if not already running):
   ```powershell
   mvn spring-boot:run
   ```

3. **In a new terminal, start ngrok**:
   ```powershell
   ngrok http 8082
   ```

4. **Copy the HTTPS URL** (e.g., `https://abc123.ngrok.io`)

5. **Configure webhook in Meta**:
   - Go to your app dashboard → WhatsApp → Configuration
   - Click "Edit" next to Webhook
   - **Callback URL**: `https://YOUR_NGROK_URL/meta/webhook`
   - **Verify token**: `tutormate_secure_verify_token_2026`
   - Click "Verify and Save"
   - Subscribe to `messages` webhook field

6. **Test it**: Send a WhatsApp message to `+1 555 142 4698` (the Meta test number)

7. **Check your app logs** - you should see:
   ```
   Received webhook from Meta: ...
   Processing message from 27785312360: hi
   ```

---

## ⚠️ Important Notes

### Temporary Access Token
- **Valid for**: 24 hours
- **Expires**: ~February 24, 2026
- **For production**: Create a permanent System User token

### Test Phone Number
- **Free for**: 90 days (~until May 24, 2026)
- **Max recipients**: 5 test phone numbers
- **For production**: Business verification required

### Webhook URL
- Must be **HTTPS** (ngrok provides this)
- Must be **publicly accessible**
- Must respond to GET requests for verification

---

## 🎯 What's Next?

1. ✅ **Test sending messages** (you've got the credentials!)
2. ⏳ **Set up webhook** (to receive messages)
3. ⏳ **Test conversation flow** (send "hi" to the bot)
4. ⏳ **Create message templates** (for notifications)
5. ⏳ **Business verification** (for production)

---

## 🆘 Troubleshooting

### Message Not Sent
- ✅ Check if app is running on port 8082
- ✅ Verify credentials are set correctly
- ✅ Check application logs for errors
- ✅ Ensure phone number format is correct (no spaces or +)

### Webhook Verification Failed
- ✅ Verify token must match: `tutormate_secure_verify_token_2026`
- ✅ URL must be HTTPS (ngrok provides this)
- ✅ App must be running when you click "Verify and Save"

### Not Receiving Messages
- ✅ Webhook must be configured and verified
- ✅ Subscribe to `messages` field in Meta dashboard
- ✅ Send message TO the Meta number: `+1 555 142 4698`
- ✅ Check app logs for incoming webhook data

---

**Ready to go! Run `.\setup-meta-credentials.ps1` and then start the app!** 🚀
