package com.bank.onboarding.interventionservice;

import com.bank.onboarding.commonslib.persistence.models.Intervention;
import com.bank.onboarding.commonslib.persistence.repositories.InterventionRepository;
import com.bank.onboarding.commonslib.web.SecurityConfig;
import com.bank.onboarding.interventionservice.services.InterventionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static com.bank.onboarding.commonslib.utils.TestOnboardingUtils.buildIntervention;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InterventionApiIntegrationTests {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InterventionRepository interventionRepository;

    @Autowired
    private InterventionService interventionService;

    @Autowired
    private SecurityConfig securityConfig;

    @Value("${bank.onboarding.client.id}")
    private String clientId;

    private String token;

    private HttpHeaders httpHeaders;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        token = securityConfig.generateJWToken();
        httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setBearerAuth(token);
        httpHeaders.set("X-Onboarding-Client-Id", clientId);
        objectMapper.registerModule(new JavaTimeModule());
    }

    private String createURLWithPort() {
        return "http://localhost:" + port + "/interventions/";
    }

    private String insertInterventionDB() {
        String IBAN = "PT50 0000 2927 8040 8012 4082 5";
        Intervention interventionSaved = interventionRepository.save(
                buildIntervention(IBAN.trim().replaceAll(" ", "").substring(IBAN.length()-19),"C123456789"));

        return interventionSaved.getId();
    }

    @Test
    void deleteInterventionTest(){
        String interventionId = insertInterventionDB();

        HttpEntity<String> entity = new HttpEntity<>(null, httpHeaders);
        ResponseEntity<?> response = restTemplate.exchange(
                createURLWithPort() + interventionId, HttpMethod.DELETE, entity, new ParameterizedTypeReference<>(){});

        assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(204));
        assertTrue(interventionRepository.findById(interventionId).isEmpty());
    }
}
