package com.westonsublett.jobboard.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Stashes the {@code redirect} query parameter sent to the OAuth authorization endpoint so that
 * {@link JwtAuthenticationSuccessHandler} can replay it after the provider redirects back.
 * <p>
 * The query parameter itself is deliberately dropped here rather than forwarded to the provider,
 * and it never has to survive in the authorization request because the cookie lives on this origin
 * for the whole round trip.
 */
public class LoginRedirectCaptureFilter extends OncePerRequestFilter {

    private final LoginRedirectCookie loginRedirectCookie;

    public LoginRedirectCaptureFilter(LoginRedirectCookie loginRedirectCookie) {
        this.loginRedirectCookie = loginRedirectCookie;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        loginRedirectCookie.captureFrom(request)
                .ifPresent(path -> loginRedirectCookie.write(response, path));

        filterChain.doFilter(request, response);
    }
}