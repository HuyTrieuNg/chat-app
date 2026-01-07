package com.trieuhuy.chatapp.infrastructure.security.oauth;

import com.trieuhuy.chatapp.domain.service.AuthDomainService;
import com.trieuhuy.chatapp.infrastructure.security.jwt.JwtProperties;
import com.trieuhuy.chatapp.infrastructure.security.jwt.JwtTokenProvider;
import com.trieuhuy.chatapp.infrastructure.security.oauthuser.CustomerOAuth2User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthDomainService authDomainService;
    private final JwtProperties jwtProperties;

    @Value("${oauth2.redirect-uri}")
    private String redirectUri;

    @Value("${cookies.secure}")
    private boolean cookieSecure;

    @Override
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Authentication authentication
    ) throws IOException, ServletException {

        CustomerOAuth2User oauthUser = (CustomerOAuth2User) authentication.getPrincipal();

        if (oauthUser == null) {
            throw new IllegalArgumentException("OAuth2 user information is missing");
        }

        // Clear temporary authentication session
        clearAuthenticationAttributes(request);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(oauthUser.getUser().getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(oauthUser.getUser().getUsername());

        // Save refresh token to database
        authDomainService.createRefreshToken(
                oauthUser.getId(),
                refreshToken,
                jwtProperties.getRefreshTokenExpiration()
        );

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(jwtProperties.getRefreshTokenExpiration() / 1000)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        // Redirect to frontend with tokens
        String targetUrl = UriComponentsBuilder
                .fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
