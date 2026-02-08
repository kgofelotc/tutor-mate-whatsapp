package com.example.demo.service;

import com.infobip.ApiClient;
import com.infobip.ApiException;
import com.infobip.api.WhatsAppApi;
import com.infobip.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;

@Service
public class InfobipService {
    
    private static final Logger logger = LoggerFactory.getLogger(InfobipService.class);
    
    @Value("${infobip.api.key}")
    private String apiKey;
    
    @Value("${infobip.base.url}")
    private String baseUrl;
    
    @Value("${infobip.whatsapp.from}")
    private String from;
    
    private WhatsAppApi whatsAppApi;
    
    @PostConstruct
    public void init() {
        ApiClient apiClient = ApiClient.forApiKey(apiKey)
                .withBaseUrl(baseUrl)
                .build();
        this.whatsAppApi = new WhatsAppApi(apiClient);
        logger.info("Infobip WhatsApp API initialized with base URL: {}", baseUrl);
    }
    
    /**
     * Send a text message to a WhatsApp number
     */
    public void sendTextMessage(String to, String messageText) {
        try {
            WhatsAppTextMessage textMessage = new WhatsAppTextMessage()
                    .from(from)
                    .to(to)
                    .content(new WhatsAppTextContent().text(messageText));
            
            WhatsAppBulkMessage bulkMessage = new WhatsAppBulkMessage()
                    .messages(Collections.singletonList(textMessage));
            
            WhatsAppBulkMessageInfo response = whatsAppApi.sendWhatsAppTextMessage(bulkMessage).execute();
            
            logger.info("Message sent successfully to {}: {}", to, response.getMessages().get(0).getMessageId());
            
        } catch (ApiException e) {
            logger.error("Error sending WhatsApp message to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send WhatsApp message: " + e.getMessage(), e);
        }
    }
    
    /**
     * Send a text message with interactive buttons
     */
    public void sendButtonMessage(String to, String bodyText, String footerText, String... buttonTexts) {
        try {
            WhatsAppInteractiveButtonsMessage buttonMessage = new WhatsAppInteractiveButtonsMessage()
                    .from(from)
                    .to(to)
                    .content(new WhatsAppInteractiveButtonsContent()
                            .body(new WhatsAppInteractiveBodyContent().text(bodyText))
                            .action(createButtonAction(buttonTexts)));
            
            if (footerText != null && !footerText.isEmpty()) {
                buttonMessage.getContent().footer(new WhatsAppInteractiveFooterContent().text(footerText));
            }
            
            WhatsAppSingleMessageInfo response = whatsAppApi.sendWhatsAppInteractiveButtonsMessage(buttonMessage).execute();
            
            logger.info("Button message sent successfully to {}: {}", to, response.getMessageId());
            
        } catch (ApiException e) {
            logger.error("Error sending WhatsApp button message to {}: {}", to, e.getMessage(), e);
            // Fallback to text message if buttons not supported
            sendTextMessage(to, bodyText + "\n\n" + String.join("\n", buttonTexts));
        }
    }
    
    /**
     * Send a list message with selectable options
     */
    public void sendListMessage(String to, String bodyText, String buttonText, String sectionTitle, String... listItems) {
        try {
            WhatsAppInteractiveListMessage listMessage = new WhatsAppInteractiveListMessage()
                    .from(from)
                    .to(to)
                    .content(new WhatsAppInteractiveListContent()
                            .body(new WhatsAppInteractiveBodyContent().text(bodyText))
                            .action(new WhatsAppInteractiveListActionContent()
                                    .title(buttonText)
                                    .sections(Collections.singletonList(
                                            createListSection(sectionTitle, listItems)
                                    ))));
            
            WhatsAppSingleMessageInfo response = whatsAppApi.sendWhatsAppInteractiveListMessage(listMessage).execute();
            
            logger.info("List message sent successfully to {}: {}", to, response.getMessageId());
            
        } catch (ApiException e) {
            logger.error("Error sending WhatsApp list message to {}: {}", to, e.getMessage(), e);
            // Fallback to text message
            StringBuilder fallbackMessage = new StringBuilder(bodyText + "\n\n");
            for (int i = 0; i < listItems.length; i++) {
                fallbackMessage.append((i + 1)).append(". ").append(listItems[i]).append("\n");
            }
            sendTextMessage(to, fallbackMessage.toString());
        }
    }
    
    private WhatsAppInteractiveButtonsActionContent createButtonAction(String... buttonTexts) {
        WhatsAppInteractiveButtonsActionContent action = new WhatsAppInteractiveButtonsActionContent();
        
        for (int i = 0; i < Math.min(buttonTexts.length, 3); i++) { // WhatsApp supports max 3 buttons
            action.addButtonsItem(new WhatsAppInteractiveReplyButtonContent()
                    .id("btn_" + i)
                    .title(buttonTexts[i]));
        }
        
        return action;
    }
    
    private WhatsAppInteractiveListSectionContent createListSection(String title, String... items) {
        WhatsAppInteractiveListSectionContent section = new WhatsAppInteractiveListSectionContent()
                .title(title);
        
        for (int i = 0; i < Math.min(items.length, 10); i++) { // WhatsApp supports max 10 items per section
            section.addRowsItem(new WhatsAppInteractiveRowContent()
                    .id("item_" + i)
                    .title(items[i]));
        }
        
        return section;
    }
}
