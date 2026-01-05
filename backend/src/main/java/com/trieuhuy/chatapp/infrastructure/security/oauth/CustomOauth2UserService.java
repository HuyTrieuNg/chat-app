package com.trieuhuy.chatapp.infrastructure.security.oauth;

import com.trieuhuy.chatapp.application.dto.OAuthUserCommand;
import com.trieuhuy.chatapp.application.service.OAuthLoginService;
import com.trieuhuy.chatapp.domain.model.AuthProvider;
import com.trieuhuy.chatapp.domain.model.User;
import com.trieuhuy.chatapp.infrastructure.security.oauthuser.CustomerOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOauth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final OAuthLoginService oAuthLoginService;
    private final GithubEmailFetcher githubEmailFetcher;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oauthUser = new DefaultOAuth2UserService().loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuthUserInfo userInfo = OAuthUserInfoFactory.of(
                registrationId,
                oauthUser.getAttributes()
        );

        String email = userInfo.getEmail();

        if (email == null && "github".equalsIgnoreCase(registrationId)) {
            String accessToken = userRequest.getAccessToken().getTokenValue();
            email = githubEmailFetcher.fetchPrimaryEmail(accessToken);
        }

        OAuthUserCommand cmd = new OAuthUserCommand(
                email,
                userInfo.getName(),
                AuthProvider.valueOf(registrationId.toUpperCase()),
                userInfo.getId()
        );

        User user = oAuthLoginService.login(cmd);

        return new CustomerOAuth2User(user, oauthUser.getAttributes());
    }
}
