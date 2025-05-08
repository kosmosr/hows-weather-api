package app.weather.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Configuration
@Data
@Slf4j
public class QWeatherApiConfig {

    @Value("${qweather.api-host}")
    private String apiHost;

    @Value("${qweather.project-id}")
    private String projectId;

    @Value("${qweather.key-id}")
    private String keyId;

    @Value("${qweather.private-key-path}")
    private Resource privateKeyResource;

    @Value("${QWEATHER_PRIVATE_KEY}")
    private String privateKey;

    @Value("${spring.profiles.active}")
    private String profileActive;

    @PostConstruct
    public void init() {
        if (!profileActive.equals("prod")) {
            try (Reader reader = new InputStreamReader(privateKeyResource.getInputStream(), StandardCharsets.UTF_8)) {
                privateKey = FileCopyUtils.copyToString(reader);
                privateKey = privateKey.trim();
                // 移除 BEGIN 和 END 标记行（如果存在）
                privateKey = privateKey.replaceAll("-----BEGIN PRIVATE KEY-----", "")
                        .replaceAll("-----END PRIVATE KEY-----", "")
                        .replaceAll("\\s+", "");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            log.info("privateKey: {}", privateKey);
        }
    }
}
