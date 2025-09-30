package com.droneanalytics.backend.entity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Geometry;  // 👈 ИМПОРТ ДЛЯ ГЕОМЕТРИИ

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "russian_regions")
public class RussianRegion {
    
    @Id
    @Column(name = "gid")
    private Long gid;
    
    @Column(name = "name_1")
    private String name;
    
    @Column(name = "engtype_1")
    private String regionType;
    
    @Column(name = "iso_3166_2")
    private String isoCode;
    
    // ⚠️ ИСПРАВЛЕННО: Используем Geometry вместо byte[]
    @JsonIgnore
    @Column(name = "geom", columnDefinition = "geometry(Geometry,4326)")
    private Geometry geom;  // 👈 ТЕПЕРЬ Geometry, а не byte[]
    
    // Конструкторы
    public RussianRegion() {}
    
    public RussianRegion(Long gid, String name, String regionType, String isoCode, Geometry geom) {
        this.gid = gid;
        this.name = name;
        this.regionType = regionType;
        this.isoCode = isoCode;
        this.geom = geom;
    }
    
    // Геттеры и сеттеры
    public Long getGid() { return gid; }
    public void setId(Long gid) { this.gid = gid; }
    
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
                "gid=" + gid +
                ", name='" + name + '\'' +
                ", regionType='" + regionType + '\'' +
                ", isoCode='" + isoCode + '\'' +
                '}';
    }
}