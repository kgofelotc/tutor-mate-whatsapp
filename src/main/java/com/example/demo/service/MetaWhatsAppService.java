package com.example.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Service for sending WhatsApp messages using Meta's WhatsApp Business Platform
 * API
 * This replaces TwilioService with Meta's Cloud API
 */
@Service
public class MetaWhatsAppService {

    private static final Logger logger = LoggerFactory.getLogger(MetaWhatsAppService.class);

    @Value("${meta.whatsapp.access.token}")
    private String accessToken;

    @Value("${meta.whatsapp.phone.number.id}")
    private String phoneNumberId;

    @Value("${meta.whatsapp.business.account.id}")
    private String businessAccountId;

    private static final String META_API_VERSION = "v21.0";
    private String apiBaseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MetaWhatsAppService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        this.apiBaseUrl = String.format("https://graph.facebook.com/%s/%s", META_API_VERSION, phoneNumberId);
        logger.info("Meta WhatsApp API initialized with Phone Number ID: {}", phoneNumberId);
    }

    /**
     * Send a text message to a WhatsApp number
     */
    public void sendTextMessage(String to, String messageText) {
        try {
            // Remove whatsapp: prefix if present and any + signs
            String toNumber = to.replace("whatsapp:", "").replace("+", "");

            Map<String, Object> payload = new HashMap<>();
            payload.put("messaging_product", "whatsapp");
            payload.put("recipient_type", "individual");
            payload.put("to", toNumber);
            payload.put("type", "text");

            Map<String, String> text = new HashMap<>();
            text.put("preview_url", "false");
            text.put("body", messageText);
            payload.put("text", text);

            sendRequest(payload);
            logger.info("Message sent successfully to {}", to);

        } catch (Exception e) {
            logger.error("Error sending WhatsApp message to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send WhatsApp message: " + e.getMessage(), e);
        }
    }

    /**
     * Send a message with interactive reply buttons (up to 3 buttons)
     * Meta's Interactive Buttons feature
     */
    public void sendMessageWithButtons(String to, String messageText, List<String> buttons) {
        try {
            if (buttons == null || buttons.isEmpty() || buttons.size() > 3) {
                throw new IllegalArgumentException("Must provide 1-3 buttons");
            }

            String toNumber = to.replace("whatsapp:", "").replace("+", "");

            Map<String, Object> payload = new HashMap<>();
            payload.put("messaging_product", "whatsapp");
            payload.put("recipient_type", "individual");
            payload.put("to", toNumber);
            payload.put("type", "interactive");

            Map<String, Object> interactive = new HashMap<>();
            interactive.put("type", "button");

            // Body
            Map<String, String> body = new HashMap<>();
            body.put("text", messageText);
            interactive.put("body", body);

            // Action with buttons
            Map<String, Object> action = new HashMap<>();
            List<Map<String, Object>> buttonList = new ArrayList<>();

            for (int i = 0; i < buttons.size(); i++) {
                Map<String, Object> button = new HashMap<>();
                button.put("type", "reply");

                Map<String, String> reply = new HashMap<>();
                reply.put("id", "button_" + (i + 1));
                reply.put("title", truncateText(buttons.get(i), 20)); // Max 20 chars for button title
                button.put("reply", reply);

                buttonList.add(button);
            }

            action.put("buttons", buttonList);
            interactive.put("action", action);

            payload.put("interactive", interactive);

            sendRequest(payload);
            logger.info("Interactive button message sent successfully to {}", to);

        } catch (Exception e) {
            logger.error("Error sending WhatsApp message with buttons to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send WhatsApp message with buttons: " + e.getMessage(), e);
        }
    }

    /**
     * Send an interactive list message with multiple options
     * Meta's Interactive List feature
     */
    public void sendListMessage(String to, String headerText, String bodyText, List<ListOption> options) {
        try {
            if (options == null || options.isEmpty() || options.size() > 10) {
                throw new IllegalArgumentException("Must provide 1-10 options for a list");
            }

            String toNumber = to.replace("whatsapp:", "").replace("+", "");

            Map<String, Object> payload = new HashMap<>();
            payload.put("messaging_product", "whatsapp");
            payload.put("recipient_type", "individual");
            payload.put("to", toNumber);
            payload.put("type", "interactive");

            Map<String, Object> interactive = new HashMap<>();
            interactive.put("type", "list");

            // Header (optional)
            if (headerText != null && !headerText.isEmpty()) {
                Map<String, String> header = new HashMap<>();
                header.put("type", "text");
                header.put("text", truncateText(headerText, 60)); // Max 60 chars
                interactive.put("header", header);
            }

            // Body
            Map<String, String> body = new HashMap<>();
            body.put("text", truncateText(bodyText, 1024)); // Max 1024 chars
            interactive.put("body", body);

            // Action with list
            Map<String, Object> action = new HashMap<>();
            action.put("button", "View Options"); // The button text that opens the list

            List<Map<String, Object>> sections = new ArrayList<>();
            Map<String, Object> section = new HashMap<>();

            List<Map<String, Object>> rows = new ArrayList<>();
            for (ListOption option : options) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", option.getId());
                row.put("title", truncateText(option.getTitle(), 24)); // Max 24 chars

                if (option.getDescription() != null && !option.getDescription().isEmpty()) {
                    row.put("description", truncateText(option.getDescription(), 72)); // Max 72 chars
                }

                rows.add(row);
            }

            section.put("rows", rows);
            sections.add(section);

            action.put("sections", sections);
            interactive.put("action", action);

            payload.put("interactive", interactive);

            sendRequest(payload);
            logger.info("Interactive list message sent successfully to {}", to);

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
            // For link buttons, we can use text with URL or interactive CTA button
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
     * Send a template message (requires pre-approved templates)
     * Use this for notifications, reminders, etc.
     */
    public void sendTemplateMessage(String to, String templateName, String languageCode, List<String> parameters) {
        try {
            String toNumber = to.replace("whatsapp:", "").replace("+", "");

            Map<String, Object> payload = new HashMap<>();
            payload.put("messaging_product", "whatsapp");
            payload.put("to", toNumber);
            payload.put("type", "template");

            Map<String, Object> template = new HashMap<>();
            template.put("name", templateName);

            Map<String, String> language = new HashMap<>();
            language.put("code", languageCode != null ? languageCode : "en");
            template.put("language", language);

            if (parameters != null && !parameters.isEmpty()) {
                List<Map<String, Object>> components = new ArrayList<>();
                Map<String, Object> component = new HashMap<>();
                component.put("type", "body");

                List<Map<String, String>> params = new ArrayList<>();
                for (String param : parameters) {
                    Map<String, String> p = new HashMap<>();
                    p.put("type", "text");
                    p.put("text", param);
                    params.add(p);
                }
                component.put("parameters", params);
                components.add(component);
                template.put("components", components);
            }

            payload.put("template", template);

            sendRequest(payload);
            logger.info("Template message '{}' sent successfully to {}", templateName, to);

        } catch (Exception e) {
            logger.error("Error sending template message to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send template message: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to send HTTP request to Meta API
     */
    private void sendRequest(Map<String, Object> payload) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        String jsonPayload = objectMapper.writeValueAsString(payload);
        logger.debug("Sending request to Meta API: {}", jsonPayload);

        HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);

        String url = apiBaseUrl + "/messages";
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            logger.debug("Meta API response: {}", response.getBody());
        } else {
            throw new RuntimeException("Meta API returned error: " + response.getStatusCode());
        }
    }

    /**
     * Helper to truncate text to max length
     */
    private String truncateText(String text, int maxLength) {
        if (text == null)
            return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Helper to get emoji for numbers
     */
    private String getEmojiForNumber(int number) {
        switch (number) {
            case 1:
                return "1️⃣";
            case 2:
                return "2️⃣";
            case 3:
                return "3️⃣";
            case 4:
                return "4️⃣";
            case 5:
                return "5️⃣";
            case 6:
                return "6️⃣";
            case 7:
                return "7️⃣";
            case 8:
                return "8️⃣";
            case 9:
                return "9️⃣";
            case 10:
                return "🔟";
            default:
                return "▪️";
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
