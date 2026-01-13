package github.fekom.catalog.infrastructure.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Tratamento global de exceções da aplicação.
 *
 * POR QUE ISSO É IMPORTANTE:
 *
 * 1. SEGURANÇA:
 *    - Evita expor stack traces ao cliente (informação sensível)
 *    - Retorna mensagens genéricas para erros internos
 *    - Não vaza detalhes da implementação
 *
 * 2. CONSISTÊNCIA:
 *    - Todas as respostas de erro seguem o mesmo formato
 *    - Frontend pode tratar erros de forma padronizada
 *    - Facilita debugging com logs estruturados
 *
 * 3. CÓDIGO HTTP CORRETO:
 *    - 404 para "não encontrado" (não 500)
 *    - 400 para validação (não 500)
 *    - 401 para não autenticado
 *    - 500 apenas para erros realmente inesperados
 *
 * FORMATO DA RESPOSTA DE ERRO:
 * {
 *   "timestamp": "2025-01-12T10:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Product not found with id: 123",
 *   "path": "/api/products/123"
 * }
 *
 * @RestControllerAdvice: Aplica-se a todos os @RestController
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Classe interna para representar erros de forma consistente.
     */
    public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
    ) {
        public static ErrorResponse of(HttpStatus status, String message, String path) {
            return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
            );
        }
    }

    /**
     * Trata IllegalArgumentException (ex: "Product not found").
     *
     * Retorna 404 NOT FOUND.
     *
     * QUANDO É LANÇADO:
     * - ProductService.delete() quando produto não existe
     * - Validações de negócio que falham
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Argumento inválido: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
            HttpStatus.BAD_REQUEST,
            ex.getMessage(),
            "" // Path seria ideal, mas requer HttpServletRequest
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Trata NoSuchElementException (ex: Optional.orElseThrow()).
     *
     * Retorna 404 NOT FOUND.
     *
     * QUANDO É LANÇADO:
     * - ProductController.getProductById() quando produto não existe
     * - Qualquer .orElseThrow() sem mensagem customizada
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException ex) {
        logger.warn("Recurso não encontrado: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
            HttpStatus.NOT_FOUND,
            "Resource not found",
            ""
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Trata erros de validação (@Valid no @RequestBody).
     *
     * Retorna 400 BAD REQUEST com detalhes dos campos inválidos.
     *
     * QUANDO É LANÇADO:
     * - CreateProductRequest com name vazio
     * - Price negativo
     * - Stock negativo
     * - Qualquer violação das anotações @NotBlank, @Min, @Max, etc
     *
     * RESPOSTA:
     * {
     *   "timestamp": "...",
     *   "status": 400,
     *   "error": "Validation Failed",
     *   "message": "Invalid fields",
     *   "errors": {
     *     "name": "must not be blank",
     *     "price": "must be greater than 0"
     *   }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        logger.warn("Erro de validação: {} campos inválidos", ex.getBindingResult().getErrorCount());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            logger.debug("Campo inválido: {} - {}", fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("message", "Invalid fields");
        response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Trata ResourceAccessException (erro de conexão com serviços externos).
     *
     * Retorna 503 SERVICE UNAVAILABLE.
     *
     * QUANDO É LANÇADO:
     * - AuthUtils quando User Service está down
     * - RestTemplate.exchange() quando há timeout ou connection refused
     * - Circuit Breaker quando todas as tentativas falham
     *
     * IMPORTANTE: Não expõe detalhes do erro interno (segurança)
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleResourceAccess(ResourceAccessException ex) {
        logger.error("Erro ao acessar recurso externo: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
            HttpStatus.SERVICE_UNAVAILABLE,
            "External service temporarily unavailable. Please try again later.",
            ""
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    /**
     * Trata IllegalStateException (erros de estado inválido).
     *
     * Retorna 409 CONFLICT.
     *
     * QUANDO É LANÇADO:
     * - NoSQLProductRepository.update() quando falha ao recuperar produto atualizado
     * - Operações que violam regras de negócio relacionadas a estado
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        logger.error("Estado ilegal: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
            HttpStatus.CONFLICT,
            ex.getMessage(),
            ""
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Trata RuntimeException genérica (último recurso).
     *
     * Retorna 500 INTERNAL SERVER ERROR.
     *
     * QUANDO É LANÇADO:
     * - Qualquer erro não tratado pelos handlers acima
     * - Bugs inesperados no código
     *
     * IMPORTANTE:
     * - Loga stack trace completo para debugging
     * - Retorna mensagem genérica ao cliente (não vaza detalhes)
     * - Em produção, deve disparar alertas (PagerDuty, etc)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        // Log completo do erro para debugging
        logger.error("Erro inesperado: {}", ex.getMessage(), ex);

        // Mensagem genérica para o cliente (não vaza detalhes)
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred. Please try again later.",
            ""
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Trata Exception genérica (fallback absoluto).
     *
     * Retorna 500 INTERNAL SERVER ERROR.
     *
     * Captura qualquer exceção checked que não foi tratada.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Erro não tratado: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An error occurred while processing your request.",
            ""
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
