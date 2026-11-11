package com.photography.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // 1. Initial Admin Notification (When booking is created)
    public void sendAdminBookingNotification(String adminEmail, String clientName, String eventDate, String contact, String eventType, String clientEmail) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(adminEmail);
            helper.setSubject("📸 New Booking Request - Lakshmi Digital Studios");

            String htmlContent = "<div style=\"font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px; background-color: #f9f9f9;\">" +
                    "<h2 style=\"color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 8px;\">New Booking Request</h2>" +
                    "<p style=\"font-size: 16px; color: #333;\">Hello Rayagiri NagaBabu,</p>" +
                    "<p style=\"font-size: 16px; color: #333;\">You have received a new booking request:</p>" +
                    "<table style=\"width: 100%; margin-top: 15px; border-collapse: collapse;\">" +
                    "  <tr><td style=\"padding: 8px; font-weight: bold; color: #555;\">Client Name:</td><td style=\"padding: 8px; color: #222;\">" + clientName + "</td></tr>" +
                    "  <tr style=\"background-color: #fff;\"><td style=\"padding: 8px; font-weight: bold; color: #555;\">Event Type:</td><td style=\"padding: 8px; color: #222;\">" + (eventType != null ? eventType : "General") + "</td></tr>" +
                    "  <tr><td style=\"padding: 8px; font-weight: bold; color: #555;\">Event Date:</td><td style=\"padding: 8px; color: #222;\">" + eventDate + "</td></tr>" +
                    "  <tr style=\"background-color: #fff;\"><td style=\"padding: 8px; font-weight: bold; color: #555;\">Contact:</td><td style=\"padding: 8px;\"><a href=\"tel:" + contact + "\" style=\"color: #3498db; text-decoration: none; font-weight: bold;\">" + contact + " 📞</a></td></tr>" +
                    "  <tr><td style=\"padding: 8px; font-weight: bold; color: #555;\">Email:</td><td style=\"padding: 8px;\"><a href=\"mailto:" + clientEmail + "\" style=\"color: #3498db; text-decoration: none;\">" + clientEmail + "</a></td></tr>" +
                    "</table>" +
                    "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.err.println("Failed to send admin email: " + e.getMessage());
        }
    }

    // 2. Initial Customer Pending Confirmation (1st Email to Customer)
    public void sendCustomerPendingEmail(String customerEmail, String clientName, String eventDate, String eventType) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(customerEmail);
            helper.setSubject("Booking Request Received! - Lakshmi Digital Studios");

            String htmlContent = "<div style=\"font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px; background-color: #f9f9f9;\">" +
                    "<h2 style=\"color: #2980b9; border-bottom: 2px solid #3498db; padding-bottom: 8px;\">Booking Request Received</h2>" +
                    "<p style=\"font-size: 16px; color: #333;\">Dear <strong>" + clientName + "</strong>,</p>" +
                    "<p style=\"font-size: 16px; color: #333;\">Thank you for choosing <strong>Lakshmi Digital Studios</strong>. Your booking request has been submitted and is currently <strong>Pending</strong> review.</p>" +
                    "<ul style=\"font-size: 15px; color: #555; line-height: 1.6;\">" +
                    "  <li><strong>Event Type:</strong> " + (eventType != null ? eventType : "General") + "</li>" +
                    "  <li><strong>Event Date:</strong> " + eventDate + "</li>" +
                    "</ul>" +
                    "<p style=\"margin-top: 25px; font-size: 14px; color: #7f8c8d;\">We will notify you once your booking is accepted.</p>" +
                    "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.err.println("Failed to send pending email: " + e.getMessage());
        }
    }

    // 3. Status Update Email (2nd Email to Customer: Accepted or Rejected)
    public void sendCustomerStatusUpdateEmail(String customerEmail, String clientName, String eventDate, String status) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            boolean isConfirmed = "Confirmed".equalsIgnoreCase(status);
            String statusColor = isConfirmed ? "#27ae60" : "#c0392b";
            String statusText = isConfirmed ? "Confirmed & Accepted!" : "Declined / Cancelled";

            helper.setTo(customerEmail);
            helper.setSubject("Booking Update: " + statusText + " - Lakshmi Digital Studios");

            String htmlContent = "<div style=\"font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px; background-color: #f9f9f9;\">" +
                    "<h2 style=\"color: " + statusColor + "; border-bottom: 2px solid " + statusColor + "; padding-bottom: 8px;\">Booking Status Update</h2>" +
                    "<p style=\"font-size: 16px; color: #333;\">Dear <strong>" + clientName + "</strong>,</p>" +
                    "<p style=\"font-size: 16px; color: #333;\">Your booking for date <strong>" + eventDate + "</strong> has been updated by the admin.</p>" +
                    "<p style=\"font-size: 18px; color: " + statusColor + "; font-weight: bold; margin-top: 15px;\">Status: " + status.toUpperCase() + "</p>" +
                    (isConfirmed ? "<p style=\"font-size: 15px; color: #333;\">We are looking forward to capturing your event!</p>" : "<p style=\"font-size: 15px; color: #333;\">Unfortunately, we are unavailable on this date. Feel free to reach out for alternative slots.</p>") +
                    "<p style=\"margin-top: 25px; font-size: 14px; color: #2c3e50;\">Best Regards,<br><strong>Lakshmi Digital Studios</strong></p>" +
                    "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.err.println("Failed to send status update email: " + e.getMessage());
        }
    }
}