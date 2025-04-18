package app.weather.model.qweather;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * 用于映射和风天气【天气指数】 API 响应的数据传输对象 (DTO).
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WeatherIndicesResponse extends QWeatherApiResponseBase {
    /**
     * 天气指数列表
     */
    private List<DailyIndex> daily;

    /**
     * 天气指数类
     */
    @Data
    public static class DailyIndex {
        private String date;

        /**
         * 生活指数类型ID
         */
        private String type;

        /**
         *  生活指数类型的名称
         */
        private String name;

        /**
         * 生活指数预报等级
         */
        private String level;

        /**
         * 生活指数预报级别名称
         */
        private String category;

        /**
         * 生活指数预报的详细描述文本，提供关于特定生活指数的详细说明或建议。
         */
        private String text;
    }
}
