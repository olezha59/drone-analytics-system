package com.droneanalytics.backend.controller;

import com.droneanalytics.backend.entity.FlightRecord;
import com.droneanalytics.backend.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/flights")
@CrossOrigin(origins = "http://localhost:3000")
public class FlightController {
    
    @Autowired
    private FlightService flightService;
    
    /**
     * 📌 GET /api/flights
     * Получить все полеты с пагинацией
     */
    @GetMapping
    public ResponseEntity<Page<FlightRecord>> getAllFlights(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<FlightRecord> flights = flightService.getAllFlights(pageable);
        return ResponseEntity.ok(flights);
    }
    
    /**
     * 📌 GET /api/flights/{id}
     * Получить полет по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<FlightRecord> getFlightById(@PathVariable UUID id) {
        Optional<FlightRecord> flight = flightService.getFlightById(id);
        return flight.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 📌 GET /api/flights/search?operator=Аэроскан&dateFrom=2024-01-01
     * Поиск полетов по критериям
     */
    @GetMapping("/search")
    public ResponseEntity<List<FlightRecord>> searchFlights(
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        
        List<FlightRecord> flights = flightService.searchFlights(operator, region, dateFrom, dateTo);
        return ResponseEntity.ok(flights);
    }
    
    /**
     * 📌 GET /api/flights/operator/{operatorName}
     * Получить все полеты оператора
     */
    @GetMapping("/operator/{operatorName}")
    public ResponseEntity<List<FlightRecord>> getFlightsByOperator(@PathVariable String operatorName) {
        List<FlightRecord> flights = flightService.getFlightsByOperator(operatorName);
        return ResponseEntity.ok(flights);
    }
    
    /**
     * 📌 GET /api/flights/region/{regionName}
     * Получить все полеты в регионе
     */
    @GetMapping("/region/{regionName}")
    public ResponseEntity<List<FlightRecord>> getFlightsByRegion(@PathVariable String regionName) {
        List<FlightRecord> flights = flightService.getFlightsByRegion(regionName);
        return ResponseEntity.ok(flights);
    }
}
