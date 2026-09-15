package com.westonsublett.jobboard.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserOAuthAccountRepository extends JpaRepository<UserOAuthAccount, Long> {

    Optional<UserOAuthAccount> findByProviderAndProviderSubject(String provider, String providerSubject);
}
