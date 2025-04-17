package app.weather.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@TestPropertySource(properties = {
        "qweather.project-id=test",
        "qweather.key-id=test"
})
class QWeatherApiConfigTest {

    @Autowired
    private QWeatherApiConfig qWeatherApiConfig;

    @Test
    void testQWeatherApiConfig() {
        assertNotNull(qWeatherApiConfig);

        log.info("privateKey: {}", qWeatherApiConfig.getPrivateKey());
        assertEquals("test", qWeatherApiConfig.getProjectId());
        assertEquals("test", qWeatherApiConfig.getKeyId());
        assertNotNull(qWeatherApiConfig.getPrivateKey());
    }
}
