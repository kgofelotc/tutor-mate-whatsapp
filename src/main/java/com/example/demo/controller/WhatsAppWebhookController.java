package com.example.demo.controller;

import com.example.demo.dto.InfobipWebhookRequest;
import com.example.demo.service.ConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/infobip")
public class WhatsAppWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppWebhookController.class);

    @Autowired
    private ConversationService conversationService;

    /**
     * Webhook endpoint to receive incoming WhatsApp messages from Infobip
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleIncomingMessage(@RequestBody InfobipWebhookRequest request) {
        try {
            logger.info("Received webhook from Infobip with {} message(s)", request.getMessageCount());

            if (request.getResults() != null && !request.getResults().isEmpty()) {
                for (InfobipWebhookRequest.Result result : request.getResults()) {
                    String fromNumber = result.getFrom();
                    String messageText = "";

                    if (result.getMessage() != null && result.getMessage().getText() != null) {
                        messageText = result.getMessage().getText();
                    }

                    logger.info("Processing message from {}: {}", fromNumber, messageText);

                    // Process the message through the conversation service
                    conversationService.processMessage(fromNumber, messageText);
                }
            }

            return ResponseEntity.ok("Message processed");

        } catch (Exception e) {
            logger.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing message: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/webhook")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Webhook endpoint is active");
    }
}
