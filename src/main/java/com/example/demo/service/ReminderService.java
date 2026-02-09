package com.example.demo.service;

import com.example.demo.entity.Payment;
import com.example.demo.entity.TutoringSession;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.TutoringSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class ReminderService {

    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");

    @Autowired
    private TutoringSessionRepository sessionRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TwilioService twilioService;

    /**
     * Send 24-hour reminders for upcoming sessions
     * Runs every hour
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void send24HourReminders() {
        logger.info("Running 24-hour reminder check");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in24Hours = now.plusHours(24);
        LocalDateTime in25Hours = now.plusHours(25); // 1-hour window
        
        List<TutoringSession> sessions = sessionRepository.findSessionsInDateRange(
                in24Hours, in25Hours, 
                Arrays.asList(TutoringSession.SessionStatus.CONFIRMED));
        
        for (TutoringSession session : sessions) {
            send24HourReminderToStudent(session);
            send24HourReminderToTutor(session);
        }
        
        logger.info("Sent 24-hour reminders for {} sessions", sessions.size());
    }

    /**
     * Send 1-hour reminders for upcoming sessions
     * Runs every 15 minutes
     */
    @Scheduled(cron = "0 */15 * * * *") // Every 15 minutes
    public void send1HourReminders() {
        logger.info("Running 1-hour reminder check");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in1Hour = now.plusHours(1);
        LocalDateTime in1Hour15Min = now.plusHours(1).plusMinutes(15);
        
        List<TutoringSession> sessions = sessionRepository.findSessionsInDateRange(
                in1Hour, in1Hour15Min,
                Arrays.asList(TutoringSession.SessionStatus.CONFIRMED));
        
        for (TutoringSession session : sessions) {
            send1HourReminderToStudent(session);
            send1HourReminderToTutor(session);
        }
        
        logger.info("Sent 1-hour reminders for {} sessions", sessions.size());
    }

    /**
     * Send payment reminders for unpaid sessions
     * Runs twice daily
     */
    @Scheduled(cron = "0 0 9,18 * * *") // At 9 AM and 6 PM
    public void sendPaymentReminders() {
        logger.info("Running payment reminder check");
        
        List<Payment> unpaidPayments = paymentRepository.findByStatus(Payment.PaymentStatus.PENDING);
        
        for (Payment payment : unpaidPayments) {
            // Only send reminder if session is more than 2 hours away
            if (payment.getSession().getSessionDateTime().isAfter(LocalDateTime.now().plusHours(2))) {
                sendPaymentReminder(payment);
            }
        }
        
        logger.info("Sent payment reminders for {} unpaid sessions", unpaidPayments.size());
    }

    /**
     * Request post-session reviews
     * Runs every hour
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void requestPostSessionReviews() {
        logger.info("Running post-session review check");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoHoursAgo = now.minusHours(2);
        LocalDateTime oneHourAgo = now.minusHours(1);
        
        // Find sessions that completed in the last hour
        List<TutoringSession> completedSessions = sessionRepository.findSessionsInDateRange(
                twoHoursAgo, oneHourAgo,
                Arrays.asList(TutoringSession.SessionStatus.COMPLETED));
        
        for (TutoringSession session : completedSessions) {
            sendReviewRequest(session);
        }
        
        logger.info("Sent review requests for {} completed sessions", completedSessions.size());
    }

    /**
     * Send monthly earnings summary to tutors
     * Runs on the 1st of every month at 9 AM
     */
    @Scheduled(cron = "0 0 9 1 * *") // 9 AM on 1st of month
    public void sendMonthlyEarningsSummaries() {
        logger.info("Sending monthly earnings summaries");
        // This will be called by PaymentService for each tutor
    }

    // Reminder methods
    private void send24HourReminderToStudent(TutoringSession session) {
        String message = String.format(
                "⏰ *Session Reminder - 24 Hours*\n\n" +
                "Your tutoring session is tomorrow!\n\n" +
                "Tutor: %s\n" +
                "Subject: %s\n" +
                "Date: %s\n" +
                "Duration: %d minutes\n" +
                "Type: %s\n\n" +
                "%s\n\n" +
                "_Type CANCEL %d if you need to cancel_",
                session.getTutor().getFullName(),
                session.getSubject().getName(),
                session.getSessionDateTime().format(DATE_TIME_FORMATTER),
                session.getDurationMinutes(),
                session.getType().name(),
                session.getType() == TutoringSession.SessionType.ONLINE
                    ? "Meeting Link: " + session.getMeetingLink()
                    : "Location: " + session.getLocation(),
                session.getId());
        
        twilioService.sendTextMessage(session.getStudent().getPhoneNumber(), message);
    }

    private void send24HourReminderToTutor(TutoringSession session) {
        String message = String.format(
                "⏰ *Session Reminder - 24 Hours*\n\n" +
                "You have a tutoring session tomorrow!\n\n" +
                "Student: %s\n" +
                "Subject: %s\n" +
                "Date: %s\n" +
                "Duration: %d minutes\n" +
                "Type: %s\n\n" +
                "%s\n\n" +
                "_Prepare any materials you want to share_",
                session.getStudent().getFullName(),
                session.getSubject().getName(),
                session.getSessionDateTime().format(DATE_TIME_FORMATTER),
                session.getDurationMinutes(),
                session.getType().name(),
                session.getType() == TutoringSession.SessionType.ONLINE
                    ? "Meeting Link: " + session.getMeetingLink()
                    : "Location: " + session.getLocation());
        
        twilioService.sendTextMessage(session.getTutor().getPhoneNumber(), message);
    }

    private void send1HourReminderToStudent(TutoringSession session) {
        String message = String.format(
                "🔔 *Session Starting Soon - 1 Hour*\n\n" +
                "Your session starts in 1 hour!\n\n" +
                "Tutor: %s\n" +
                "Subject: %s\n" +
                "Time: %s\n\n" +
                "%s\n\n" +
                "_Get ready and prepare any questions you have_",
                session.getTutor().getFullName(),
                session.getSubject().getName(),
                session.getSessionDateTime().format(DATE_TIME_FORMATTER),
                session.getType() == TutoringSession.SessionType.ONLINE
                    ? "🔗 Join now: " + session.getMeetingLink()
                    : "📍 Location: " + session.getLocation());
        
        twilioService.sendTextMessage(session.getStudent().getPhoneNumber(), message);
    }

    private void send1HourReminderToTutor(TutoringSession session) {
        String message = String.format(
                "🔔 *Session Starting Soon - 1 Hour*\n\n" +
                "Your session starts in 1 hour!\n\n" +
                "Student: %s\n" +
                "Subject: %s\n" +
                "Time: %s\n\n" +
                "%s\n\n" +
                "_Make sure you're ready and have your materials prepared_",
                session.getStudent().getFullName(),
                session.getSubject().getName(),
                session.getSessionDateTime().format(DATE_TIME_FORMATTER),
                session.getType() == TutoringSession.SessionType.ONLINE
                    ? "🔗 Join now: " + session.getMeetingLink()
                    : "📍 Location: " + session.getLocation());
        
        twilioService.sendTextMessage(session.getTutor().getPhoneNumber(), message);
    }

    private void sendPaymentReminder(Payment payment) {
        TutoringSession session = payment.getSession();
        
        String message = String.format(
                "⚠️ *Payment Reminder*\n\n" +
                "Your session is upcoming but payment is still pending.\n\n" +
                "Amount Due: R%.2f\n" +
                "Session: %s with %s\n" +
                "Date: %s\n\n" +
                "Please complete payment to confirm your booking.\n\n" +
                "Reference: %s",
                payment.getTotalAmount(),
                session.getSubject().getName(),
                session.getTutor().getFullName(),
                session.getSessionDateTime().format(DATE_TIME_FORMATTER),
                payment.getPaymentReference());
        
        twilioService.sendMessageWithLink(payment.getStudent().getPhoneNumber(),
                                         message,
                                         payment.getPaymentLink(),
                                         "Pay Now");
    }

    private void sendReviewRequest(TutoringSession session) {
        String message = String.format(
                "⭐ *How Was Your Session?*\n\n" +
                "Please rate your session with %s\n\n" +
                "Subject: %s\n" +
                "Date: %s\n\n" +
                "Rate from 1-5 stars:",
                session.getTutor().getFullName(),
                session.getSubject().getName(),
                session.getSessionDateTime().format(DATE_TIME_FORMATTER));
        
        List<String> ratingButtons = Arrays.asList(
                "⭐ 1 Star",
                "⭐⭐ 2 Stars",
                "⭐⭐⭐ 3 Stars"
        );
        
        twilioService.sendMessageWithButtons(session.getStudent().getPhoneNumber(), message, ratingButtons);
        
        // Send follow-up with remaining options
        twilioService.sendTextMessage(session.getStudent().getPhoneNumber(),
                "4️⃣ ⭐⭐⭐⭐ 4 Stars\n5️⃣ ⭐⭐⭐⭐⭐ 5 Stars\n\n_Reply with number or star count_");
    }
}
