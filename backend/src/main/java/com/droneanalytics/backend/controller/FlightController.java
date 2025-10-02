package com.droneanalytics.backend.controller;

import com.droneanalytics.backend.dto.FlightWithRegionDto;
import com.droneanalytics.backend.entity.FlightRecord;
import com.droneanalytics.backend.service.FlightService;
import com.droneanalytics.backend.service.RegionMappingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Полеты", description = "API для управления данными о полетах БПЛА")
@SecurityRequirement(name = "bearerAuth") // 👈 Требует аутентификации для всех методов
public class FlightController {
    
    @Autowired
    private FlightService flightService;
    
    @Autowired 
    private RegionMappingService regionMappingService;

    
    /**
     * 📌 GET /api/flights
     * Получить все полеты с пагинацией (С ID региона)
     */
    @Operation(summary = "Получить все полеты", description = "Возвращает список всех полетов с пагинацией")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение списка полетов"),
        @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<Page<FlightWithRegionDto>> getAllFlights(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Размер страницы", example = "20") 
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
    @Operation(summary = "Получить полет по ID", description = "Возвращает детальную информацию о полете по идентификатору")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Полет найден"),
        @ApiResponse(responseCode = "404", description = "Полет не найден"),
        @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<FlightWithRegionDto> getFlightById(
            @Parameter(description = "UUID полета", example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable UUID id) {
        Optional<FlightRecord> flight = flightService.getFlightById(id);
        return flight.map(f -> ResponseEntity.ok(new FlightWithRegionDto(f, regionMappingService)))
                    .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 📌 GET /api/flights/search?operator=Аэроскан&dateFrom=2024-01-01
     * Поиск полетов по критериям (С ID региона)
     */
    @Operation(summary = "Поиск полетов", description = "Поиск полетов по оператору, региону и дате")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешный поиск"),
        @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<List<FlightWithRegionDto>> searchFlights(
            @Parameter(description = "Название оператора", example = "Аэроскан") 
            @RequestParam(required = false) String operator,
            
            @Parameter(description = "Название региона", example = "Московская область") 
            @RequestParam(required = false) String region,
            
            @Parameter(description = "Дата начала периода (ГГГГ-ММ-ДД)", example = "2024-01-01") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            
            @Parameter(description = "Дата окончания периода (ГГГГ-ММ-ДД)", example = "2024-12-31") 
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
    @Operation(summary = "Получить полеты оператора", description = "Возвращает все полеты указанного оператора")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение"),
        @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping("/operator/{operatorName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<List<FlightWithRegionDto>> getFlightsByOperator(
            @Parameter(description = "Название оператора", example = "Аэроскан") 
            @PathVariable String operatorName) {
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
    @Operation(summary = "Получить полеты в регионе", description = "Возвращает все полеты в указанном регионе")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение"),
        @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping("/region/{regionName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<List<FlightWithRegionDto>> getFlightsByRegion(
            @Parameter(description = "Название региона", example = "Московская область") 
            @PathVariable String regionName) {
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
    @Operation(summary = "Получить все полеты (legacy)", description = "Старая версия API без ID региона для обратной совместимости")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешное получение"),
        @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping("/legacy")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<Page<FlightRecord>> getAllFlightsLegacy(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Размер страницы", example = "20") 
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<FlightRecord> flights = flightService.getAllFlights(pageable);
        return ResponseEntity.ok(flights);
    }
    
    /**
     * 📌 DELETE /api/flights/{id}
     * Удалить полет по ID (ТОЛЬКО для админов)
     */
    @Operation(summary = "Удалить полет", description = "Удаляет полет по идентификатору (только для администраторов)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Полет успешно удален"),
        @ApiResponse(responseCode = "404", description = "Полет не найден"),
        @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав (требуется роль ADMIN)")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteFlight(
            @Parameter(description = "UUID полета", example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable UUID id) {
        try {
            flightService.deleteFlight(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}