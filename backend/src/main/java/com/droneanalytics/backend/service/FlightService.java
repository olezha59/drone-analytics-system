package com.droneanalytics.backend.service;

import com.droneanalytics.backend.entity.FlightRecord;
import com.droneanalytics.backend.repository.FlightRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FlightService {

    @Autowired
    private FlightRecordRepository flightRecordRepository;

    /**
     * 📌 Получить полеты по center_code
     */
    public List<FlightRecord> getFlightsByCenterCode(String centerCode) {
        return flightRecordRepository.findByCenterCode(centerCode);
    }



    /**
     * 📌 Получить все полеты с пагинацией
     */
    public Page<FlightRecord> getAllFlights(Pageable pageable) {
        return flightRecordRepository.findAll(pageable);
    }

    /**
     * 📌 Найти полет по ID
     */
    public Optional<FlightRecord> getFlightById(UUID id) {
        return flightRecordRepository.findById(id);
    }

    /**
     * 📌 Поиск полетов по критериям (УПРОЩЕННАЯ И РАБОЧАЯ ВЕРСИЯ)
     */
    public List<FlightRecord> searchFlights(String operator, String region, 
                                           LocalDate dateFrom, LocalDate dateTo) {
        
        // ✅ НАЧИНАЕМ СО ВСЕХ ЗАПИСЕЙ
        List<FlightRecord> result = flightRecordRepository.findAll();
        
        // 📋 ФИЛЬТРАЦИЯ ПО ОПЕРАТОРУ
        if (operator != null && !operator.trim().isEmpty()) {
            result = result.stream()
                          .filter(flight -> operator.equalsIgnoreCase(flight.getOperatorName()))
                          .collect(Collectors.toList());
        }
        
        // 🗺️ ФИЛЬТРАЦИЯ ПО РЕГИОНУ
        if (region != null && !region.trim().isEmpty()) {
            result = result.stream()
                          .filter(flight -> region.equalsIgnoreCase(flight.getRegionTakeoff()))
                          .collect(Collectors.toList());
        }
        
        // 📅 ФИЛЬТРАЦИЯ ПО ДАТЕ
        if (dateFrom != null && dateTo != null) {
            result = result.stream()
                          .filter(flight -> flight.getFlightDate() != null)
                          .filter(flight -> !flight.getFlightDate().isBefore(dateFrom) && 
                                           !flight.getFlightDate().isAfter(dateTo))
                          .collect(Collectors.toList());
        }
        
        return result;
    }

    /**
     * 📌 Получить полеты оператора
     */
    public List<FlightRecord> getFlightsByOperator(String operatorName) {
        return flightRecordRepository.findByOperatorName(operatorName);
    }

    /**
     * 📌 Получить полеты в регионе
     */
    public List<FlightRecord> getFlightsByRegion(String regionName) {
        return flightRecordRepository.findByRegionTakeoff(regionName);
    }

    /**
     * 📌 Сохранить новый полет
     */
    public FlightRecord saveFlight(FlightRecord flight) {
        return flightRecordRepository.save(flight);
    }

    /**
     * 📌 Удалить полет
     */
    public void deleteFlight(UUID id) {
        flightRecordRepository.deleteById(id);
    }

    /**
     * 📌 Получить уникальных операторов
     */
    public List<String> getUniqueOperators() {
        return flightRecordRepository.findDistinctOperators();
    }

    /**
     * 📌 Получить полеты за период
     */
    public List<FlightRecord> getFlightsByDateRange(LocalDate start, LocalDate end) {
        return flightRecordRepository.findByFlightDateBetween(start, end);
    }
}