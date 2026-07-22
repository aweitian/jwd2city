package com.example.jwd2city.service;

import com.example.jwd2city.entity.CityRegion;
import com.example.jwd2city.util.Point;
import com.example.jwd2city.util.PolygonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class CityService {

    private final List<CityRegionCache> cache = new CopyOnWriteArrayList<>();
    private final Map<String, AdcodeInfo> adcodeMap = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        log.info("Loading city region data from JSON files...");
        loadFromJsonFiles();
        loadAdcodeMap();
        log.info("Loaded {} valid city regions into memory", cache.size());
    }

    private void loadFromJsonFiles() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:cityData/*.json");
            
            if (resources == null || resources.length == 0) {
                log.warn("No JSON files found in cityData directory");
                return;
            }

            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    List<CityRegion> regions = objectMapper.readValue(is, new TypeReference<List<CityRegion>>() {});
                    for (CityRegion region : regions) {
                        List<Point> polygon = PolygonUtil.parsePolyline(region.getPolyline());
                        if (!polygon.isEmpty()) {
                            cache.add(new CityRegionCache(region, polygon));
                        }
                    }
                    log.debug("Loaded {} regions from {}", regions.size(), resource.getFilename());
                } catch (IOException e) {
                    log.warn("Failed to load file {}: {}", resource.getFilename(), e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Failed to read cityData directory: {}", e.getMessage());
        }
    }

    private void loadAdcodeMap() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource resource = resolver.getResource("classpath:adcode_map.json");
            
            if (!resource.exists()) {
                log.warn("adcode_map.json not found in resources");
                return;
            }

            try (InputStream is = resource.getInputStream()) {
                Map<String, AdcodeInfo> map = objectMapper.readValue(is, new TypeReference<Map<String, AdcodeInfo>>() {});
                adcodeMap.putAll(map);
                log.info("Loaded {} adcode mappings", adcodeMap.size());
            }
        } catch (IOException e) {
            log.error("Failed to load adcode_map.json: {}", e.getMessage());
        }
    }

    public CityResult findCityByCoordinate(double lon, double lat) {
        Point point = new Point(lon, lat);

        for (CityRegionCache cacheItem : cache) {
            if (!PolygonUtil.isPointInMBR(point, cacheItem.getMin(), cacheItem.getMax())) {
                continue;
            }

            if (PolygonUtil.isPointInPolygon(point, cacheItem.getPolygon())) {
                Integer adcode = cacheItem.getAdcode();
                String province = null;
                String city = null;
                if (adcode != null) {
                    AdcodeInfo info = adcodeMap.get(String.valueOf(adcode).substring(0,4));
                    if (info != null) {
                        province = info.getProvince();
                        city = info.getCity();
                    }
                }
                return new CityResult(cacheItem.getName(), adcode, province, city);
            }
        }

        return null;
    }

    public int getCachedCount() {
        return cache.size();
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class CityResult {
        private String name;
        private Integer adcode;
        private String province;
        private String city;
    }

    @lombok.Data
    public static class AdcodeInfo {
        private String province;
        private String city;
    }

    private static class CityRegionCache {
        private final String name;
        private final Integer adcode;
        private final String min;
        private final String max;
        private final List<Point> polygon;

        public CityRegionCache(CityRegion region, List<Point> polygon) {
            this.name = region.getName();
            this.adcode = region.getAdcode();
            this.min = region.getMin();
            this.max = region.getMax();
            this.polygon = new ArrayList<>(polygon);
        }

        public String getName() {
            return name;
        }

        public Integer getAdcode() {
            return adcode;
        }

        public String getMin() {
            return min;
        }

        public String getMax() {
            return max;
        }

        public List<Point> getPolygon() {
            return polygon;
        }
    }
}