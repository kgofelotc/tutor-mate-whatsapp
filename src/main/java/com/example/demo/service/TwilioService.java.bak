package com.example.demo.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class TwilioService {
    
    private static final Logger logger = LoggerFactory.getLogger(TwilioService.class);
    
    @Value("${twilio.account.sid}")
    private String accountSid;
    
    @Value("${twilio.auth.token}")
    private String authToken;
    
    @Value("${twilio.whatsapp.from}")
    private String from;
    
    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
        logger.info("Twilio API initialized with Account SID: {}", accountSid);
    }
    
    /**
     * Send a text message to a WhatsApp number
     */
    public void sendTextMessage(String to, String messageText) {
        try {
            // Ensure numbers have whatsapp: prefix
            String fromNumber = from.startsWith("whatsapp:") ? from : "whatsapp:" + from;
            String toNumber = to.startsWith("whatsapp:") ? to : "whatsapp:" + to;
            
            Message message = Message.creator(
                    new PhoneNumber(toNumber),
                    new PhoneNumber(fromNumber),
                    messageText)
                    .create();
            
            logger.info("Message sent successfully to {}: {}", to, message.getSid());
            
        } catch (Exception e) {
            logger.error("Error sending WhatsApp message to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send WhatsApp message: " + e.getMessage(), e);
        }
    }
    
    /**
     * Send a message with clickable reply buttons (up to 3 buttons)
     * Users can click buttons instead of typing responses
     */
    public void sendMessageWithButtons(String to, String messageText, List<String> buttons) {
        try {
            if (buttons == null || buttons.isEmpty() || buttons.size() > 3) {
                throw new IllegalArgumentException("Must provide 1-3 buttons");
            }
            
            // Build button options - each button becomes a numbered option in the message
            StringBuilder message = new StringBuilder(messageText);
            message.append("\n\n");
            
            for (int i = 0; i < buttons.size(); i++) {
                String emoji = getEmojiForNumber(i + 1);
                message.append(emoji).append(" ").append(buttons.get(i)).append("\n");
            }
            
            message.append("\n_Reply with the number or click to respond_");
            
            sendTextMessage(to, message.toString());
            
        } catch (Exception e) {
            logger.error("Error sending WhatsApp message with buttons to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send WhatsApp message with buttons: " + e.getMessage(), e);
        }
    }
    
    /**
     * Send an interactive list message with multiple options
     * Great for displaying tutors, subjects, available times, etc.
     */
    public void sendListMessage(String to, String headerText, String bodyText, List<ListOption> options) {
        try {
            if (options == null || options.isEmpty()) {
                throw new IllegalArgumentException("Must provide at least one option");
            }
            
            StringBuilder message = new StringBuilder();
            
            // Add header if provided
            if (headerText != null && !headerText.isEmpty()) {
                message.append("*").append(headerText).append("*\n\n");
            }
            
            // Add body text
            message.append(bodyText).append("\n\n");
            
            // Add numbered options
            for (int i = 0; i < options.size(); i++) {
                ListOption option = options.get(i);
                String emoji = getEmojiForNumber(i + 1);
                message.append(emoji).append(" *").append(option.getTitle()).append("*");
                
                if (option.getDescription() != null && !option.getDescription().isEmpty()) {
                    message.append("\n   ").append(option.getDescription());
                }
                message.append("\n\n");
            }
            
            message.append("_Reply with the number to select_");
            
            sendTextMessage(to, message.toString());
            
        } catch (Exception e) {
            logger.error("Error sending list message to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send list message: " + e.getMessage(), e);
        }
    }
    
    /**
     * Send a message with a clickable link button
     */
    public void sendMessageWithLink(String to, String messageText, String linkUrl, String linkText) {
        try {
            String message = messageText + "\n\n🔗 " + linkText + ": " + linkUrl;
            sendTextMessage(to, message);
            
        } catch (Exception e) {
            logger.error("Error sending message with link to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send message with link: " + e.getMessage(), e);
        }
    }
    
    /**
     * Send a confirmation message with Yes/No buttons
     */
    public void sendConfirmationMessage(String to, String messageText) {
        List<String> buttons = new ArrayList<>();
        buttons.add("✅ Yes, Confirm");
        buttons.add("❌ No, Cancel");
        sendMessageWithButtons(to, messageText, buttons);
    }
    
    /**
     * Helper to get emoji for numbers
     */
    private String getEmojiForNumber(int number) {
        switch (number) {
            case 1: return "1️⃣";
            case 2: return "2️⃣";
            case 3: return "3️⃣";
            case 4: return "4️⃣";
            case 5: return "5️⃣";
            case 6: return "6️⃣";
            case 7: return "7️⃣";
            case 8: return "8️⃣";
            case 9: return "9️⃣";
            case 10: return "🔟";
            default: return "▪️";
        }
    }
    
    /**
     * Helper class for list options
     */
    public static class ListOption {
        private String id;
        private String title;
        private String description;
        
        public ListOption(String id, String title) {
            this.id = id;
            this.title = title;
        }
        
        public ListOption(String id, String title, String description) {
            this.id = id;
            this.title = title;
            this.description = description;
        }
        
        public String getId() {
            return id;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
