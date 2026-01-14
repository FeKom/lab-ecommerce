package github.fekom.catalog.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Configuração de Cache com Redis.
 *
 * POR QUE ESTA CONFIGURAÇÃO É IMPORTANTE:
 *
 * 1. @EnableCaching: Ativa o sistema de cache do Spring
 *    - Permite usar @Cacheable, @CacheEvict, @CachePut
 *    - Sem isso, as anotações são ignoradas
 *
 * 2. Serialização JSON:
 *    - StringRedisSerializer para as chaves (ex: "products::123")
 *    - GenericJackson2JsonRedisSerializer para os valores (objetos Product)
 *    - Isso permite armazenar objetos complexos no Redis
 *
 * 3. TTL (Time To Live):
 *    - Configurado para 10 minutos (600 segundos)
 *    - Após esse tempo, o cache expira automaticamente
 *    - Evita dados desatualizados ficarem muito tempo em cache
 *
 * 4. disableCachingNullValues:
 *    - Não cacheia valores null ou Optional.empty()
 *    - Economiza memória Redis
 *    - Evita cache hits falsos
 *
 * TRADE-OFFS:
 * - TTL muito alto (1h+): Risco de dados desatualizados
 * - TTL muito baixo (1min-): Pouco benefício do cache
 * - 10 minutos é um bom equilíbrio para catálogo de produtos
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configura o RedisCacheManager que gerencia todas as operações de cache.
     *
     * @param connectionFactory Factory de conexões Redis (injetada automaticamente)
     * @return RedisCacheManager configurado
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // Configuração padrão para todos os caches
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            // TTL: quanto tempo o cache fica válido
            .entryTtl(Duration.ofMinutes(10))

            // Não cachear valores null (Optional.empty())
            .disableCachingNullValues()

            // Serialização da chave: "products::123" (String)
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()
                )
            )

            // Serialização do valor: Product convertido para JSON
            // Configura ObjectMapper com suporte a Java 8 date/time types
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer(createObjectMapper())
                )
            );

        // Cria o CacheManager com a configuração acima
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .transactionAware() // Integra com @Transactional
            .build();
    }

    /**
     * Cria ObjectMapper configurado para suportar tipos Java 8 date/time.
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
            mapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        return mapper;
    }
}
