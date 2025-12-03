package com.testnext.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testnext.user.SystemUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import org.springframework.test.context.TestPropertySource;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest(classes = com.testnext.config.TestAppConfig.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("dev")
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Transactional
public class SystemUserApiTest {

        private MockMvc mockMvc;

        @Autowired
        private WebApplicationContext webApplicationContext;

        private ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setup() {
                this.mockMvc = webAppContextSetup(this.webApplicationContext)
                                .apply(springSecurity())
                                .build();
        }

        @Test
        void testCreateUser() throws Exception {
                SystemUser user = new SystemUser();
                user.setUsername("testuser_" + UUID.randomUUID());
                user.setEmail("test@example.com");
                user.setHashedPassword("password123");
                user.setDisplayName("Test User");
                user.setRole("ROLE_VIEWER");

                mockMvc.perform(post("/api/system/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user))
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.username").value(user.getUsername()))
                                .andExpect(jsonPath("$.email").value(user.getEmail()));
        }

        @Test
        void testListUsers() throws Exception {
                mockMvc.perform(get("/api/system/users")
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());
        }

        @Test
        void testUpdateUser() throws Exception {
                // First create a user
                SystemUser user = new SystemUser();
                user.setUsername("updatetest_" + UUID.randomUUID());
                user.setEmail("update@example.com");
                user.setHashedPassword("password123");
                user.setRole("ROLE_VIEWER");

                MvcResult createResult = mockMvc.perform(post("/api/system/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user))
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                                .andExpect(status().isOk())
                                .andReturn();

                SystemUser createdUser = objectMapper.readValue(
                                createResult.getResponse().getContentAsString(), SystemUser.class);

                // Update the user
                createdUser.setDisplayName("Updated Name");
                createdUser.setEmail("updated@example.com");

                mockMvc.perform(put("/api/system/users/" + createdUser.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createdUser))
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.displayName").value("Updated Name"))
                                .andExpect(jsonPath("$.email").value("updated@example.com"));
        }

        @Test
        void testDeleteUser() throws Exception {
                // Create a user to delete
                SystemUser user = new SystemUser();
                user.setUsername("deletetest_" + UUID.randomUUID());
                user.setEmail("delete@example.com");
                user.setHashedPassword("password123");
                user.setRole("ROLE_VIEWER");

                MvcResult createResult = mockMvc.perform(post("/api/system/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user))
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                                .andExpect(status().isOk())
                                .andReturn();

                SystemUser createdUser = objectMapper.readValue(
                                createResult.getResponse().getContentAsString(), SystemUser.class);

                // Delete the user
                mockMvc.perform(delete("/api/system/users/" + createdUser.getId())
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                                .andExpect(status().isOk());
        }

        @Test
        void testGetCurrentUser() throws Exception {
                String username = "currentuser_" + UUID.randomUUID();

                mockMvc.perform(get("/api/system/users/me")
                                .with(jwt().jwt(builder -> builder.subject(username).claim("username", username))
                                                .authorities(new SimpleGrantedAuthority("ROLE_VIEWER"))))
                                .andExpect(status().isOk());
        }

        @Test
        void testGenerateApiKey() throws Exception {
                // Create a user first
                SystemUser user = new SystemUser();
                user.setUsername("apikeytest_" + UUID.randomUUID());
                user.setEmail("apikey@example.com");
                user.setHashedPassword("password123");
                user.setRole("ROLE_VIEWER");

                MvcResult createResult = mockMvc.perform(post("/api/system/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(user))
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                                .andExpect(status().isOk())
                                .andReturn();

                SystemUser createdUser = objectMapper.readValue(
                                createResult.getResponse().getContentAsString(), SystemUser.class);

                // Generate API key
                mockMvc.perform(post("/api/system/users/" + createdUser.getId() + "/api-key")
                                .with(jwt().jwt(builder -> builder.subject(createdUser.getUsername())
                                                .claim("username", createdUser.getUsername()))
                                                .authorities(new SimpleGrantedAuthority("ROLE_VIEWER"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isString());
        }

        @Test
        void testUnauthorizedAccess() throws Exception {
                // Non-admin should not be able to list users
                mockMvc.perform(get("/api/system/users")
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_VIEWER"))))
                                .andExpect(status().isForbidden());
        }
}
