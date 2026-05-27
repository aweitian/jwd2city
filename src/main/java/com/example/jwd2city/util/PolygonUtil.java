package com.example.jwd2city.util;

import java.util.ArrayList;
import java.util.List;

public class PolygonUtil {

    public static boolean isPointInPolygon(Point point, List<Point> polygon) {
        if (polygon == null || polygon.size() < 3) {
            return false;
        }

        int n = polygon.size();
        boolean inside = false;

        for (int i = 0, j = n - 1; i < n; j = i++) {
            Point pi = polygon.get(i);
            Point pj = polygon.get(j);

            if (((pi.getLat() > point.getLat()) != (pj.getLat() > point.getLat()))
                    && (point.getLon() < (pj.getLon() - pi.getLon()) * (point.getLat() - pi.getLat()) / (pj.getLat() - pi.getLat()) + pi.getLon())) {
                inside = !inside;
            }
        }

        return inside;
    }

    public static boolean isPointInMBR(Point point, String min, String max) {
        if (min == null || max == null) {
            return true;
        }

        try {
            String[] minParts = min.split(",");
            String[] maxParts = max.split(",");
            double minLon = Double.parseDouble(minParts[0].trim());
            double minLat = Double.parseDouble(minParts[1].trim());
            double maxLon = Double.parseDouble(maxParts[0].trim());
            double maxLat = Double.parseDouble(maxParts[1].trim());

            return point.getLon() >= minLon && point.getLon() <= maxLon
                    && point.getLat() >= minLat && point.getLat() <= maxLat;
        } catch (Exception e) {
            return true;
        }
    }

    public static List<Point> parsePolyline(String polyline) {
        if (polyline == null || polyline.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return java.util.Arrays.stream(polyline.split(";"))
                .map(pointStr -> {
                    String[] parts = pointStr.split(",");
                    if (parts.length >= 2) {
                        try {
                            double lon = Double.parseDouble(parts[0].trim());
                            double lat = Double.parseDouble(parts[1].trim());
                            return new Point(lon, lat);
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    }
                    return null;
                })
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());
    }
}