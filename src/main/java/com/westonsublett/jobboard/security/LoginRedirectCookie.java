package com.westonsublett.jobboard.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Carries the in-app path the user was on before starting the OAuth flow across the round trip to
 * the external identity provider.
 * <p>
 * The path is read from the {@code redirect} query parameter of the authorization endpoint and
 * stashed in a short-lived, HttpOnly cookie that {@link JwtAuthenticationSuccessHandler} reads back
 * once the provider redirects to the callback endpoint.
 */
@Component
public class LoginRedirectCookie {

    public static final String COOKIE_NAME = "login_redirect";
    public static final String PARAM_NAME = "redirect";

    private static final Pattern SAFE_PATH = Pattern.compile("^/[A-Za-z0-9._~%+/?&=#-]*$");

    private static final Duration MAX_AGE = Duration.ofMinutes(10);

    public Optional<String> captureFrom(HttpServletRequest request) {
        return sanitize(request.getParameter(PARAM_NAME));
    }

    public Optional<String> readFrom(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(COOKIE_NAME)) {
                return sanitize(cookie.getValue());
            }
        }

        return Optional.empty();
    }

    public void write(HttpServletResponse response, String path) {
        response.addHeader(HttpHeaders.SET_COOKIE, builder(path).maxAge(MAX_AGE.getSeconds()).build().toString());
    }

    public void clear(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, builder("").maxAge(0).build().toString());
    }

    private ResponseCookie.ResponseCookieBuilder builder(String value) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax");
    }

    private Optional<String> sanitize(String path) {
        if (path == null) {
            return Optional.empty();
        }

        URI reference;
        try {
            reference = new URI(path);
        } catch (URISyntaxException e) {
            return Optional.empty();
        }

        if (reference.isAbsolute() || reference.getAuthority() != null
                || reference.getRawPath() == null || !reference.getRawPath().startsWith("/")) {
            return Optional.empty();
        }

        String encoded = reference.toASCIIString();
        if (!SAFE_PATH.matcher(encoded).matches()) {
            return Optional.empty();
        }

        return Optional.of(encoded);
    }
}