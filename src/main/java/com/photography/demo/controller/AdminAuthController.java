
package com.photography.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAdmin(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                Map.of(
                        "authenticated", true,
                        "username", authentication.getName(),
                        "message", "Admin authentication successful."
                )
        );
    }
}
