package com.jbilling.resource;


import com.fasterxml.jackson.databind.JsonNode;
import com.jbilling.service.WookieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("wookie")
public class WookieResource {

    private static final Logger emailLogger = LoggerFactory.getLogger("emailLogger");
    private static final Logger wookieLogger = LoggerFactory.getLogger("wookieLogger");
    private final WookieService wookieService;

    @Autowired
    public WookieResource(WookieService wookieService) {
        this.wookieService = wookieService;
    }

    @GetMapping("/email")
    public String test() {
        return "Hello From Wookie External API";
    }


    @PostMapping("/email")
    public ResponseEntity<String> printNotification(@RequestBody JsonNode message) {
        ResponseEntity<String> responseEntity;
        String template = message.get("type").asText();
        String emailTemplate;
        try {
            switch (template) {
                case "Usage Pool":
                    emailTemplate = wookieService.usagesPoolTemplate(message);
                    break;
                case "Optus Mur":
                    emailTemplate = wookieService.optusMurTemplate(message);
                    break;
                case "Credit Pool":
                    emailTemplate = wookieService.creditPoolTemplate(message);
                    break;
                default:
                    emailTemplate = "No Email Template found for the type " + template;
                    wookieLogger.error("No Email Template found for the type {}", template);
            }
            if (!emailTemplate.contains("No Email Template found"))
                emailLogger.info(emailTemplate);
            responseEntity = new ResponseEntity<>(emailTemplate, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            responseEntity = new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }
}

