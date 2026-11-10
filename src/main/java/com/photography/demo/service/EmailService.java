package com.photography.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendBookingNotification(String toEmail, String clientName, String eventDate, String contact) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("New Booking Received - Lakshmi Digital Studios");
        message.setText("Hello Admin,\n\nYou have a new booking request!\n\n" +
                "Client Name: " + clientName + "\n" +
                "Contact: " + contact + "\n" +
                "Event Date: " + eventDate + "\n\n" +
                "Please check your admin dashboard to confirm or reject.");

        mailSender.send(message);
    }
}