package com.example.jwd2city.repository;

import com.example.jwd2city.entity.CityRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRegionRepository extends JpaRepository<CityRegion, Integer> {
}