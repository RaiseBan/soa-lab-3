package com.musicband.grammy.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlRootElement(name = "addParticipantResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
public class AddParticipantResponse {

    @XmlElement
    private Participant participant;

    @XmlElement
    private Integer updatedParticipantsCount;

    @XmlElement
    private BandInfo bandInfo;

    public AddParticipantResponse(Participant participant, Integer updatedParticipantsCount,
                                  Integer bandId, String bandName) {
        this.participant = participant;
        this.updatedParticipantsCount = updatedParticipantsCount;
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