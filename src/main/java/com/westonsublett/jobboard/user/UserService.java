package com.westonsublett.jobboard.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserOAuthAccountRepository oauthAccountRepository;

    public UserService(UserRepository userRepository,
                       UserOAuthAccountRepository oauthAccountRepository) {
        this.userRepository = userRepository;
        this.oauthAccountRepository = oauthAccountRepository;
    }

    @Transactional(readOnly = true)
    public Optional<User> getById(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User findOrCreateOAuthUser(String provider, String subject, String email, String name, String givenName) {
        UserOAuthAccount account = oauthAccountRepository
                .findByProviderAndProviderSubject(provider, subject)
                .orElse(null);

        if (account != null) {
            updateProfile(account.getUser(), email, name, givenName);
            return account.getUser();
        }

        User user = email != null
                ? userRepository.findByEmail(email).orElse(null)
                : null;

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(name);
        } else {
            updateProfile(user, email, name, givenName);
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

    private void updateProfile(User user, String email, String name, String givenName) {
        if (email != null && !email.isBlank()) {
            user.setEmail(email);
        }
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        if (givenName != null && !givenName.isBlank()) {
            user.setGivenName(givenName);
        }
    }
}