package com.trieuhuy.chatapp.api.controller;

import com.trieuhuy.chatapp.application.dto.*;
import com.trieuhuy.chatapp.application.service.AuthApplicationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplicationService authService;
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final int REFRESH_TOKEN_COOKIE_MAX_AGE = 7 * 24 * 60 * 60;

    @Value("${cookies.secure}")
    private boolean cookieSecure;

    @PostMapping("/login")
    public ResponseEntity<@NonNull Map<@NonNull String, @NonNull Object>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request);
        setRefreshTokenCookie(response, loginResponse.refreshToken());
        return ResponseEntity.ok(buildLoginResponseBody(loginResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<@NonNull Map<@NonNull String, @NonNull Object>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {
        LoginResponse loginResponse = authService.register(request);
        setRefreshTokenCookie(response, loginResponse.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(buildLoginResponseBody(loginResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<@NonNull RefreshTokenResponse> refreshToken(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME) String refreshToken,
            HttpServletResponse response) {
        RefreshTokenResponse refreshResponse = authService.refreshToken(new RefreshTokenRequest(refreshToken));

        setRefreshTokenCookie(response, refreshResponse.refreshToken());

        return ResponseEntity.ok(new RefreshTokenResponse(
                refreshResponse.accessToken(),
                refreshResponse.expiresIn()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<@NonNull Map<@NonNull String, @NonNull String>> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        clearRefreshTokenCookie(response);

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(REFRESH_TOKEN_COOKIE_MAX_AGE);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private Map<String, Object> buildLoginResponseBody(LoginResponse loginResponse) {
        return Map.of(
                "accessToken", loginResponse.accessToken(),
                "expiresIn", loginResponse.expiresIn(),
                "userId", loginResponse.userId(),
                "username", loginResponse.username(),
                "email", loginResponse.email()
        );
    }
}
