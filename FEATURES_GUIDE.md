# TutorMate - Enhanced Features Guide

## 🎉 New Features Overview

This guide documents all the enhanced features added to the TutorMate WhatsApp tutoring platform. The platform now supports comprehensive student and tutor workflows with interactive buttons and automated reminders.

---

## 📱 Interactive Messaging

All interactions now use **interactive buttons and links** instead of requiring users to type numbers or words. This makes the platform much more user-friendly and reduces errors.

### Button Types
- **Quick Reply Buttons** - Up to 3 clickable options
- **List Messages** - Display multiple items with descriptions
- **Link Buttons** - Clickable URLs for payments and meetings
- **Confirmation Buttons** - Yes/No quick responses

---

## 👨‍🎓 Student Workflows

### 1. Session Booking Flow

**Command:** `BOOK` or click "📚 Book a Session" button

**Flow:**
1. **Browse Subjects** - Interactive list of available subjects with descriptions
2. **Select Tutor** - Browse tutors with ratings, hourly rates, and qualifications
3. **Choose Session Type** - Online or In-Person (interactive buttons)
4. **Select Date/Time** - Enter preferred date and time
5. **Choose Duration** - 30, 60, or 90 minutes (interactive buttons)
6. **Confirm Booking** - Review all details with Yes/No confirmation
7. **Instant Confirmation** - Receive booking reference and wait for tutor acceptance

**Example:**
```
Student: BOOK
Bot: 📚 Select a Subject
     1️⃣ Mathematics
        Algebra, Calculus, Statistics
     2️⃣ Science
        Physics, Chemistry, Biology
     ...
```

### 2. Session Reminders

Students automatically receive:

- **24-hour reminder** - Session details with meeting link/location
- **1-hour reminder** - Final reminder with join link (for online sessions)
- **Post-session review request** - Rate the session 1-5 stars
- **Payment reminders** - If payment is pending (sent at 9 AM and 6 PM)

### 3. Quick Actions

All actions use interactive buttons and simple commands:

| Action | Command | Description |
|--------|---------|-------------|
| Book Session | `BOOK` | Start booking flow with subject selection |
| View Sessions | `SESSIONS` | List all upcoming sessions |
| Find Tutors | `FIND [subject]` | Search tutors for specific subject |
| Cancel Session | `CANCEL [id]` | Cancel with confirmation dialog |
| Rate Session | `RATE [id]` | Rate completed session with stars |

**Example Commands:**
```
SESSIONS              # View all your sessions
FIND Mathematics      # Find math tutors
CANCEL 123           # Cancel session #123
RATE 456             # Rate session #456
```

### 4. Session Management

**View Sessions:**
- Shows upcoming confirmed sessions
- Displays tutor name, subject, date, price, and status
- Provides session IDs for easy cancellation

**Cancel Session:**
- Interactive confirmation dialog
- Automatic refund processing if paid
- Notifies tutor automatically

**Payment Flow:**
- Receive payment link after tutor accepts
- Clickable payment button
- Instant confirmation with receipt
- Commission transparency (shows platform fee and tutor earnings)

---

## 👨‍🏫 Tutor Workflows

### 1. Session Management

**Commands:**

| Command | Description |
|---------|-------------|
| `SESSIONS` | View all upcoming sessions |
| `PENDING` | View booking requests awaiting response |
| `ACCEPT [id]` | Accept a booking request |
| `DECLINE [id]` | Decline a booking request |
| `COMPLETE [id]` | Mark session as completed |

**Flow Example:**
```
Tutor: PENDING
Bot: 🔔 Pending Booking Requests
     
     ID: 123
     Student: John Doe
     Subject: Mathematics
     Date: 2026-02-15
     Duration: 60 min
     Price: R500.00
     
     Reply with:
     • ACCEPT [id]
     • DECLINE [id]

Tutor: ACCEPT 123
Bot: ✅ Booking accepted! The student has been notified 
     and will receive a payment link.
```

### 2. Booking Notifications

Tutors receive instant notifications for:
- New booking requests with all session details
- Student cancellations
- Payment confirmations
- Session reminders (24h and 1h before)

### 3. Availability Management

**Command:** `AVAILABILITY`

Features coming soon:
- Set weekly availability schedule
- Block out specific dates
- Set "Available Now" quick status
- Manage multiple time slots

### 4. Session Materials

