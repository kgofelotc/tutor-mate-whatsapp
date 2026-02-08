package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.entity.UserSession;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class ConversationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConversationService.class);
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    @Autowired
    private UserSessionRepository sessionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private InfobipService infobipService;
    
    /**
     * Main entry point for processing incoming messages
     */
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
        
        infobipService.sendTextMessage(session.getPhoneNumber(), welcomeMessage);
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
            infobipService.sendTextMessage(session.getPhoneNumber(),
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
                roleEmoji, role
        );
        
        infobipService.sendTextMessage(session.getPhoneNumber(), actionMessage);
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
            infobipService.sendTextMessage(session.getPhoneNumber(),
                    "❌ Invalid selection. Please reply with:\n" +
                    "1 or REGISTER\n2 or LOGIN\n3 or STATUS");
        }
    }
    
    private void startRegistration(UserSession session) {
        // Check if user already exists
        if (userRepository.existsByPhoneNumber(session.getPhoneNumber())) {
            infobipService.sendTextMessage(session.getPhoneNumber(),
                    "⚠️ An account with this phone number already exists.\n\n" +
                    "Please use LOGIN option instead.");
            sendActionMenu(session, session.getSelectedRole());
            return;
        }
        
        infobipService.sendTextMessage(session.getPhoneNumber(),
                "📝 *Registration - Step 1 of 3*\n\n" +
                "Please enter your full name:");
        session.setState(UserSession.ConversationState.REGISTER_NAME);
    }
    
    private void handleRegistrationName(UserSession session, String messageText) {
        String fullName = messageText.trim();
        
        if (fullName.length() < 2) {
            infobipService.sendTextMessage(session.getPhoneNumber(),
                    "❌ Name is too short. Please enter your full name:");
            return;
        }
        
        session.setTempFullName(fullName);
        infobipService.sendTextMessage(session.getPhoneNumber(),
                "📝 *Registration - Step 2 of 3*\n\n" +
                "Please enter your email address:");
        session.setState(UserSession.ConversationState.REGISTER_EMAIL);
    }
    
    private void handleRegistrationEmail(UserSession session, String messageText) {
        String email = messageText.trim().toLowerCase();
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            infobipService.sendTextMessage(session.getPhoneNumber(),
                    "❌ Invalid email format. Please enter a valid email address:");
            return;
        }
        
        if (userRepository.existsByEmail(email)) {
            infobipService.sendTextMessage(session.getPhoneNumber(),
                    "⚠️ This email is already registered.\n\n" +
                    "Please enter a different email address:");
            return;
        }
        
        session.setTempEmail(email);
        infobipService.sendTextMessage(session.getPhoneNumber(),
                "📝 *Registration - Step 3 of 3*\n\n" +
                "Please create a password (minimum 6 characters):");
        session.setState(UserSession.ConversationState.REGISTER_PASSWORD);
    }
    
    private void handleRegistrationPassword(UserSession session, String messageText) {
        String password = messageText.trim();
        
        if (password.length() < 6) {
            infobipService.sendTextMessage(session.getPhoneNumber(),
                    "❌ Password is too short. Please enter at least 6 characters:");
            return;
        }
        
        session.setTempPassword(password);
        
        // Create user account
        User newUser = new User(
                session.getPhoneNumber(),
                session.getTempFullName(),
                User.UserRole.valueOf(session.getSelectedRole()),
                session.getTempPassword()
        );
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
                session.getSelectedRole()
        );
        
        infobipService.sendTextMessage(session.getPhoneNumber(), successMessage);
        
        session.clearTempData();
        session.setState(UserSession.ConversationState.AUTHENTICATED);
    }
    
    private void startLogin(UserSession session) {
        Optional<User> userOpt = userRepository.findByPhoneNumber(session.getPhoneNumber());
        
        if (userOpt.isEmpty()) {
            infobipService.sendTextMessage(session.getPhoneNumber(),
                    "❌ No account found with this phone number.\n\n" +
                    "Please use REGISTER option to create an account.");
            sendActionMenu(session, session.getSelectedRole());
            return;
        }
        
        User user = userOpt.get();
        
        // Check if role matches
        if (!user.getRole().name().equals(session.getSelectedRole())) {
            infobipService.sendTextMessage(session.getPhoneNumber(),
                    String.format(
                            "⚠️ This phone number is registered as a %s, not a %s.\n\n" +
                            "Please select the correct role from the main menu.",
                            user.getRole().name(),
                            session.getSelectedRole()
                    ));
            session.setState(UserSession.ConversationState.INITIAL);
            sendWelcomeMessage(session);
            return;
        }
        
        infobipService.sendTextMessage(session.getPhoneNumber(),
                String.format("🔐 Welcome back, %s!\n\nPlease enter your password:", user.getFullName()));
        session.setState(UserSession.ConversationState.LOGIN_PASSWORD);
    }
    
    private void handleLoginPassword(UserSession session, String messageText) {
        String password = messageText.trim();
        
        Optional<User> userOpt = userRepository.findByPhoneNumber(session.getPhoneNumber());
        
        if (userOpt.isEmpty()) {
            infobipService.sendTextMessage(session.getPhoneNumber(),
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
                    user.getStatus().name()
            );
            
            infobipService.sendTextMessage(session.getPhoneNumber(), successMessage);
            session.setState(UserSession.ConversationState.AUTHENTICATED);
        } else {
            infobipService.sendTextMessage(session.getPhoneNumber(),
                    "❌ Incorrect password. Please try again:");
        }
    }
    
    private void checkStatus(UserSession session) {
        Optional<User> userOpt = userRepository.findByPhoneNumber(session.getPhoneNumber());
        
        if (userOpt.isEmpty()) {
            infobipService.sendTextMessage(session.getPhoneNumber(),
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
                user.getLastLoginAt() != null ? user.getLastLoginAt().toLocalDate().toString() : "Never"
        );
        
        infobipService.sendTextMessage(session.getPhoneNumber(), statusMessage);
        sendActionMenu(session, session.getSelectedRole());
    }
    
    private void handleAuthenticatedUser(UserSession session, String messageText) {
        String normalizedMessage = messageText.trim().toLowerCase();
        
        Optional<User> userOpt = userRepository.findByPhoneNumber(session.getPhoneNumber());
        
        if (userOpt.isEmpty()) {
            infobipService.sendTextMessage(session.getPhoneNumber(),
                    "❌ Session expired. Please type MENU to start over.");
            session.setState(UserSession.ConversationState.INITIAL);
            return;
        }
        
        User user = userOpt.get();
        
        if (normalizedMessage.equals("menu")) {
            sendAuthenticatedMenu(session, user);
        } else if (normalizedMessage.equals("logout")) {
            handleLogout(session);
        } else if (normalizedMessage.equals("profile")) {
            showProfile(session, user);
        } else if (normalizedMessage.equals("help")) {
            sendHelpMessage(session);
        } else {
            infobipService.sendTextMessage(session.getPhoneNumber(),
                    "Available commands:\n" +
                    "• MENU - Show main menu\n" +
                    "• PROFILE - View your profile\n" +
                    "• LOGOUT - Sign out\n" +
                    "• HELP - Get help");
        }
    }
    
    private void sendAuthenticatedMenu(UserSession session, User user) {
        String roleEmoji = user.getRole() == User.UserRole.TUTOR ? "👨‍🏫" : "👨‍🎓";
        
        String menuMessage = String.format(
                "%s *%s Dashboard*\n\n" +
                "Hello, %s!\n\n" +
                "Available options:\n" +
                "• PROFILE - View your profile\n" +
                "• LOGOUT - Sign out\n" +
                "• HELP - Get help\n\n" +
                "_More features coming soon!_",
                roleEmoji,
                user.getRole().name(),
                user.getFullName()
        );
        
        infobipService.sendTextMessage(session.getPhoneNumber(), menuMessage);
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
                user.getCreatedAt().toLocalDate()
        );
        
        infobipService.sendTextMessage(session.getPhoneNumber(), profileMessage);
    }
    
    private void handleLogout(UserSession session) {
        infobipService.sendTextMessage(session.getPhoneNumber(),
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
        
        infobipService.sendTextMessage(session.getPhoneNumber(), helpMessage);
    }
}
