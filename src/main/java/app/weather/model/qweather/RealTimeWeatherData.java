package app.weather.model.qweather;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;

/**
 * 实时天气返回数据
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RealTimeWeatherData {
    /**
     * 数据观测时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mmXXX", timezone = "GMT+8")
    private Date obsTime;

    /**
     * 温度，默认单位：摄氏度
     */
    private String temp;

    /**
     * 体感温度，默认单位：摄氏度
     */
    private String feelsLike;

    /**
     * 天气状况的图标代码
     */
    private String icon;

    /**
     * 天气状况的文字描述，包括阴晴雨雪等天气状态的描述
     */
    private String text;

    /**
     * 风向360角度
     */
    private String wind360;

    /**
     * 风向
     */
    private String windDir;

    /**
     * 风力等级
     */
    private String windScale;

    /**
     * 风速，公里/小时
     */
    private String windSpeed;

    /**
     * 相对湿度，百分比数值
     */
    private String humidity;

    /**
     * 过去1小时降水量，默认单位：毫米
     */
    private String precip;

    /**
     * 大气压强，默认单位：百帕
     */
    private String pressure;

    /**
     * 能见度，默认单位：公里
     */
    private String vis;

    /**
     * 云量，百分比数值。可能为空
     */
    private String cloud;

    /**
     * 露点温度。可能为空
     */
    private String dew;
}
