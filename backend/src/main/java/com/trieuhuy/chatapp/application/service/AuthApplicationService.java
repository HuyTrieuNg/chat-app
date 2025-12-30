package com.trieuhuy.chatapp.application.service;

import com.trieuhuy.chatapp.application.dto.*;
import com.trieuhuy.chatapp.domain.model.RefreshToken;
import com.trieuhuy.chatapp.domain.model.User;
import com.trieuhuy.chatapp.domain.repository.UserRepository;
import com.trieuhuy.chatapp.domain.service.AuthDomainService;
import com.trieuhuy.chatapp.domain.service.UserDomainService;
import com.trieuhuy.chatapp.infrastructure.security.jwt.JwtProperties;
import com.trieuhuy.chatapp.infrastructure.security.jwt.JwtTokenProvider;
import com.trieuhuy.chatapp.infrastructure.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthApplicationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;
    private final UserDomainService userDomainService;
    private final AuthDomainService authDomainService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Objects.requireNonNull(userDetails, "User details cannot be null");

            User user = userRepository.findByUsername(request.username())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate tokens
            String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails.getUsername());

            authDomainService.createRefreshToken(
                    user.getId(),
                    refreshToken,
                    jwtProperties.getRefreshTokenExpiration()
            );

            log.info("User logged in successfully: {}", request.username());

            return new LoginResponse(
                    accessToken,
                    refreshToken,
                    jwtProperties.getAccessTokenExpiration(),
                    user.getId(),
                    user.getUsername(),
                    user.getEmail()
            );
        } catch (BadCredentialsException e) {
            log.error("Invalid credentials for user: {}", request.username());
            throw new RuntimeException("Invalid username or password");
        }
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        String encodedPassword = passwordEncoder.encode(request.password());
        User savedUser = userDomainService.createUser(
                request.username(),
                request.email(),
                encodedPassword
        );

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(request.username());
        String refreshToken = jwtTokenProvider.generateRefreshToken(request.username());

        authDomainService.createRefreshToken(
                savedUser.getId(),
                refreshToken,
                jwtProperties.getRefreshTokenExpiration()
        );

        log.info("User registered successfully: {}", request.username());

        return new LoginResponse(
                accessToken,
                refreshToken,
                jwtProperties.getAccessTokenExpiration(),
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()
        );
    }

    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = authDomainService.validateRefreshToken(request.refreshToken());

        // Get user and generate new access token
        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername());

        authDomainService.revokeToken(request.refreshToken());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
        authDomainService.createRefreshToken(
                user.getId(),
                newRefreshToken,
                jwtProperties.getRefreshTokenExpiration()
        );

        log.info("Access token refreshed for user: {}", user.getUsername());

        return new RefreshTokenResponse(accessToken, jwtProperties.getAccessTokenExpiration());
    }

    @Transactional
    public void logout(String refreshToken) {
        authDomainService.revokeToken(refreshToken);
        log.info("User logged out successfully");
    }
}
