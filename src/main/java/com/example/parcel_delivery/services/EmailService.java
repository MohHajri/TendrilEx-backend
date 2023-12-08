package com.example.parcel_delivery.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.example.parcel_delivery.models.enums.ParcelStatus;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    public void sendHtmlEmail(String to, String subject, String name, ParcelStatus status, Integer transactionCode) throws MessagingException {
        // Prepare the evaluation context
        final Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("status", status);
        ctx.setVariable("transactionCode", transactionCode);

        // Prepare message using a Spring helper
        String process = templateEngine.process("emailTemplate", ctx);
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
        message.setSubject(subject);
        message.setFrom("yehajri@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(process, true /* isHtml */);

        // Send mail
        mailSender.send(mimeMessage);


    }


    
}
