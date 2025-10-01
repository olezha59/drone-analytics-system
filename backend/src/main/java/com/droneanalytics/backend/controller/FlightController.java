package com.droneanalytics.backend.controller;

import com.droneanalytics.backend.dto.FlightWithRegionDto;
import com.droneanalytics.backend.entity.FlightRecord;
import com.droneanalytics.backend.service.FlightService;
import com.droneanalytics.backend.service.RegionMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/flights")
@CrossOrigin(origins = "http://localhost:3000")
public class FlightController {
    
    @Autowired
    private FlightService flightService;
    
    @Autowired 
    private RegionMappingService regionMappingService;

    
    /**
     * 📌 GET /api/flights
     * Получить все полеты с пагинацией (С ID региона)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<Page<FlightWithRegionDto>> getAllFlights(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<FlightRecord> flights = flightService.getAllFlights(pageable);
        
        // ⚠️ КОНВЕРТИРУЕМ В DTO С ID РЕГИОНА!
        Page<FlightWithRegionDto> flightDtos = flights.map(flight -> 
            new FlightWithRegionDto(flight, regionMappingService)
        );
        
        return ResponseEntity.ok(flightDtos);
    }
    
    
    /**
     * 📌 GET /api/flights/{id}
     * Получить полет по ID (С ID региона)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<FlightWithRegionDto> getFlightById(@PathVariable UUID id) {
        Optional<FlightRecord> flight = flightService.getFlightById(id);
        return flight.map(f -> ResponseEntity.ok(new FlightWithRegionDto(f, regionMappingService)))
                    .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 📌 GET /api/flights/search?operator=Аэроскан&dateFrom=2024-01-01
     * Поиск полетов по критериям (С ID региона)
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<List<FlightWithRegionDto>> searchFlights(
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        
        List<FlightRecord> flights = flightService.searchFlights(operator, region, dateFrom, dateTo);
        
        // ⚠️ КОНВЕРТИРУЕМ В DTO С ID РЕГИОНА!
        List<FlightWithRegionDto> flightDtos = flights.stream()
            .map(flight -> new FlightWithRegionDto(flight, regionMappingService))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(flightDtos);
    }
    
    /**
     * 📌 GET /api/flights/operator/{operatorName}
     * Получить все полеты оператора (С ID региона)
     */
    @GetMapping("/operator/{operatorName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<List<FlightWithRegionDto>> getFlightsByOperator(@PathVariable String operatorName) {
        List<FlightRecord> flights = flightService.getFlightsByOperator(operatorName);
        
        // ⚠️ КОНВЕРТИРУЕМ В DTO С ID РЕГИОНА!
        List<FlightWithRegionDto> flightDtos = flights.stream()
            .map(flight -> new FlightWithRegionDto(flight, regionMappingService))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(flightDtos);
    }
    
    /**
     * 📌 GET /api/flights/region/{regionName}
     * Получить все полеты в регионе (С ID региона)
     */
    @GetMapping("/region/{regionName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<List<FlightWithRegionDto>> getFlightsByRegion(@PathVariable String regionName) {
        List<FlightRecord> flights = flightService.getFlightsByRegion(regionName);
        
        // ⚠️ КОНВЕРТИРУЕМ В DTO С ID РЕГИОНА!
        List<FlightWithRegionDto> flightDtos = flights.stream()
            .map(flight -> new FlightWithRegionDto(flight, regionMappingService))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(flightDtos);
    }
    
    /**
     * 📌 GET /api/flights/legacy
     * Старая версия без ID региона (для обратной совместимости)
     */
    @GetMapping("/legacy")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<Page<FlightRecord>> getAllFlightsLegacy(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<FlightRecord> flights = flightService.getAllFlights(pageable);
        return ResponseEntity.ok(flights);
    }
    
    /**
     * 📌 DELETE /api/flights/{id}
     * Удалить полет по ID (ТОЛЬКО для админов)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteFlight(@PathVariable UUID id) {
        try {
            flightService.deleteFlight(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}