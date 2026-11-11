package com.photography.demo.repository;

import com.photography.demo.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Checks if a booking with the exact same name, phone, and date already exists
    boolean existsByClientNameAndContactAndEventDate(String clientName, String contact, LocalDate eventDate);

    // Fetch all bookings with a specific status (e.g., "Confirmed")
    List<Booking> findByStatusIgnoreCase(String status);
}
