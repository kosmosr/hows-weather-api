package app.weather.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
@Order(-1)
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter implements WebFilter {

    private final ReactiveStringRedisTemplate redisTemplate;

    @Value("${api.rate-limit.enabled}")
    private boolean rateLimitEnabled;

    @Value("${api.rate-limit.max-requests-per-day}")
    private int maxRequestsPerDay;

    @Value("${api.rate-limit.paths}")
    private String[] paths;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:ip:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (!rateLimitEnabled || !isPathMatched(exchange.getRequest().getURI())) {
            return chain.filter(exchange);
        }
        // 获取客户端 IP
        String clientIp = getClientIp(exchange.getRequest());
        if (clientIp == null) {
            log.warn("无法获取客户端 IP 地址，跳过限流");
            return chain.filter(exchange); // 或者返回错误，取决于策略
        }

        // 构造 Redis Key
        String currentDay = LocalDate.now().format(DATE_FORMATTER);
        String redisKey = RATE_LIMIT_PREFIX + clientIp + ":" + currentDay;

        // 执行 Redis 操作并检查限流
        return redisTemplate.opsForValue()
                .increment(redisKey) // 增加计数
                .flatMap(currentCount -> {
                    Mono<Void> resultMono;
                    if (currentCount == 1L) {
                        // 如果是当天第一次访问，设置过期时间为 24 小时
                        resultMono = redisTemplate.expire(redisKey, Duration.ofHours(24))
                                .then(checkLimitAndProceed(exchange, chain, redisKey, currentCount, clientIp));
                    } else {
                        // 非首次访问，直接检查限流
                        resultMono = checkLimitAndProceed(exchange, chain, redisKey, currentCount, clientIp);
                    }
                    return resultMono;
                })
                .onErrorResume(ex -> {
                    // Redis 操作异常，可以选择放行或记录错误后拒绝
                    log.error("Redis 操作失败，限流检查异常 for IP: {}", clientIp, ex);
                    // 策略：放行，避免 Redis 故障影响服务
                    return chain.filter(exchange);
                });
    }

    private boolean isPathMatched(URI uri) {
        for (String path : paths) {
            if (uri.getPath().equals(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查计数值并决定是放行还是拒绝
     */
    private Mono<Void> checkLimitAndProceed(ServerWebExchange exchange, WebFilterChain chain, String redisKey, long currentCount, String clientIp) {
        if (currentCount > maxRequestsPerDay) {
            // 超过限制，返回 429
            log.warn("IP: {} 已达到每日请求上限 ({}), Key: {}, Current Count: {}", clientIp, maxRequestsPerDay, redisKey, currentCount);
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            // 可以选择性地设置响应体
            // response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            // return response.writeWith(Mono.just(response.bufferFactory().wrap("{\"message\":\"Too Many Requests\"}".getBytes())));
            return response.setComplete(); // 直接完成响应，不传递给后续 Filter
        } else {
            // 未超限，放行
            log.info("IP: {} 请求计数: {}/{} (Key: {})", clientIp, currentCount, maxRequestsPerDay, redisKey);
            return chain.filter(exchange);
        }
    }

    /**
     * 获取客户端真实 IP 地址 (考虑 X-Forwarded-For 和 X-Real-IP)
     * 注意：这只是一个基础实现，生产环境中可能需要更复杂的信任代理配置
     */
    private String getClientIp(ServerHttpRequest request) {
        // 优先检查 X-Forwarded-For
        String xff = request.getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            // X-Forwarded-For 可能包含多个 IP，取第一个非 unknown 的
            String[] ips = xff.split(",");
            for (String ip : ips) {
                String trimmedIp = ip.trim();
                if (!trimmedIp.isEmpty() && !"unknown".equalsIgnoreCase(trimmedIp)) {
                    return trimmedIp;
                }
            }
        }

        // 其次检查 X-Real-IP
        String realIp = request.getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isEmpty() && !"unknown".equalsIgnoreCase(realIp)) {
            return realIp;
        }

        // 最后使用 remoteAddress
        return Optional.ofNullable(request.getRemoteAddress())
                .map(InetSocketAddress::getAddress)
                .map(java.net.InetAddress::getHostAddress)
                .orElse(null);
    }
}
