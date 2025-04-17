package app.weather.model.vo;

import lombok.Data;

/**
 * 城市搜索API 返回对象
 */
@Data
public class GeoLookupVO {
    /**
     * 地区/城市名称
     */
    private String name;

    /**
     * 地区/城市所属一级行政区域
     */
    private String adm1;

    /**
     * 地区/城市的上级行政区划名称
     */
    private String adm2;

    /**
     * 地区/城市纬度
     */
    private Float lat;

    /**
     * 地区/城市经度
     */
    private Float lon;
}
