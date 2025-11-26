package security;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Interceptor
@Component
public class Authentication {
    private static final Logger logger = LoggerFactory.getLogger(Authentication.class);
    private static final String INTROSPECTION_URL = "http://keycloak:8080/realms/master/protocol/openid-connect/token/introspect";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Authentication() {
    }

    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLED)
    public void incomingRequest(RequestDetails requestDetails) {
        String authHeader = requestDetails.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String clientId = extractClientId(token);

            String clientSecret = getClientCredentials().get(clientId);
            if (clientSecret == null || clientSecret.isEmpty()) {
                throw new AuthenticationException("Unauthorized client or secret not configured: " + clientId);
            }

            if (!validateToken(token, clientId, clientSecret)) {
                throw new AuthenticationException("Request is not authorized");
            }
        } else {
            throw new AuthenticationException("Missing or invalid Authorization header");
        }
    }

    /**
     * Estrae il client ID ("azp") dal token JWT.
     */
    private String extractClientId(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            String clientId = claims.getStringClaim("azp");
            if (clientId == null || clientId.isEmpty()) {
                throw new AuthenticationException("Token missing 'azp' claim");
            }
            return clientId;
        } catch (ParseException e) {
            logger.error("Error parsing JWT token", e);
            throw new AuthenticationException("Invalid token format");
        }
    }

    /**
     * Ritorna la mappa dei client secrets letti dalle variabili d'ambiente.
     */
    private Map<String, String> getClientCredentials() {
        Map<String, String> creds = new HashMap<>();
        creds.put("upload-client", System.getenv("UPLOAD_CLIENT_SECRET"));
        creds.put("analytics-client", System.getenv("ANALYTICS_CLIENT_SECRET"));
        creds.put("visualization-client", System.getenv("VISUALIZATION_CLIENT_SECRET"));
        return creds;
    }

    /**
     * Valida il token chiamando l'endpoint di introspezione di Keycloak.
     */
    private boolean validateToken(String token, String clientId, String clientSecret) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("token", token);
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(INTROSPECTION_URL, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                boolean isActive = jsonNode.path("active").asBoolean(false);
                if (isActive) {
                    logger.info("Token is valid for client: {}", clientId);
                    return true;
                }
                logger.warn("Token is invalid or expired for client: {}", clientId);
            } else {
                logger.error("Failed to introspect token. HTTP Status: {}", response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            logger.error("Token validation failed: {}", e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Unexpected error during token validation", e);
        }

        return false;
    }
}
