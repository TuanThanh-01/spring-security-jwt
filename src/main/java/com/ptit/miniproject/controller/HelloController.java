package com.ptit.miniproject.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hello-controller")
public class HelloController {

    @GetMapping("/public")
    public ResponseEntity<String> publicApi() {
        return ResponseEntity.status(HttpStatus.OK)
                .body("Hello, this is public api");
    }

}
