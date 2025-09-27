package com.droneanalytics.backend.dto;

import java.util.List;
import java.util.Map;

public class RegionAnalyticsDto {
    private Long totalFlights;
    private Long uniqueOperators;
    private Double averageFlightDuration;
    private List<RegionStatsDto> topRegions;
    private Map<String, Long> flightsByDate;
    
    // Конструкторы
    public RegionAnalyticsDto() {}
    
    public RegionAnalyticsDto(Long totalFlights, Long uniqueOperators, 
                             Double averageFlightDuration, List<RegionStatsDto> topRegions,
                             Map<String, Long> flightsByDate) {
        this.totalFlights = totalFlights;
        this.uniqueOperators = uniqueOperators;
        this.averageFlightDuration = averageFlightDuration;
        this.topRegions = topRegions;
        this.flightsByDate = flightsByDate;
    }
    
    // Геттеры и сеттеры
    public Long getTotalFlights() { return totalFlights; }
    public void setTotalFlights(Long totalFlights) { this.totalFlights = totalFlights; }
    
    public Long getUniqueOperators() { return uniqueOperators; }
    public void setUniqueOperators(Long uniqueOperators) { this.uniqueOperators = uniqueOperators; }
    
    public Double getAverageFlightDuration() { return averageFlightDuration; }
    public void setAverageFlightDuration(Double averageFlightDuration) { this.averageFlightDuration = averageFlightDuration; }
    
    public List<RegionStatsDto> getTopRegions() { return topRegions; }
    public void setTopRegions(List<RegionStatsDto> topRegions) { this.topRegions = topRegions; }
    
    public Map<String, Long> getFlightsByDate() { return flightsByDate; }
    public void setFlightsByDate(Map<String, Long> flightsByDate) { this.flightsByDate = flightsByDate; }
}
