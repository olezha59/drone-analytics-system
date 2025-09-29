package com.droneanalytics.backend.entity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Geometry;  // 👈 ИМПОРТ ДЛЯ ГЕОМЕТРИИ

@Entity
@Table(name = "russian_regions")
public class RussianRegion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name_1")
    private String name;
    
    @Column(name = "engtype_1")
    private String regionType;
    
    @Column(name = "iso_3166_2")
    private String isoCode;
    
    // ⚠️ ИСПРАВЛЕННО: Используем Geometry вместо byte[]
    @Column(name = "geom", columnDefinition = "geometry(Geometry,4326)")
    private Geometry geom;  // 👈 ТЕПЕРЬ Geometry, а не byte[]
    
    // Конструкторы
    public RussianRegion() {}
    
    public RussianRegion(String name, String regionType, String isoCode, Geometry geom) {
        this.name = name;
        this.regionType = regionType;
        this.isoCode = isoCode;
        this.geom = geom;
    }
    
    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getRegionType() { return regionType; }
    public void setRegionType(String regionType) { this.regionType = regionType; }
    
    public String getIsoCode() { return isoCode; }
    public void setIsoCode(String isoCode) { this.isoCode = isoCode; }
    
    // ⚠️ ИСПРАВЛЕННЫЕ ГЕТТЕРЫ И СЕТТЕРЫ ДЛЯ GEOMETRY
    public Geometry getGeom() { return geom; }
    public void setGeom(Geometry geom) { this.geom = geom; }
    
    @Override
    public String toString() {
        return "RussianRegion{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", regionType='" + regionType + '\'' +
                ", isoCode='" + isoCode + '\'' +
                '}';
    }
}