package com.example.jwd2city.entity;

import lombok.Data;

@Data
public class CityRegion {

    private Integer id;
    private String name;
    private Integer citycode;
    private String polyline;
    private String center;
    private Integer adcode;
    private String max;
    private String min;
    private String geom;
    private String mbr;
}