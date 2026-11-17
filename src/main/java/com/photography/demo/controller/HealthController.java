
package com.photography.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {

        boolean databaseConnected = false;

        try (Connection connection = dataSource.getConnection()) {

            databaseConnected = connection.isValid(3);

        } catch (SQLException e) {

            System.err.println(
                    "Database health check failed: " + e.getMessage()
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "backend", "UP",
                        "database", databaseConnected ? "UP" : "DOWN",
                        "message", databaseConnected
                                ? "Backend and database are connected."
                                : "Backend is running, but database is unavailable."
                )
        );
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {

        return ResponseEntity.ok(
                Map.of("status", "UP")
        );
    }
}
