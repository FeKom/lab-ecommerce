package github.fekom.catalog.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Configuração CORS para permitir que o frontend Angular acesse a API.
     *
     * Por que isso é importante:
     * - CORS (Cross-Origin Resource Sharing) é uma política de segurança do navegador
     * - Sem esta configuração, o browser bloqueia requests de localhost:4200 para localhost:8080
     * - Em produção, substitua "http://localhost:4200" pela URL real do frontend
     *
     * Configurações aplicadas:
     * - allowedOrigins: Especifica de onde as requisições podem vir
     * - allowedMethods: Quais métodos HTTP são permitidos (GET, POST, PUT, DELETE, etc)
     * - allowedHeaders: Permite todos os headers (necessário para autenticação)
     * - allowCredentials: Permite envio de cookies (necessário para sessões)
     * - maxAge: Quanto tempo o browser pode cachear a resposta preflight (1h)
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:4200",
                    "http://localhost:80",
                    "http://localhost"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // 1 hour
    }
}