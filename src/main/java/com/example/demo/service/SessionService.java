package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class SessionService {

    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("MMM dd, yyyy 'at' hh:mm a");

    @Autowired
    private TutoringSessionRepository sessionRepository;

    @Autowired
    private TutorSubjectRepository tutorSubjectRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MetaWhatsAppService metaWhatsAppService;

    @Autowired
    private PaymentService paymentService;

    /**
     * Create a new tutoring session booking
     */
    @Transactional
    public TutoringSession createSessionBooking(User student, User tutor, Subject subject,
            LocalDateTime sessionDateTime, Integer durationMinutes,
            TutoringSession.SessionType type, String location) {

        // Get tutor's rate for this subject
        TutorSubject tutorSubject = tutorSubjectRepository.findByTutorAndSubject(tutor, subject)
                .orElseThrow(() -> new RuntimeException("Tutor does not teach this subject"));

        // Calculate price based on hourly rate and duration
        BigDecimal price = tutorSubject.getHourlyRate()
                .multiply(new BigDecimal(durationMinutes))
                .divide(new BigDecimal(60), 2, BigDecimal.ROUND_HALF_UP);

        // Create session
        TutoringSession session = new TutoringSession(tutor, student, subject, sessionDateTime,
                durationMinutes, price, type);
        session.setLocation(location);
        session.setStatus(TutoringSession.SessionStatus.PENDING);

        // Generate meeting link for online sessions
        if (type == TutoringSession.SessionType.ONLINE) {
            String meetingLink = generateMeetingLink(session);
            session.setMeetingLink(meetingLink);
        }

        session = sessionRepository.save(session);

        // Notify tutor about new booking request
        notifyTutorOfNewBooking(session);

        logger.info("Created new session booking: {} with tutor {} for student {}",
                session.getId(), tutor.getFullName(), student.getFullName());

        return session;
    }

    /**
     * Tutor accepts a session request
     */
    @Transactional
    public void acceptSession(Long sessionId, User tutor) {
        TutoringSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getTutor().getId().equals(tutor.getId())) {
            throw new RuntimeException("You are not the tutor for this session");
        }

        if (session.getStatus() != TutoringSession.SessionStatus.PENDING) {
            throw new RuntimeException("Session is not pending");
        }

        session.setStatus(TutoringSession.SessionStatus.CONFIRMED);
        sessionRepository.save(session);

        // Notify student
        notifyStudentOfConfirmedSession(session);

        // Create payment record
        paymentService.createPayment(session);

        logger.info("Tutor {} accepted session {}", tutor.getFullName(), sessionId);
    }

    /**
     * Tutor declines a session request
     */
    @Transactional
    public void declineSession(Long sessionId, User tutor, String reason) {
        TutoringSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getTutor().getId().equals(tutor.getId())) {
            throw new RuntimeException("You are not the tutor for this session");
        }

        if (session.getStatus() != TutoringSession.SessionStatus.PENDING) {
            throw new RuntimeException("Session is not pending");
        }

        session.setStatus(TutoringSession.SessionStatus.CANCELLED);
        session.setCancelledAt(LocalDateTime.now());
        session.setCancellationReason(reason != null ? reason : "Declined by tutor");
        sessionRepository.save(session);

        // Notify student
        notifyStudentOfDeclinedSession(session, reason);

        logger.info("Tutor {} declined session {}", tutor.getFullName(), sessionId);
    }

    /**
     * Cancel a session
     */
    @Transactional
    public void cancelSession(Long sessionId, User user, String reason) {
        TutoringSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        boolean isStudent = session.getStudent().getId().equals(user.getId());
        boolean isTutor = session.getTutor().getId().equals(user.getId());

        if (!isStudent && !isTutor) {
            throw new RuntimeException("You are not part of this session");
        }

        if (session.getStatus() == TutoringSession.SessionStatus.CANCELLED ||
                session.getStatus() == TutoringSession.SessionStatus.COMPLETED) {
            throw new RuntimeException("Session cannot be cancelled");
        }

        session.setStatus(TutoringSession.SessionStatus.CANCELLED);
        session.setCancelledAt(LocalDateTime.now());
        session.setCancellationReason(reason);
        sessionRepository.save(session);

        // Notify the other party
        if (isStudent) {
            notifyTutorOfCancellation(session, reason);
        } else {
            notifyStudentOfCancellation(session, reason);
        }

        // Handle refund if payment was made
        paymentService.handleRefund(session);

        logger.info("Session {} cancelled by {}", sessionId, user.getFullName());
    }

    /**
     * Mark session as completed
     */
    @Transactional
    public void completeSession(Long sessionId, User tutor) {
        TutoringSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getTutor().getId().equals(tutor.getId())) {
            throw new RuntimeException("You are not the tutor for this session");
        }

        session.setStatus(TutoringSession.SessionStatus.COMPLETED);
        session.setCompletedAt(LocalDateTime.now());
        sessionRepository.save(session);

        // Request review from student
        requestStudentReview(session);

        logger.info("Session {} marked as completed by tutor {}", sessionId, tutor.getFullName());
    }

    /**
     * Get upcoming sessions for a user
     */
    public List<TutoringSession> getUpcomingSessions(User user) {
        LocalDateTime now = LocalDateTime.now();

        if (user.getRole() == User.UserRole.TUTOR) {
            return sessionRepository.findUpcomingSessionsForTutor(user,
                    TutoringSession.SessionStatus.CONFIRMED, now);
        } else {
            return sessionRepository.findUpcomingSessionsForStudent(user,
                    TutoringSession.SessionStatus.CONFIRMED, now);
        }
    }

    /**
     * Get pending sessions for tutor (waiting for acceptance)
     */
    public List<TutoringSession> getPendingSessions(User tutor) {
        return sessionRepository.findByTutorAndStatus(tutor, TutoringSession.SessionStatus.PENDING);
    }

    /**
     * Get all sessions for a user
     */
    public List<TutoringSession> getAllSessions(User user) {
        if (user.getRole() == User.UserRole.TUTOR) {
            return sessionRepository.findByTutorOrderBySessionDateTimeDesc(user);
        } else {
            return sessionRepository.findByStudentOrderBySessionDateTimeDesc(user);
        }
    }

    // Notification methods
    private void notifyTutorOfNewBooking(TutoringSession session) {
        String message = String.format(
                "🔔 *New Session Request!*\n\n" +
                        "Student: %s\n" +
                        "Subject: %s\n" +
                        "Date: %s\n" +
                        "Duration: %d minutes\n" +
                        "Type: %s\n" +
                        "Price: R%.2f\n\n" +
                        "Session ID: %d\n\n" +
                        "_Type SESSIONS to manage your bookings_",
                session.getStudent().getFullName(),
                session.getSubject().getName(),
                session.getSessionDateTime().format(DATE_TIME_FORMATTER),
                session.getDurationMinutes(),
                session.getType().name(),
                session.getPrice(),
                session.getId());

        metaWhatsAppService.sendTextMessage(session.getTutor().getPhoneNumber(), message);
    }

    private void notifyStudentOfConfirmedSession(TutoringSession session) {
        String message = String.format(
                "✅ *Session Confirmed!*\n\n" +
                        "Your session has been confirmed by %s\n\n" +
                        "Subject: %s\n" +
                        "Date: %s\n" +
                        "Duration: %d minutes\n" +
                        "Type: %s\n" +
                        "Price: R%.2f\n\n" +
                        "%s\n\n" +
                        "_You will receive a reminder 24 hours before the session_",
                session.getTutor().getFullName(),
                session.getSubject().getName(),
                session.getSessionDateTime().format(DATE_TIME_FORMATTER),
                session.getDurationMinutes(),
                session.getType().name(),
                session.getPrice(),
                session.getType() == TutoringSession.SessionType.ONLINE
                        ? "Meeting Link: " + session.getMeetingLink()
                        : "Location: " + session.getLocation());

        metaWhatsAppService.sendTextMessage(session.getStudent().getPhoneNumber(), message);
    }

    private void notifyStudentOfDeclinedSession(TutoringSession session, String reason) {
        String message = String.format(
                "❌ *Session Declined*\n\n" +
                        "Unfortunately, %s is not available for the requested session.\n\n" +
                        "Subject: %s\n" +
                        "Date: %s\n\n" +
                        "%s\n\n" +
                        "_Type BOOK to find another tutor_",
                session.getTutor().getFullName(),
                session.getSubject().getName(),
                session.getSessionDateTime().format(DATE_TIME_FORMATTER),
                reason != null ? "Reason: " + reason : "");

        metaWhatsAppService.sendTextMessage(session.getStudent().getPhoneNumber(), message);
    }

    private void notifyTutorOfCancellation(TutoringSession session, String reason) {
        String message = String.format(
                "❌ *Session Cancelled*\n\n" +
                        "%s has cancelled the session.\n\n" +
                        "Subject: %s\n" +
                        "Date: %s\n\n" +
                        "%s",
                session.getStudent().getFullName(),
                session.getSubject().getName(),
                session.getSessionDateTime().format(DATE_TIME_FORMATTER),
                reason != null ? "Reason: " + reason : "");

        metaWhatsAppService.sendTextMessage(session.getTutor().getPhoneNumber(), message);
    }

    private void notifyStudentOfCancellation(TutoringSession session, String reason) {
        String message = String.format(
                "❌ *Session Cancelled*\n\n" +
                        "%s has cancelled the session.\n\n" +
                        "Subject: %s\n" +
                        "Date: %s\n\n" +
                        "%s\n\n" +
                        "_Type BOOK to find another tutor_",
                session.getTutor().getFullName(),
                session.getSubject().getName(),
                session.getSessionDateTime().format(DATE_TIME_FORMATTER),
                reason != null ? "Reason: " + reason : "");

        metaWhatsAppService.sendTextMessage(session.getStudent().getPhoneNumber(), message);
    }

    private void requestStudentReview(TutoringSession session) {
        String message = String.format(
                "⭐ *Rate Your Session*\n\n" +
                        "How was your session with %s?\n\n" +
                        "Subject: %s\n" +
                        "Date: %s\n\n" +
                        "Please rate the session from 1-5 stars.\n" +
                        "_Type RATE %d to leave your review_",
                session.getTutor().getFullName(),
                session.getSubject().getName(),
                session.getSessionDateTime().format(DATE_TIME_FORMATTER),
                session.getId());

        metaWhatsAppService.sendTextMessage(session.getStudent().getPhoneNumber(), message);
    }

    private String generateMeetingLink(TutoringSession session) {
        // In production, integrate with Zoom, Google Meet, or Microsoft Teams API
        // For now, return a placeholder
        return "https://meet.tutormate.com/session-" + System.currentTimeMillis();
    }
}
