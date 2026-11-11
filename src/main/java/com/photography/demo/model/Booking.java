package com.photography.demo.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "bookings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"clientName", "contact", "eventDate"})
})
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientName;
    private String contact;
    private String email; // <--- New field to store customer's email

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate eventDate;
    private String eventType; // <--- New field (e.g., Birthday, Marriage, Function, etc.)
    private String status; // e.g., Pending, Confirmed
}