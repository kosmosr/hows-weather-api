package app.weather.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CityDataService {
    private Map<String, Map<String, List<String>>> cityDistrictMap;

    public static final String CITY_DISTRICT_JSON_FILE = "pca.json";

    @PostConstruct
    public void init() {
        loadCityData();
    }

    /**
     * 加载城市区数据
     */
    private void loadCityData() {
        try {
            ClassPathResource resource = new ClassPathResource(CITY_DISTRICT_JSON_FILE);
            ObjectMapper mapper = new ObjectMapper();

            try (InputStream inputStream = resource.getInputStream()) {
                cityDistrictMap = mapper.readValue(inputStream, new TypeReference<>() {
                });
                log.info("加载城市区数据完成，数据量: {}", cityDistrictMap.size());
            }
        } catch (IOException e) {
            log.error("Failed to load city data. e: {}", Throwables.getStackTraceAsString(e));
            cityDistrictMap = new HashMap<>();
        }
    }

    // 获取所有城市名称
    public Set<String> getAllCities() {
        return cityDistrictMap.keySet();
    }

    /**
     * 根据一级名称获取所有城市的区县列表
     *
     * @param province 一级名称
     * @return
     */
    public Map<String, List<String>> getCityDistrictMap(String province) {
        if (StringUtils.isEmpty(province)) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> map = cityDistrictMap.get(province);
        if (CollectionUtils.isEmpty(map)) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> result = new HashMap<>(map.size());

        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String cityName = entry.getKey();
            List<String> districts = entry.getValue();

            // 处理市辖区和县的情况
            if (cityName.equals("市辖区") || cityName.equals("县")) {
                cityName = province;
            }
            if (cityName.equals("省直辖县级行政区划")) {
                for (String district : districts) {
                    result.put(district, Collections.singletonList(district));
                }
                continue;
            }

            // 将城市名称和区县列表添加到结果中
            if (result.containsKey(cityName)) {
                result.get(cityName).addAll(districts);
            } else {
                result.put(cityName, districts);
            }
        }

        return result;
    }

    /**
     * 根据关键词模糊搜索区县，返回"区县-城市"格式的结果
     *
     * @param keyword 搜索关键词
     * @return 匹配的区县列表，格式为"区县-城市"
     */
    public List<String> searchDistrictWithCity(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }

        List<String> results = new ArrayList<>();

        // 遍历每个城市/省
        for (Map.Entry<String, Map<String, List<String>>> cityEntry : cityDistrictMap.entrySet()) {
            String province = cityEntry.getKey(); // 一级行政区、省、直辖市
            Map<String, List<String>> regionTypes = getCityDistrictMap(province);

            // 遍历每种区域类型
            for (Map.Entry<String, List<String>> regionTypeEntry : regionTypes.entrySet()) {
                String cityName = regionTypeEntry.getKey();
                List<String> districts = regionTypeEntry.getValue(); // 具体区县列表
                if (cityName.contains(keyword)) {
                    results.add(cityName + "-" + province);
                }
                // 遍历每个区县
                for (String district : districts) {
                    // 检查区县名称是否包含搜索关键词
                    if (district.contains(keyword)) {
                        results.add(district + "-" + cityName + "-" + province);
                    }
                }
            }
        }

        return results;
    }
}
