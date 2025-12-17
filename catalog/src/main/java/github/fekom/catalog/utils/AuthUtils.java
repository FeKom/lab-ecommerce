package github.fekom.catalog.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@Component
public class AuthUtils {

    private static final Logger logger = LoggerFactory.getLogger(AuthUtils.class);

    private final RestTemplate restTemplate;

    @Value("${user.service.url:http://localhost:6060}")
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
            logger.debug("Iniciando extração do userId");

            // 1. Extrair cookies da requisição
            String cookieHeader = request.getHeader("Cookie");
            logger.debug("Cookie header: {}", cookieHeader);

            if (cookieHeader == null || cookieHeader.isEmpty()) {
                logger.debug("Nenhum cookie encontrado na requisição");
                return Optional.empty();
            }

            // 2. Preparar headers para a chamada ao user service
            HttpHeaders headers = new HttpHeaders();
            headers.set("Cookie", cookieHeader);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            logger.debug("Preparando chamada para user service: {}", userServiceUrl + "/api/auth/get-session");

            // 3. Fazer chamada para validar sessão
            ResponseEntity<Map> response = restTemplate.exchange(
                userServiceUrl + "/api/auth/get-session",
                HttpMethod.GET,
                entity,
                Map.class
            );

            logger.debug("Resposta do user service - Status: {}", response.getStatusCode());
            logger.debug("Resposta do user service - Body: {}", response.getBody());

            // 4. Se a sessão for válida, extrair userId
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> session = response.getBody();
                Map<String, Object> user = (Map<String, Object>) session.get("user");
                logger.debug("Dados da sessão extraídos: {}", user);
                if (user != null && user.get("id") != null) {
                    String userId = user.get("id").toString();
                    logger.info("UserId extraído com sucesso: {}", userId);
                    return Optional.of(userId);
                } else {
                    logger.warn("User não encontrado na sessão ou sem ID");
                }
            } else {
                logger.warn("Sessão inválida ou resposta vazia");
            }

        } catch (Exception e) {
            // Log do erro, mas não lançar exception - apenas retornar empty
            logger.error("Erro ao validar sessão: {}", e.getMessage(), e);
        }

        logger.debug("Retornando Optional.empty() - usuário não autenticado");
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