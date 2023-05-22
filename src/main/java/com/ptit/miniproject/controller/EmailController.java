package com.ptit.miniproject.controller;

import com.ptit.miniproject.entity.EmailDetails;
import com.ptit.miniproject.service.EmailSenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/email")
public class EmailController {

    private final EmailSenderService emailService;

    @PostMapping("/send-mail")
    public ResponseEntity<String> sendMail(@RequestBody EmailDetails emailDetails) {
        String status = emailService.sendEmail(emailDetails);
        return ResponseEntity.status(HttpStatus.OK)
                .body(status);
    }
}
