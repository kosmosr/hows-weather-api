package app.weather.service;

import app.weather.config.QWeatherApiConfig;
import app.weather.model.qweather.DailyWeatherData;
import app.weather.model.qweather.GeoLookupData;
import app.weather.model.qweather.RealTimeWeatherData;
import app.weather.model.qweather.param.TopCityQuery;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class QWeatherApi {
    private static final String REDIS_JWT_KEY = "qweather:jwt:token";
    @Autowired
    private QWeatherApiConfig qWeatherApiConfig;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 生成JWT TOKEN用于鉴权
     *
     * @return
     */
    public String generateJWT() {
        // 先从缓存中获取
        String token = redisTemplate.opsForValue().get(REDIS_JWT_KEY);
        if (!StringUtils.isEmpty(token)) {
            return token;
        }
        // header
        JSONObject header = new JSONObject();
        JSONObject payload = null;
        try {
            header.put("alg", "EdDSA");
            header.put("kid", qWeatherApiConfig.getKeyId());
            // payload
            payload = new JSONObject();
            payload.put("sub", qWeatherApiConfig.getProjectId());
            payload.put("iat", DateTime.now().getMillis() / 1000);
            payload.put("exp", DateTime.now().plusDays(1).getMillis() / 1000);
        } catch (JSONException e) {
            log.error("Failed to create JWT, error: {}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException(e);
        }

        // Base64url header+payload
        String headerEncoded = base64UrlEncode(header.toString().getBytes(StandardCharsets.UTF_8));
        String payloadEncoded = base64UrlEncode(payload.toString().getBytes(StandardCharsets.UTF_8));
        // Create signing input
        String data = headerEncoded + "." + payloadEncoded;

        // sign
        byte[] signature = null;
        try {
            signature = sign(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Failed to sign data, error: {}", Throwables.getStackTraceAsString(e));
            throw new RuntimeException(e);
        }
        String signatureEncoded = base64UrlEncode(signature);

        token = data + "." + signatureEncoded;
        // 缓存一天
        redisTemplate.opsForValue().set(REDIS_JWT_KEY, token, 24 * 60 * 60, TimeUnit.SECONDS);
        return token;
    }

    private String query(String url) {
        String token = generateJWT();
        String response = HttpRequest.get(url)
                .header("Authorization", "Bearer " + token)
                .execute()
                .body();
        return response;
    }

    /**
     * 城市搜索
     * https://dev.qweather.com/docs/api/geoapi/city-lookup/
     *
     * @param location 需要查询地区的名称，支持文字、以英文逗号分隔的经度,纬度坐标
     */
    public List<GeoLookupData> lookupGeo(String location) {
        if (StringUtils.isEmpty(location)) {
            throw new IllegalArgumentException("location is empty");
        }
        String response = query("https://geoapi.qweather.com/v2/city/lookup?range=cn&location=" + location);
        ObjectMapper objectMapper = new ObjectMapper();
        List<GeoLookupData> data;
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode error = jsonNode.get("error");
            if (error != null) {
                log.error("Failed to lookup geo, location param: {}, response: {}", location, response);
                return List.of();
            }
            int code = jsonNode.get("code").asInt();
            if (code != 200) {
                log.error("Failed to lookup geo, location param: {}, response: {}", location, response);
                throw new RuntimeException("Failed to lookup geo");
            }
            data = objectMapper.readValue(jsonNode.get("location").toString(), new TypeReference<>() {
            });
            log.debug("Lookup geo success, data: {}", data);
        } catch (Exception e) {
            log.error("Failed to parse response, error: {},location param: {}, response: {}",
                    Throwables.getStackTraceAsString(e), location, response);
            throw new RuntimeException(e);
        }
        return data;
    }

    /**
     * 实时天气
     * https://dev.qweather.com/docs/api/weather/weather-now/
     *
     * @param location 需要查询地区的LocationID或以英文逗号分隔的经度,纬度坐标 例如 location=101010100 或 location=116.41,39.92
     * @return
     */
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

    /**
     * 每日天气预报
     * https://dev.qweather.com/docs/api/weather/weather-daily-forecast/
     *
     * @param location       需要查询地区的LocationID或以英文逗号分隔的经度,纬度坐标 例如 location=101010100 或 location=116.41,39.92
     * @param queryDailyType 查询类型 1:3天 2:7天 3:10天 4:15天 5:30天
     * @return
     */
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
    }

    private byte[] sign(byte[] data) throws Exception {
        // Decode private key from Base64
        byte[] privateKeyBytes = Base64.getDecoder().decode(qWeatherApiConfig.getPrivateKey());

        // Create EdDSA private key
        PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        PrivateKey signingKey = new EdDSAPrivateKey(encodedKeySpec);

        // Sign
        EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
        final Signature s = new EdDSAEngine(MessageDigest.getInstance(spec.getHashAlgorithm()));
        s.initSign(signingKey);
        s.update(data);
        return s.sign();
    }

    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
}
