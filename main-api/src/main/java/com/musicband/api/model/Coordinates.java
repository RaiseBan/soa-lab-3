package com.musicband.api.model;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

/**
 * Coordinates model
 */
@XmlRootElement(name = "coordinates")
@XmlAccessorType(XmlAccessType.FIELD)
public class Coordinates {
    
    private Double x;
    
    private Long y; // Required, max 945
    
    public Coordinates() {
    }
    
    public Coordinates(Double x, Long y) {
        this.x = x;
        this.y = y;
    }
    
    public Double getX() {
        return x;
    }
    
    public void setX(Double x) {
        this.x = x;
    }
    
    public Long getY() {
        return y;
    }
    
    public void setY(Long y) {
        this.y = y;
    }
}