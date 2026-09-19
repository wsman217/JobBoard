package com.westonsublett.jobboard;

import com.westonsublett.jobboard.user.User;
import com.westonsublett.jobboard.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JobActivityApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private UserRepository userRepository;

    @Test
    void unauthenticatedAccessIsRejected() throws Exception {
        mockMvc.perform(get("/api/job-activities"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createThenListReturnsCreatedActivity() throws Exception {
        User user = newUser("create@example.com");
        String token = encodeToken(user);

        String body = """
                {
                  "activityDate": "2026-09-14T00:00:00Z",
                  "searchType": "Applied for Job",
                  "jobType": "Full-time",
                  "companyName": "Acme Corporation",
                  "status": "Pending"
                }
                """;

        mockMvc.perform(post("/api/job-activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.companyName").value("Acme Corporation"))
                .andExpect(jsonPath("$.status").value("Pending"));

        mockMvc.perform(get("/api/job-activities")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].companyName").value("Acme Corporation"))
                .andExpect(jsonPath("$[0].jobType").value("Full-time"));
    }

    @Test
    void listAppliesFilters() throws Exception {
        User user = newUser("filters@example.com");
        String token = encodeToken(user);

        createActivity(token, "Acme Corporation", "Full-time", "Applied for Job", "Pending");
        createActivity(token, "Globex Industries", "Part-time", "Interview", "Accepted");

        mockMvc.perform(get("/api/job-activities")
                        .param("jobType", "Part-time")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].companyName").value("Globex Industries"));

        mockMvc.perform(get("/api/job-activities")
                        .param("search", "acme")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].companyName").value("Acme Corporation"));
    }

    @Test
    void activitiesAreScopedToTheAuthenticatedUser() throws Exception {
        User owner = newUser("owner@example.com");
        User other = newUser("other@example.com");

        createActivity(encodeToken(owner), "Acme Corporation", "Full-time", "Applied for Job", "Pending");

        mockMvc.perform(get("/api/job-activities")
                        .header("Authorization", "Bearer " + encodeToken(other)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createRejectsMissingRequiredFields() throws Exception {
        User user = newUser("invalid@example.com");
        String token = encodeToken(user);

        mockMvc.perform(post("/api/job-activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "activityDate": "2026-09-14T00:00:00Z",
                                  "searchType": "Applied for Job",
                                  "jobType": "",
                                  "companyName": "Acme Corporation",
                                  "status": "Pending"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    private void createActivity(String token, String companyName, String jobType,
                                String searchType, String status) throws Exception {
        String body = """
                {
                  "activityDate": "2026-09-14T00:00:00Z",
                  "searchType": "%s",
                  "jobType": "%s",
                  "companyName": "%s",
                  "status": "%s"
                }
                """.formatted(searchType, jobType, companyName, status);

        mockMvc.perform(post("/api/job-activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated());
    }

    private User newUser(String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        return userRepository.save(user);
    }

    private String encodeToken(User user) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("jobboard")
                .audience(List.of("jobboard"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .build();
        return jwtEncoder.encode(
                        JwtEncoderParameters.from(
                                JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
