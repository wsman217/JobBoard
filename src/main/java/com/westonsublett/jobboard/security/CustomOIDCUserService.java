package com.westonsublett.jobboard.security;

import com.westonsublett.jobboard.user.*;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOIDCUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate = new OidcUserService();

    private final UserRepository userRepository;
    private final UserOAuthAccountRepository oauthAccountRepository;

    public CustomOIDCUserService(UserRepository userRepository,
                                 UserOAuthAccountRepository oauthAccountRepository) {
        this.userRepository = userRepository;
        this.oauthAccountRepository = oauthAccountRepository;
    }

    @Override
    @Transactional
    public OidcUser loadUser(@NonNull OidcUserRequest userRequest) throws OAuth2AuthenticationException, NullPointerException {
        OidcUser oidcUser = delegate.loadUser(userRequest);

        Map<String, Object> attributes = oidcUser.getAttributes();
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String subject = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        User user = findOrCreateUser(provider, subject, email, name);

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

    private User findOrCreateUser(String provider, String subject, String email, String name) {
        UserOAuthAccount account = oauthAccountRepository
                .findByProviderAndProviderSubject(provider, subject)
                .orElse(null);

        if (account != null) {
            User user = account.getUser();
            updateProfile(user, email, name);
            return userRepository.save(user);
        }

        User user = email != null
                ? userRepository.findByEmail(email).orElse(null)
                : null;

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(name);
        } else {
            updateProfile(user, email, name);
        }
        user = userRepository.save(user);

        UserOAuthAccount newAccount = new UserOAuthAccount();
        newAccount.setUser(user);
        newAccount.setProvider(provider);
        newAccount.setProviderSubject(subject);
        newAccount.setProviderEmail(email);
        oauthAccountRepository.save(newAccount);

        return user;
    }

    private void updateProfile(User user, String email, String name) {
        if (email != null && !email.isBlank()) {
            user.setEmail(email);
        }
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
    }
}
