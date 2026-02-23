package com.example.demo.controller;

import com.example.demo.service.ConversationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Webhook controller for receiving WhatsApp messages from Meta's WhatsApp
 * Business Platform
 * Handles both webhook verification and incoming message events
 */
@RestController
@RequestMapping("/meta/webhook")
public class MetaWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(MetaWebhookController.class);

    @Autowired
    private ConversationService conversationService;

    @Value("${meta.webhook.verify.token}")
    private String verifyToken;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Webhook verification endpoint (GET request)
     * Meta will send a GET request to verify the webhook URL during setup
     * 
     * Expected query parameters:
     * - hub.mode: "subscribe"
     * - hub.verify_token: your verification token
     * - hub.challenge: random string to echo back
     */
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        logger.info("Webhook verification request received - Mode: {}, Token: {}", mode, token);

        // Verify the token and mode
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            logger.info("Webhook verified successfully");
            return ResponseEntity.ok(challenge);
        } else {
            logger.error("Webhook verification failed - Invalid token or mode");
            return ResponseEntity.status(403).body("Forbidden");
        }
    }

    /**
     * Webhook endpoint to receive incoming WhatsApp messages and events (POST
     * request)
     * Meta sends data as JSON
     * 
     * Example payload structure:
     * {
     * "object": "whatsapp_business_account",
     * "entry": [{
     * "id": "WHATSAPP_BUSINESS_ACCOUNT_ID",
     * "changes": [{
     * "value": {
     * "messaging_product": "whatsapp",
     * "metadata": {
     * "display_phone_number": "PHONE_NUMBER",
     * "phone_number_id": "PHONE_NUMBER_ID"
     * },
     * "contacts": [{
     * "profile": {
     * "name": "NAME"
     * },
     * "wa_id": "PHONE_NUMBER"
     * }],
     * "messages": [{
     * "from": "PHONE_NUMBER",
     * "id": "MESSAGE_ID",
     * "timestamp": "TIMESTAMP",
     * "type": "text|interactive|button|image|...",
     * "text": {
     * "body": "MESSAGE_CONTENT"
     * },
     * "interactive": {
     * "type": "button_reply|list_reply",
     * "button_reply": {
     * "id": "button_1",
     * "title": "Button Title"
     * },
     * "list_reply": {
     * "id": "option_1",
     * "title": "Option Title"
     * }
     * }
     * }]
     * },
     * "field": "messages"
     * }]
     * }]
     * }
     */
    @PostMapping
    public ResponseEntity<String> handleIncomingMessage(@RequestBody String payload) {
        try {
            logger.info("Received webhook from Meta: {}", payload);

            JsonNode rootNode = objectMapper.readTree(payload);

            // Check if this is a WhatsApp Business Account event
            String object = rootNode.path("object").asText();
            if (!"whatsapp_business_account".equals(object)) {
                logger.warn("Received non-WhatsApp event, ignoring");
                return ResponseEntity.ok("OK");
            }

            // Process each entry
            JsonNode entries = rootNode.path("entry");
            for (JsonNode entry : entries) {
                JsonNode changes = entry.path("changes");

                for (JsonNode change : changes) {
                    String field = change.path("field").asText();

                    // Only process message events
                    if ("messages".equals(field)) {
                        JsonNode value = change.path("value");
                        processMessageEvent(value);
                    } else if ("message_status".equals(field)) {
                        // Handle message status updates (sent, delivered, read, failed)
                        logger.debug("Received message status update");
                    }
                }
            }

            // Meta expects 200 OK response
            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            logger.error("Error processing webhook: {}", e.getMessage(), e);
            // Return 200 OK even on error to prevent Meta from retrying
            return ResponseEntity.ok("OK");
        }
    }

    /**
     * Process individual message events
     */
    private void processMessageEvent(JsonNode value) {
        try {
            JsonNode metadata = value.path("metadata");
            String phoneNumberId = metadata.path("phone_number_id").asText();

            JsonNode contacts = value.path("contacts");
            JsonNode messages = value.path("messages");

            // Process each message
            for (JsonNode message : messages) {
                String from = message.path("from").asText(); // Sender's phone number
                String messageId = message.path("id").asText();
                String timestamp = message.path("timestamp").asText();
                String messageType = message.path("type").asText();

                String messageText = null;

                // Extract message content based on type
                switch (messageType) {
                    case "text":
                        messageText = message.path("text").path("body").asText();
                        break;

                    case "interactive":
                        // Handle interactive message responses (button clicks, list selections)
                        JsonNode interactive = message.path("interactive");
                        String interactiveType = interactive.path("type").asText();

                        if ("button_reply".equals(interactiveType)) {
                            // User clicked a button
                            String buttonId = interactive.path("button_reply").path("id").asText();
                            String buttonTitle = interactive.path("button_reply").path("title").asText();
                            messageText = buttonTitle; // Use button title as message
                            logger.info("Button clicked - ID: {}, Title: {}", buttonId, buttonTitle);

                        } else if ("list_reply".equals(interactiveType)) {
                            // User selected from a list
                            String listId = interactive.path("list_reply").path("id").asText();
                            String listTitle = interactive.path("list_reply").path("title").asText();
                            messageText = listTitle; // Use list option title as message
                            logger.info("List option selected - ID: {}, Title: {}", listId, listTitle);
                        }
                        break;

                    case "button":
                        // Handle quick reply button responses
                        messageText = message.path("button").path("text").asText();
                        break;

                    case "image":
                        logger.info("Received image message from {}", from);
                        messageText = "[Image received]";
                        break;

                    case "document":
                        logger.info("Received document message from {}", from);
                        messageText = "[Document received]";
                        break;

                    case "audio":
                        logger.info("Received audio message from {}", from);
                        messageText = "[Audio received]";
                        break;

                    case "video":
                        logger.info("Received video message from {}", from);
                        messageText = "[Video received]";
                        break;

                    case "location":
                        logger.info("Received location message from {}", from);
                        messageText = "[Location received]";
                        break;

                    default:
                        logger.warn("Unsupported message type: {}", messageType);
                        messageText = "[Unsupported message type]";
                }

                if (messageText != null && !messageText.isEmpty()) {
                    logger.info("Processing message from {}: {}", from, messageText);

                    // Remove any + prefix from phone number
                    String phoneNumber = from.replace("+", "");

                    // Process the message through the conversation service
                    conversationService.processMessage(phoneNumber, messageText);
                } else {
                    logger.warn("No message text extracted from message type: {}", messageType);
                }
            }

        } catch (Exception e) {
            logger.error("Error processing message event: {}", e.getMessage(), e);
        }
    }
}