Tutors can:
- Send preparation materials before sessions
- Share documents during sessions (WhatsApp media support)
- Provide study materials after sessions

---

## 💰 Payment Workflows

### For Students

1. **Payment Link Receipt**
   - Sent automatically after tutor accepts
   - Clickable payment button
   - Shows total amount and breakdown

2. **Payment Confirmation**
   - Instant confirmation message
   - Digital receipt with download link
   - Commission transparency breakdown

3. **Refund Processing**
   - Automatic for cancelled sessions
   - Refund notification with timeline
   - 3-5 business day processing

**Example:**
```
💳 Payment Required

Your session has been confirmed! Please complete payment 
to secure your booking.

Amount: R500.00
Session: Mathematics
Tutor: Jane Smith
Date: 2026-02-15

Reference: PAY-1234567890

🔗 Pay Now: https://pay.tutormate.com/pay/PAY-1234567890
```

### For Tutors

1. **Earnings Notifications**
   - Instant notification when payment received
   - Shows your earnings after commission
   - Current commission rate displayed

2. **Commission Tiers**
   The platform uses a tiered commission structure that rewards active tutors:
   
   | Tier | Lifetime Earnings | Commission Rate |
   |------|-------------------|-----------------|
   | 1 | R0 - R4,999 | 20% |
   | 2 | R5,000 - R14,999 | 15% |
   | 3 | R15,000+ | 10% |

3. **Tier Upgrade Notifications**
   - Automatic notification when reaching new tier
   - Shows progress to next tier
   - Motivational messages

4. **Monthly Earnings Summary**
   - Sent on 1st of each month at 9 AM
   - Current month earnings
   - Lifetime earnings
   - Number of sessions completed
   - Current commission rate

**Example:**
```
💰 Payment Received

Good news! Payment received for your session.

Your Earnings: R400.00
Platform Fee: R100.00 (20%)

Session: Mathematics
Student: John Doe
Date: 2026-02-15

Total Earnings: R5,200.00

---

🎊 Commission Tier Upgrade!

Congratulations! Your commission rate has been reduced to 15%!

Total Earnings: R5,200.00
New Rate: 15%

💪 Earn R9,800 more to reach the top tier (10% commission)!
```

---

## 💬 Communication Features

### Pre-Session

**Students can:**
- Ask questions about the session
- Discuss specific topics to cover
- Request preparation materials

**Tutors can:**
- Send preparation materials
- Share study guides
- Confirm session details
- Send location for in-person sessions

### During Session

- Share documents via WhatsApp media
- Send images of problems/solutions
- Exchange links and resources

### After Session

**Automatic Flows:**

1. **Tutor marks session complete**
   ```
   Tutor: COMPLETE 123
   Bot: ✅ Session marked as complete! The student will 
        be asked to leave a review.
   ```

2. **Student receives review request**
   ```
   ⭐ Rate Your Session
   
   How was your session with Jane Smith?
   
   Subject: Mathematics
   Date: Feb 15, 2026
   
   Please rate the session from 1-5 stars.
   
   1️⃣ ⭐ 1 Star
   2️⃣ ⭐⭐ 2 Stars
   3️⃣ ⭐⭐⭐ 3 Stars
   4️⃣ ⭐⭐⭐⭐ 4 Stars
   5️⃣ ⭐⭐⭐⭐⭐ 5 Stars
   ```

3. **Optional written review**
   ```
   Student: 5
   Bot: ✅ Thank you for rating! You gave 5 stars.
        
        Would you like to add a written review? 
        (Reply with review text or type SKIP)
   
   Student: Excellent tutor! Very patient and explained 
            concepts clearly.
   Bot: ✅ Thank you for your review! Your feedback 
        helps other students.
   ```

---

## 🔔 Automated Reminders

The platform runs scheduled tasks for automated reminders:

### Session Reminders
- **24-hour reminder** - Runs every hour, checks for sessions in 24-25 hour window
- **1-hour reminder** - Runs every 15 minutes, checks for sessions in 1-hour window

### Payment Reminders
- Runs twice daily (9 AM and 6 PM)
- Only for unpaid sessions more than 2 hours away
- Includes payment link

### Review Requests
- Runs every hour
- Checks for sessions completed 1-2 hours ago
- Automatic star rating request

### Monthly Summaries
- Runs on 1st of month at 9 AM
- Sent to all tutors
- Includes earnings breakdown and commission info

