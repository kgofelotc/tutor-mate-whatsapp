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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    private static final Logger logger = LoggerFactory.getLogger(ConversationService.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Autowired
    private UserSessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TwilioService twilioService;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TutorSubjectRepository tutorSubjectRepository;

    @Autowired
    private TutoringSessionRepository tutoringSessionRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RatingRepository ratingRepository;
    @Transactional
    public void processMessage(String phoneNumber, String messageText) {
        logger.info("Processing message from {}: {}", phoneNumber, messageText);

        // Get or create session
        UserSession session = sessionRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> {
                    UserSession newSession = new UserSession(phoneNumber);
                    return sessionRepository.save(newSession);
                });

        session.updateLastInteraction();

        // Handle message based on current state
        switch (session.getState()) {
            case INITIAL:
                handleInitialMessage(session, messageText);
                break;
            case AWAITING_ROLE:
                handleRoleSelection(session, messageText);
                break;
            case AWAITING_ACTION:
                handleActionSelection(session, messageText);
                break;
            case REGISTER_NAME:
                handleRegistrationName(session, messageText);
                break;
            case REGISTER_EMAIL:
                handleRegistrationEmail(session, messageText);
                break;
            case REGISTER_PASSWORD:
                handleRegistrationPassword(session, messageText);
                break;
            case LOGIN_PASSWORD:
                handleLoginPassword(session, messageText);
                break;
            case AUTHENTICATED:
                handleAuthenticatedUser(session, messageText);
                break;
            // Student booking flow
            case SELECTING_SUBJECT:
                handleSubjectSelection(session, messageText);
                break;
            case SELECTING_TUTOR:
                handleTutorSelection(session, messageText);
                break;
            case SELECTING_DATETIME:
                handleDateTimeSelection(session, messageText);
                break;
            case SELECTING_DURATION:
                handleDurationSelection(session, messageText);
                break;
            case SELECTING_TYPE:
                handleTypeSelection(session, messageText);
                break;
            case CONFIRMING_BOOKING:
                handleBookingConfirmation(session, messageText);
                break;
            // Session management
            case VIEWING_SESSIONS:
                handleSessionViewing(session, messageText);
                break;
            case CANCELING_SESSION:
                handleSessionCancellation(session, messageText);
                break;
            // Rating flow
            case RATING_SESSION:
                handleSessionRating(session, messageText);
                break;
            case WRITING_REVIEW:
                handleReviewWriting(session, messageText);
                break;
            // Tutor flows
            case RESPONDING_TO_BOOKING:
                handleBookingResponse(session, messageText);
                break;
            case UPDATING_AVAILABILITY:
                handleAvailabilityUpdate(session, messageText);
                break;
        }
        }

        sessionRepository.save(session);
    }

    private void handleInitialMessage(UserSession session, String messageText) {
        String normalizedMessage = messageText.trim().toLowerCase();

        // Check if it's a greeting or start command
        if (normalizedMessage.matches("hi|hello|hey|start|menu")) {
            sendWelcomeMessage(session);
        } else {
            // Any other message also triggers welcome
            sendWelcomeMessage(session);
        }
    }

    private void sendWelcomeMessage(UserSession session) {
        String welcomeMessage = "🎓 *Welcome to TutorMate!*\n\n" +
                "Your learning companion for connecting students and tutors.\n\n" +
                "Please select your role:\n" +
                "1️⃣ TUTOR - Share your knowledge\n" +
                "2️⃣ STUDENT - Find help";

        twilioService.sendTextMessage(session.getPhoneNumber(), welcomeMessage);
        session.setState(UserSession.ConversationState.AWAITING_ROLE);
    }

    private void handleRoleSelection(UserSession session, String messageText) {
        String normalizedMessage = messageText.trim().toLowerCase();
        String selectedRole = null;

        if (normalizedMessage.matches("1|tutor")) {
            selectedRole = "TUTOR";
        } else if (normalizedMessage.matches("2|student")) {
            selectedRole = "STUDENT";
        } else {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "❌ Invalid selection. Please reply with:\n1 or TUTOR\n2 or STUDENT");
            return;
        }

        session.setSelectedRole(selectedRole);
        sendActionMenu(session, selectedRole);
    }

    private void sendActionMenu(UserSession session, String role) {
        String roleEmoji = role.equals("TUTOR") ? "👨‍🏫" : "👨‍🎓";

        String actionMessage = String.format(
                "%s *%s Account*\n\n" +
                        "What would you like to do?\n\n" +
                        "1️⃣ REGISTER - Create a new account\n" +
                        "2️⃣ LOGIN - Access your account\n" +
                        "3️⃣ STATUS - Check account status",
                roleEmoji, role);

        twilioService.sendTextMessage(session.getPhoneNumber(), actionMessage);
        session.setState(UserSession.ConversationState.AWAITING_ACTION);
    }

    private void handleActionSelection(UserSession session, String messageText) {
        String normalizedMessage = messageText.trim().toLowerCase();

        if (normalizedMessage.matches("1|register")) {
            startRegistration(session);
        } else if (normalizedMessage.matches("2|login")) {
            startLogin(session);
        } else if (normalizedMessage.matches("3|status")) {
            checkStatus(session);
        } else {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "❌ Invalid selection. Please reply with:\n" +
                            "1 or REGISTER\n2 or LOGIN\n3 or STATUS");
        }
    }

    private void startRegistration(UserSession session) {
        // Check if user already exists
        if (userRepository.existsByPhoneNumber(session.getPhoneNumber())) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "⚠️ An account with this phone number already exists.\n\n" +
                            "Please use LOGIN option instead.");
            sendActionMenu(session, session.getSelectedRole());
            return;
        }

        twilioService.sendTextMessage(session.getPhoneNumber(),
                "📝 *Registration - Step 1 of 3*\n\n" +
                        "Please enter your full name:");
        session.setState(UserSession.ConversationState.REGISTER_NAME);
    }

    private void handleRegistrationName(UserSession session, String messageText) {
        String fullName = messageText.trim();

        if (fullName.length() < 2) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "❌ Name is too short. Please enter your full name:");
            return;
        }

        session.setTempFullName(fullName);
        twilioService.sendTextMessage(session.getPhoneNumber(),
                "📝 *Registration - Step 2 of 3*\n\n" +
                        "Please enter your email address:");
        session.setState(UserSession.ConversationState.REGISTER_EMAIL);
    }

    private void handleRegistrationEmail(UserSession session, String messageText) {
        String email = messageText.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "❌ Invalid email format. Please enter a valid email address:");
            return;
        }

        if (userRepository.existsByEmail(email)) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "⚠️ This email is already registered.\n\n" +
                            "Please enter a different email address:");
            return;
        }

        session.setTempEmail(email);
        twilioService.sendTextMessage(session.getPhoneNumber(),
                "📝 *Registration - Step 3 of 3*\n\n" +
                        "Please create a password (minimum 6 characters):");
        session.setState(UserSession.ConversationState.REGISTER_PASSWORD);
    }

    private void handleRegistrationPassword(UserSession session, String messageText) {
        String password = messageText.trim();

        if (password.length() < 6) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "❌ Password is too short. Please enter at least 6 characters:");
            return;
        }

        session.setTempPassword(password);

        // Create user account
        User newUser = new User(
                session.getPhoneNumber(),
                session.getTempFullName(),
                User.UserRole.valueOf(session.getSelectedRole()),
                session.getTempPassword());
        newUser.setEmail(session.getTempEmail());
        newUser.setStatus(User.UserStatus.ACTIVE);

        userRepository.save(newUser);

        String successMessage = String.format(
                "✅ *Registration Successful!*\n\n" +
                        "Welcome to TutorMate, %s!\n\n" +
                        "Your account details:\n" +
                        "📱 Phone: %s\n" +
                        "📧 Email: %s\n" +
                        "👤 Role: %s\n\n" +
                        "You are now logged in. Type MENU to see available options.",
                session.getTempFullName(),
                session.getPhoneNumber(),
                session.getTempEmail(),
                session.getSelectedRole());

        twilioService.sendTextMessage(session.getPhoneNumber(), successMessage);

        session.clearTempData();
        session.setState(UserSession.ConversationState.AUTHENTICATED);
    }

    private void startLogin(UserSession session) {
        Optional<User> userOpt = userRepository.findByPhoneNumber(session.getPhoneNumber());

        if (userOpt.isEmpty()) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "❌ No account found with this phone number.\n\n" +
                            "Please use REGISTER option to create an account.");
            sendActionMenu(session, session.getSelectedRole());
            return;
        }

        User user = userOpt.get();

        // Check if role matches
        if (!user.getRole().name().equals(session.getSelectedRole())) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    String.format(
                            "⚠️ This phone number is registered as a %s, not a %s.\n\n" +
                                    "Please select the correct role from the main menu.",
                            user.getRole().name(),
                            session.getSelectedRole()));
            session.setState(UserSession.ConversationState.INITIAL);
            sendWelcomeMessage(session);
            return;
        }

        twilioService.sendTextMessage(session.getPhoneNumber(),
                String.format("🔐 Welcome back, %s!\n\nPlease enter your password:", user.getFullName()));
        session.setState(UserSession.ConversationState.LOGIN_PASSWORD);
    }

    private void handleLoginPassword(UserSession session, String messageText) {
        String password = messageText.trim();

        Optional<User> userOpt = userRepository.findByPhoneNumber(session.getPhoneNumber());

        if (userOpt.isEmpty()) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "❌ Session expired. Please start over by typing MENU");
            session.setState(UserSession.ConversationState.INITIAL);
            return;
        }

        User user = userOpt.get();

        if (user.getPassword().equals(password)) {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            String successMessage = String.format(
                    "✅ *Login Successful!*\n\n" +
                            "Welcome back, %s!\n\n" +
                            "Your account:\n" +
                            "📱 Phone: %s\n" +
                            "👤 Role: %s\n" +
                            "📊 Status: %s\n\n" +
                            "Type MENU to see available options.",
                    user.getFullName(),
                    user.getPhoneNumber(),
                    user.getRole().name(),
                    user.getStatus().name());

            twilioService.sendTextMessage(session.getPhoneNumber(), successMessage);
            session.setState(UserSession.ConversationState.AUTHENTICATED);
        } else {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "❌ Incorrect password. Please try again:");
        }
    }

    private void checkStatus(UserSession session) {
        Optional<User> userOpt = userRepository.findByPhoneNumber(session.getPhoneNumber());

        if (userOpt.isEmpty()) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "❌ No account found with this phone number.\n\n" +
                            "Please use REGISTER option to create an account.");
            sendActionMenu(session, session.getSelectedRole());
            return;
        }

        User user = userOpt.get();

        String statusMessage = String.format(
                "📊 *Account Status*\n\n" +
                        "Name: %s\n" +
                        "Phone: %s\n" +
                        "Email: %s\n" +
                        "Role: %s\n" +
                        "Status: %s\n" +
                        "Member since: %s\n" +
                        "Last login: %s\n\n" +
                        "Type MENU to return to main menu.",
                user.getFullName(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt().toLocalDate(),
                user.getLastLoginAt() != null ? user.getLastLoginAt().toLocalDate().toString() : "Never");

        twilioService.sendTextMessage(session.getPhoneNumber(), statusMessage);
        sendActionMenu(session, session.getSelectedRole());
    }

    private void handleAuthenticatedUser(UserSession session, String messageText) {
        String normalizedMessage = messageText.trim().toLowerCase();

        Optional<User> userOpt = userRepository.findByPhoneNumber(session.getPhoneNumber());

        if (userOpt.isEmpty()) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "❌ Session expired. Please type MENU to start over.");
            session.setState(UserSession.ConversationState.INITIAL);
            return;
        }

        User user = userOpt.get();

        // Common commands
        if (normalizedMessage.equals("menu")) {
            sendAuthenticatedMenu(session, user);
        } else if (normalizedMessage.equals("logout")) {
            handleLogout(session);
        } else if (normalizedMessage.equals("profile")) {
            showProfile(session, user);
        } else if (normalizedMessage.equals("help")) {
            sendHelpMessage(session);
        } 
        // Student commands
        else if (user.getRole() == User.UserRole.STUDENT) {
            if (normalizedMessage.equals("book") || normalizedMessage.equals("1")) {
                startBookingFlow(session, user);
            } else if (normalizedMessage.matches("sessions?") || normalizedMessage.equals("2")) {
                showMySessions(session, user);
            } else if (normalizedMessage.startsWith("find ")) {
                String subject = messageText.substring(5).trim();
                findTutorsForSubject(session, user, subject);
            } else if (normalizedMessage.startsWith("cancel ")) {
                String sessionId = messageText.substring(7).trim();
                initiateCancelSession(session, user, sessionId);
            } else if (normalizedMessage.startsWith("rate ")) {
                String sessionId = messageText.substring(5).trim();
                initiateRateSession(session, user, sessionId);
            } else {
                sendStudentHelpMessage(session);
            }
        }
        // Tutor commands
        else if (user.getRole() == User.UserRole.TUTOR) {
            if (normalizedMessage.equals("sessions") || normalizedMessage.equals("1")) {
                showTutorSessions(session, user);
            } else if (normalizedMessage.equals("pending") || normalizedMessage.equals("2")) {
                showPendingBookings(session, user);
            } else if (normalizedMessage.equals("availability") || normalizedMessage.equals("3")) {
                updateAvailability(session, user);
            } else if (normalizedMessage.equals("earnings") || normalizedMessage.equals("4")) {
                showEarnings(session, user);
            } else if (normalizedMessage.startsWith("accept ")) {
                String sessionId = messageText.substring(7).trim();
                acceptBooking(session, user, sessionId);
            } else if (normalizedMessage.startsWith("decline ")) {
                String sessionId = messageText.substring(8).trim();
                declineBooking(session, user, sessionId);
            } else if (normalizedMessage.startsWith("complete ")) {
                String sessionId = messageText.substring(9).trim();
                completeSession(session, user, sessionId);
            } else {
                sendTutorHelpMessage(session);
            }
        } else {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "Type MENU to see available options");
        }
    }

    private void sendAuthenticatedMenu(UserSession session, User user) {
        String roleEmoji = user.getRole() == User.UserRole.TUTOR ? "👨‍🏫" : "👨‍🎓";

        if (user.getRole() == User.UserRole.STUDENT) {
            String menuMessage = String.format(
                    "%s *Student Dashboard*\n\n" +
                            "Hello, %s! 👋\n\n" +
                            "What would you like to do?",
                    roleEmoji,
                    user.getFullName());

            List<String> buttons = new ArrayList<>();
            buttons.add("📚 Book a Session");
            buttons.add("📅 View My Sessions");
            buttons.add("👤 Profile");

            twilioService.sendMessageWithButtons(session.getPhoneNumber(), menuMessage, buttons);

            // Send additional commands
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "\n*Quick Commands:*\n" +
                            "• BOOK - Start booking\n" +
                            "• SESSIONS - View sessions\n" +
                            "• FIND [subject] - Find tutors\n" +
                            "• CANCEL [id] - Cancel session\n" +
                            "• RATE [id] - Rate session\n" +
                            "• PROFILE - Your profile\n" +
                            "• LOGOUT - Sign out");
        } else {
            String menuMessage = String.format(
                    "%s *Tutor Dashboard*\n\n" +
                            "Hello, %s! 👋\n\n" +
                            "What would you like to do?",
                    roleEmoji,
                    user.getFullName());

            List<String> buttons = new ArrayList<>();
            buttons.add("📅 My Sessions");
            buttons.add("🔔 Pending Requests");
            buttons.add("⏰ Update Availability");

            twilioService.sendMessageWithButtons(session.getPhoneNumber(), menuMessage, buttons);

            // Send additional commands
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "\n*Quick Commands:*\n" +
                            "• SESSIONS - View sessions\n" +
                            "• PENDING - Pending bookings\n" +
                            "• AVAILABILITY - Update schedule\n" +
                            "• EARNINGS - View earnings\n" +
                            "• ACCEPT [id] - Accept booking\n" +
                            "• DECLINE [id] - Decline booking\n" +
                            "• COMPLETE [id] - Mark complete\n" +
                            "• PROFILE - Your profile\n" +
                            "• LOGOUT - Sign out");
        }
    }

    private void showProfile(UserSession session, User user) {
        String profileMessage = String.format(
                "👤 *Your Profile*\n\n" +
                        "Name: %s\n" +
                        "Phone: %s\n" +
                        "Email: %s\n" +
                        "Role: %s\n" +
                        "Status: %s\n" +
                        "Member since: %s\n\n" +
                        "Type MENU to return.",
                user.getFullName(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt().toLocalDate());

        twilioService.sendTextMessage(session.getPhoneNumber(), profileMessage);
    }

    private void handleLogout(UserSession session) {
        twilioService.sendTextMessage(session.getPhoneNumber(),
                "👋 You have been logged out successfully.\n\n" +
                        "Type HI or MENU to start again.");
        session.setState(UserSession.ConversationState.INITIAL);
    }

    private void sendHelpMessage(UserSession session) {
        String helpMessage = "ℹ️ *TutorMate Help*\n\n" +
                "*Getting Started:*\n" +
                "• Type HI or MENU to start\n" +
                "• Choose your role (Tutor/Student)\n" +
                "• Register or Login\n\n" +
                "*Commands:*\n" +
                "• MENU - Main menu\n" +
                "• PROFILE - Your profile\n" +
                "• STATUS - Account status\n" +
                "• LOGOUT - Sign out\n" +
                "• HELP - This message\n\n" +
                "Need assistance? Contact support.";

        twilioService.sendTextMessage(session.getPhoneNumber(), helpMessage);
    }

    // ============ STUDENT WORKFLOW METHODS ============

    private void startBookingFlow(UserSession session, User student) {
        List<Subject> subjects = subjectRepository.findByActiveTrue();
        
        if (subjects.isEmpty()) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "No subjects available at the moment. Please try again later.");
            return;
        }

        List<TwilioService.ListOption> options = subjects.stream()
                .map(s -> new TwilioService.ListOption(s.getId().toString(), s.getName(), s.getDescription()))
                .collect(Collectors.toList());

        twilioService.sendListMessage(session.getPhoneNumber(),
                "📚 Select a Subject",
                "Choose the subject you need help with:",
                options);

        session.setState(UserSession.ConversationState.SELECTING_SUBJECT);
        sessionRepository.save(session);
    }

    private void handleSubjectSelection(UserSession session, String messageText) {
        try {
            int selection = Integer.parseInt(messageText.trim());
            List<Subject> subjects = subjectRepository.findByActiveTrue();
            
            if (selection < 1 || selection > subjects.size()) {
                twilioService.sendTextMessage(session.getPhoneNumber(),
                        "Invalid selection. Please enter a number from the list.");
                return;
            }

            Subject selectedSubject = subjects.get(selection - 1);
            session.setTempSubjectId(selectedSubject.getId());
            
            // Find tutors for this subject
            List<TutorSubject> tutorSubjects = tutorSubjectRepository.findTutorsForSubjectOrderedByRate(selectedSubject);
            
            if (tutorSubjects.isEmpty()) {
                twilioService.sendTextMessage(session.getPhoneNumber(),
                        "No tutors available for " + selectedSubject.getName() + " at the moment.\n\n" +
                        "Type BOOK to try another subject.");
                session.setState(UserSession.ConversationState.AUTHENTICATED);
                sessionRepository.save(session);
                return;
            }

            List<TwilioService.ListOption> options = tutorSubjects.stream()
                    .map(ts -> {
                        Double avgRating = ratingRepository.calculateAverageRating(ts.getTutor());
                        String rating = avgRating != null ? String.format("%.1f⭐", avgRating) : "New";
                        return new TwilioService.ListOption(
                                ts.getTutor().getId().toString(),
                                ts.getTutor().getFullName(),
                                String.format("R%.0f/hr • %s • %s", ts.getHourlyRate(), rating, 
                                            ts.getQualifications() != null ? ts.getQualifications() : "")
                        );
                    })
                    .collect(Collectors.toList());

            twilioService.sendListMessage(session.getPhoneNumber(),
                    "👨‍🏫 Select a Tutor",
                    "Choose your tutor for " + selectedSubject.getName() + ":",
                    options);

            session.setState(UserSession.ConversationState.SELECTING_TUTOR);
            sessionRepository.save(session);

        } catch (NumberFormatException e) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "Please enter a number from the list.");
        }
    }

    private void handleTutorSelection(UserSession session, String messageText) {
        try {
            Subject subject = subjectRepository.findById(session.getTempSubjectId()).orElseThrow();
            List<TutorSubject> tutorSubjects = tutorSubjectRepository.findTutorsForSubjectOrderedByRate(subject);
            
            int selection = Integer.parseInt(messageText.trim());
            if (selection < 1 || selection > tutorSubjects.size()) {
                twilioService.sendTextMessage(session.getPhoneNumber(),
                        "Invalid selection. Please enter a number from the list.");
                return;
            }

            User tutor = tutorSubjects.get(selection - 1).getTutor();
            session.setTempTutorId(tutor.getId());

            // Ask for session type
            List<String> buttons = new ArrayList<>();
            buttons.add("💻 Online Session");
            buttons.add("📍 In-Person Session");

            twilioService.sendMessageWithButtons(session.getPhoneNumber(),
                    "How would you like to have your session?",
                    buttons);

            session.setState(UserSession.ConversationState.SELECTING_TYPE);
            sessionRepository.save(session);

        } catch (NumberFormatException e) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "Please enter a number from the list.");
        } catch (Exception e) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "An error occurred. Please try again.");
            logger.error("Error in tutor selection", e);
        }
    }

    private void handleTypeSelection(UserSession session, String messageText) {
        String normalizedMessage = messageText.trim().toLowerCase();
        String type;

        if (normalizedMessage.matches("1|online.*")) {
            type = "ONLINE";
        } else if (normalizedMessage.matches("2|in-person.*|in person.*")) {
            type = "IN_PERSON";
        } else {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "Please select:\n1 - Online\n2 - In-Person");
            return;
        }

        session.setTempSessionType(type);

        twilioService.sendTextMessage(session.getPhoneNumber(),
                "📅 *Choose Date & Time*\n\n" +
                "Please enter your preferred date and time:\n\n" +
                "*Format:* YYYY-MM-DD HH:MM\n" +
                "*Example:* 2026-02-15 14:00\n\n" +
                "_Make sure to check the tutor's availability_");

        session.setState(UserSession.ConversationState.SELECTING_DATETIME);
        sessionRepository.save(session);
    }

    private void handleDateTimeSelection(UserSession session, String messageText) {
        // Parse date time (simplified - in production use proper validation)
        session.setTempDateTime(messageText.trim());

        List<String> buttons = new ArrayList<>();
        buttons.add("⏱️ 30 minutes");
        buttons.add("⏱️ 60 minutes");
        buttons.add("⏱️ 90 minutes");

        twilioService.sendMessageWithButtons(session.getPhoneNumber(),
                "How long would you like the session to be?",
                buttons);

        session.setState(UserSession.ConversationState.SELECTING_DURATION);
        sessionRepository.save(session);
    }

    private void handleDurationSelection(UserSession session, String messageText) {
        String normalizedMessage = messageText.trim().toLowerCase();
        int duration;

        if (normalizedMessage.matches(".*30.*|1")) {
            duration = 30;
        } else if (normalizedMessage.matches(".*60.*|2")) {
            duration = 60;
        } else if (normalizedMessage.matches(".*90.*|3")) {
            duration = 90;
        } else {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "Please select duration:\n1 - 30 min\n2 - 60 min\n3 - 90 min");
            return;
        }

        try {
            // Load all booking data
            User student = userRepository.findByPhoneNumber(session.getPhoneNumber()).orElseThrow();
            User tutor = userRepository.findById(session.getTempTutorId()).orElseThrow();
            Subject subject = subjectRepository.findById(session.getTempSubjectId()).orElseThrow();
            TutorSubject tutorSubject = tutorSubjectRepository.findByTutorAndSubject(tutor, subject).orElseThrow();
            
            // Calculate price
            BigDecimal hourlyRate = tutorSubject.getHourlyRate();
            BigDecimal price = hourlyRate.multiply(new BigDecimal(duration))
                    .divide(new BigDecimal(60), 2, BigDecimal.ROUND_HALF_UP);

            // Show confirmation
            String confirmMessage = String.format(
                    "📋 *Confirm Your Booking*\n\n" +
                    "Tutor: %s\n" +
                    "Subject: %s\n" +
                    "Date/Time: %s\n" +
                    "Duration: %d minutes\n" +
                    "Type: %s\n" +
                    "Price: R%.2f\n\n" +
                    "Confirm this booking?",
                    tutor.getFullName(),
                    subject.getName(),
                    session.getTempDateTime(),
                    duration,
                    session.getTempSessionType(),
                    price);

            twilioService.sendConfirmationMessage(session.getPhoneNumber(), confirmMessage);
            
            // Store duration in notes temporarily
            session.setTempNotes(String.valueOf(duration));
            session.setState(UserSession.ConversationState.CONFIRMING_BOOKING);
            sessionRepository.save(session);

        } catch (Exception e) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "An error occurred. Please start over by typing BOOK.");
            logger.error("Error in duration selection", e);
            session.setState(UserSession.ConversationState.AUTHENTICATED);
            sessionRepository.save(session);
        }
    }

    private void handleBookingConfirmation(UserSession session, String messageText) {
        String normalizedMessage = messageText.trim().toLowerCase();

        if (normalizedMessage.matches("1|yes.*|confirm.*")) {
            try {
                User student = userRepository.findByPhoneNumber(session.getPhoneNumber()).orElseThrow();
                User tutor = userRepository.findById(session.getTempTutorId()).orElseThrow();
                Subject subject = subjectRepository.findById(session.getTempSubjectId()).orElseThrow();
                
                // Parse date time (simplified)
                LocalDateTime sessionDateTime = LocalDateTime.parse(session.getTempDateTime().replace(" ", "T"));
                int duration = Integer.parseInt(session.getTempNotes());
                TutoringSession.SessionType type = TutoringSession.SessionType.valueOf(session.getTempSessionType());

                // Create booking
                TutoringSession tutoringSession = sessionService.createSessionBooking(
                        student, tutor, subject, sessionDateTime, duration, type, null);

                twilioService.sendTextMessage(session.getPhoneNumber(),
                        String.format("✅ *Booking Request Sent!*\n\n" +
                                "Your booking request has been sent to %s.\n" +
                                "You'll receive a notification once they respond.\n\n" +
                                "Booking ID: %d\n\n" +
                                "Type SESSIONS to view your bookings.",
                                tutor.getFullName(),
                                tutoringSession.getId()));

                session.clearTempData();
                session.setState(UserSession.ConversationState.AUTHENTICATED);
                sessionRepository.save(session);

            } catch (Exception e) {
                twilioService.sendTextMessage(session.getPhoneNumber(),
                        "An error occurred while creating the booking. Please try again.");
                logger.error("Error confirming booking", e);
                session.setState(UserSession.ConversationState.AUTHENTICATED);
                sessionRepository.save(session);
            }
        } else {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "Booking cancelled. Type BOOK to start over.");
            session.clearTempData();
            session.setState(UserSession.ConversationState.AUTHENTICATED);
            sessionRepository.save(session);
        }
    }

    private void showMySessions(UserSession session, User student) {
        List<TutoringSession> sessions = sessionService.getUpcomingSessions(student);

        if (sessions.isEmpty()) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "📅 You have no upcoming sessions.\n\nType BOOK to schedule one!");
            return;
        }

        StringBuilder message = new StringBuilder("📅 *Your Upcoming Sessions*\n\n");
        int count = 1;
        for (TutoringSession sess : sessions) {
            message.append(String.format("%d. %s with %s\n" +
                    "   📚 %s\n" +
                    "   📅 %s\n" +
                    "   💰 R%.2f • Status: %s\n\n",
                    count++,
                    sess.getSubject().getName(),
                    sess.getTutor().getFullName(),
                    sess.getType().name(),
                    sess.getSessionDateTime().toLocalDate(),
                    sess.getPrice(),
                    sess.getStatus()));
        }

        message.append("_Type CANCEL [id] to cancel a session_");
        twilioService.sendTextMessage(session.getPhoneNumber(), message.toString());
    }

    private void findTutorsForSubject(UserSession session, User student, String subjectName) {
        Optional<Subject> subjectOpt = subjectRepository.findByName(subjectName);

        if (subjectOpt.isEmpty()) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "Subject not found. Type BOOK to see all available subjects.");
            return;
        }

        Subject subject = subjectOpt.get();
        List<TutorSubject> tutorSubjects = tutorSubjectRepository.findTutorsForSubjectOrderedByRate(subject);

        if (tutorSubjects.isEmpty()) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "No tutors available for " + subjectName + " at the moment.");
            return;
        }

        StringBuilder message = new StringBuilder(
                String.format("👨‍🏫 *Tutors for %s*\n\n", subject.getName()));

        for (int i = 0; i < tutorSubjects.size(); i++) {
            TutorSubject ts = tutorSubjects.get(i);
            Double avgRating = ratingRepository.calculateAverageRating(ts.getTutor());
            String rating = avgRating != null ? String.format("%.1f⭐", avgRating) : "New tutor";
            
            message.append(String.format("%d. %s\n" +
                    "   💰 R%.0f/hr • %s\n" +
                    "   %s\n\n",
                    i + 1,
                    ts.getTutor().getFullName(),
                    ts.getHourlyRate(),
                    rating,
                    ts.getQualifications() != null ? ts.getQualifications() : ""));
        }

        message.append("_Type BOOK to schedule a session_");
        twilioService.sendTextMessage(session.getPhoneNumber(), message.toString());
    }

    private void initiateCancelSession(UserSession session, User student, String sessionIdStr) {
        try {
            Long sessionId = Long.parseLong(sessionIdStr);
            TutoringSession tutoringSession = tutoringSessionRepository.findById(sessionId).orElse(null);

            if (tutoringSession == null || !tutoringSession.getStudent().getId().equals(student.getId())) {
                twilioService.sendTextMessage(session.getPhoneNumber(),
                        "Session not found or you don't have permission to cancel it.");
                return;
            }

            session.setTempSessionId(sessionId);
            twilioService.sendConfirmationMessage(session.getPhoneNumber(),
                    String.format("Are you sure you want to cancel your session with %s on %s?",
                            tutoringSession.getTutor().getFullName(),
                            tutoringSession.getSessionDateTime().toLocalDate()));

            session.setState(UserSession.ConversationState.CANCELING_SESSION);
            sessionRepository.save(session);

        } catch (NumberFormatException e) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "Invalid session ID. Please use: CANCEL [id]");
        }
    }

    private void handleSessionCancellation(UserSession session, String messageText) {
        String normalizedMessage = messageText.trim().toLowerCase();

        if (normalizedMessage.matches("1|yes.*|confirm.*")) {
            try {
                User student = userRepository.findByPhoneNumber(session.getPhoneNumber()).orElseThrow();
                sessionService.cancelSession(session.getTempSessionId(), student, "Cancelled by student");

                twilioService.sendTextMessage(session.getPhoneNumber(),
                        "✅ Session cancelled successfully.");

            } catch (Exception e) {
                twilioService.sendTextMessage(session.getPhoneNumber(),
                        "Error cancelling session: " + e.getMessage());
            }
        } else {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "Cancellation aborted.");
        }

        session.clearTempData();
        session.setState(UserSession.ConversationState.AUTHENTICATED);
        sessionRepository.save(session);
    }

    private void initiateRateSession(UserSession session, User student, String sessionIdStr) {
        try {
            Long sessionId = Long.parseLong(sessionIdStr);
            TutoringSession tutoringSession = tutoringSessionRepository.findById(sessionId).orElse(null);

            if (tutoringSession == null || !tutoringSession.getStudent().getId().equals(student.getId())) {
                twilioService.sendTextMessage(session.getPhoneNumber(),
                        "Session not found or you don't have permission to rate it.");
                return;
            }

            if (tutoringSession.getStatus() != TutoringSession.SessionStatus.COMPLETED) {
                twilioService.sendTextMessage(session.getPhoneNumber(),
                        "You can only rate completed sessions.");
                return;
            }

            // Check if already rated
            if (ratingRepository.findBySession(tutoringSession).isPresent()) {
                twilioService.sendTextMessage(session.getPhoneNumber(),
                        "You have already rated this session.");
                return;
            }

            session.setTempSessionId(sessionId);

            String message = String.format(
                    "⭐ *Rate Your Session*\n\n" +
                    "Session with %s\n" +
                    "Subject: %s\n\n" +
                    "How many stars? (1-5)",
                    tutoringSession.getTutor().getFullName(),
                    tutoringSession.getSubject().getName());

            List<String> buttons = new ArrayList<>();
            buttons.add("⭐ 1 Star");
            buttons.add("⭐⭐ 2 Stars");
            buttons.add("⭐⭐⭐ 3 Stars");

            twilioService.sendMessageWithButtons(session.getPhoneNumber(), message, buttons);
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "4️⃣ ⭐⭐⭐⭐ 4 Stars\n5️⃣ ⭐⭐⭐⭐⭐ 5 Stars");

            session.setState(UserSession.ConversationState.RATING_SESSION);
            sessionRepository.save(session);

        } catch (NumberFormatException e) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "Invalid session ID. Please use: RATE [id]");
        }
    }

    private void handleSessionRating(UserSession session, String messageText) {
        try {
            String normalized = messageText.trim().toLowerCase();
            int stars;

            // Parse stars from various formats
            if (normalized.matches(".*5.*|five")) stars = 5;
            else if (normalized.matches(".*4.*|four")) stars = 4;
            else if (normalized.matches(".*3.*|three")) stars = 3;
            else if (normalized.matches(".*2.*|two")) stars = 2;
            else if (normalized.matches(".*1.*|one")) stars = 1;
            else {
                twilioService.sendTextMessage(session.getPhoneNumber(),
                        "Please enter a rating from 1 to 5 stars.");
                return;
            }

            User student = userRepository.findByPhoneNumber(session.getPhoneNumber()).orElseThrow();
            TutoringSession tutoringSession = tutoringSessionRepository.findById(session.getTempSessionId()).orElseThrow();

            // Create rating
            Rating rating = new Rating(tutoringSession, tutoringSession.getTutor(), student, stars);
            ratingRepository.save(rating);

            twilioService.sendTextMessage(session.getPhoneNumber(),
                    String.format("✅ Thank you for rating! You gave %d stars.\n\n" +
                            "Would you like to add a written review? (Reply with review text or type SKIP)",
                            stars));

            session.setState(UserSession.ConversationState.WRITING_REVIEW);
            sessionRepository.save(session);

        } catch (Exception e) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "Error saving rating. Please try again.");
            logger.error("Error in rating session", e);
        }
    }

    private void handleReviewWriting(UserSession session, String messageText) {
        String normalized = messageText.trim().toLowerCase();

        if (!normalized.equals("skip")) {
            try {
                User student = userRepository.findByPhoneNumber(session.getPhoneNumber()).orElseThrow();
                TutoringSession tutoringSession = tutoringSessionRepository.findById(session.getTempSessionId()).orElseThrow();
                Rating rating = ratingRepository.findBySession(tutoringSession).orElseThrow();

                rating.setReview(messageText.trim());
                ratingRepository.save(rating);

                twilioService.sendTextMessage(session.getPhoneNumber(),
                        "✅ Thank you for your review! Your feedback helps other students.");

            } catch (Exception e) {
                twilioService.sendTextMessage(session.getPhoneNumber(),
                        "Error saving review, but your rating was recorded.");
                logger.error("Error saving review", e);
            }
        } else {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "✅ Rating recorded. Thank you!");
        }

        session.clearTempData();
        session.setState(UserSession.ConversationState.AUTHENTICATED);
        sessionRepository.save(session);
    }

    private void sendStudentHelpMessage(UserSession session) {
        twilioService.sendTextMessage(session.getPhoneNumber(),
                "📚 *Student Commands*\n\n" +
                "• BOOK - Book a session\n" +
                "• SESSIONS - View sessions\n" +
                "• FIND [subject] - Find tutors\n" +
                "• CANCEL [id] - Cancel session\n" +
                "• RATE [id] - Rate session\n" +
                "• PROFILE - View profile\n" +
                "• MENU - Main menu\n" +
                "• LOGOUT - Sign out");
    }

    // ============ TUTOR WORKFLOW METHODS ============

    private void showTutorSessions(UserSession session, User tutor) {
        List<TutoringSession> sessions = sessionService.getUpcomingSessions(tutor);

        if (sessions.isEmpty()) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "📅 You have no upcoming sessions.");
            return;
        }

        StringBuilder message = new StringBuilder("📅 *Your Upcoming Sessions*\n\n");
        int count = 1;
        for (TutoringSession sess : sessions) {
            message.append(String.format("%d. %s with %s\n" +
                    "   📚 %s\n" +
                    "   📅 %s\n" +
                    "   💰 R%.2f\n" +
                    "   ID: %d\n\n",
                    count++,
                    sess.getSubject().getName(),
                    sess.getStudent().getFullName(),
                    sess.getType().name(),
                    sess.getSessionDateTime().toLocalDate(),
                    sess.getPrice(),
                    sess.getId()));
        }

        message.append("_Type COMPLETE [id] to mark session as done_");
        twilioService.sendTextMessage(session.getPhoneNumber(), message.toString());
    }

    private void showPendingBookings(UserSession session, User tutor) {
        List<TutoringSession> pendingSessions = sessionService.getPendingSessions(tutor);

        if (pendingSessions.isEmpty()) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "🔔 No pending booking requests.");
            return;
        }

        StringBuilder message = new StringBuilder("🔔 *Pending Booking Requests*\n\n");
        for (TutoringSession sess : pendingSessions) {
            message.append(String.format("ID: %d\n" +
                    "Student: %s\n" +
                    "Subject: %s\n" +
                    "Date: %s\n" +
                    "Duration: %d min\n" +
                    "Price: R%.2f\n\n",
                    sess.getId(),
                    sess.getStudent().getFullName(),
                    sess.getSubject().getName(),
                    sess.getSessionDateTime().toLocalDate(),
                    sess.getDurationMinutes(),
                    sess.getPrice()));
        }

        message.append("_Reply with:_\n• ACCEPT [id]\n• DECLINE [id]");
        twilioService.sendTextMessage(session.getPhoneNumber(), message.toString());
    }

    private void acceptBooking(UserSession session, User tutor, String sessionIdStr) {
        try {
            Long sessionId = Long.parseLong(sessionIdStr);
            sessionService.acceptSession(sessionId, tutor);

            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "✅ Booking accepted! The student has been notified and will receive a payment link.");

        } catch (NumberFormatException e) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "Invalid ID. Use: ACCEPT [id]");
        } catch (Exception e) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "Error accepting booking: " + e.getMessage());
        }
    }

    private void declineBooking(UserSession session, User tutor, String sessionIdStr) {
        try {
            Long sessionId = Long.parseLong(sessionIdStr);
            sessionService.declineSession(sessionId, tutor, "Not available at this time");

            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "Booking declined. The student has been notified.");

        } catch (NumberFormatException e) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "Invalid ID. Use: DECLINE [id]");
        } catch (Exception e) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "Error declining booking: " + e.getMessage());
        }
    }

    private void completeSession(UserSession session, User tutor, String sessionIdStr) {
        try {
            Long sessionId = Long.parseLong(sessionIdStr);
            sessionService.completeSession(sessionId, tutor);

            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "✅ Session marked as complete! The student will be asked to leave a review.");

        } catch (NumberFormatException e) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "Invalid ID. Use: COMPLETE [id]");
        } catch (Exception e) {
            twilioService.sendTextMessage(session.getPhoneNumber(),
                    "Error completing session: " + e.getMessage());
        }
    }

    private void updateAvailability(UserSession session, User tutor) {
        twilioService.sendTextMessage(session.getPhoneNumber(),
                "⏰ *Update Availability*\n\n" +
                "Feature coming soon!\n" +
                "You'll be able to set your weekly schedule here.");
    }

    private void showEarnings(UserSession session, User tutor) {
        BigDecimal totalEarnings = paymentService.calculateTutorEarnings(tutor);
        BigDecimal monthlyEarnings = paymentService.calculateMonthlyEarnings(tutor);

        String message = String.format(
                "💰 *Your Earnings*\n\n" +
                "This Month: R%.2f\n" +
                "Total Lifetime: R%.2f\n\n" +
                "_Type MENU for more options_",
                monthlyEarnings,
                totalEarnings);

        twilioService.sendTextMessage(session.getPhoneNumber(), message);
    }

    private void handleBookingResponse(UserSession session, String messageText) {
        // Handler for tutor responding to bookings
        session.setState(UserSession.ConversationState.AUTHENTICATED);
        sessionRepository.save(session);
    }

    private void handleAvailabilityUpdate(UserSession session, String messageText) {
        // Handler for updating availability
        session.setState(UserSession.ConversationState.AUTHENTICATED);
        sessionRepository.save(session);
    }

    private void handleSessionViewing(UserSession session, String messageText) {
        // Handler for viewing sessions
        session.setState(UserSession.ConversationState.AUTHENTICATED);
        sessionRepository.save(session);
    }

    private void sendTutorHelpMessage(UserSession session) {
        twilioService.sendTextMessage(session.getPhoneNumber(),
                "👨‍🏫 *Tutor Commands*\n\n" +
                "• SESSIONS - View sessions\n" +
                "• PENDING - Pending requests\n" +
                "• ACCEPT [id] - Accept booking\n" +
                "• DECLINE [id] - Decline booking\n" +
                "• COMPLETE [id] - Mark complete\n" +
                "• AVAILABILITY - Update schedule\n" +
                "• EARNINGS - View earnings\n" +
                "• PROFILE - View profile\n" +
                "• MENU - Main menu\n" +
                "• LOGOUT - Sign out");
    }
}
