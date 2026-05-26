package com.example.jwd2city.service;

import com.example.jwd2city.entity.CityRegion;
import com.example.jwd2city.repository.CityRegionRepository;
import com.example.jwd2city.util.Point;
import com.example.jwd2city.util.PolygonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class CityService {

    private final CityRegionRepository cityRegionRepository;
    private final List<CityRegionCache> cache = new CopyOnWriteArrayList<>();

    @Autowired
    public CityService(CityRegionRepository cityRegionRepository) {
        this.cityRegionRepository = cityRegionRepository;
    }

    @PostConstruct
    public void init() {
        log.info("Loading city region data into memory...");
        List<CityRegion> regions = cityRegionRepository.findAll();
        log.info("Found {} city regions", regions.size());

        for (CityRegion region : regions) {
            List<Point> polygon = PolygonUtil.parsePolyline(region.getPolyline());
            if (!polygon.isEmpty()) {
                cache.add(new CityRegionCache(region, polygon));
            }
        }

        log.info("Loaded {} valid city regions into memory", cache.size());
    }

    public CityResult findCityByCoordinate(double lon, double lat) {
        Point point = new Point(lon, lat);

        for (CityRegionCache cacheItem : cache) {
            if (!PolygonUtil.isPointInMBR(point, cacheItem.getMin(), cacheItem.getMax())) {
                continue;
            }

            if (PolygonUtil.isPointInPolygon(point, cacheItem.getPolygon())) {
                return new CityResult(cacheItem.getName(), cacheItem.getAdcode());
            }
        }

        return null;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class CityResult {
        private String name;
        private Integer adcode;
    }

    public int getCachedCount() {
        return cache.size();
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