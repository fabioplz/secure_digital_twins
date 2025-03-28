package security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;

import java.text.ParseException;
import java.util.Map;

@Interceptor
@Component
@ComponentScan
public class Authentication {

    private static final Logger logger = LoggerFactory.getLogger(Authentication.class);
    private static final String INTROSPECTION_URL = "http://keycloak:8080/realms/master/protocol/openid-connect/token/introspect";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Mappa di client_id e client_secret (recuperati da variabili d'ambiente)
    private static final Map<String, String> CLIENT_CREDENTIALS = Map.of(
        "upload-client" , "SIQFQCsoHAsyPcxnuhGxA4mL6YclpeeG",
        "analytics-client", "HbK3QbKCdhHyu7JFy7oT4cy1OE8eYFro",
        "visualization-client", "LY6zkcsAi5lJeTXnFS1kRYcm4g5cDfVB"
    );

    /**
     * Metodo invocato per ogni richiesta in ingresso.
     * Estrae il token dall'header Authorization e lo valida.
     */
    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLED)
    public void incomingRequest(RequestDetails requestDetails) {
        String authHeader = requestDetails.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthenticationException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7); // Rimuove "Bearer " dal token
        String clientId = extractClientId(token);

        // Recupera il client_secret associato
        String clientSecret = CLIENT_CREDENTIALS.get(clientId);
        if (clientSecret == null) {
            throw new AuthenticationException("Unauthorized client: " + clientId);
        }

        // Valida il token
        if (!validateToken(token, clientId, clientSecret)) {
            throw new AuthenticationException("Request is not authorized");
        }
    }

    /**
     * Decodifica il token JWT per ottenere il client_id dal campo "azp".
     * 
     * @param token il token JWT
     * @return il client_id che ha generato il token
     */
    private String extractClientId(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            return claims.getStringClaim("azp"); // Campo "azp" contiene il client_id
        } catch (ParseException e) {
            logger.error("Error parsing JWT token", e);
            throw new AuthenticationException("Invalid token format");
        }
    }

    /**
     * Valida il token effettuando una chiamata all'endpoint di introspezione di Keycloak.
     *
     * @param token        il token da validare
     * @param clientId     il client_id associato al token
     * @param clientSecret il client_secret corrispondente
     * @return true se il token è valido, false altrimenti
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
                } else {
                    logger.warn("Token is invalid or expired for client: {}", clientId);
                }
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
