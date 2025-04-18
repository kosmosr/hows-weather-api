package app.weather.model.qweather;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * 用于映射和风天气【逐小时天气预报】 API 响应的数据传输对象 (DTO).
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HourlyWeatherResponse extends QWeatherApiResponseBase {
    private List<HourlyData> hourly; // 逐小时预报特有的字段

    @Data
    @NoArgsConstructor
    public static class HourlyData {
        /**
         * 预报时间
         */
        private String fxTime;

        /**
         * 温度，默认单位：摄氏度
         */
        private String temp;

        /**
         * 天气状况的图标代码
         */
        private String icon;

        /**
         * 天气状况的文字描述
         */
        private String text;

        /**
         * 风向360角度
         */
        private String wind360;

        /**
         * 风向文字描述
         */
        private String windDir;

        /**
         * 风力等级
         */
        private String windScale;

        /**
         * 风速，默认单位：公里每小时
         */
        private String windSpeed;

        /**
         * 相对湿度
         */
        private String humidity;

        /**
         * 逐小时预报降水概率，百分比数值
         */
        private String pop;

        /**
         * 当前小时累计降水量，默认单位：毫米
         */
        private String precip;

        /**
         * 气压，默认单位：百帕
         */
        private String pressure;

        /**
         * 云量，百分比数值。可能为空
         */
        private String cloud;

        /**
         * 露点温度。可能为空
         */
        private String dew;
    }
}
