package com.ptit.miniproject.service;

import com.ptit.miniproject.entity.EmailDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    public String sendEmail(EmailDetails emailDetails) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(sender);
            message.setTo(emailDetails.getRecipient());
            message.setText(emailDetails.getMsgBody());
            message.setSubject(emailDetails.getSubject());

            javaMailSender.send(message);
            return "Mail Sent Successfully...";
        }
        catch (Exception e) {
            return "Error While Sending Mail";
        }
    }
}
