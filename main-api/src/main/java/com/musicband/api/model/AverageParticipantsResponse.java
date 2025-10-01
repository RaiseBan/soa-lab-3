package com.musicband.api.model;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

/**
 * Response for average participants statistics
 */
@XmlRootElement(name = "averageParticipantsResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class AverageParticipantsResponse {
    
    private Double averageParticipants;
    private Integer totalBands;
    
    public AverageParticipantsResponse() {
    }
    
    public AverageParticipantsResponse(Double averageParticipants, Integer totalBands) {
        this.averageParticipants = averageParticipants;
        this.totalBands = totalBands;
    }
    
    public Double getAverageParticipants() {
        return averageParticipants;
    }
    
    public void setAverageParticipants(Double averageParticipants) {
        this.averageParticipants = averageParticipants;
    }
    
    public Integer getTotalBands() {
        return totalBands;
    }
    
    public void setTotalBands(Integer totalBands) {
        this.totalBands = totalBands;
    }
}