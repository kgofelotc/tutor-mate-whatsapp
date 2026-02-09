# 🚀 Deploying TutorMate to Render

## Quick Deploy Guide

### Prerequisites
✅ GitHub repository with your code  
✅ Twilio account with WhatsApp sandbox or approved Business API  
✅ Render account (free tier available)

---

## Option 1: Quick Deploy (H2 In-Memory Database)

**Best for:** Testing and demos  
**Database:** H2 (resets on restart)  
**Cost:** Free

### Steps:

1. **Push your code to GitHub**
   ```bash
   git add .
   git commit -m "Add TutorMate platform with interactive features"
   git push origin main
   ```

2. **Connect to Render**
   - Go to https://render.com
   - Sign in with GitHub
   - Click "New +" → "Web Service"
   - Connect your repository

3. **Configure the Service**
   - **Name:** `tutormate-whatsapp`
   - **Runtime:** Docker
   - **Branch:** main
   - **Plan:** Free

4. **Set Environment Variables** (in Render dashboard)
   ```
   TWILIO_ACCOUNT_SID=your_actual_account_sid
   TWILIO_AUTH_TOKEN=your_actual_auth_token
   TWILIO_WHATSAPP_FROM=+14155238886
   SERVER_PORT=8082
   SPRING_DATASOURCE_URL=jdbc:h2:mem:tutormate
   SPRING_JPA_HIBERNATE_DDL_AUTO=update
   ```

5. **Deploy!**
   - Click "Create Web Service"
   - Wait for build (~5-10 minutes)
   - Get your URL: `https://tutormate-whatsapp.onrender.com`

6. **Configure Twilio Webhook**
   - Go to Twilio Console → WhatsApp Sandbox Settings
   - Set webhook URL: `https://tutormate-whatsapp.onrender.com/twilio/webhook`
   - Method: POST
   - Save!

7. **Test It!**
   - Send "HI" to your Twilio WhatsApp number
   - Follow the interactive prompts

---

## Option 2: Production Deploy (PostgreSQL Database)

**Best for:** Production use  
**Database:** PostgreSQL (persistent)  
**Cost:** Free tier available

### Additional Steps:

1. **Create PostgreSQL Database on Render**
   - Click "New +" → "PostgreSQL"
   - Name: `tutormate-db`
   - Plan: Free (1GB storage)
   - Click "Create Database"
   - Copy the "Internal Database URL"

2. **Update Environment Variables**
   Replace `SPRING_DATASOURCE_URL` with your PostgreSQL URL:
   ```
   SPRING_DATASOURCE_URL=postgresql://user:pass@host:5432/dbname
   ```

3. **Deploy and Auto-Connect**
   Render will automatically configure the connection!

---

## 🔧 Environment Variables Explained

| Variable | Purpose | Example |
|----------|---------|---------|
| `TWILIO_ACCOUNT_SID` | Your Twilio account identifier | `AC...` |
| `TWILIO_AUTH_TOKEN` | Twilio authentication token | `your_token` |
| `TWILIO_WHATSAPP_FROM` | Your WhatsApp number | `+14155238886` |
| `SERVER_PORT` | Port for the app (Render uses 10000) | `8082` |
| `SPRING_DATASOURCE_URL` | Database connection string | See options above |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | DB schema management | `update` |

---

## 📦 What Gets Deployed

Your deployment includes:
- ✅ All 7 new entities (Subject, TutoringSession, etc.)
- ✅ Interactive WhatsApp messaging
- ✅ Complete student/tutor workflows
- ✅ Automated reminders (24h, 1h, payment, reviews)
- ✅ Commission tracking system
- ✅ Payment workflows
- ✅ Rating system

---

## 🎯 Post-Deployment Setup

### 1. Initialize Subjects (Optional)

**Option A - Automatic (Recommended for testing):**

Uncomment `@Component` in `DataInitializationService.java` before deploying:
```java
@Component  // Uncomment this
public class DataInitializationService implements CommandLineRunner {
```

This will automatically create 17 subjects on startup.

**Option B - Manual (Via H2 Console):**

If using H2 database:
1. Visit: `https://your-app.onrender.com/h2-console`
2. JDBC URL: `jdbc:h2:mem:tutormate`
3. Username: `sa`, Password: (leave blank)
4. Run SQL:
   ```sql
   INSERT INTO subjects (name, description, category, active) 
   VALUES ('Mathematics', 'Algebra, Calculus, Statistics', 'MATHEMATICS', true);
   
   INSERT INTO subjects (name, description, category, active) 
   VALUES ('Physics', 'Mechanics, thermodynamics', 'SCIENCE', true);
   -- etc.
   ```

### 2. Create Test Accounts

**Via WhatsApp:**
1. Send "HI" to your WhatsApp number
2. Select "TUTOR" or "STUDENT"
3. Follow registration flow

**First Tutor Setup:**
1. Register tutor account via WhatsApp
2. Note the phone number
3. Add subjects manually in database:
   ```sql
   -- Get tutor user_id
   SELECT * FROM users WHERE role = 'TUTOR';
   
   -- Add subject (example: tutor teaches Math)
   INSERT INTO tutor_subjects (tutor_id, subject_id, hourly_rate, active)
   VALUES (1, 1, 250.00, true);
   ```

### 3. Test Complete Flow

1. **Register Student:** WhatsApp → "HI" → Student → Register
2. **Register Tutor:** WhatsApp → "HI" → Tutor → Register  
3. **Add Tutor Subject:** Via database (see above)
4. **Book Session:** Student → "BOOK" → Follow prompts
5. **Accept Booking:** Tutor → "PENDING" → "ACCEPT [id]"
6. **Test Reminders:** Wait for scheduled times or adjust cron
7. **Complete Flow:** Tutor → "COMPLETE [id]"
8. **Rate Session:** Student → "RATE [id]" → Stars → Review

