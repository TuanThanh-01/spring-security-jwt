package com.ptit.miniproject.controller;

import com.ptit.miniproject.data.request.AuthenticationRequest;
import com.ptit.miniproject.data.request.RegisterRequest;
import com.ptit.miniproject.data.response.AuthenticationResponse;
import com.ptit.miniproject.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest registerRequest
            ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(authenticationService.register(registerRequest));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest authenticationRequest
            ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(authenticationService.authenticate(authenticationRequest));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        return ResponseEntity.status(HttpStatus.OK)
                .body(authenticationService.refreshToken(request, response));
    }
}
