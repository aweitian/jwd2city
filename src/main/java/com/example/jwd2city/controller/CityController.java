package com.example.jwd2city.controller;

import com.example.jwd2city.service.CityService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping("/city")
    public ResponseEntity<CityResponse> getCityByCoordinate(
            @RequestParam double lon,
            @RequestParam double lat) {

        log.info("Query city by coordinate: lon={}, lat={}", lon, lat);
        
        CityService.CityResult result = cityService.findCityByCoordinate(lon, lat);
        
        if (result != null) {
            return ResponseEntity.ok(new CityResponse(result.getProvince(),result.getCity(), result.getAdcode(), "success"));
        } else {
            return ResponseEntity.ok(new CityResponse(null,null, null, "not_found"));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats() {
        return ResponseEntity.ok(new StatsResponse(cityService.getCachedCount()));
    }

    @Data
    @AllArgsConstructor
    public static class CityResponse {
        private String province;
        private String city;
        private Integer adcode;
        private String status;
    }

    @Data
    @AllArgsConstructor
    public static class StatsResponse {
        private int cachedCount;
    }
}