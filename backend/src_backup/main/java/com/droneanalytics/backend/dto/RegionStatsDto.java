package com.droneanalytics.backend.dto;

public class RegionStatsDto {
    private String regionName;
    private Long flightCount;
    private Long operatorCount;
    private Double averageDuration;
    
    public RegionStatsDto() {}
    
    public RegionStatsDto(String regionName, Long flightCount, Long operatorCount, Double averageDuration) {
        this.regionName = regionName;
        this.flightCount = flightCount;
        this.operatorCount = operatorCount;
        this.averageDuration = averageDuration;
    }
    
    // Геттеры и сеттеры
    public String getRegionName() { return regionName; }
    public void setRegionName(String regionName) { this.regionName = regionName; }
    
    public Long getFlightCount() { return flightCount; }
    public void setFlightCount(Long flightCount) { this.flightCount = flightCount; }
    
    public Long getOperatorCount() { return operatorCount; }
    public void setOperatorCount(Long operatorCount) { this.operatorCount = operatorCount; }
    
    public Double getAverageDuration() { return averageDuration; }
    public void setAverageDuration(Double averageDuration) { this.averageDuration = averageDuration; }
}