package com.photography.demo.service;

import tools.jackson.databind.json.JsonMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private final JsonMapper jsonMapper =
            JsonMapper.builder().build();

    private final HttpClient httpClient;

    private final String apiUrl;
    private final String apiKey;
    private final String senderEmail;
    private final String senderName;
    private final String adminEmail;

    public EmailService(
            @Value("${brevo.api-url}") String apiUrl,
            @Value("${brevo.api-key}") String apiKey,
            @Value("${brevo.sender-email}") String senderEmail,
            @Value("${brevo.sender-name}") String senderName,
            @Value("${brevo.admin-email}") String adminEmail
    ) {

        this.apiUrl = apiUrl == null ? "" : apiUrl.trim();
        this.apiKey = apiKey;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.adminEmail = adminEmail;

        if (this.apiUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Brevo API URL is missing."
            );
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Brevo API key is missing."
            );
        }

        if (senderEmail == null || senderEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Brevo sender email is missing."
            );
        }

        if (adminEmail == null || adminEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Admin email is missing."
            );
        }

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    // =====================================================
    // 1. NEW BOOKING
    // Sends one notification to admin and one to customer.
    // =====================================================

    public void sendNewBookingEmails(
            String clientName,
            String eventDate,
            String contact,
            String eventType,
            String clientEmail
    ) {

        System.out.println("========== NEW BOOKING EMAILS ==========");
        System.out.println("Client: " + clientName);

        List<RuntimeException> failures = new ArrayList<>();

        // Admin notification
        String adminSubject =
                "New Booking Request - Lakshmi Digital Studios";

        String adminText = """
                New booking request received.

                Client Name: %s
                Event Type: %s
                Event Date: %s
                Contact: %s
                Customer Email: %s
                Status: Pending
                """.formatted(
                safe(clientName),
                safe(eventType),
                safe(eventDate),
                safe(contact),
                safe(clientEmail)
        );

        String adminHtml = """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial,sans-serif;color:#333;
                             line-height:1.6;padding:20px;">

                    <h2 style="color:#26304f;">
                        New Booking Request
                    </h2>

                    <p>You have received a new booking request.</p>

                    <table style="border-collapse:collapse;width:100%%;">
                        <tr>
                            <td><b>Client Name</b></td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td><b>Event Type</b></td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td><b>Event Date</b></td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td><b>Contact</b></td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td><b>Customer Email</b></td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td><b>Status</b></td>
                            <td>Pending</td>
                        </tr>
                    </table>

                    <p>Please review this booking request.</p>

                    <p>
                        <b>Lakshmi Digital Studios</b>
                    </p>

                </body>
                </html>
                """.formatted(
                escapeHtml(clientName),
                escapeHtml(eventType),
                escapeHtml(eventDate),
                escapeHtml(contact),
                escapeHtml(clientEmail)
        );

        try {
            String messageId = sendBrevoEmail(
                    adminEmail,
                    adminSubject,
                    adminText,
                    adminHtml
            );

            System.out.println(
                    "Admin notification accepted. Message ID: " + messageId
            );

        } catch (RuntimeException e) {

            System.err.println(
                    "Failed to send admin booking notification."
            );

            failures.add(e);
        }

        // Customer pending confirmation
        if (clientEmail != null && !clientEmail.isBlank()) {

            String customerSubject =
                    "Booking Request Received - Lakshmi Digital Studios";

            String customerText = """
                    Dear %s,

                    Thank you for choosing Lakshmi Digital Studios.

                    Your booking request has been received and is currently
                    Pending review.

                    Event Type: %s
                    Event Date: %s

                    We will notify you when your booking status is updated.

                    Best Regards,
                    Lakshmi Digital Studios
                    """.formatted(
                    safe(clientName),
                    safe(eventType),
                    safe(eventDate)
            );

            String customerHtml = """
                    <!DOCTYPE html>
                    <html>
                    <body style="font-family:Arial,sans-serif;color:#333;
                                 line-height:1.6;padding:20px;">

                        <h2 style="color:#2980b9;">
                            Booking Request Received!
                        </h2>

                        <p>Dear <b>%s</b>,</p>

                        <p>
                            Thank you for choosing
                            <b>Lakshmi Digital Studios</b>.
                        </p>

                        <p>
                            Your booking request has been successfully
                            received and is currently
                            <b style="color:#e67e22;">Pending</b> review.
                        </p>

                        <p><b>Booking Details</b></p>

                        <ul>
                            <li>Event Type: %s</li>
                            <li>Event Date: %s</li>
                        </ul>

                        <p>
                            We will notify you once your booking
                            status has been updated.
                        </p>

                        <p>
                            Best Regards,<br>
                            <b>Lakshmi Digital Studios</b>
                        </p>

                    </body>
                    </html>
                    """.formatted(
                    escapeHtml(clientName),
                    escapeHtml(eventType),
                    escapeHtml(eventDate)
            );

            try {
                String messageId = sendBrevoEmail(
                        clientEmail.trim(),
                        customerSubject,
                        customerText,
                        customerHtml
                );

                System.out.println(
                        "Customer pending email accepted. Message ID: "
                                + messageId
                );

            } catch (RuntimeException e) {

                System.err.println(
                        "Failed to send customer pending email."
                );

                failures.add(e);
            }

        } else {

            System.out.println(
                    "Customer email skipped because it is empty."
            );
        }

        System.out.println("=======================================");

        if (!failures.isEmpty()) {

            IllegalStateException exception =
                    new IllegalStateException(
                            "One or more booking notification emails failed.",
                            failures.get(0)
                    );

            for (int i = 1; i < failures.size(); i++) {
                exception.addSuppressed(failures.get(i));
            }

            throw exception;
        }
    }

    // =====================================================
    // 2. BOOKING STATUS UPDATE
    // Sends confirmation or rejection to the customer.
    // =====================================================

    public void sendCustomerStatusUpdateEmail(
            String customerEmail,
            String clientName,
            String eventDate,
            String status
    ) {

        if (customerEmail == null || customerEmail.isBlank()) {

            System.out.println(
                    "Status email skipped because customer email is empty."
            );

            return;
        }

        String currentStatus = safe(status).trim();

        boolean isConfirmed =
                "Confirmed".equalsIgnoreCase(currentStatus);

        boolean isRejected =
                "Rejected".equalsIgnoreCase(currentStatus);

        String subjectStatus = isConfirmed
                ? "Confirmed & Accepted!"
                : isRejected
                ? "Declined / Cancelled"
                : currentStatus;

        String subject =
                "Booking Update: " + subjectStatus
                        + " - Lakshmi Digital Studios";

        String statusColor = isConfirmed
                ? "#27ae60"
                : isRejected
                ? "#c0392b"
                : "#e67e22";

        String statusMessage;

        if (isConfirmed) {

            statusMessage =
                    "We are looking forward to capturing your event!";

        } else if (isRejected) {

            statusMessage =
                    "Unfortunately, we are unavailable on this date. "
                            + "Please contact us for alternative dates.";

        } else {

            statusMessage =
                    "Your booking status has been updated. "
                            + "Please contact us if you need any clarification.";
        }

        String textContent = """
                Dear %s,

                Your booking for %s has been updated.

                Current Status: %s

                %s

                Best Regards,
                Lakshmi Digital Studios
                """.formatted(
                safe(clientName),
                safe(eventDate),
                currentStatus,
                statusMessage
        );

        String htmlContent = """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial,sans-serif;color:#333;
                             line-height:1.6;padding:20px;">

                    <h2 style="color:%s;">
                        Booking Status Update
                    </h2>

                    <p>Dear <b>%s</b>,</p>

                    <p>
                        Your booking for
                        <b>%s</b>
                        has been updated by the studio.
                    </p>

                    <p style="font-size:18px;color:%s;font-weight:bold;">
                        Status: %s
                    </p>

                    <p>%s</p>

                    <p>
                        Best Regards,<br>
                        <b>Lakshmi Digital Studios</b>
                    </p>

                </body>
                </html>
                """.formatted(
                statusColor,
                escapeHtml(clientName),
                escapeHtml(eventDate),
                statusColor,
                escapeHtml(currentStatus),
                escapeHtml(statusMessage)
        );

        String messageId = sendBrevoEmail(
                customerEmail.trim(),
                subject,
                textContent,
                htmlContent
        );

        System.out.println(
                "Customer status email accepted. Message ID: " + messageId
        );
    }

    // =====================================================
    // 3. COMMON BREVO API METHOD
    // =====================================================

    private String sendBrevoEmail(
            String recipient,
            String subject,
            String textContent,
            String htmlContent
    ) {

        URI endpoint;

        try {
            endpoint = URI.create(apiUrl);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Invalid Brevo API URL.",
                    e
            );
        }

        if (!"https".equalsIgnoreCase(endpoint.getScheme())
                || !"api.brevo.com".equalsIgnoreCase(endpoint.getHost())
                || !"/v3/smtp/email".equals(endpoint.getPath())) {

            throw new IllegalStateException(
                    "Incorrect Brevo API endpoint: " + endpoint
            );
        }

        // DNS diagnostic
        try {

            InetAddress[] addresses =
                    InetAddress.getAllByName(endpoint.getHost());

            if (addresses.length == 0) {
                throw new UnknownHostException(
                        "No addresses found for Brevo."
                );
            }

        } catch (UnknownHostException e) {

            throw new IllegalStateException(
                    "Cannot resolve Brevo server. Check DNS and network.",
                    e
            );
        }

        // Sender
        Map<String, Object> sender = new HashMap<>();

        sender.put("name", senderName);
        sender.put("email", senderEmail);

        // Recipient
        Map<String, Object> recipientDetails = new HashMap<>();

        recipientDetails.put("email", recipient);

        // Payload
        Map<String, Object> payload = new HashMap<>();

        payload.put("sender", sender);
        payload.put("to", List.of(recipientDetails));
        payload.put("subject", subject);
        payload.put("textContent", textContent);
        payload.put("htmlContent", htmlContent);

        try {

            String json =
                    jsonMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(endpoint)
                    .timeout(Duration.ofSeconds(45))
                    .header("accept", "application/json")
                    .header("api-key", apiKey)
                    .header("content-type", "application/json")
                    .POST(
                            HttpRequest.BodyPublishers.ofString(json)
                    )
                    .build();

            System.out.println(
                    "Sending Brevo email to: " + recipient
            );

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            System.out.println(
                    "Brevo HTTP Status: " + response.statusCode()
            );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new IllegalStateException(
                        "Brevo API error. HTTP Status: "
                                + response.statusCode()
                                + ", Response: "
                                + response.body()
                );
            }

            Map<?, ?> result = jsonMapper.readValue(
                    response.body(),
                    Map.class
            );

            Object rawMessageId = result.get("messageId");

            String messageId = rawMessageId == null
                    ? "Unknown"
                    : rawMessageId.toString();

            System.out.println(
                    "Brevo accepted email. Message ID: " + messageId
            );

            return messageId;

        } catch (HttpTimeoutException e) {

            throw new IllegalStateException(
                    "Brevo request timed out. The email may already "
                            + "have been accepted. Check Brevo logs "
                            + "before retrying.",
                    e
            );

        } catch (ConnectException e) {

            throw new IllegalStateException(
                    "Could not connect to Brevo. Check DNS, network "
                            + "and firewall settings.",
                    e
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Brevo email request was interrupted.",
                    e
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Could not communicate with Brevo.",
                    e
            );
        }
    }

    // =====================================================
    // 4. UTILITY METHODS
    // =====================================================

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
