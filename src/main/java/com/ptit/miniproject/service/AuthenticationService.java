package com.ptit.miniproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptit.miniproject.data.request.AuthenticationRequest;
import com.ptit.miniproject.data.request.RegisterRequest;
import com.ptit.miniproject.data.response.AuthenticationResponse;
import com.ptit.miniproject.entity.*;
import com.ptit.miniproject.repository.ConfirmationTokenRepository;
import com.ptit.miniproject.repository.TokenRepository;
import com.ptit.miniproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final ConfirmationTokenRepository confirmTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailSenderService emailService;
    private final AuthenticationManager authenticationManager;

    public String register(RegisterRequest registerRequest) {
        User user = User.builder()
                .firstname(registerRequest.getFirstname())
                .lastname(registerRequest.getLastname())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.USER)
                .enabled(false)
                .build();
        User savedUser = userRepository.save(user);

        ConfirmationToken confirmationToken = new ConfirmationToken(savedUser);
        confirmTokenRepository.save(confirmationToken);
        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setRecipient(savedUser.getEmail());
        emailDetails.setSubject("Complete Registration!");
        emailDetails.setMsgBody("Hi, " + user.getFirstname() + " " + user.getLastname()
        + "\nTo confirm your account, please click here to verify your account: " +
                "http://localhost:8082/api/v1/auth/confirm-account?token=" + confirmationToken.getToken());
        emailService.sendEmail(emailDetails);
        return "Verify email by the link sent on your email address";
    }

    public String confirmEmail(String confirmToken) {
        ConfirmationToken token = confirmTokenRepository.findByToken(confirmToken);
        if(token != null) {
            User user = userRepository.findByEmail(token.getUser().getEmail())
                    .orElseThrow();
            user.setEnabled(true);
            userRepository.save(user);
            return "Email verified successfully!";
        }
        return "Couldn't verify email";
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getEmail(),
                        authenticationRequest.getPassword()
                )
        );
        User user = userRepository.findByEmail(authenticationRequest.getEmail())
                .orElseThrow();
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserToken(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String refreshToken = authHeader.substring(7);
        String userEmail = jwtService.extractUsername(refreshToken);
        if(userEmail != null) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow();
            if(jwtService.isValidToken(refreshToken, user)) {
                String accessToken = jwtService.generateToken(user);
                revokeAllUserToken(user);
                saveUserToken(user, accessToken);
                return AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken) // keep the same refresh token. If refresh token expired, user must log in again
                        .build();
            }
        }
        new ObjectMapper()
                .writeValue(response.getOutputStream(),
                        "Refresh token expired, please log in again!");
        return null;
    }

    private void revokeAllUserToken(User user) {
        List<Token> validUserToken = tokenRepository.findAllValidTokenByUser(user.getId());
        if(validUserToken.isEmpty()) {
            return;
        }
        validUserToken.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });
        tokenRepository.saveAll(validUserToken);
    }

    private void saveUserToken(User user, String jwtToken) {
        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .revoked(false)
                .expired(false)
                .build();
        tokenRepository.save(token);
    }
}