---

## 🎯 Key Features Summary

### Interactive Elements
✅ Button-based navigation (no more typing numbers!)  
✅ Interactive subject/tutor lists with descriptions  
✅ Clickable payment links  
✅ Yes/No confirmation dialogs  
✅ Star rating buttons  

### Student Features
✅ Browse tutors by subject with ratings and prices  
✅ Book sessions with date/time selection  
✅ Receive automatic reminders (24h, 1h)  
✅ One-click payment links  
✅ Easy session cancellation  
✅ Post-session rating system  

### Tutor Features
✅ Instant booking notifications  
✅ Accept/decline with one command  
✅ Session management dashboard  
✅ Earnings tracking with tier system  
✅ Commission transparency  
✅ Monthly earnings summaries  

### Payment Features
✅ Automatic payment link generation  
✅ Commission-based pricing (20%, 15%, 10% tiers)  
✅ Instant payment confirmations  
✅ Digital receipts  
✅ Refund processing  

### Communication Features
✅ Pre-session Q&A  
✅ Document/material sharing  
✅ Post-session feedback  
✅ Rating and review system  

---

## 📋 Complete Command Reference

### Common Commands
- `MENU` - Show role-specific dashboard
- `PROFILE` - View your profile
- `LOGOUT` - Sign out
- `HELP` - Show available commands

### Student Commands
- `BOOK` - Start booking a session
- `SESSIONS` - View your sessions
- `FIND [subject]` - Find tutors for subject
- `CANCEL [id]` - Cancel a session
- `RATE [id]` - Rate a completed session

### Tutor Commands
- `SESSIONS` - View your sessions
- `PENDING` - View pending booking requests
- `ACCEPT [id]` - Accept a booking
- `DECLINE [id]` - Decline a booking
- `COMPLETE [id]` - Mark session complete
- `AVAILABILITY` - Update your availability
- `EARNINGS` - View earnings summary

---

## 🗄️ Database Schema

### New Entities Created

1. **Subject** - Available tutoring subjects
2. **TutoringSession** - Individual tutoring sessions
3. **TutorSubject** - Tutor-subject relationships with rates
4. **TutorAvailability** - Tutor weekly availability
5. **Payment** - Payment records with commission tracking
6. **Rating** - Session ratings and reviews

### Key Relationships
- Users → TutorSubjects (tutors can teach multiple subjects)
- Users → TutoringSession (as student or tutor)
- TutoringSession → Payment (one-to-one)
- TutoringSession → Rating (one-to-one)
- Users → TutorAvailability (tutor schedule)

---

## 🚀 Next Steps

To fully activate all features:

1. **Configure Payment Gateway**
   - Integrate PayFast, Stripe, or preferred payment provider
   - Update `PaymentService.generatePaymentLink()`

2. **Set up Video Conferencing**
   - Integrate Zoom, Google Meet, or Teams API
   - Update `SessionService.generateMeetingLink()`

3. **Populate Initial Data**
   - Add subjects to the database
   - Create tutor profiles with subjects and rates
   - Set tutor availability schedules

4. **Enable Scheduled Tasks**
   - Ensure application is running 24/7 for reminders
   - Configure scheduler thread pool if needed

5. **Test All Flows**
   - Test complete booking flow
   - Verify payment integration
   - Test reminder system
   - Verify cancellation and refunds

---

## 💡 Tips for Best Experience

### For Students
- Use the interactive buttons whenever possible
- Book sessions at least 24 hours in advance
- Complete payment promptly to confirm booking
- Leave reviews to help other students

### For Tutors
- Respond to bookings quickly
- Keep your availability up to date
- Mark sessions complete promptly
- Track your progress toward commission tiers

---

## 🛠️ Technical Implementation

### Services Created
- `SessionService` - Manages tutoring sessions, bookings, cancellations
- `PaymentService` - Handles payments, commissions, earnings
- `ReminderService` - Automated scheduled reminders
- Enhanced `TwilioService` - Interactive buttons and lists
- Enhanced `ConversationService` - Complete conversational flows

### Key Features
- Transaction management with `@Transactional`
- Scheduled tasks with `@Scheduled`
- Repository pattern with Spring Data JPA
- Service layer architecture
- Interactive WhatsApp messaging

---

## 📞 Support

For technical support or feature requests, contact the development team.

**Version:** 2.0  
**Last Updated:** February 2026
