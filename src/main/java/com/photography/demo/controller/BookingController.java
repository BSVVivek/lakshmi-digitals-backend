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
@RequestMapping("/api/bookings")
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
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        Booking savedBooking = bookingRepository.save(booking);

        try {
            // Replace with your actual receiving email address
            emailService.sendBookingNotification(
                    "lakshmistudio6677@gmail.com",
                    booking.getClientName(),
                    booking.getEventDate().toString(),
                    booking.getContact()
            );
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }

        return ResponseEntity.ok(savedBooking);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        if (bookingRepository.existsById(id)) {
            bookingRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Booking> updateBookingStatus(@PathVariable Long id, @RequestBody Booking updatedBooking) {
        return bookingRepository.findById(id)
                .map(booking -> {
                    booking.setStatus(updatedBooking.getStatus());
                    Booking savedBooking = bookingRepository.save(booking);
                    return ResponseEntity.ok(savedBooking);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}