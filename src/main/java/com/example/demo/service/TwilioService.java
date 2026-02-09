package com.example.demo.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

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
}
