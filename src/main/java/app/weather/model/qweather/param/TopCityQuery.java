package app.weather.model.qweather.param;

import lombok.Builder;
import lombok.Data;

/**
 * 和风天气API-热门城市查询-查询参数
 */
@Data
@Builder
public class TopCityQuery {
    /**
     * 搜索范围，可设定只在某个国家或地区范围内进行搜索，国家和地区名称需使用ISO 3166 所定义的国家代码。如果不设置此参数，搜索范围将在所有城市。例如 range=cn
     */
    private String range;

    /**
     * 返回结果的数量，取值范围1-20，默认返回10个结果。
     */
    private String number;

    /**
     * 多语言设置，请阅读多语言文档，了解我们的多语言是如何工作、如何设置以及数据是否支持多语言。
     */
    private String lang;
}
