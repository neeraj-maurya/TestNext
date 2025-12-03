package com.testnext.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testnext.api.dto.TenantDto;
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
public class TenantApiTest {

        private MockMvc mockMvc;

        @Autowired
        private WebApplicationContext webApplicationContext;

        private ObjectMapper objectMapper = new ObjectMapper();
        private UUID testManagerId;

        @BeforeEach
        void setup() throws Exception {
                this.mockMvc = webAppContextSetup(this.webApplicationContext)
                                .apply(springSecurity())
                                .build();

                // Create a test manager for tenant creation
                SystemUser testManager = new SystemUser();
                testManager.setUsername("tm_tenant_" + UUID.randomUUID());
                testManager.setEmail("tm@example.com");
                testManager.setHashedPassword("password123");
                testManager.setRole("ROLE_TEST_MANAGER");

                MvcResult result = mockMvc.perform(post("/api/system/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testManager))
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                                .andExpect(status().isOk())
                                .andReturn();

                SystemUser created = objectMapper.readValue(result.getResponse().getContentAsString(),
                                SystemUser.class);
                testManagerId = created.getId();
        }

        @Test
        void testCreateTenant() throws Exception {
                TenantDto tenant = new TenantDto();
                tenant.name = "Test Tenant " + UUID.randomUUID();
                tenant.schemaName = "test_schema_" + UUID.randomUUID().toString().replace("-", "");
                tenant.testManagerId = testManagerId;

                mockMvc.perform(post("/api/tenants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tenant))
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.name").value(tenant.name))
                                .andExpect(jsonPath("$.schemaName").value(tenant.schemaName));
        }

        @Test
        void testListTenants() throws Exception {
                mockMvc.perform(get("/api/tenants")
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());
        }

        @Test
        void testUpdateTenant() throws Exception {
                // Create tenant
                TenantDto tenant = new TenantDto();
                tenant.name = "Update Test Tenant";
                tenant.schemaName = "update_schema_" + UUID.randomUUID().toString().replace("-", "");
                tenant.testManagerId = testManagerId;

                MvcResult createResult = mockMvc.perform(post("/api/tenants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tenant))
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                                .andExpect(status().isOk())
                                .andReturn();

                TenantDto created = objectMapper.readValue(createResult.getResponse().getContentAsString(),
                                TenantDto.class);

                // Update tenant
                created.name = "Updated Tenant Name";

                mockMvc.perform(put("/api/tenants/" + created.id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(created))
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Updated Tenant Name"));
        }

        @Test
        void testDeleteTenant() throws Exception {
                // Create tenant
                TenantDto tenant = new TenantDto();
                tenant.name = "Delete Test Tenant";
                tenant.schemaName = "delete_schema_" + UUID.randomUUID().toString().replace("-", "");
                tenant.testManagerId = testManagerId;

                MvcResult createResult = mockMvc.perform(post("/api/tenants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tenant))
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                                .andExpect(status().isOk())
                                .andReturn();

                TenantDto created = objectMapper.readValue(createResult.getResponse().getContentAsString(),
                                TenantDto.class);

                // Delete tenant
                mockMvc.perform(delete("/api/tenants/" + created.id)
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                                .andExpect(status().isOk());
        }
}