---

## 🐛 Troubleshooting

### App Won't Start
- Check logs in Render dashboard
- Verify all environment variables are set
- Ensure TWILIO credentials are correct

### Webhook Not Receiving Messages
- Verify webhook URL in Twilio console
- Check: `https://your-app.onrender.com/twilio/webhook`
- Ensure app is running (check Render dashboard)
- Test health check: `https://your-app.onrender.com/actuator/health`

### Database Issues (H2)
- **Data lost on restart?** That's expected with H2 in-memory
- **Solution:** Use PostgreSQL for persistent storage
- H2 console: `https://your-app.onrender.com/h2-console`

### Reminders Not Sending
- Free tier apps sleep after 15min inactivity
- **Solution:** Upgrade to paid plan ($7/month) for 24/7 uptime
- Or use a service like UptimeRobot to ping your app

### Buttons Not Working
- Twilio sandbox has limitations
- Some interactive features require WhatsApp Business API approval
- Fallback to text commands always works

---

## 💰 Cost Breakdown

### Free Tier (Testing)
- **Web Service:** Free (sleeps after 15min inactivity)
- **H2 Database:** Free (in-memory, no persistence)
- **Total:** $0/month

### Recommended Production
- **Web Service:** Starter ($7/month, 512MB RAM, no sleep)
- **PostgreSQL:** Free tier (1GB) or Starter ($7/month, 1GB)
- **Total:** $7-14/month

### Features by Tier

| Feature | Free | Starter |
|---------|------|---------|
| Basic functionality | ✅ | ✅ |
| Automated reminders | ⚠️ (unreliable) | ✅ |
| Persistent database | ❌ (H2 only) | ✅ |
| Custom domain | ❌ | ✅ |
| 24/7 uptime | ❌ | ✅ |

---

## 🚀 Optimization Tips

### 1. Keep Free Tier Alive
Use UptimeRobot or similar to ping your app every 14 minutes:
```
https://your-app.onrender.com/actuator/health
```

### 2. Enable Postgres for Persistence
Even on free tier, switch to PostgreSQL to keep data between deploys.

### 3. Monitor Logs
Check Render dashboard logs regularly during testing.

### 4. Set Up Alerts
Configure Twilio alerts for webhook failures.

---

## 📱 Testing Checklist

After deployment, test these flows:

### Student Flow
- [ ] Register new student account
- [ ] View subjects list (BOOK)
- [ ] Browse tutors with ratings
- [ ] Book a session with interactive buttons
- [ ] Receive confirmation
- [ ] View sessions (SESSIONS)
- [ ] Cancel a session (CANCEL [id])

### Tutor Flow
- [ ] Register new tutor account
- [ ] View pending requests (PENDING)
- [ ] Accept a booking (ACCEPT [id])
- [ ] View sessions (SESSIONS)
- [ ] Mark session complete (COMPLETE [id])
- [ ] View earnings (EARNINGS)

### Automated Features
- [ ] 24-hour reminder (wait or adjust timing)
- [ ] 1-hour reminder
- [ ] Payment reminder
- [ ] Review request after completion
- [ ] Monthly summary (test on 1st of month)

### Interactive Elements
- [ ] Button clicks work
- [ ] Lists display properly
- [ ] Confirmation dialogs work
- [ ] Star rating buttons
- [ ] Links are clickable

---

## 🔐 Security Checklist

Before going live:
- [ ] Change default passwords
- [ ] Use strong Twilio auth token
- [ ] Enable HTTPS only (Render does this automatically)
- [ ] Don't commit secrets to Git
- [ ] Use Render environment variables for sensitive data
- [ ] Enable Twilio webhook signature validation
- [ ] Set up rate limiting (if needed)

---

## 📊 Monitoring

### Built-in Endpoints
- **Health Check:** `/actuator/health`
- **Webhook:** `/twilio/webhook`
- **H2 Console:** `/h2-console` (if H2 enabled)

### Render Dashboard
- View logs in real-time
- Monitor CPU/Memory usage
- Track deployment history
- Set up notifications

---

## 🎯 Next Steps After Deployment

1. **Payment Gateway Integration**
   - Sign up with PayFast, Stripe, or Yoco
   - Update `PaymentService.generatePaymentLink()`
   - Add API keys to Render env vars

2. **Video Conferencing**
   - Get Zoom/Google Meet API credentials
   - Update `SessionService.generateMeetingLink()`
   - Configure OAuth if needed

3. **WhatsApp Business API**
   - Apply for Facebook Business verification
   - Get approved for WhatsApp Business API
   - Migrate from sandbox to production number

4. **Custom Domain**
   - Upgrade to Starter plan
   - Add custom domain in Render
   - Update Twilio webhook URL

5. **Monitoring & Analytics**
   - Set up Sentry for error tracking
   - Add Google Analytics (if web interface)
   - Track key metrics (bookings, payments, ratings)

---

## 📞 Support Resources

- **Render Docs:** https://render.com/docs
- **Twilio WhatsApp Docs:** https://www.twilio.com/docs/whatsapp
- **Spring Boot Docs:** https://spring.io/projects/spring-boot

---

## 🎉 You're Ready!

Your TutorMate platform with all enhanced features is ready to deploy on Render!

**Quick Deploy Command:**
```bash
git add .
git commit -m "Ready for Render deployment"
git push origin main
```

Then follow Option 1 or Option 2 above depending on your needs.

**Questions?** Check the troubleshooting section or review the implementation guides in the repository.

---

**Version:** 2.0  
**Last Updated:** February 2026  
**Platform:** Render Cloud
