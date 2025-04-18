package app.weather.service;

import app.weather.model.qweather.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class QWeatherApi {

    private final JwtService jwtService;
    private final WebClient webClient;

    @Autowired
    public QWeatherApi(JwtService jwtService, WebClient webClient) {
        this.jwtService = jwtService;
        this.webClient = webClient;
    }

    /**
     * 统一处理 API 调用返回的错误状态码.
     *
     * @param clientResponse 客户端响应
     * @return 包含 WebClientResponseException 的 Mono 错误信号
     */
    private Mono<Throwable> handleApiError(org.springframework.web.reactive.function.client.ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
            log.error("和风天气 API 调用失败: Status={}, Body={}", clientResponse.statusCode(), errorBody);
            return Mono.error(WebClientResponseException.create(
                    clientResponse.statusCode().value(),
                    clientResponse.statusCode().toString(),
                    clientResponse.headers().asHttpHeaders(),
                    errorBody.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    java.nio.charset.StandardCharsets.UTF_8
            ));
        });
    }

    /**
     * 统一记录 API 成功响应日志.
     *
     * @param apiName      API 名称 (用于日志区分)
     * @param responseCode API 返回的 code
     * @param params       参数
     */
    private void logApiResponse(String apiName, String responseCode, String params) {
        if ("200".equals(responseCode)) {
            log.info("成功从 API 获取{}数据: params:{}", apiName, params);
        } else {
            log.warn("从 API 获取的{}数据 code 非 200 (不会缓存): responseCode={}, location={}",
                    apiName, responseCode, params);
        }
    }

    /**
     * 获取指定地点的【天气生活指数】 (当天).
     * 使用 @Cacheable 注解启用 Redis 缓存 ('weatherIndices').
     */
    @Cacheable(value = "weatherIndices", key = "#location + '-' + #type", unless = "#result == null || !'200'.equals(#result.code)")
    public Mono<WeatherIndicesResponse> getWeatherIndices(String location, String type) {
        log.info("getWeatherIndices location: {}, type: {}", location, type);
        String jwtToken = jwtService.generateJwtToken();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v7/indices/1d")
                        .queryParam("location", location)
                        .queryParam("type", type)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleApiError)
                .bodyToMono(WeatherIndicesResponse.class)
                .doOnSuccess(response -> logApiResponse("天气指数", response != null ? response.getCode() : null,
                        "location=" + location + ", type=" + type))
                .doOnError(error -> !(error instanceof WebClientResponseException),
                        error -> log.error("调用天气指数 API 或处理响应时发生非 API 错误: location={}, type={}", location, type, error));
    }

    /**
     * 城市搜索 API (用于定位). 不进行缓存
     */
    public Mono<GeoLookupResponse> lookup(String location) {
        log.info("lookup location: {}", location);
        String jwtToken = jwtService.generateJwtToken();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/geo/v2/city/lookup")
                        .queryParam("location", location)
                        .queryParam("range", "cn")
                        .build())
                .header("Authorization", "Bearer " + jwtToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleApiError)
                .bodyToMono(GeoLookupResponse.class)
                .doOnSuccess(response -> logApiResponse("城市搜索", response != null ? response.getCode() : null,
                        "location=" + location))
                .doOnError(error -> !(error instanceof WebClientResponseException),
                        error -> log.error("调用城市搜索 API 或处理响应时发生非 API 错误: location={}", location, error));
    }

    /**
     * 获取指定地点的【实时天气】.
     *
     * @param location 经纬度
     * @return
     */
    @Cacheable(value = "realtimeWeatherCache", key = "#location", unless = "#result == null || !'200'.equals(#result.code)")
    public Mono<RealTimeWeatherResponse> getRealtimeWeather(String location) {
        log.info("getRealtimeWeather location: {}", location);
        String jwtToken = jwtService.generateJwtToken();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v7/weather/now")
                        .queryParam("location", location)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleApiError)
                .bodyToMono(RealTimeWeatherResponse.class)
                .doOnSuccess(response -> logApiResponse("实时天气", response != null ? response.getCode() : null,
                        "location=" + location))
                .doOnError(error -> !(error instanceof WebClientResponseException),
                        error -> log.error("调用实时天气 API 或处理响应时发生非 API 错误: location={}", location, error));
    }

    /**
     * 每日天气预报(最近7天)
     *
     * @param location 经纬度
     * @return
     */
    @Cacheable(value = "dailyWeatherCache", key = "#location", unless = "#result == null || !'200'.equals(#result.code)")
    public Mono<DailyWeatherResponse> getDailyWeather(String location) {
        log.info("getDailyWeather location: {}", location);
        String jwtToken = jwtService.generateJwtToken();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v7/weather/7d")
                        .queryParam("location", location)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleApiError)
                .bodyToMono(DailyWeatherResponse.class)
                .doOnSuccess(response -> logApiResponse("每日天气", response != null ? response.getCode() : null,
                        "location=" + location))
                .doOnError(error -> !(error instanceof WebClientResponseException),
                        error -> log.error("调用每日天气 API 或处理响应时发生非 API 错误: location={}", location, error));
    }

    /**
     * 获取指定地点的【逐小时天气预报】 (未来 24 小时).
     */
    @Cacheable(value = "hourlyWeatherCache", key = "#location", unless = "#result == null || !'200'.equals(#result.code)")
    public Mono<HourlyWeatherResponse> getHourlyWeatherForecast24h(String location) {
        log.info("逐小时天气预报缓存未命中或已过期: location={}", location);
        String jwtToken = jwtService.generateJwtToken();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v7/weather/24h").queryParam("location", location).build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleApiError)
                .bodyToMono(HourlyWeatherResponse.class)
                .doOnSuccess(response -> logApiResponse("逐小时天气预报", response != null ? response.getCode() : null, "location=" + location))
                .doOnError(error -> !(error instanceof WebClientResponseException),
                        error -> log.error("调用逐小时天气预报 API 或处理响应时发生非 API 错误: location={}", location, error));
    }


    /*

     *//**
     * 实时天气
     * https://dev.qweather.com/docs/api/weather/weather-now/
     *
     * @param location 需要查询地区的LocationID或以英文逗号分隔的经度,纬度坐标 例如 location=101010100 或 location=116.41,39.92
     * @return
     *//*
    public RealTimeWeatherData weatherNow(String location) {
        if (StringUtils.isEmpty(location)) {
            throw new IllegalArgumentException("location is empty");
        }
        String response = query("https://api.qweather.com/v7/weather/now?location=" + location);
        ObjectMapper objectMapper = new ObjectMapper();
        RealTimeWeatherData data;
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            int code = jsonNode.get("code").asInt();
            if (code != 200) {
                log.error("Failed to get weather now, response: {}", response);
                throw new RuntimeException("Failed to get weather now");
            }
            data = objectMapper.readValue(jsonNode.get("now").toString(), new TypeReference<>() {
            });
            log.debug("WeatherNow success, data: {}", data);
        } catch (Exception e) {
            log.error("Failed to parse response, error: {}, response: {}", Throwables.getStackTraceAsString(e), response);
            throw new RuntimeException(e);
        }
        return data;
    }

    *//**
     * 每日天气预报
     * https://dev.qweather.com/docs/api/weather/weather-daily-forecast/
     *
     * @param location       需要查询地区的LocationID或以英文逗号分隔的经度,纬度坐标 例如 location=101010100 或 location=116.41,39.92
     * @param queryDailyType 查询类型 1:3天 2:7天 3:10天 4:15天 5:30天
     * @return
     *//*
    public List<DailyWeatherData> dailyWeather(String location, Integer queryDailyType) {
        if (StringUtils.isEmpty(location)) {
            throw new IllegalArgumentException("location is empty");
        }
        String days = switch (queryDailyType) {
            case 1 -> "3d";
            case 2 -> "7d";
            case 3 -> "10d";
            case 4 -> "15d";
            case 5 -> "30d";
            default -> "3d";
        };
        String response = query("https://api.qweather.com/v7/weather/" + days + "?location=" + location);
        ObjectMapper objectMapper = new ObjectMapper();
        List<DailyWeatherData> data;
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            int code = jsonNode.get("code").asInt();
            if (code != 200) {
                log.error("Failed to get daily weather, response: {}", response);
                throw new RuntimeException("Failed to get daily weather");
            }
            data = objectMapper.readValue(jsonNode.get("daily").toString(), new TypeReference<>() {
            });
            log.debug("DailyWeather success, data: {}", data);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse response, error: {}, response: {}", Throwables.getStackTraceAsString(e), response);
            throw new RuntimeException(e);
        }
        return data;
    }

    public List<GeoLookupData> topCityList(TopCityQuery query) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map queryMap = objectMapper.convertValue(query, Map.class);
        String params = HttpUtil.toParams(queryMap, StandardCharsets.UTF_8);
        String response = query("https://geoapi.qweather.com/v2/city/top?" + params);
        List<GeoLookupData> data;
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            int code = jsonNode.get("code").asInt();
            if (code != 200) {
                log.error("Failed to get top cityList, response: {}", response);
                throw new RuntimeException("Failed to lookup geo");
            }
            data = objectMapper.readValue(jsonNode.get("topCityList").toString(), new TypeReference<>() {
            });
            log.info("get top cityList, data: {}", jsonNode.get("topCityList"));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse response, error: {}, response: {}", Throwables.getStackTraceAsString(e), response);
            throw new RuntimeException(e);
        }
        return data;
    }*/
}
