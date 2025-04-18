package app.weather.controller;

import app.weather.model.qweather.DailyWeatherResponse;
import app.weather.model.qweather.RealTimeWeatherResponse;
import app.weather.model.qweather.WeatherIndicesResponse;
import app.weather.model.response.ResultResponse;
import app.weather.model.response.StatusEnum;
import app.weather.model.vo.GeoLookupVO;
import app.weather.model.vo.GetWeatherVO;
import app.weather.service.CityDataService;
import app.weather.service.QWeatherApi;
import app.weather.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/weather")
@Slf4j
public class WeatherController {

    private final QWeatherApi qWeatherApi;

    private final CityDataService cityDataService;

    private final WeatherService weatherService;

    @Autowired
    public WeatherController(QWeatherApi qWeatherApi, CityDataService cityDataService, WeatherService weatherService) {
        this.qWeatherApi = qWeatherApi;
        this.cityDataService = cityDataService;
        this.weatherService = weatherService;
    }

    /**
     * 城市搜索API(用于定位)
     *
     * @param location 城市名称或经纬度坐标
     * @return
     */
    @GetMapping("/geo/lookup")
    public Mono<ResultResponse<List<GeoLookupVO>>> geoLookup(@RequestParam String location) {
        if (StringUtils.isEmpty(location)) {
            return Mono.just(ResultResponse.error(StatusEnum.PARAM_ERROR));
        }
        return qWeatherApi.lookup(location)
                .map(data -> {
                    List<GeoLookupVO> collect = data.getLocation().stream()
                            .map(e -> {
                                GeoLookupVO vo = new GeoLookupVO();
                                String province = e.getAdm1();
                                String city = e.getAdm2();
                                vo.setAdm1(province);
                                vo.setLat(Float.parseFloat(e.getLat()));
                                vo.setLon(Float.parseFloat(e.getLon()));
                                // 获取省市区数据
                                Map<String, List<String>> cityDistrictMap = cityDataService.getCityDistrictMap(province);
                                cityDistrictMap
                                        .keySet().stream()
                                        .filter(k -> k.startsWith(city))
                                        .findFirst()
                                        .ifPresent(fullCityName -> {
                                            // 找到完整的城市名称
                                            vo.setAdm2(fullCityName);
                                            if (e.getName().equals(city)) {
                                                vo.setName(fullCityName);
                                            } else {
                                                // 获取区县列表
                                                cityDistrictMap.get(fullCityName)
                                                        .stream()
                                                        .filter(k -> k.startsWith(e.getName()))
                                                        .findFirst()
                                                        .ifPresent(vo::setName);
                                            }
                                        });
                                return vo;
                            })
                            .collect(Collectors.toList());
                    return ResultResponse.success(collect);
                });
    }

    /**
     * 获取天气数据(聚合请求天气API)
     *
     * @param location 经纬度
     * @return
     */
    @GetMapping("/get")
    public Mono<ResultResponse<GetWeatherVO>> getWeather(@RequestParam String location) {
        if (StringUtils.isEmpty(location)) {
            return Mono.just(ResultResponse.error(StatusEnum.PARAM_ERROR));
        }
        return weatherService.getWeather(location).map(ResultResponse::success);
    }

    /**
     * 每日天气生活指数API
     *
     * @param location 地点标识，经纬度坐标（经度,纬度）
     * @param type     生活指数的类型ID，包括洗车指数、穿衣指数、钓鱼指数等。可以一次性获取多个类型的生活指数，多个类型用英文,分割。例如type=3,5。
     * @return 包含WeatherIndicesResponse对象的Mono异步响应，其中封装了请求状态及每日天气指数数据
     */
    @GetMapping("/indices/daily")
    public Mono<ResultResponse<WeatherIndicesResponse>> getDailyIndices(@RequestParam String location,
                                                                        @RequestParam String type) {
        if (StringUtils.isEmpty(location) || StringUtils.isEmpty(type)) {
            return Mono.just(ResultResponse.error(StatusEnum.PARAM_ERROR));
        }
        return qWeatherApi.getWeatherIndices(location, type)
                .map(ResultResponse::success);
    }
}
