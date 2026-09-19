package com.westonsublett.jobboard.security;

import com.westonsublett.jobboard.user.User;
import com.westonsublett.jobboard.user.UserService;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class OidcUserServiceAdapter implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate = new OidcUserService();

    private final UserService userService;

    public OidcUserServiceAdapter(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OidcUser loadUser(@NonNull OidcUserRequest userRequest) throws OAuth2AuthenticationException, NullPointerException {
        OidcUser oidcUser = delegate.loadUser(userRequest);

        Map<String, Object> attributes = oidcUser.getAttributes();
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String subject = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String givenName = (String) attributes.get("given_name");

        User user = userService.findOrCreateOAuthUser(provider, subject, email, name, givenName);

        Map<String, Object> enriched = new HashMap<>(attributes);
        enriched.put("app_user_id", user.getId().toString());

        String nameAttributeKey = userRequest
                .getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        if (nameAttributeKey == null) {
            throw new NullPointerException();
        }

        OidcIdToken idToken = oidcUser.getIdToken();
        OidcUserInfo userInfo = oidcUser.getUserInfo();
        return new DefaultOidcUser(
                new ArrayList<>(),
                idToken,
                userInfo,
                nameAttributeKey
        ) {
            @Override
            @NonNull
            public Map<String, Object> getAttributes() {
                return enriched;
            }
        };
    }
}