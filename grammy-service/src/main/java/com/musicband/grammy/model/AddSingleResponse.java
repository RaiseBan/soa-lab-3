package com.musicband.grammy.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlRootElement(name = "addSingleResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
public class AddSingleResponse {

    @XmlElement
    private Single single;

    @XmlElement
    private BandInfo bandInfo;

    public AddSingleResponse(Single single, Integer bandId, String bandName) {
        this.single = single;
        this.bandInfo = new BandInfo(bandId, bandName);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    @NoArgsConstructor
    public static class BandInfo {
        @XmlElement
        private Integer id;

        @XmlElement(name = "n")
        private String name;

        public BandInfo(Integer id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}