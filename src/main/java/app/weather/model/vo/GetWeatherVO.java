package app.weather.model.vo;


import app.weather.model.qweather.DailyWeatherResponse;
import app.weather.model.qweather.HourlyWeatherResponse;
import app.weather.model.qweather.RealTimeWeatherResponse;
import app.weather.model.qweather.WeatherIndicesResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
     * 风向
     */
    private String windDir;

    /**
     * 风力等级
     */
    private String windScale;

    /**
     * 风速 公里/小时
     */
    private String windSpeed;

    /**
     * 未来天气
     */
    private List<DailyWeather> dailyWeatherList;

    /**
     * 逐小时天气
     */
    private List<HourlyWeather> hourlyWeatherList;

    /**
     * 生活指数
     */
    private List<WeatherIndices> indicesList;

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

    @Data
    public static class HourlyWeather {
        /**
         * 预报时间
         */
        @JsonFormat(pattern = "HH:mm", timezone = "GMT+8")
        private Date fxDate;

        /**
         * 温度
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
         * 风速，默认单位：公里/每小时
         */
        private String windSpeed;
    }

    @Data
    public static class WeatherIndices {
        /**
         * 生活指数类型的名称
         */
        private String name;

        /**
         * 生活指数类型ID
         */
        private String type;

        /**
         * 生活指数预报级别名称
         */
        private String category;

        /**
         * 生活指数预报的详细描述文本
         */
        private String text;
    }

    /**
     * 构建实时天气
     *
     * @param realTimeWeatherResponse 实时天气响应
     * @return GetWeatherVO
     */
    public GetWeatherVO buildRealtimeWeather(RealTimeWeatherResponse realTimeWeatherResponse) {
        RealTimeWeatherResponse.NowData now = realTimeWeatherResponse.getNow();
        this.temp = now.getTemp();
        this.feelsLike = now.getFeelsLike();
        this.humidity = now.getHumidity();
        this.icon = now.getIcon();
        this.text = now.getText();
        this.windDir = now.getWindDir();
        this.windScale = now.getWindScale();
        this.windSpeed = now.getWindSpeed();
        return this;
    }

    public GetWeatherVO buildDailyWeather(DailyWeatherResponse dailyWeatherResponse) {
        List<DailyWeather> collect = dailyWeatherResponse.getDaily().stream()
                .map(data -> {
                    DailyWeather dailyWeather = new DailyWeather();
                    Date fxDate = DateTimeFormat.forPattern("yyyy-MM-dd")
                            .parseDateTime(data.getFxDate())
                            .toDate();
                    // 设置日期格式为"MM月dd日"
                    dailyWeather.setFxDate(fxDate);
                    if (LocalDate.fromDateFields(fxDate).equals(LocalDate.now())) {
                        dailyWeather.setDayOfWeek("今天");
                    } else {
                        String dayOfWeek = LocalDate.fromDateFields(fxDate).dayOfWeek().getAsShortText(Locale.CHINA);
                        dailyWeather.setDayOfWeek(dayOfWeek);
                    }
                    dailyWeather.setTempMax(data.getTempMax());
                    dailyWeather.setTempMin(data.getTempMin());
                    dailyWeather.setIcon(data.getIconDay());
                    dailyWeather.setText(data.getTextDay());
                    return dailyWeather;
                })
                .collect(Collectors.toList());
        this.setDailyWeatherList(collect);
        return this;
    }

    public GetWeatherVO buildHourlyWeather(HourlyWeatherResponse hourlyWeatherResponse) {
        List<HourlyWeather> collect = hourlyWeatherResponse.getHourly().stream()
                .map(data -> {
                    DateTime parseDateTime = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mmZ")
                            .parseDateTime(data.getFxTime());
                    HourlyWeather hourlyWeather = new HourlyWeather();
                    hourlyWeather.setFxDate(parseDateTime.toDate());
                    hourlyWeather.setTemp(data.getTemp());
                    hourlyWeather.setIcon(data.getIcon());
                    hourlyWeather.setText(data.getText());
                    hourlyWeather.setWindSpeed(data.getWindSpeed());
                    return hourlyWeather;
                })
                .collect(Collectors.toList());
        this.setHourlyWeatherList(collect);
        return this;
    }

    public GetWeatherVO buildWeatherIndices(WeatherIndicesResponse weatherIndicesResponse) {
        List<WeatherIndices> collect = weatherIndicesResponse.getDaily().stream()
                .map(data -> {
                    WeatherIndices weatherIndices = new WeatherIndices();
                    weatherIndices.setName(data.getName());
                    weatherIndices.setType(data.getType());
                    weatherIndices.setCategory(data.getCategory());
                    weatherIndices.setText(data.getText());
                    return weatherIndices;
                })
                .collect(Collectors.toList());
        this.setIndicesList(collect);
        return this;
    }
}
