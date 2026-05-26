package com.example.jwd2city.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "city_region")
public class CityRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "citycode")
    private Integer citycode;

    @Column(name = "polyline", columnDefinition = "longtext")
    private String polyline;

    @Column(name = "center", length = 100)
    private String center;

    @Column(name = "adcode")
    private Integer adcode;

    @Column(name = "max", length = 100)
    private String max;

    @Column(name = "min", length = 100)
    private String min;

    @Column(name = "geom", columnDefinition = "polygon")
    private String geom;

    @Column(name = "mbr", columnDefinition = "polygon")
    private String mbr;
}