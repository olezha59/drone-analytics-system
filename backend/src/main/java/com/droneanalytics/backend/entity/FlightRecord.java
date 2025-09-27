package com.droneanalytics.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;  // 👈 ИМПОРТ ДЛЯ ГЕОДАННЫХ
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "flight_records")
public class FlightRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "center_code")
    private String centerCode;
    
    @Column(name = "flight_date")
    private LocalDate flightDate;
    
    @Column(name = "flight_number")
    private String flightNumber;
    
    @Column(name = "aircraft_type")  
    private String aircraftType;
    
    @Column(name = "operator_name")
    private String operatorName;
    
    @Column(name = "flight_duration_minutes")
    private Integer flightDurationMinutes;
    
    @Column(name = "takeoff_coords_text")
    private String takeoffCoordsText;
    
    // ⚠️ ИСПРАВЛЕННО: Используем Point вместо String
    @JsonIgnore
    @Column(name = "takeoff_coords", columnDefinition = "geometry(Point,4326)")
    private Point takeoffCoords;  // 👈 ТЕПЕРЬ Point, а не String
    
    @Column(name = "region_takeoff")
    private String regionTakeoff;
    
    // Конструкторы, геттеры, сеттеры остаются без изменений
    public FlightRecord() {}
    
    // Конструктор
    public FlightRecord(String centerCode, LocalDate flightDate, String flightNumber, 
                       String aircraftType, String operatorName, Integer flightDurationMinutes, 
                       String takeoffCoordsText, Point takeoffCoords, String regionTakeoff) {
        this.centerCode = centerCode;
        this.flightDate = flightDate;
        this.flightNumber = flightNumber;
        this.aircraftType = aircraftType;
        this.operatorName = operatorName;
        this.flightDurationMinutes = flightDurationMinutes;
        this.takeoffCoordsText = takeoffCoordsText;
        this.takeoffCoords = takeoffCoords;
        this.regionTakeoff = regionTakeoff;
    }
    
    // Геттеры и сеттеры
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getCenterCode() { return centerCode; }
    public void setCenterCode(String centerCode) { this.centerCode = centerCode; }
    
    public LocalDate getFlightDate() { return flightDate; }
    public void setFlightDate(LocalDate flightDate) { this.flightDate = flightDate; }
    
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    
    public String getAircraftType() { return aircraftType; }
    public void setAircraftType(String aircraftType) { this.aircraftType = aircraftType; }
    
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    
    public Integer getFlightDurationMinutes() { return flightDurationMinutes; }
    public void setFlightDurationMinutes(Integer flightDurationMinutes) { this.flightDurationMinutes = flightDurationMinutes; }
    
    public String getTakeoffCoordsText() { return takeoffCoordsText; }
    public void setTakeoffCoordsText(String takeoffCoordsText) { this.takeoffCoordsText = takeoffCoordsText; }
    
    // ⚠️ ИСПРАВЛЕННЫЕ ГЕТТЕРЫ И СЕТТЕРЫ ДЛЯ POINT
    public Point getTakeoffCoords() { return takeoffCoords; }
    public void setTakeoffCoords(Point takeoffCoords) { this.takeoffCoords = takeoffCoords; }
    
    public String getRegionTakeoff() { return regionTakeoff; }
    public void setRegionTakeoff(String regionTakeoff) { this.regionTakeoff = regionTakeoff; }
    
    @Override
    public String toString() {
        return "FlightRecord{" +
                "id=" + id +
                ", centerCode='" + centerCode + '\'' +
                ", flightDate=" + flightDate +
                ", flightNumber='" + flightNumber + '\'' +
                ", aircraftType='" + aircraftType + '\'' +
                ", operatorName='" + operatorName + '\'' +
                '}';
    }
}