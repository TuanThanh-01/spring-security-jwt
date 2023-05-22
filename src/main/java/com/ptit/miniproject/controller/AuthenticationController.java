package com.ptit.miniproject.controller;

import com.ptit.miniproject.data.request.AuthenticationRequest;
import com.ptit.miniproject.data.request.RegisterRequest;
import com.ptit.miniproject.data.response.AuthenticationResponse;
import com.ptit.miniproject.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody RegisterRequest registerRequest
            ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(authenticationService.register(registerRequest));
    }

    @RequestMapping(value = "/confirm-account", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> confirmUserAccount(@RequestParam("token")String confirmToken) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(authenticationService.confirmEmail(confirmToken));
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
