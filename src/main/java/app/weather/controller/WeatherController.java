package app.weather.controller;

import app.weather.model.qweather.DailyWeatherData;
import app.weather.model.qweather.GeoLookupData;
import app.weather.model.qweather.RealTimeWeatherData;
import app.weather.model.response.ResultResponse;
import app.weather.model.response.StatusEnum;
import app.weather.model.vo.GeoLookupVO;
import app.weather.model.vo.GetWeatherVO;
import app.weather.service.CityDataService;
import app.weather.service.QWeatherApi;
import app.weather.service.WeatherService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/weather")
@Slf4j
public class WeatherController {

    @Autowired
    private QWeatherApi qWeatherApi;

    @Autowired
    private CityDataService cityDataService;

    @Autowired
    private WeatherService weatherService;

    /**
     * 城市搜索API
     *
     * @param keyword 城市关键词
     * @return
     */
    @GetMapping("/city/search")
    public ResultResponse<List<String>> searchCity(String keyword) {
        if (StringUtils.isEmpty(keyword)) {
            return ResultResponse.error(StatusEnum.PARAM_ERROR);
        }

        List<String> results = cityDataService.searchDistrictWithCity(keyword);
        return ResultResponse.success(results);
    }

    /**
     * 城市搜索API(用于定位)
     * 查询地区的名称，支持文字、以英文逗号分隔的经度,纬度坐标
     *
     * @param location 北京, 116.41,39.92
     * @return
     */
    @GetMapping("/geo")
    public ResultResponse<List<GeoLookupVO>> lookup(String location) {
        if (StringUtils.isEmpty(location)) {
            return ResultResponse.error(StatusEnum.PARAM_ERROR);
        }
        List<GeoLookupData> data = qWeatherApi.lookupGeo(location);
        if (CollectionUtils.isEmpty(data)) {
            return ResultResponse.success(Collections.emptyList());
        }
//        log.info("城市搜索API, location: {}, data: {}", location, data);
        // data to vo
        List<GeoLookupVO> list = data.stream()
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
        return ResultResponse.success(list);
    }

    /**
     * 天气预报API
     *
     * @param location 经纬度
     * @return
     */
    @GetMapping("/get")
    public ResultResponse<GetWeatherVO> get(String location) {
        if (StringUtils.isEmpty(location)) {
            return ResultResponse.error(StatusEnum.PARAM_ERROR);
        }
        GetWeatherVO data = weatherService.getWeather(location);
        if (data == null) {
            return ResultResponse.error(StatusEnum.FAIL);
        }
//        log.info("天气预报API, location: {}, data: {}", location, data);
        return ResultResponse.success(data);
    }

    /**
     * 实时天气API
     *
     * @param location 经纬度
     * @return
     */
    @GetMapping("/now")
    public ResultResponse<RealTimeWeatherData> weatherNow(String location) {
        if (StringUtils.isEmpty(location)) {
            return ResultResponse.error(StatusEnum.PARAM_ERROR);
        }
        RealTimeWeatherData data = qWeatherApi.weatherNow(location);
        if (data == null) {
            return ResultResponse.error(StatusEnum.FAIL);
        }
        log.info("实时天气API, location: {}, data: {}", location, data);
        return ResultResponse.success(data);
    }

    /**
     * 未来天气API
     *
     * @param location       经纬度
     * @param queryDailyType 查询类型 1: 未来3天 2: 未来7天
     * @return
     */
    @GetMapping("/daily")
    public ResultResponse<List<DailyWeatherData>> dailyWeather(String location, Integer queryDailyType) {
        if (StringUtils.isEmpty(location)) {
            return ResultResponse.error(StatusEnum.PARAM_ERROR);
        }
        if (queryDailyType == null) {
            queryDailyType = 1;
        }
        List<DailyWeatherData> data = qWeatherApi.dailyWeather(location, queryDailyType);
        if (CollectionUtils.isEmpty(data)) {
            return ResultResponse.error(StatusEnum.FAIL);
        }
        log.info("未来天气API, location: {}, data: {}", location, data);
        return ResultResponse.success(data);
    }
}
