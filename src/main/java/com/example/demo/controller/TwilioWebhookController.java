package com.example.demo.controller;

import com.example.demo.service.ConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/twilio")
public class TwilioWebhookController {
    
    private static final Logger logger = LoggerFactory.getLogger(TwilioWebhookController.class);
    
    @Autowired
    private ConversationService conversationService;
    
    /**
     * Webhook endpoint to receive incoming WhatsApp messages from Twilio
     * Twilio sends data as application/x-www-form-urlencoded
     */
    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> handleIncomingMessage(@RequestParam Map<String, String> payload) {
        try {
            logger.info("Received webhook from Twilio: {}", payload);
            
            String from = payload.get("From"); // Format: whatsapp:+1234567890
            String body = payload.get("Body");
            String messageSid = payload.get("MessageSid");
            
            if (from != null && body != null) {
                // Remove whatsapp: prefix if present
                String phoneNumber = from.replace("whatsapp:", "").replace("+", "");
                
                logger.info("Processing message from {}: {}", phoneNumber, body);
                
                // Process the message through the conversation service
                conversationService.processMessage(phoneNumber, body);
            }
            
            // Twilio expects 200 OK with TwiML or empty response
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_XML)
                    .body("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response></Response>");
            
        } catch (Exception e) {
            logger.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_XML)
                    .body("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response></Response>");
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/webhook")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Twilio webhook endpoint is active");
    }
    
    /**
     * Status callback endpoint for message delivery status
     */
    @PostMapping(value = "/status", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> handleStatusCallback(@RequestParam Map<String, String> payload) {
        try {
            logger.info("Received status callback from Twilio: {}", payload);
            
            String messageSid = payload.get("MessageSid");
            String messageStatus = payload.get("MessageStatus");
            
            logger.info("Message {} status: {}", messageSid, messageStatus);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_XML)
                    .body("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response></Response>");
            
        } catch (Exception e) {
            logger.error("Error processing status callback: {}", e.getMessage(), e);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_XML)
                    .body("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response></Response>");
        }
    }
}
