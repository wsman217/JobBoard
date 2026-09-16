package com.westonsublett.jobboard.security;

import com.westonsublett.jobboard.config.AppProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtEncoder jwtEncoder;
    private final AppProperties appProperties;
    private final String redirectUri;

    public JwtAuthenticationSuccessHandler(JwtEncoder jwtEncoder,
                                           AppProperties appProperties,
                                           @Value("${app.frontend.redirect-uri:http://localhost:3000}") String redirectUri) {
        this.jwtEncoder = jwtEncoder;
        this.appProperties = appProperties;
        this.redirectUri = redirectUri;
    }

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request,
                                        @NonNull HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        if (!(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
            throw new IOException("Authentication principal is not an OidcUser.");
        }

        Map<String, Object> attributes = oidcUser.getAttributes();
        String subject = (String) attributes.get("app_user_id");

        if (subject == null) {
            throw new IOException("Authentication principal does not have app_user_id.");
        }

        String roles = String.join(",", getRoles(authentication));

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(appProperties.getJwt().getIssuer())
                .audience(List.of(appProperties.getJwt().getAudience()))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(appProperties.getJwt().getAccessTokenTtlSeconds()))
                .subject(subject)
                .claim("email", attributes.get("email"))
                .claim("roles", roles)
                .build();

        String token = jwtEncoder
                .encode(
                        JwtEncoderParameters.from(
                                JwsHeader
                                        .with(MacAlgorithm.HS256)
                                        .build(),
                                claims)
                )
                .getTokenValue();

        ResponseCookie resCookie = ResponseCookie.from("access_token", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(7).getSeconds())
                .sameSite("Lax")
                .domain("int-test.com")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, resCookie.toString());
        response.sendRedirect(redirectUri);
    }

    private List<String> getRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).filter(Objects::nonNull)
                .filter(a -> a.startsWith("ROLE_"))
                .toList();
    }
}
