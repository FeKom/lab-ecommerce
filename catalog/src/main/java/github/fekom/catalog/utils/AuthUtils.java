package github.fekom.catalog.utils;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

/**
 * Utilitário para autenticação e validação de sessões com o User Service.
 *
 * MELHORIAS IMPLEMENTADAS:
 * - Circuit Breaker: Protege contra falhas em cascata
 * - Retry: Tenta novamente em caso de falhas temporárias
 * - Fallback: Retorna resposta segura quando User Service está indisponível
 *
 * Como funciona:
 * 1. Tenta chamar o User Service
 * 2. Se falhar, retenta até 3x com delay de 500ms
 * 3. Se continuar falhando, abre o circuito por 10 segundos
 * 4. Quando circuito está aberto, chama o fallback imediatamente (sem tentar)
 */
@Component
public class AuthUtils {

    private static final Logger logger = LoggerFactory.getLogger(AuthUtils.class);

    private final RestTemplate restTemplate;

    @Value("${app.user-service.url:http://localhost:8085}")
    private String userServiceUrl;

    public AuthUtils(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Extrai o userId da requisição HTTP verificando a sessão no user service.
     *
     * Protegido com Circuit Breaker e Retry:
     * - CircuitBreaker "userService": Abre circuito após 50% de falhas em 20 chamadas
     * - Retry "userService": Retenta até 3x com delay de 500ms
     * - Fallback: fallbackExtractUserId() quando circuito está aberto
     *
     * @param request A requisição HTTP contendo os cookies de sessão
     * @return Optional contendo o userId se a sessão for válida, Optional.empty() caso contrário
     */
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackExtractUserId")
    @Retry(name = "userService")
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
            logger.debug("Preparando chamada para user service: {}", userServiceUrl + "/api/users/session");

            // 3. Fazer chamada para validar sessão (com type-safe generics)
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.exchange(
                userServiceUrl + "/api/users/session",
                HttpMethod.GET,
                entity,
                Map.class
            );

            logger.debug("Resposta do user service - Status: {}", response.getStatusCode());
            logger.debug("Resposta do user service - Body: {}", response.getBody());

            // 4. Se a sessão for válida, extrair userId
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> session = (Map<String, Object>) response.getBody();
                @SuppressWarnings("unchecked")
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

    /**
     * Fallback method chamado quando o Circuit Breaker está aberto.
     *
     * Por que isso é importante:
     * - Evita que toda a aplicação trave quando User Service está down
     * - Retorna resposta rápida (sem esperar timeout)
     * - Loga o problema para investigação
     * - Permite que funcionalidades que não precisam de auth continuem funcionando
     *
     * IMPORTANTE: Este método retorna Optional.empty(), negando acesso.
     * Em produção, você poderia implementar estratégias mais sofisticadas:
     * - Cache local de sessões válidas (com TTL curto)
     * - Modo degradado com funcionalidades limitadas
     * - Autenticação via JWT (se implementado)
     */
    private Optional<String> fallbackExtractUserId(HttpServletRequest request, Throwable throwable) {
        logger.error("User Service indisponível! Circuit Breaker ativado. Erro: {}",
                    throwable.getMessage());
        logger.warn("Negando acesso por segurança. User Service precisa estar online.");

        // Em produção, você poderia verificar cache local aqui
        // ou retornar modo degradado com permissões limitadas

        return Optional.empty();
    }
}