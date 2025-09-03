package github.fekom.catalog.infrastructure.cache;

// import java.time.Duration;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.data.redis.cache.RedisCacheConfiguration;
// import org.springframework.data.redis.cache.RedisCacheManager;
// import org.springframework.data.redis.connection.RedisConnection;
// import org.springframework.data.redis.connection.RedisConnectionFactory;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
// import org.springframework.data.redis.serializer.RedisSerializationContext;
// import org.springframework.data.redis.serializer.StringRedisSerializer;

// @Configuration
// public class RedisCacheConfig {

//     @Bean
//     public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
//         //cria um serializador para os objetos product
//         //GenericJackson2JsonRedisSerializer é mais flexivel e não precisa que implementem Serializable
//         RedisSerializationContext.SerializationPair<Object> jsonSerializer =
//             RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer());

//         RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
//             .entryTtl(Duration.ofMinutes(30))
//             .disableCachingNullValues()
//             //configura como as keys vão ser serializadas(string é a forma mais comum)
//             .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
//             .serializeValuesWith(jsonSerializer);

//         return RedisCacheManager.builder(connectionFactory)
//             .cacheDefaults(cacheConfiguration)
//             .build();
//     }

//     @Bean
//     public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
//          try {
//             RedisConnection connection = connectionFactory.getConnection();
//             String result = connection.ping();
//             System.out.println("Conexão com Redis: " + result);
//         } catch (Exception e) {
//             System.out.println("Falha na conexão com Redis: " + e.getMessage());
//             e.printStackTrace();
//         }
//         System.out.println("Conexão com Redis:");

//         RedisTemplate<String, Object> template = new RedisTemplate<>();
//         template.setConnectionFactory(connectionFactory);
//         template.setKeySerializer(new StringRedisSerializer());
//         template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
//         template.afterPropertiesSet();
//         return template;
//     }
//}
