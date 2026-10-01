package com.westonsublett.jobboard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoginRedirectCaptureTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void startingLoginStashesTheRequestedPage() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google").param("redirect", "/test-page?tab=1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("login_redirect=/test-page?tab=1")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")));
    }

    @Test
    void requestedPageIsNotLeakedToTheProvider() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google").param("redirect", "/test-page"))
                .andExpect(header().string(HttpHeaders.LOCATION, startsWith("https://accounts.google.com")))
                .andExpect(header().string(HttpHeaders.LOCATION, not(containsString("redirect="))));
    }

    @Test
    void startingLoginWithQueryParametersStashesTheWholeLocation() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google")
                        .param("redirect", "/job-activities?search=c%2B%2B+dev&status=applied"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        containsString("login_redirect=/job-activities?search=c%2B%2B+dev&status=applied")));
    }

@Test
    void startingLoginWithoutRequestedPageStashesNothing() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://evil.example/steal",
            "//evil.example/steal",
            "/\\evil.example/steal",
            "test-page",
            "/test page",
            "/%zz",
            "/jobs/!bang",
            "/jobs/1*dollar*star"
    })
    void unusableRequestedPageIsNeverStashed(String redirect) throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google").param("redirect", redirect))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
    }
}