package com.photography.demo.controller;

import com.photography.demo.model.Booking;
import com.photography.demo.repository.BookingRepository;
import com.photography.demo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingRepository bookingRepository;

    @Autowired
    private EmailService emailService;

    public BookingController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        boolean isDuplicate = bookingRepository.existsByClientNameAndContactAndEventDate(
                booking.getClientName(),
                booking.getContact(),
                booking.getEventDate()
        );

        if (isDuplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("A booking with these exact details and date already exists!");
        }

        booking.setStatus("Pending");
        Booking savedBooking = bookingRepository.save(booking);

        // --- DEBUG PRINTS ---
        System.out.println("--- DEBUG EMAIL TRIGGER ---");
        System.out.println("Client Name: " + booking.getClientName());
        System.out.println("Client Email received from frontend: " + booking.getEmail());

        try {
            // 1. Send Admin Email
            emailService.sendAdminBookingNotification(
                    "lakshmistudio6677@gmail.com",
                    booking.getClientName(),
                    booking.getEventDate().toString(),
                    booking.getContact(),
                    booking.getEventType(),
                    booking.getEmail()
            );
            System.out.println("Admin email sent successfully.");

            // 2. Send Customer Email
            if (booking.getEmail() != null && !booking.getEmail().trim().isEmpty()) {
                emailService.sendCustomerPendingEmail(
                        booking.getEmail(),
                        booking.getClientName(),
                        booking.getEventDate().toString(),
                        booking.getEventType()
                );
                System.out.println("Customer pending email sent successfully to: " + booking.getEmail());
            } else {
                System.out.println("Customer email was skipped because it was null or empty!");
            }

        } catch (Exception e) {
            System.err.println("FAILED TO SEND EMAIL: " + e.getMessage());
            e.printStackTrace(); // This will show the exact line if something fails
        }

        return ResponseEntity.ok(savedBooking);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Booking> updateBookingStatus(@PathVariable Long id, @RequestBody Booking updatedBooking) {
        return bookingRepository.findById(id)
                .map(booking -> {
                    String oldStatus = booking.getStatus();
                    String newStatus = updatedBooking.getStatus();

                    booking.setStatus(newStatus);
                    Booking savedBooking = bookingRepository.save(booking);

                    // Trigger 2nd email when admin changes status to Confirmed or Rejected
                    try {
                        if (booking.getEmail() != null && !booking.getEmail().isEmpty() && !oldStatus.equalsIgnoreCase(newStatus)) {
                            emailService.sendCustomerStatusUpdateEmail(
                                    booking.getEmail(),
                                    booking.getClientName(),
                                    booking.getEventDate().toString(),
                                    newStatus
                            );
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to send status update email: " + e.getMessage());
                    }

                    return ResponseEntity.ok(savedBooking);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        if (bookingRepository.existsById(id)) {
            bookingRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/confirmed-dates")
    public ResponseEntity<List<Booking>> getConfirmedBookingDates(){
        // Fetches all bookings where status is 'Confirmed' or 'Accepted'
        List<Booking> confirmedBookings = bookingRepository.findByStatusIgnoreCase("Confirmed");
        return ResponseEntity.ok(confirmedBookings);
    }
}