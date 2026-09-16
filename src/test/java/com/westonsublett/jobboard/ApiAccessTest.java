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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private UserRepository userRepository;

    @Test
    void unauthenticatedAccessIsRejected() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedJwtCanReadOwnProfile() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("candidate@example.com");
        user.setName("Candidate One");
        user = userRepository.save(user);

        String token = encodeToken(user, user.getEmail());

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.email").value(user.getEmail()));
    }

    @Test
    void authenticatedJwtCanReadOwnProfileFromAccessTokenCookie() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("cookie@example.com");
        user.setName("Cookie User");
        user = userRepository.save(user);

        String token = encodeToken(user, user.getEmail());

        mockMvc.perform(get("/api/users/me")
                        .cookie(new jakarta.servlet.http.Cookie("access_token", token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.email").value(user.getEmail()));
    }

    private String encodeToken(User user, String email) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("jobboard")
                .audience(List.of("jobboard"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .subject(user.getId().toString())
                .claim("email", email)
                .claim("roles", "ROLE_CANDIDATE")
                .build();
        return jwtEncoder.encode(
                        JwtEncoderParameters.from(
                                JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
