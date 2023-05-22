package com.ptit.miniproject.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptit.miniproject.data.request.AuthenticationRequest;
import com.ptit.miniproject.data.request.RegisterRequest;
import com.ptit.miniproject.data.response.AuthenticationResponse;
import com.ptit.miniproject.entity.Role;
import com.ptit.miniproject.entity.Token;
import com.ptit.miniproject.entity.TokenType;
import com.ptit.miniproject.entity.User;
import com.ptit.miniproject.repository.TokenRepository;
import com.ptit.miniproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest registerRequest) {
        User user = User.builder()
                .firstname(registerRequest.getFirstname())
                .lastname(registerRequest.getLastname())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.ADMIN)
                .build();
        User savedUser = userRepository.save(user);
        String jwtToken = jwtService.generateToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
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
