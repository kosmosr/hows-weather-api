package app.weather.model.qweather;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 每日天气预报
 */
@Data
public class DailyWeatherData {
    /**
     * 预报日期
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date fxDate;

    /**
     * 日出时间
     */
    private String sunrise;

    /**
     * 日落时间
     */
    private String sunset;

    /**
     * 当天月升时间
     */
    private String moonrise;

    /**
     * 当天月落时间
     */
    private String moonset;

    /**
     * 月相名称
     */
    private String moonPhase;

    /**
     * 月相图标代码
     */
    private String moonPhaseIcon;

    /**
     * 最高气温，默认单位：摄氏度
     */
    private String tempMax;

    /**
     * 最低气温，默认单位：摄氏度
     */
    private String tempMin;

    /**
     * 白天天气状况的图标代码
     */
    private String iconDay;

    /**
     * 白天天气状况的文字描述，包括阴晴雨雪等天气状态的描述
     */
    private String textDay;

    /**
     * 夜间天气状况的图标代码
     */
    private String iconNight;

    /**
     * 预报晚间天气状况文字描述，包括阴晴雨雪等天气状态的描述
     */
    private String textNight;

    /**
     * 白天风向360角度
     */
    private String wind360Day;

    /**
     * 白天风向
     */
    private String windDirDay;

    /**
     * 白天风力等级
     */
    private String windScaleDay;

    /**
     * 白天风速，公里/小时
     */
    private String windSpeedDay;

    /**
     * 夜间风向360角度
     */
    private String wind360Night;

    /**
     * 夜间风向
     */
    private String windDirNight;

    /**
     * 夜间风力等级
     */
    private String windScaleNight;

    /**
     * 夜间风速，公里/小时
     */
    private String windSpeedNight;

    /**
     * 相对湿度，百分比数值（范围0~100）
     */
    private String humidity;

    /**
     * 降水量，默认单位：毫米（mm）
     */
    private String precip;

    /**
     * 大气压，默认单位：百帕（hPa）
     */
    private String pressure;
    /**
     * 能见度，默认单位：公里（km）
     */
    private String vis;

    /**
     * 云量，百分比数值（范围0~100）
     */
    private String cloud;
    /**
     * 紫外线强度指数
     */
    private String uvIndex;
}
