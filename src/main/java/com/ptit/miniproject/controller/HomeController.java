package com.ptit.miniproject.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
public class HomeController {

    @GetMapping
    public ResponseEntity<String> home() {
        return ResponseEntity.status(HttpStatus.OK)
                .body("Home page");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<String> admin() {
        return ResponseEntity.status(HttpStatus.OK)
                .body("Admin page");
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/user")
    public ResponseEntity<String> user() {
        return ResponseEntity.status(HttpStatus.OK)
                .body("User page");
    }
}
