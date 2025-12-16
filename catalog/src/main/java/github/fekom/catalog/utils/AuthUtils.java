package github.fekom.catalog.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@Component
public class AuthUtils {

    private final RestTemplate restTemplate;

    @Value("${user.service.url:http://localhost:8085}")
    private String userServiceUrl;

    public AuthUtils(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Extrai o userId da requisição HTTP verificando a sessão no user service
     * @param request A requisição HTTP contendo os cookies de sessão
     * @return Optional contendo o userId se a sessão for válida, Optional.empty() caso contrário
     */
    public Optional<String> extractUserId(HttpServletRequest request) {
        try {
            // 1. Extrair cookies da requisição
            String cookieHeader = request.getHeader("Cookie");
            if (cookieHeader == null || cookieHeader.isEmpty()) {
                return Optional.empty();
            }

            // 2. Preparar headers para a chamada ao user service
            HttpHeaders headers = new HttpHeaders();
            headers.set("Cookie", cookieHeader);
            HttpEntity<?> entity = new HttpEntity<>(headers);

            // 3. Fazer chamada para validar sessão
            ResponseEntity<Map> response = restTemplate.exchange(
                userServiceUrl + "/api/auth/session",
                HttpMethod.GET,
                entity,
                Map.class
            );

            // 4. Se a sessão for válida, extrair userId
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> session = response.getBody();
                Map<String, Object> user = (Map<String, Object>) session.get("user");
                if (user != null && user.get("id") != null) {
                    return Optional.of(user.get("id").toString());
                }
            }

        } catch (Exception e) {
            // Log do erro, mas não lançar exception - apenas retornar empty
            System.err.println("Erro ao validar sessão: " + e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Verifica se o usuário está autenticado
     * @param request A requisição HTTP
     * @return true se autenticado, false caso contrário
     */
    public boolean isAuthenticated(HttpServletRequest request) {
        return extractUserId(request).isPresent();
    }
}