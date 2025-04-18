package app.weather.model.qweather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * QWeatherApiResponseBase类是和风天气API响应的基础类，包含了所有API响应的公共属性。
 * 该类定义了状态码、更新时间、天气预报网页链接等通用信息，并通过Refer对象提供了数据来源与许可信息。
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QWeatherApiResponseBase {
    /**
     * 状态码
     */
    private String code;

    /**
     * 当前API的最近更新时间
     */
    private String updateTime;

    /**
     * 该地区的天气预报网页链接，便于嵌入你的网站或应用。
     */
    private String fxLink;

    /**
     * 包含数据来源和许可信息的引用对象，用于标识API响应中使用的数据源及其许可情况。
     */
    private Refer refer;

    @Data
    @NoArgsConstructor
    public static class Refer {
        /**
         * 原始数据来源，或数据源说明，可能为空
         */
        private List<String> sources;

        /**
         * 数据许可或版权声明，可能为空
         */
        private List<String> license;
    }
}
