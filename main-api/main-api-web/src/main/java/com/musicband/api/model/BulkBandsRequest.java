package com.musicband.api.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "bulkBandsRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class BulkBandsRequest {
    
    @XmlElement(name = "musicBand")
    private List<com.musicband.api.model.MusicBand> bands;

    public BulkBandsRequest() {
    }

    public BulkBandsRequest(List<com.musicband.api.model.MusicBand> bands) {
        this.bands = bands;
    }

    public List<com.musicband.api.model.MusicBand> getBands() {
        return bands;
    }

    public void setBands(List<com.musicband.api.model.MusicBand> bands) {
        this.bands = bands;
    }
}
