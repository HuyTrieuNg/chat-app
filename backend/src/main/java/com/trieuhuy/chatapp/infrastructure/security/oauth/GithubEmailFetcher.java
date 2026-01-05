package com.trieuhuy.chatapp.infrastructure.security.oauth;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class GithubEmailFetcher {

    private static final String GITHUB_EMAILS_API = "https://api.github.com/user/emails";
    private final RestTemplate restTemplate = new RestTemplate();

    @Nullable
    public String fetchPrimaryEmail(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<@Nullable String> entity = new HttpEntity<>(null, headers);

            ResponseEntity<@Nullable List<Map<String, Object>>> response = restTemplate.exchange(
                    GITHUB_EMAILS_API,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            List<Map<String, Object>> emails = response.getBody();

            if (emails != null && !emails.isEmpty()) {
                // Try to find primary email first
                return emails.stream()
                        .filter(email -> Boolean.TRUE.equals(email.get("primary")))
                        .filter(email -> Boolean.TRUE.equals(email.get("verified")))
                        .map(email -> (String) email.get("email"))
                        .findFirst()
                        .orElseGet(() ->
                            // Fallback to first verified email
                            emails.stream()
                                .filter(email -> Boolean.TRUE.equals(email.get("verified")))
                                .map(email -> (String) email.get("email"))
                                .findFirst()
                                .orElse(null)
                        );
            }
        } catch (Exception e) {
            log.error("Failed to fetch GitHub emails", e);
        }

        return null;
    }
}

