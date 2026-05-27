package com.example.jwd2city.util;

import com.example.jwd2city.entity.CityRegion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataExportUtil {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/geo_city?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    public static void main(String[] args) {
        List<CityRegion> regions = new ArrayList<>();
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL driver not found: " + e.getMessage());
            return;
        }
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT name, citycode, id, polyline, center, adcode, max, min, geom, mbr FROM city_region";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    CityRegion region = new CityRegion();
                    region.setName(rs.getString("name"));
                    region.setCitycode(rs.getInt("citycode"));
                    region.setId(rs.getInt("id"));
                    region.setPolyline(rs.getString("polyline"));
                    region.setCenter(rs.getString("center"));
                    region.setAdcode(rs.getInt("adcode"));
                    region.setMax(rs.getString("max"));
                    region.setMin(rs.getString("min"));
                    region.setGeom(rs.getString("geom"));
                    region.setMbr(rs.getString("mbr"));
                    regions.add(region);
                }
            }
            
            System.out.println("Loaded " + regions.size() + " city regions from database");
            
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return;
        }
        
        String outputDir = "src/main/resources/cityData";
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        List<CityRegion> currentCityRegions = new ArrayList<>();
        String currentCityName = null;
        int fileCount = 0;
        
        for (CityRegion region : regions) {
            String cityName = region.getName();
            if (cityName == null || cityName.isEmpty()) continue;
            
            if (!cityName.equals(currentCityName)) {
                if (currentCityName != null && !currentCityRegions.isEmpty()) {
                    writeCityFile(currentCityName, currentCityRegions, mapper, outputDir);
                    fileCount++;
                }
                currentCityName = cityName;
                currentCityRegions = new ArrayList<>();
            }
            currentCityRegions.add(region);
        }
        
        if (currentCityName != null && !currentCityRegions.isEmpty()) {
            writeCityFile(currentCityName, currentCityRegions, mapper, outputDir);
            fileCount++;
        }
        
        System.out.println("\nTotal exported " + fileCount + " files");
    }
    
    private static void writeCityFile(String cityName, List<CityRegion> regions, ObjectMapper mapper, String outputDir) {
        String safeName = cityName.replaceAll("[\\\\/:*?\"<>|]", "_");
        String fileName = outputDir + "/" + safeName + ".json";
        
        try {
            mapper.writeValue(new File(fileName), regions);
            System.out.println("Exported: " + fileName + " (" + regions.size() + " regions)");
        } catch (IOException e) {
            System.err.println("Failed to export " + cityName + ": " + e.getMessage());
        }
    }
}