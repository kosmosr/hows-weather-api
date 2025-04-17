package app.weather.model.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 天气预报API
 */
@Data
public class GetWeatherVO {
    /**
     * 实时温度
     */
    private String temp;

    /**
     * 体感温度，默认单位：摄氏度
     */
    private String feelsLike;

    /**
     * 相对湿度，百分比数值
     */
    private String humidity;

    /**
     * 天气状况的图标代码
     */
    private String icon;

    /**
     * 天气状况的文字描述，包括阴晴雨雪等天气状态的描述
     */
    private String text;

    /**
     * 未来天气
     */
    private List<DailyWeather> dailyWeatherList;

    @Data
    public static class DailyWeather {
        /**
         * 预报日期
         */
        @JsonFormat(pattern = "MM月dd日", timezone = "GMT+8")
        private Date fxDate;

        /**
         * 周几
         */
        private String dayOfWeek;

        /**
         * 最高气温，默认单位：摄氏度
         */
        private String tempMax;

        /**
         * 最低气温，默认单位：摄氏度
         */
        private String tempMin;

        /**
         * 天气状况的图标代码
         */
        private String icon;

        /**
         * 天气状况的文字描述，包括阴晴雨雪等天气状态的描述
         */
        private String text;

    }
}
