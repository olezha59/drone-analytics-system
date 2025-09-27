package com.droneanalytics.backend.repository;

import com.droneanalytics.backend.entity.FlightRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface FlightRecordRepository extends JpaRepository<FlightRecord, UUID> {
    
    // ========================
    // АВТОМАТИЧЕСКИЕ ЗАПРОСЫ ПО ИМЕНИ МЕТОДА
    // ========================
    
    /**
     * 📌 Автоматически генерирует: 
     * SELECT * FROM flight_records WHERE center_code = ?
     */
    List<FlightRecord> findByCenterCode(String centerCode);
    
    /**
     * 📌 Найти полеты по оператору
     * Генерирует: SELECT * FROM flight_records WHERE operator_name = ?
     */
    List<FlightRecord> findByOperatorName(String operatorName);
    
    /**
     * 📌 Найти полеты по дате
     * Генерирует: SELECT * FROM flight_records WHERE flight_date = ?
     */
    List<FlightRecord> findByFlightDate(LocalDate flightDate);
    
    /**
     * 📌 Найти полеты по диапазону дат
     * Генерирует: SELECT * FROM flight_records WHERE flight_date BETWEEN ? AND ?
     */
    List<FlightRecord> findByFlightDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 📌 Найти полеты по типу воздушного судна
     * Генерирует: SELECT * FROM flight_records WHERE aircraft_type = ?
     */
    List<FlightRecord> findByAircraftType(String aircraftType);
    
    /**
     * 📌 Подсчитать полеты по оператору
     * Генерирует: SELECT COUNT(*) FROM flight_records WHERE operator_name = ?
     */
    Long countByOperatorName(String operatorName);
    
    // ========================
    // СЛОЖНЫЕ ЗАПРОСЫ ЧЕРЕЗ @Query
    // ========================
    
    /**
     * 📌 Подсчет полетов за период
     * JPQL запрос (Java Persistence Query Language)
     */
    @Query("SELECT COUNT(f) FROM FlightRecord f WHERE f.flightDate BETWEEN :start AND :end")
    Long countFlightsInPeriod(@Param("start") LocalDate start, @Param("end") LocalDate end);
    
    /**
     * 📌 Статистика по операторам за период
     * Группировка по операторам + подсчет
     */
    @Query("SELECT f.operatorName, COUNT(f) FROM FlightRecord f " +
           "WHERE f.flightDate BETWEEN :start AND :end " +
           "GROUP BY f.operatorName " +
           "ORDER BY COUNT(f) DESC")
    List<Object[]> getOperatorStats(@Param("start") LocalDate start, @Param("end") LocalDate end);
    
    /**
     * 📌 Найти топ-N самых активных дней
     */
    @Query("SELECT f.flightDate, COUNT(f) as flightCount FROM FlightRecord f " +
           "GROUP BY f.flightDate " +
           "ORDER BY flightCount DESC " +
           "LIMIT :limit")
    List<Object[]> findTopActiveDays(@Param("limit") int limit);
    
    /**
     * 📌 Найти полеты по региону взлета
     */
    List<FlightRecord> findByRegionTakeoff(String regionName);
    
    /**
     * 📌 Найти уникальных операторов
     */
    @Query("SELECT DISTINCT f.operatorName FROM FlightRecord f ORDER BY f.operatorName")
    List<String> findDistinctOperators();
    
    /**
     * 📌 Средняя продолжительность полетов по оператору
     */
    @Query("SELECT f.operatorName, AVG(f.flightDurationMinutes) FROM FlightRecord f " +
           "GROUP BY f.operatorName " +
           "HAVING AVG(f.flightDurationMinutes) IS NOT NULL")
    List<Object[]> findAverageDurationByOperator();

    @Query("SELECT COUNT(DISTINCT f.operatorName) FROM FlightRecord f")
    Long countDistinctOperators();
}