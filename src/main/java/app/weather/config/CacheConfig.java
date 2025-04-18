package app.weather.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class CacheConfig {
    /**
     * 配置 Redis 缓存管理器.
     * 设置默认的缓存过期时间 (TTL) 和序列化方式 (String for key, Jackson JSON for value).
     * 为 'weatherIndices', 'jwtTokenCache', 'hourlyWeatherCache' 设置特定的 TTL.
     * @return RedisCacheManagerBuilderCustomizer
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        // Jackson 序列化器配置
        GenericJackson2JsonRedisSerializer jacksonSerializer = createGenericJackson2JsonRedisSerializer();
        // String 序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        return (builder) -> builder
                // 天气指数缓存 (6 小时)
                .withCacheConfiguration("weatherIndices",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofHours(6)) // 天气指数缓存 6 小时
                                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jacksonSerializer))
                                .disableCachingNullValues()
                )
                // JWT Token 缓存 (24 小时)
                .withCacheConfiguration("jwtTokenCache",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofHours(24)) // JWT 缓存 24 小时
                                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                                .disableCachingNullValues()
                )
                // 逐小时天气缓存 (30 分钟)
                .withCacheConfiguration("hourlyWeatherCache",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(30)) // 逐小时天气缓存 30 分钟 (符合文档建议 30-60 min)
                                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jacksonSerializer))
                                .disableCachingNullValues()
                )
                // 实时天气缓存 (10 分钟)
                .withCacheConfiguration("realtimeWeatherCache",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(10)) // 实时数据缓存 10 分钟 (符合文档建议 10-30 min)
                                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jacksonSerializer))
                                .disableCachingNullValues()
                )
                // 每日天气缓存 (1 小时)
                .withCacheConfiguration("dailyWeatherCache",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofHours(1)) // 每日天气缓存 1小时
                                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jacksonSerializer))
                                .disableCachingNullValues()
                )
                // 其他缓存默认设置 (1 小时)
                .cacheDefaults(
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofHours(1)) // 其他缓存默认 TTL 为 1 小时
                                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jacksonSerializer))
                                .disableCachingNullValues()
                );
    }
    /**
     * 创建并配置 Jackson 序列化器.
     * @return GenericJackson2JsonRedisSerializer
     */
    private GenericJackson2JsonRedisSerializer createGenericJackson2JsonRedisSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}
