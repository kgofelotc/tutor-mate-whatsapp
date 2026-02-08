# 🚀 TutorMate WhatsApp Deployment Guide

## Deploying to Render

### Prerequisites
- GitHub account
- Render account (sign up at https://render.com)
- Infobip account with WhatsApp sender configured

---

## Step 1: Push Code to GitHub

```powershell
# Initialize git repository (if not already done)
cd c:\repository\personal\twilio-whatsapp-springboot
git init

# Add all files
git add .

# Commit
git commit -m "Initial commit: TutorMate WhatsApp application"

# Create repository on GitHub and push
git remote add origin https://github.com/YOUR_USERNAME/tutormate-whatsapp.git
git branch -M main
git push -u origin main
```

---

## Step 2: Deploy on Render

### Option A: Using render.yaml (Recommended)

1. **Go to Render Dashboard**
   - Visit https://dashboard.render.com
   - Click **"New +"** → **"Blueprint"**

2. **Connect Repository**
   - Select your GitHub repository: `tutormate-whatsapp`
   - Render will detect `render.yaml` automatically

3. **Configure Environment Variables**
   - Click on the detected service
   - Go to **"Environment"** tab
   - Add your Infobip API Key:
     ```
     INFOBIP_API_KEY=05e2528d857d11fc3bb326170342ded4-4098c481-33a1-43e3-9d0a-d195776ce9aa
     ```

4. **Deploy**
   - Click **"Apply"**
   - Wait for deployment to complete (5-10 minutes)

### Option B: Manual Deployment

1. **Go to Render Dashboard**
   - Visit https://dashboard.render.com
   - Click **"New +"** → **"Web Service"**

2. **Connect Repository**
   - Connect to GitHub
   - Select `tutormate-whatsapp` repository

3. **Configure Service**
   ```
   Name: tutormate-whatsapp
   Runtime: Docker
   Region: Choose closest to you
   Branch: main
   Docker Build Context Directory: ./
   Dockerfile Path: ./Dockerfile
   Instance Type: Free
   ```

4. **Add Environment Variables**
   ```
   INFOBIP_API_KEY=05e2528d857d11fc3bb326170342ded4-4098c481-33a1-43e3-9d0a-d195776ce9aa
   INFOBIP_BASE_URL=https://e5v55q.api.infobip.com
   INFOBIP_WHATSAPP_FROM=447860088970
   SERVER_PORT=8082
   ```

5. **Deploy**
   - Click **"Create Web Service"**
   - Wait for deployment

---

## Step 3: Get Your Render URL

After deployment completes, you'll get a URL like:
```
https://tutormate-whatsapp.onrender.com
```

**Webhook URL will be:**
```
https://tutormate-whatsapp.onrender.com/infobip/webhook
```

---

## Step 4: Configure Infobip Webhook

1. **Login to Infobip Portal**
   - Go to https://portal.infobip.com
   - Login with your credentials

2. **Navigate to WhatsApp Configuration**
   - Go to **"Channels & Numbers"** → **"WhatsApp"**
   - Find your sender: `447860088970`

3. **Set Webhook URL**
   - Click **"Configure"** or **"Edit"**
   - Find **"Inbound Messages"** section
   - Set webhook URL to:
     ```
     https://tutormate-whatsapp.onrender.com/infobip/webhook
     ```
   - Method: **POST**
   - Save changes

4. **Test Webhook**
   - Infobip may have a "Test" button
   - Or send a test message to verify

---

## Step 5: Test Your Application

### Test via WhatsApp

1. **Send Initial Message**
   - Open WhatsApp on your phone
   - Send "Hi" to: `+44 7860 088970`

2. **Expected Response**
   ```
   🎓 Welcome to TutorMate!

   Your learning companion for connecting students and tutors.

   Please select your role:
   1️⃣ TUTOR - Share your knowledge
   2️⃣ STUDENT - Find help
   ```

3. **Follow the Flow**
   - Select role (1 or 2)
   - Choose action (Register/Login/Status)
   - Complete registration or login

### Monitor Logs

1. **View Render Logs**
   - Go to Render Dashboard
   - Click on your service
   - Go to **"Logs"** tab
   - Watch real-time logs

2. **Check Health**
   - Visit: `https://tutormate-whatsapp.onrender.com/actuator/health`
   - Should return: `{"status":"UP"}`

---

## Important Notes

### ⚠️ Render Free Tier Limitations

- **Sleeps after 15 minutes of inactivity**
- **First message may take 30-60 seconds** (cold start)
- Upgrade to paid plan ($7/month) for always-on service

### 💡 Cold Start Workaround

Keep service active with a cron job (using external service):
```
# Ping every 14 minutes
https://cron-job.org/en/
Target: https://tutormate-whatsapp.onrender.com/actuator/health
Interval: Every 14 minutes
```

### 🔒 Security

For production:
- Move API keys to Render environment variables ✅ (Already done)
- Enable HTTPS (Render provides free SSL) ✅ (Automatic)
- Add request validation in webhook endpoint
- Consider using PostgreSQL instead of H2

---

## Troubleshooting

### Issue: Webhook not receiving messages

**Solution:**
1. Check Infobip webhook configuration
2. Verify URL is correct (include `/infobip/webhook`)
3. Check Render logs for errors
4. Ensure service is running (not sleeping)

### Issue: Service returns 503 or timeout

**Solution:**
1. Check if service is sleeping (free tier)
2. Visit health endpoint to wake up
3. Check Render logs for startup errors

### Issue: Database connection errors

**Solution:**
1. Check H2 is configured correctly
2. Verify environment variables in Render
3. Check JPA configuration in logs

---

## Next Steps

1. ✅ Deploy to Render
2. ✅ Configure Infobip webhook
3. ✅ Test with WhatsApp
4. 📊 Monitor usage and performance
5. 🎨 Add more features (tutoring sessions, payments, etc.)

---

## Useful Commands

### Local Testing
```powershell
# Build and run locally
mvn clean install
mvn spring-boot:run

# Build Docker image locally
docker build -t tutormate-whatsapp .
docker run -p 8082:8082 tutormate-whatsapp
```

### GitHub Updates
```powershell
# After making changes
git add .
git commit -m "Your commit message"
git push origin main

# Render will auto-deploy on push
```

---

## Support

- **Render Docs:** https://render.com/docs
- **Infobip Docs:** https://www.infobip.com/docs/whatsapp
- **Spring Boot:** https://spring.io/guides

---

**Your TutorMate WhatsApp application is now live! 🎉**
