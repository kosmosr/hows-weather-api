package app.weather.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private final QWeatherApiConfig qWeatherApiConfig;

    @Autowired
    public WebClientConfig(QWeatherApiConfig qWeatherApiConfig) {
        this.qWeatherApiConfig = qWeatherApiConfig;
    }

    @Bean
    public WebClient qWeatherWebClient() {
        return WebClient.builder()
                .baseUrl(qWeatherApiConfig.getApiHost())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
