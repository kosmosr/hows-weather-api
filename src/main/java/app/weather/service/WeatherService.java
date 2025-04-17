package app.weather.service;

import app.weather.model.qweather.DailyWeatherData;
import app.weather.model.qweather.RealTimeWeatherData;
import app.weather.model.vo.GetWeatherVO;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WeatherService {
    @Autowired
    private QWeatherApi qWeatherApi;

    public GetWeatherVO getWeather(String location) {
        // 1. 获取实时天气数据
        RealTimeWeatherData realTimeWeatherData = qWeatherApi.weatherNow(location);
        if (realTimeWeatherData == null) {
            log.error("获取实时天气数据失败");
            return null;
        }

        // 2. 获取未来7天的天气预报数据
        List<DailyWeatherData> dailyWeatherData = qWeatherApi.dailyWeather(location, 2);
        if (dailyWeatherData == null) {
            log.error("获取未来7天的天气预报数据失败");
            return null;
        }

        // 3. 构建返回对象
        GetWeatherVO vo = new GetWeatherVO();
        vo.setTemp(realTimeWeatherData.getTemp());
        vo.setFeelsLike(realTimeWeatherData.getFeelsLike());
        vo.setHumidity(realTimeWeatherData.getHumidity());
        vo.setIcon(realTimeWeatherData.getIcon());
        vo.setText(realTimeWeatherData.getText());

        // 4. 设置未来天气数据
        List<GetWeatherVO.DailyWeather> dailyWeatherList = dailyWeatherData.stream()
                .map(data -> {
                    GetWeatherVO.DailyWeather dailyWeather = new GetWeatherVO.DailyWeather();
                    // 设置日期格式为"MM月dd日"
                    dailyWeather.setFxDate(data.getFxDate());
                    if (LocalDate.fromDateFields(data.getFxDate()).equals(LocalDate.now())) {
                        dailyWeather.setDayOfWeek("今天");
                    } else {
                        String dayOfWeek = LocalDate.fromDateFields(data.getFxDate()).dayOfWeek().getAsShortText(Locale.CHINA);
                        dailyWeather.setDayOfWeek(dayOfWeek);
                    }
                    dailyWeather.setTempMax(data.getTempMax());
                    dailyWeather.setTempMin(data.getTempMin());
                    dailyWeather.setIcon(data.getIconDay());
                    dailyWeather.setText(data.getTextDay());
                    return dailyWeather;
                })
                .collect(Collectors.toList());
        vo.setDailyWeatherList(dailyWeatherList);
        return vo;
    }
}
