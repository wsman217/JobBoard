package com.westonsublett.jobboard;

import com.westonsublett.jobboard.config.AppProperties;
import com.westonsublett.jobboard.security.JwtAuthenticationSuccessHandler;
import com.westonsublett.jobboard.security.LoginRedirectCookie;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LoginRedirectTest {

    private static final String FRONTEND_REDIRECT_URI = "http://localhost:3000";

    @Autowired
    private JwtAuthenticationSuccessHandler successHandler;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private LoginRedirectCookie loginRedirectCookie;

    @Test
    void stashedPageIsReplayedAfterLogin() throws Exception {
        MockHttpServletResponse response = login(new Cookie("login_redirect", "/test-page?tab=1"));

        assertThat(response.getRedirectedUrl()).isEqualTo(FRONTEND_REDIRECT_URI + "/test-page?tab=1");
    }

    @Test
    void stashedPageKeepsItsHashFragment() throws Exception {
        MockHttpServletResponse response = login(new Cookie("login_redirect", "/test-page#applied"));

        assertThat(response.getRedirectedUrl()).isEqualTo(FRONTEND_REDIRECT_URI + "/test-page#applied");
    }

    @Test
    void stashedPageKeepsADoubleSlashInsideThePath() throws Exception {
        MockHttpServletResponse response = login(new Cookie("login_redirect", "/jobs//42"));

        assertThat(response.getRedirectedUrl()).isEqualTo(FRONTEND_REDIRECT_URI + "/jobs//42");
    }

    @Test
    void stashedPageKeepsMultipleQueryParameters() throws Exception {
        String path = "/job-activities?search=c%2B%2B+dev&searchType=title&status=applied";

        MockHttpServletResponse response = login(new Cookie("login_redirect", path));

        assertThat(response.getRedirectedUrl()).isEqualTo(FRONTEND_REDIRECT_URI + path);
    }

    @Test
    void stashedPageKeepsExistingPercentEncoding() throws Exception {
        MockHttpServletResponse response = login(new Cookie("login_redirect", "/jobs/a%2Fb"));

        assertThat(response.getRedirectedUrl()).isEqualTo(FRONTEND_REDIRECT_URI + "/jobs/a%2Fb");
    }

    @Test
    void nonAsciiStashedPageIsPercentEncoded() throws Exception {
        MockHttpServletResponse response = login(new Cookie("login_redirect", "/caf\u00e9"));

        assertThat(response.getRedirectedUrl()).isEqualTo(FRONTEND_REDIRECT_URI + "/caf%C3%A9");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://evil.example/steal",
            "//evil.example/steal",
            "/\\evil.example/steal",
            "javascript:alert(1)",
            "test-page",
            "/test page",
            "/%zz",
            "/a,b"
    })
    void unusableStashedPageIsIgnored(String path) throws Exception {
        MockHttpServletResponse response = login(new Cookie("login_redirect", path));

        assertThat(response.getRedirectedUrl()).isEqualTo(FRONTEND_REDIRECT_URI);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/jobs/!bang",
            "/jobs/1$dollar",
            "/jobs/O'brien",
            "/jobs/(paren)",
            "/jobs/1*star",
            "/jobs/c;comma",
            "/jobs/user:handle",
            "/jobs/user@host"
    })
    void charactersOutsideOurUrlStandardAreIgnored(String path) throws Exception {
        MockHttpServletResponse response = login(new Cookie("login_redirect", path));

        assertThat(response.getRedirectedUrl()).isEqualTo(FRONTEND_REDIRECT_URI);
    }

    @Test
    void loginWithoutStashedPageFallsBackToTheConfiguredFrontendUri() throws Exception {
        MockHttpServletResponse response = login();

        assertThat(response.getRedirectedUrl()).isEqualTo(FRONTEND_REDIRECT_URI);
    }

    @Test
    void stashedPageIsClearedAfterLogin() throws Exception {
        MockHttpServletResponse response = login(new Cookie("login_redirect", "/test-page"));

        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE))
                .anySatisfy(header -> assertThat(header).startsWith("login_redirect=;"));
    }

    @Test
    void basePathInTheConfiguredFrontendUriIsPreserved() throws Exception {
        JwtAuthenticationSuccessHandler handler = handlerWithBase("https://app.int-test.com/jb");

        MockHttpServletResponse response = login(handler, new Cookie("login_redirect", "/test-page"));

        assertThat(response.getRedirectedUrl()).isEqualTo("https://app.int-test.com/jb/test-page");
    }

    @Test
    void trailingSlashInTheConfiguredFrontendUriDoesNotDoubleUp() throws Exception {
        JwtAuthenticationSuccessHandler handler = handlerWithBase("https://app.int-test.com/jb/");

        MockHttpServletResponse response = login(handler, new Cookie("login_redirect", "/test-page"));

        assertThat(response.getRedirectedUrl()).isEqualTo("https://app.int-test.com/jb/test-page");
    }

    private JwtAuthenticationSuccessHandler handlerWithBase(String redirectUri) {
        AppProperties properties = new AppProperties();
        properties.getFrontend().setRedirectUri(redirectUri);

        return new JwtAuthenticationSuccessHandler(jwtEncoder, properties, loginRedirectCookie);
    }

    private MockHttpServletResponse login(Cookie... cookies) throws Exception {
        return login(successHandler, cookies);
    }

    private MockHttpServletResponse login(JwtAuthenticationSuccessHandler handler, Cookie... cookies) throws Exception {
        OidcIdToken idToken = new OidcIdToken(
                "id-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of(
                        "sub", "google-subject",
                        "app_user_id", "user-1",
                        "email", "candidate@example.com"));

        OidcUser oidcUser = new DefaultOidcUser(
                AuthorityUtils.createAuthorityList("ROLE_CANDIDATE"), idToken, "sub");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(cookies);

        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(request, response, new TestingAuthenticationToken(oidcUser, null));

        return response;
    }
}