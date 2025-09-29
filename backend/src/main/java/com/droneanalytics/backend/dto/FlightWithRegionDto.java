package com.droneanalytics.backend.dto;

import com.droneanalytics.backend.entity.FlightRecord;
import com.droneanalytics.backend.service.RegionMappingService;
import java.util.UUID;

public class FlightWithRegionDto {
    private UUID id;
    private String centerCode;
    private String flightDate;
    private String flightNumber;
    private String aircraftType;
    private String operatorName;
    private String takeoffCoordsText;
    private Double[] takeoffCoordsArray;
    private String regionTakeoff;
    private Long regionTakeoffId;
    
    // Конструктор принимает FlightRecord и RegionMappingService
    public FlightWithRegionDto(FlightRecord flight, RegionMappingService regionMappingService) {
        this.id = flight.getId();
        this.centerCode = flight.getCenterCode();
        this.flightDate = flight.getFlightDate() != null ? flight.getFlightDate().toString() : null;
        this.flightNumber = flight.getFlightNumber();
        this.aircraftType = flight.getAircraftType();
        this.operatorName = flight.getOperatorName();
        this.takeoffCoordsText = flight.getTakeoffCoordsText();
        this.takeoffCoordsArray = flight.getTakeoffCoordsArray();
        this.regionTakeoff = flight.getRegionTakeoff();
        
        // Получаем ID региона по center_code
        if (regionMappingService != null && flight.getCenterCode() != null) {
            this.regionTakeoffId = regionMappingService.getRegionIdByCenterCode(flight.getCenterCode());
            System.out.println("DEBUG: CenterCode: '" + flight.getCenterCode() + "' -> RegionId: " + this.regionTakeoffId);
        } else {
            System.out.println("DEBUG: RegionMappingService is null: " + (regionMappingService == null));
            System.out.println("DEBUG: CenterCode is null: " + (flight.getCenterCode() == null));
        }
    }
    
    // Геттеры
    public UUID getId() { return id; }
    public String getCenterCode() { return centerCode; }
    public String getFlightDate() { return flightDate; }
    public String getFlightNumber() { return flightNumber; }
    public String getAircraftType() { return aircraftType; }
    public String getOperatorName() { return operatorName; }
    public String getTakeoffCoordsText() { return takeoffCoordsText; }
    public Double[] getTakeoffCoordsArray() { return takeoffCoordsArray; }
    public String getRegionTakeoff() { return regionTakeoff; }
    public Long getRegionTakeoffId() { return regionTakeoffId; }
}