# Deploy TutorMate to Render (Meta WhatsApp)

This guide will help you deploy your Meta WhatsApp Spring Boot application to Render for free.

## Prerequisites

- GitHub account
- Render account (free) - https://render.com
- Your code pushed to GitHub

## Step 1: Push Code to GitHub

```powershell
# Initialize git if not already done
git init

# Add all files
git add .

# Commit changes
git commit -m "Meta WhatsApp migration complete - ready for deployment"

# Create a new repository on GitHub, then push
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
git branch -M main
git push -u origin main
```

## Step 2: Create Render Account

1. Go to https://render.com
2. Sign up with GitHub (recommended for easy deployment)
3. Authorize Render to access your repositories

## Step 3: Deploy to Render

### Option A: Using render.yaml (Blueprint - RECOMMENDED)

1. Click **"New +"** → **"Blueprint"**
2. Select your GitHub repository
3. Render will detect `render.yaml` automatically
4. Click **"Apply"**
5. Set the environment variable:
   - **META_WHATSAPP_ACCESS_TOKEN**: `EAATAmHycwZC8BQ3Ftyc5GhUpvbHlPZBnoCu95PDyksYku0GaZB1GhMcKtIZBBScCqSpqJUGbO8KnsAxHAb9yZBmPHTF3CTNESr53xsEZAhOVGKevjpAUu1COxZBbjevZABSN8Q8P3xVPBQFx9ukxFZASze6U6CYd3tZAqpjRen1ZBLOg5p6JJfcVa9wWCjTEe0Xugh9y3XEgcJRA0BkN1vZCq7FWXPU5KYWXTFWB9lv5YZCugv1MrZCW3H6uGpZBCeLFFnZCJvn4O7LH7Ed4pROyBfYjeeRotgZDZD`

### Option B: Manual Web Service Creation

1. Click **"New +"** → **"Web Service"**
2. Connect your GitHub repository
3. Configure:
   - **Name**: `tutormate-whatsapp`
   - **Runtime**: `Docker`
   - **Region**: Choose closest to you
   - **Branch**: `main`
   - **Dockerfile Path**: `./Dockerfile`
   - **Plan**: `Free`

4. Add Environment Variables:
   ```
   META_WHATSAPP_ACCESS_TOKEN = EAATAmHycwZC8BQ3Ftyc5GhUpvbHlPZBnoCu95PDyksYku0GaZB1GhMcKtIZBBScCqSpqJUGbO8KnsAxHAb9yZBmPHTF3CTNESr53xsEZAhOVGKevjpAUu1COxZBbjevZABSN8Q8P3xVPBQFx9ukxFZASze6U6CYd3tZAqpjRen1ZBLOg5p6JJfcVa9wWCjTEe0Xugh9y3XEgcJRA0BkN1vZCq7FWXPU5KYWXTFWB9lv5YZCugv1MrZCW3H6uGpZBCeLFFnZCJvn4O7LH7Ed4pROyBfYjeeRotgZDZD
   META_WHATSAPP_PHONE_NUMBER_ID = 959729383897901
   META_WHATSAPP_BUSINESS_ACCOUNT_ID = 941501588391536
   META_WEBHOOK_VERIFY_TOKEN = tutormate_secure_verify_token_2026
   SERVER_PORT = 8082
   SPRING_DATASOURCE_URL = jdbc:h2:mem:tutormate
   SPRING_JPA_HIBERNATE_DDL_AUTO = update
   ```

5. Click **"Create Web Service"**

## Step 4: Wait for Deployment

- Render will build your Docker image (takes 5-10 minutes first time)
- Watch the deployment logs
- Once you see "Deployed", your app is live!
- Your URL will be something like: `https://tutormate-whatsapp.onrender.com`

## Step 5: Test Your Deployment

```powershell
# Test health endpoint (replace with your actual Render URL)
Invoke-RestMethod -Uri "https://tutormate-whatsapp.onrender.com/api/meta/test/health"

# Test sending a message
Invoke-RestMethod -Uri "https://tutormate-whatsapp.onrender.com/api/meta/test/send?to=27785312360&message=Hello from Render!" -Method POST
```

## Step 6: Update Meta Webhook URL

1. Go to Meta Developer Dashboard
2. Navigate to: WhatsApp → Configuration → Webhook
3. Click **"Edit"**
4. Update:
   - **Callback URL**: `https://tutormate-whatsapp.onrender.com/meta/webhook`
   - **Verify Token**: `tutormate_secure_verify_token_2026`
5. Click **"Verify and Save"**
6. Ensure **"messages"** field is **Subscribed**

## Step 7: Test Bidirectional Communication

1. Send WhatsApp message from your phone to: **+1 555 142 4698**
2. Message: **hi**
3. You should receive TutorMate welcome message with role selection buttons!

## Troubleshooting

### Build Fails

- Check Render logs for errors
- Ensure Dockerfile is correct
- Verify pom.xml has no errors

### App Crashes on Startup

- Check environment variables are set correctly
- View Render logs: Dashboard → Your Service → Logs
- Ensure META_WHATSAPP_ACCESS_TOKEN is valid

### Webhook Verification Fails

- Ensure webhook URL is HTTPS (Render provides this automatically)
- Verify the verify token matches exactly
- Check Render logs for incoming GET requests

### No Response to WhatsApp Messages

- Check "messages" field is subscribed in Meta Dashboard
- View Render logs for incoming POST requests
- Verify your access token hasn't expired

## Important Notes

### Free Tier Limitations

- **Spins down after 15 minutes of inactivity**
- First request after spin-down takes 30-60 seconds to respond
- For 24/7 uptime, upgrade to paid plan ($7/month)

### Keep Service Awake (Optional)

Use a free uptime monitoring service like:
- UptimeRobot (https://uptimerobot.com)
- Betterstack (https://betterstack.com)

Configure it to ping your health endpoint every 10 minutes:
```
https://tutormate-whatsapp.onrender.com/api/meta/test/health
```

### Access Token Expiry

- Temporary token expires in 24 hours
- For production, create a System User token (see META_MIGRATION_GUIDE.md)
- Update META_WHATSAPP_ACCESS_TOKEN in Render when token changes

## Auto-Deploy on Git Push

Render automatically redeploys when you push to GitHub:

```powershell
git add .
git commit -m "Update code"
git push
```

Render detects the push and rebuilds/redeploys automatically!

## Next Steps

After successful deployment:

1. ✅ Test full conversation flows (student/tutor registration, booking)
2. ✅ Create message templates for notifications
3. ✅ Set up permanent System User access token
4. ✅ Consider paid Render plan for 24/7 uptime
5. ✅ Complete Business Verification for production use

## Support

- Render Docs: https://render.com/docs
- Render Community: https://community.render.com
- Meta WhatsApp Docs: https://developers.facebook.com/docs/whatsapp
