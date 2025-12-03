package com.testnext.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testnext.api.dto.ProjectDto;
import com.testnext.api.dto.TenantDto;
import com.testnext.api.dto.TestDto;
import com.testnext.api.dto.TestSuiteDto;
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
public class TestSuiteAndCaseApiTest {

        private MockMvc mockMvc;

        @Autowired
        private WebApplicationContext webApplicationContext;

        private ObjectMapper objectMapper = new ObjectMapper();
        private Long projectId;
        private Long suiteId;
        private String testManagerUsername;

        @BeforeEach
        void setup() throws Exception {
                this.mockMvc = webAppContextSetup(this.webApplicationContext)
                                .apply(springSecurity())
                                .build();

                // Create test manager
                SystemUser testManager = new SystemUser();
                testManager.setUsername("tm_suite_" + UUID.randomUUID());
                testManager.setEmail("tm_suite@example.com");
                testManager.setHashedPassword("password123");
                testManager.setRole("ROLE_TEST_MANAGER");

                MvcResult userResult = mockMvc.perform(post("/api/system/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testManager))
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                                .andExpect(status().isOk())
                                .andReturn();

                SystemUser createdManager = objectMapper.readValue(userResult.getResponse().getContentAsString(),
                                SystemUser.class);
                testManagerUsername = createdManager.getUsername();

                // Create tenant
                TenantDto tenant = new TenantDto();
                tenant.name = "Suite Test Tenant";
                tenant.schemaName = "suite_schema_" + UUID.randomUUID().toString().replace("-", "");
                tenant.testManagerId = createdManager.getId();

                MvcResult tenantResult = mockMvc.perform(post("/api/tenants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tenant))
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                                .andExpect(status().isOk())
                                .andReturn();

                TenantDto createdTenant = objectMapper.readValue(tenantResult.getResponse().getContentAsString(),
                                TenantDto.class);

                // Create project
                ProjectDto project = new ProjectDto();
                project.name = "Suite Test Project";
                project.description = "For suite testing";

                MvcResult projectResult = mockMvc.perform(post("/api/tenants/" + createdTenant.id + "/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(project))
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andReturn();

                ProjectDto createdProject = objectMapper.readValue(projectResult.getResponse().getContentAsString(),
                                ProjectDto.class);
                projectId = createdProject.id;
        }

        @Test
        void testCreateTestSuite() throws Exception {
                TestSuiteDto suite = new TestSuiteDto();
                suite.name = "Smoke Test Suite";
                suite.description = "Suite for smoke tests";

                mockMvc.perform(post("/api/projects/" + projectId + "/suites")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(suite))
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.name").value("Smoke Test Suite"))
                                .andExpect(jsonPath("$.description").value("Suite for smoke tests"));
        }

        @Test
        void testListTestSuites() throws Exception {
                // Create a suite
                TestSuiteDto suite = new TestSuiteDto();
                suite.name = "List Test Suite";
                suite.description = "For listing";

                mockMvc.perform(post("/api/projects/" + projectId + "/suites")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(suite))
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk());

                // List suites
                mockMvc.perform(get("/api/projects/" + projectId + "/suites")
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].name").value("List Test Suite"));
        }

        @Test
        void testGetTestSuite() throws Exception {
                // Create a suite
                TestSuiteDto suite = new TestSuiteDto();
                suite.name = "Get Test Suite";
                suite.description = "For get test";

                MvcResult createResult = mockMvc.perform(post("/api/projects/" + projectId + "/suites")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(suite))
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andReturn();

                TestSuiteDto created = objectMapper.readValue(createResult.getResponse().getContentAsString(),
                                TestSuiteDto.class);

                // Get suite
                mockMvc.perform(get("/api/projects/" + projectId + "/suites/" + created.id)
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(created.id))
                                .andExpect(jsonPath("$.name").value("Get Test Suite"));
        }

        @Test
        void testCreateTestCase() throws Exception {
                // Create suite first
                TestSuiteDto suite = new TestSuiteDto();
                suite.name = "Test Case Suite";
                suite.description = "For test case testing";

                MvcResult suiteResult = mockMvc.perform(post("/api/projects/" + projectId + "/suites")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(suite))
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andReturn();

                TestSuiteDto createdSuite = objectMapper.readValue(suiteResult.getResponse().getContentAsString(),
                                TestSuiteDto.class);

                // Create test case
                TestDto testCase = new TestDto();
                testCase.name = "Login Test";

                mockMvc.perform(post("/api/test-suites/" + createdSuite.id + "/tests")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testCase))
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.name").value("Login Test"));
        }

        @Test
        void testListTestCases() throws Exception {
                // Create suite
                TestSuiteDto suite = new TestSuiteDto();
                suite.name = "Test Case List Suite";

                MvcResult suiteResult = mockMvc.perform(post("/api/projects/" + projectId + "/suites")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(suite))
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andReturn();

                TestSuiteDto createdSuite = objectMapper.readValue(suiteResult.getResponse().getContentAsString(),
                                TestSuiteDto.class);

                // Create test case
                TestDto testCase = new TestDto();
                testCase.name = "List Test Case";

                mockMvc.perform(post("/api/test-suites/" + createdSuite.id + "/tests")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testCase))
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk());

                // List test cases
                mockMvc.perform(get("/api/test-suites/" + createdSuite.id + "/tests")
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].name").value("List Test Case"));
        }

        @Test
        void testListTestCasesByProject() throws Exception {
                mockMvc.perform(get("/api/projects/" + projectId + "/tests")
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());
        }

        @Test
        void testDeleteTestCase() throws Exception {
                // Create suite
                TestSuiteDto suite = new TestSuiteDto();
                suite.name = "Delete Test Suite";

                MvcResult suiteResult = mockMvc.perform(post("/api/projects/" + projectId + "/suites")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(suite))
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andReturn();

                TestSuiteDto createdSuite = objectMapper.readValue(suiteResult.getResponse().getContentAsString(),
                                TestSuiteDto.class);

                // Create test case
                TestDto testCase = new TestDto();
                testCase.name = "Delete Test Case";

                MvcResult testResult = mockMvc.perform(post("/api/test-suites/" + createdSuite.id + "/tests")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testCase))
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andReturn();

                TestDto createdTest = objectMapper.readValue(testResult.getResponse().getContentAsString(),
                                TestDto.class);

                // Delete test case
                mockMvc.perform(delete("/api/tests/" + createdTest.id)
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk());
        }
}
