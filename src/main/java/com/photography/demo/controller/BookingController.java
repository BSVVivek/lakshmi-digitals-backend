
package com.photography.demo.controller;

import com.photography.demo.model.Booking;
import com.photography.demo.repository.BookingRepository;
import com.photography.demo.service.EmailService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    private static final DateTimeFormatter EMAIL_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // Constructor injection
    public BookingController(
            BookingRepository bookingRepository,
            EmailService emailService
    ) {
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
    }

    // Format event date for emails
    private String formatEventDate(Booking booking) {

        if (booking.getEventDate() == null) {
            return "";
        }

        return booking.getEventDate().format(EMAIL_DATE_FORMAT);
    }

    // ==========================================
    // 1. GET ALL BOOKINGS
    // ==========================================

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {

        return ResponseEntity.ok(
                bookingRepository.findAll()
        );
    }

    // ==========================================
    // 2. CREATE NEW BOOKING
    // Admin notification + Customer pending email
    // ==========================================

    @PostMapping
    public ResponseEntity<?> createBooking(
            @RequestBody Booking booking
    ) {

        boolean isDuplicate =
                bookingRepository.existsByClientNameAndContactAndEventDate(
                        booking.getClientName(),
                        booking.getContact(),
                        booking.getEventDate()
                );

        if (isDuplicate) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(
                            "A booking with these exact details and date already exists!"
                    );
        }

        // Every new booking starts with Pending status
        booking.setStatus("Pending");

        // Save booking first
        Booking savedBooking = bookingRepository.save(booking);

        System.out.println("========== NEW BOOKING ==========");
        System.out.println(
                "Client Name: " + savedBooking.getClientName()
        );
        System.out.println(
                "Client Email: " + savedBooking.getEmail()
        );
        System.out.println(
                "Event Type: " + savedBooking.getEventType()
        );
        System.out.println("Booking saved successfully.");

        // Send admin and customer emails through Brevo

        try {

            emailService.sendNewBookingEmails(
                    savedBooking.getClientName(),
                    formatEventDate(savedBooking),
                    savedBooking.getContact(),
                    savedBooking.getEventType(),
                    savedBooking.getEmail()
            );

            System.out.println("Booking email tasks successfully queued.");

        } catch (RuntimeException e) {

            System.err.println(
                    "Booking was saved, but email tasks could not be queued."
            );

            e.printStackTrace();
        }

        System.out.println("=================================");

        // Return saved booking even if email delivery fails
        return ResponseEntity.ok(savedBooking);
    }

    // ==========================================
    // 3. UPDATE BOOKING STATUS
    // Customer receives Confirmed / Rejected email
    // ==========================================

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable Long id,
            @RequestBody Booking updatedBooking
    ) {

        // Validate request body
        if (updatedBooking == null
                || updatedBooking.getStatus() == null
                || updatedBooking.getStatus().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Booking status is required.");
        }

        // Validate requested status
        String requestedStatus = updatedBooking.getStatus().trim();

        boolean validStatus = List.of(
                        "Pending",
                        "Confirmed",
                        "Rejected"
                )
                .stream()
                .anyMatch(s -> s.equalsIgnoreCase(requestedStatus));

        if (!validStatus) {

            return ResponseEntity.badRequest()
                    .body("Invalid booking status.");
        }

        // Find booking
        Booking booking = bookingRepository.findById(id)
                .orElse(null);

        if (booking == null) {
            return ResponseEntity.notFound().build();
        }

        String oldStatus = booking.getStatus();
        String newStatus = requestedStatus;

        boolean statusChanged =
                oldStatus == null
                        || !oldStatus.trim().equalsIgnoreCase(newStatus);

        // Update booking status
        booking.setStatus(newStatus);

        Booking savedBooking = bookingRepository.save(booking);

        System.out.println("========== BOOKING STATUS UPDATE ==========");
        System.out.println("Booking ID: " + id);
        System.out.println("Old Status: " + oldStatus);
        System.out.println("New Status: " + newStatus);

        // Send email only when status changes to Confirmed or Rejected

        boolean isFinalStatus =
                "Confirmed".equalsIgnoreCase(newStatus)
                        || "Rejected".equalsIgnoreCase(newStatus);

        if (statusChanged
                && isFinalStatus
                && booking.getEmail() != null
                && !booking.getEmail().isBlank()) {

            try {

                emailService.sendCustomerStatusUpdateEmail(
                        booking.getEmail(),
                        booking.getClientName(),
                        formatEventDate(booking),
                        newStatus
                );

                System.out.println(
                        "Customer status email task successfully queued."
                );

            } catch (RuntimeException e) {

                System.err.println(
                        "Booking status was updated, but email task could not be queued."
                );

                e.printStackTrace();
            }
        }
 else {

            System.out.println(
                    "Status email skipped. No eligible status transition."
            );
        }

        System.out.println("============================================");

        return ResponseEntity.ok(savedBooking);
    }

    // ==========================================
    // 4. DELETE BOOKING
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(
            @PathVariable Long id
    ) {

        if (bookingRepository.existsById(id)) {

            bookingRepository.deleteById(id);

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    // ==========================================
    // 5. GET CONFIRMED BOOKING DATES
    // ==========================================

    @GetMapping("/confirmed-dates")
    public ResponseEntity<List<Booking>> getConfirmedBookingDates() {

        List<Booking> confirmedBookings =
                bookingRepository.findByStatusIgnoreCase("Confirmed");

        return ResponseEntity.ok(confirmedBookings);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getBookingCount(){
        long totalBookings = bookingRepository.count();

        return ResponseEntity.ok(totalBookings);
    }

}
