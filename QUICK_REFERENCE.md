# 🚀 TutorMate - Quick Command Reference

## 📱 For Everyone

| Command | Description |
|---------|-------------|
| `HI` or `MENU` | Show main menu |
| `PROFILE` | View your profile |
| `HELP` | Show available commands |
| `LOGOUT` | Sign out |

---

## 👨‍🎓 Student Commands

### Booking & Sessions
```bash
BOOK                    # Start booking a session
SESSIONS                # View your sessions  
FIND Mathematics        # Find tutors for subject
CANCEL 123              # Cancel session #123
RATE 456                # Rate session #456
```

### Interactive Flows
- **Book Session:** Choose subject → tutor → type → time → duration → confirm
- **View Sessions:** See all upcoming with details
- **Cancel:** Yes/No confirmation → automatic refund
- **Rate:** 1-5 stars → optional written review

---

## 👨‍🏫 Tutor Commands

### Session Management
```bash
SESSIONS                # View your sessions
PENDING                 # View booking requests
ACCEPT 123              # Accept booking
DECLINE 456             # Decline booking
COMPLETE 789            # Mark session complete
```

### Earnings
```bash
EARNINGS                # View monthly & total earnings
AVAILABILITY            # Update your schedule (coming soon)
```

### Quick Actions
- **Accept Booking:** Triggers payment flow for student
- **Complete Session:** Triggers review request to student
- **View Earnings:** See total, monthly, and commission tier

---

## 💰 Commission Tiers

| Tier | Lifetime Earnings | Rate | You Keep |
|------|-------------------|------|----------|
| 1 | R0 - R4,999 | 20% | 80% |
| 2 | R5k - R14,999 | 15% | 85% |
| 3 | R15,000+ | 10% | 90% |

**Auto-upgrade notifications when you reach new tiers!**

---

## 🔔 Automatic Notifications

### Students Receive:
- ✅ Booking confirmation
- ✅ Tutor response (accept/decline)
- ✅ Payment link
- ✅ 24-hour reminder
- ✅ 1-hour reminder with meeting link
- ✅ Review request after session
- ✅ Payment reminder if unpaid

### Tutors Receive:
- ✅ New booking requests
- ✅ Payment confirmations  
- ✅ Earnings notifications
- ✅ 24-hour reminder
- ✅ 1-hour reminder
- ✅ Cancellation notices
- ✅ Commission tier upgrades
- ✅ Monthly earnings summary (1st of month)

---

## 📊 Session Statuses

| Status | Meaning | Next Action |
|--------|---------|-------------|
| `PENDING` | Waiting for tutor | Tutor must accept/decline |
| `CONFIRMED` | Tutor accepted | Student must pay |
| `CANCELLED` | Session cancelled | No action needed |
| `COMPLETED` | Session finished | Student can rate |
| `NO_SHOW` | Student didn't attend | — |

---

## 🎯 Booking Flow (Student)

```
1. Type: BOOK
2. Select: Subject (from list)
3. Choose: Tutor (see ratings & rates)
4. Pick: Online or In-Person
5. Enter: Date & time (YYYY-MM-DD HH:MM)
6. Select: Duration (30/60/90 min)
7. Confirm: Review details → Yes/No
8. Wait: For tutor to accept
9. Pay: Click payment link
10. Join: Use meeting link or go to location
11. Rate: After completion
```

---

## 👥 User Roles

### Student (👨‍🎓)
- Browse and book tutors
- View sessions
- Make payments
- Rate sessions
- Receive reminders

### Tutor (👨‍🏫)
- Accept/decline bookings
- Manage sessions
- Track earnings
- Build reputation via ratings
- Set availability (coming soon)

---

## 📞 Example Conversations

### Quick Booking
```
You: BOOK
Bot: [Shows subject list with buttons]
You: 1
Bot: [Shows tutors for that subject]
You: 2
Bot: [Asks online or in-person]
You: 1
Bot: [Asks for date/time]
You: 2026-02-15 14:00
Bot: [Asks for duration]
You: 2
Bot: [Shows confirmation]
You: 1
Bot: ✅ Booking sent!
```

### Tutor Accepting
```
Bot: 🔔 New Session Request!
     [Details shown]
You: PENDING
Bot: [Lists pending bookings]
You: ACCEPT 123
Bot: ✅ Accepted! Student notified.
```

### Rating a Session
```
You: RATE 456
Bot: ⭐ Rate Your Session
     [Shows star buttons]
You: 5
Bot: ✅ Thank you! Add a review?
You: Excellent tutor, very patient!
Bot: ✅ Review saved. Thanks!
```

---

## 🛠️ Setup Commands (Admin)

### Initialize Subjects
Uncomment `@Component` in `DataInitializationService.java`

### Create Test Tutor
Use WhatsApp to register as tutor, then add subjects via database

### Test Full Flow
1. Register student account
2. Register tutor account  
3. Add tutor subjects in database
4. Student books session
5. Tutor accepts
6. Test reminders
7. Complete and rate

---

## 💡 Pro Tips

### For Students
- Book 24h+ in advance for more tutor availability
- Pay promptly to secure your booking
- Leave reviews to help other students
- Check tutor ratings before booking

### For Tutors
- Respond to bookings quickly
- Keep availability up to date
- Provide quality sessions for better ratings
- Track progress to next commission tier
- Mark sessions complete promptly

---

## 📱 Interactive Features

### What You'll See
- **Numbered Lists** with emoji (1️⃣ 2️⃣ 3️⃣)
- **Clickable Buttons** for options
- **Payment Links** that open in browser
- **Yes/No Confirmations** for important actions
- **Star Ratings** with visual stars ⭐⭐⭐⭐⭐
- **Rich Formatting** with emojis and bold text

### How to Respond
- **Click buttons** when available (easiest!)
- **Type numbers** (1, 2, 3) for lists
- **Type commands** in ALL CAPS or lowercase
- **Type full words** (YES, NO) for confirmations

---

## 🆘 Common Issues

### "Invalid selection"
→ Make sure to enter the number from the list shown

### "Session not found"
→ Check the session ID with SESSIONS command

### "No tutors available"
→ Subject may not have active tutors yet

### "Payment pending"
→ Click the payment link sent earlier or type SESSIONS to resend

### "Session expired"
→ Type MENU or HI to restart

---

## 📞 Support

Need help? Contact support or type `HELP` in the chat.

---

**Version 2.0** | Built with ❤️ for TutorMate
