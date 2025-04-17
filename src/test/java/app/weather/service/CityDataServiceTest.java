package app.weather.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class CityDataServiceTest {
    @Autowired
    private CityDataService cityDataService;

    @Test
    void testLoadCityData() {
        Set<String> allCities = cityDataService.getAllCities();
        assertFalse(allCities.isEmpty());

        List<String> searchDistrictWithCity = cityDataService.searchDistrictWithCity("上海");
        log.info("searchDistrictWithCity: {}", searchDistrictWithCity);
        assertFalse(searchDistrictWithCity.isEmpty());
    }
}
