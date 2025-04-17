package app.weather.service;

import app.weather.model.qweather.DailyWeatherData;
import app.weather.model.qweather.GeoLookupData;
import app.weather.model.qweather.RealTimeWeatherData;
import app.weather.model.qweather.param.TopCityQuery;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Slf4j
class QWeatherApiTest {

    @Autowired
    private QWeatherApi qWeatherApi;

    @Test
    void generateJWT() {
        String jwtToken = qWeatherApi.generateJWT();
        log.info("jwtToken: {}", jwtToken);
    }

    @Test
    void lookupGeo() {
        qWeatherApi.lookupGeo("成都");
    }



    @Test
    void topCityList() {
        TopCityQuery query = TopCityQuery.builder()
                .number("20")
                .range("cn")
                .build();
        List<GeoLookupData> data = qWeatherApi.topCityList(query);
        log.info("data: {}", data);
    }

    @Test
    void weatherNow() {
        RealTimeWeatherData data = qWeatherApi.weatherNow("104.09,30.65");
        log.info("data: {}", data);
        assertNotNull(data);
    }

    @Test
    void dailyWeather() {
        List<DailyWeatherData> data = qWeatherApi.dailyWeather("104.09,30.65", 1);
        log.info("data: {}", data);
        assertNotNull(data);
    }
}
