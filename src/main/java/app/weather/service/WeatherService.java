package app.weather.service;

import app.weather.model.qweather.DailyWeatherResponse;
import app.weather.model.qweather.HourlyWeatherResponse;
import app.weather.model.qweather.RealTimeWeatherResponse;
import app.weather.model.qweather.WeatherIndicesResponse;
import app.weather.model.vo.GetWeatherVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@Slf4j
public class WeatherService {
    private final QWeatherApi qWeatherApi;

    @Autowired
    public WeatherService(QWeatherApi qWeatherApi) {
        this.qWeatherApi = qWeatherApi;
    }

    /**
     * 获取聚合天气数据
     *
     * @param location 经纬度
     * @return
     */
    public Mono<GetWeatherVO> getWeather(String location) {

        // 并发调用天气API
        Mono<RealTimeWeatherResponse> realTimeWeatherMono = qWeatherApi.getRealtimeWeather(location)
                .onErrorResume(e -> {
                    log.error("获取实时天气失败: {}", e.getMessage());
                    return Mono.justOrEmpty(Optional.empty());
                });
        Mono<DailyWeatherResponse> dailyWeatherMono = qWeatherApi.getDailyWeather(location)
                .onErrorResume(e -> {
                    log.error("获取每日天气失败: {}", e.getMessage());
                    return Mono.justOrEmpty(Optional.empty());
                });
        Mono<HourlyWeatherResponse> hourlyWeatherMono = qWeatherApi.getHourlyWeatherForecast24h(location)
                .onErrorResume(e -> {
                    log.error("获取逐小时天气失败: {}", e.getMessage());
                    return Mono.justOrEmpty(Optional.empty());
                });
        // 获取天气指数 包含运动指数、洗车指数、穿衣指数、紫外线指数、晾晒指数
        Mono<WeatherIndicesResponse> weatherIndicesMono = qWeatherApi.getWeatherIndices(location, "1,2,3,5,14")
                .onErrorResume(e -> {
                    log.error("获取天气指数失败: {}", e.getMessage());
                    return Mono.justOrEmpty(Optional.empty());
                });

        // 3. 聚合天气数据
        return Mono.zip(realTimeWeatherMono, dailyWeatherMono, hourlyWeatherMono, weatherIndicesMono)
                .map(tuple -> {
                    RealTimeWeatherResponse realTimeWeatherResponse = tuple.getT1();
                    DailyWeatherResponse dailyWeatherResponse = tuple.getT2();
                    HourlyWeatherResponse hourlyWeatherResponse = tuple.getT3();
                    WeatherIndicesResponse weatherIndicesResponse = tuple.getT4();
                    // 构建返回对象
                    GetWeatherVO vo = new GetWeatherVO();
                    vo.buildRealtimeWeather(realTimeWeatherResponse);
                    vo.buildDailyWeather(dailyWeatherResponse);
                    vo.buildHourlyWeather(hourlyWeatherResponse);
                    vo.buildWeatherIndices(weatherIndicesResponse);
                    return vo;
                })
                .doOnError(e -> log.error("聚合天气数据时发生错误: location: {}", location, e));
    }
}
