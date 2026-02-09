package com.example.demo.controller;

import com.example.demo.service.InfobipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppTestController {
    
    @Autowired
    private InfobipService infobipService;
    
    @PostMapping("/send-text")
    public ResponseEntity<?> sendTextMessage(@RequestBody Map<String, String> request) {
        try {
            String to = request.get("to");
            String message = request.get("message");
            
            if (to == null || message == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing 'to' or 'message' field"));
            }
            
            infobipService.sendTextMessage(to, message);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Message sent to " + to
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
}
