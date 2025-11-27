package com.testnext.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.http.MediaType;

@SpringBootTest(classes = com.testnext.config.TestAppConfig.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver"
    })
@ActiveProfiles("dev")
public class DevProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void createTenantAndProject_shouldReturnOk() throws Exception {
        // Create tenant as SYSTEM_ADMIN
        mockMvc.perform(post("/api/tenants")
                .with(jwt().authorities(() -> "SYSTEM_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"acme\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.schemaName").exists());

        // Optionally, list tenants (authorized)
        mockMvc.perform(get("/api/tenants")
                .with(jwt().authorities(() -> "SYSTEM_ADMIN")))
                .andExpect(status().isOk());
    }
}
