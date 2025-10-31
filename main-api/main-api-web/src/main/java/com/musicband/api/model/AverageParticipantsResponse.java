package com.musicband.api.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@XmlRootElement(name = "averageParticipantsResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AverageParticipantsResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Double averageParticipants;
    private Integer totalBands;
}