package com.example.demo.controller;

import com.example.demo.service.MetaWhatsAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Test controller for Meta WhatsApp integration
 * Use these endpoints to verify your Meta setup is working
 */
@RestController
@RequestMapping("/api/meta/test")
public class MetaTestController {

    @Autowired
    private MetaWhatsAppService metaWhatsAppService;

    /**
     * Send a simple test message
     * GET /api/meta/test/send?to=27821234567&message=Hello
     */
    @GetMapping("/send")
    public ResponseEntity<Map<String, String>> sendTestMessage(
            @RequestParam String to,
            @RequestParam(defaultValue = "Hello from Meta WhatsApp!") String message) {

        try {
            metaWhatsAppService.sendTextMessage(to, message);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Message sent to " + to);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Send a test message with interactive buttons
     * GET /api/meta/test/buttons?to=27821234567
     */
    @GetMapping("/buttons")
    public ResponseEntity<Map<String, String>> sendTestButtons(@RequestParam String to) {
        try {
            metaWhatsAppService.sendMessageWithButtons(
                    to,
                    "Please choose one of the following options:",
                    Arrays.asList("Option 1", "Option 2", "Option 3"));

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Button message sent to " + to);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Send a test message with interactive list
     * GET /api/meta/test/list?to=27821234567
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, String>> sendTestList(@RequestParam String to) {
        try {
            java.util.List<MetaWhatsAppService.ListOption> options = Arrays.asList(
                    new MetaWhatsAppService.ListOption("1", "Mathematics", "Learn math concepts"),
                    new MetaWhatsAppService.ListOption("2", "Science", "Explore science topics"),
                    new MetaWhatsAppService.ListOption("3", "English", "Improve language skills"));

            metaWhatsAppService.sendListMessage(
                    to,
                    "Available Subjects",
                    "Choose a subject you'd like to learn:",
                    options);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "List message sent to " + to);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Health check endpoint
     * GET /api/meta/test/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "Meta WhatsApp Service");
        response.put("message", "Service is running and ready to send messages");
        return ResponseEntity.ok(response);
    }
}
