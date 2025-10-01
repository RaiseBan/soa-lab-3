package com.musicband.api.model;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

/**
 * Label model
 */
@XmlRootElement(name = "label")
@XmlAccessorType(XmlAccessType.FIELD)
public class Label {
    
    private Double sales; // Must be > 0 if specified
    
    public Label() {
    }
    
    public Label(Double sales) {
        this.sales = sales;
    }
    
    public Double getSales() {
        return sales;
    }
    
    public void setSales(Double sales) {
        this.sales = sales;
    }
}