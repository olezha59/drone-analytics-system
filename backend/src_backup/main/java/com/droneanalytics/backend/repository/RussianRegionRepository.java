package com.droneanalytics.backend.repository;

import com.droneanalytics.backend.entity.RussianRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RussianRegionRepository extends JpaRepository<RussianRegion, Long> {
    
    /**
     * 📌 Найти регион по названию
     */
    Optional<RussianRegion> findByName(String name);
    
    /**
     * 📌 Найти регионы по типу (Область, Республика и т.д.)
     */
    List<RussianRegion> findByRegionType(String regionType);
    
    /**
     * 📌 Найти регион по ISO коду (RU-MOS, RU-SPE и т.д.)
     */
    Optional<RussianRegion> findByIsoCode(String isoCode);
    
    /**
     * 📌 Поиск регионов по части названия
     */
    List<RussianRegion> findByNameContainingIgnoreCase(String namePart);
    
    // ========================
    // ГЕОПРОСТРАНСТВЕННЫЕ ЗАПРОСЫ
    // ========================
    
    /**
     * 📌 Найти регион по координатам (геопространственный запрос)
     * Использует PostGIS функцию ST_Within
     */
    @Query(value = "SELECT rr FROM RussianRegion rr WHERE ST_Within(:point, rr.geom) = true")
    Optional<RussianRegion> findRegionByCoordinates(@Param("point") org.locationtech.jts.geom.Point point);
    
    /**
     * 📌 Найти все полеты в пределах региона (через JOIN)
     * Сложный запрос соединяющий две таблицы
     */
    @Query(value = "SELECT rr.name, COUNT(fr) as flightCount " +
                   "FROM RussianRegion rr " +
                   "LEFT JOIN FlightRecord fr ON ST_Within(fr.takeoffCoords, rr.geom) = true " +
                   "WHERE fr.flightDate BETWEEN :start AND :end " +
                   "GROUP BY rr.id, rr.name " +
                   "ORDER BY flightCount DESC")
    List<Object[]> getFlightsCountByRegion(@Param("start") java.time.LocalDate start, 
                                         @Param("end") java.time.LocalDate end);
    
    /**
     * 📌 Получить статистику по регионам
     */
    @Query(value = "SELECT rr.name, rr.regionType, COUNT(fr) as flights, " +
                   "AVG(fr.flightDurationMinutes) as avgDuration " +
                   "FROM RussianRegion rr " +
                   "LEFT JOIN FlightRecord fr ON ST_Within(fr.takeoffCoords, rr.geom) = true " +
                   "GROUP BY rr.id, rr.name, rr.regionType " +
                   "ORDER BY flights DESC")
    List<Object[]> getRegionStatistics();
}