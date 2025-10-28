package com.musicband.api.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Embeddable
@XmlRootElement(name = "coordinates")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coordinates implements Serializable {
    private static final long serialVersionUID = 1L;
    private Double x;

    @NotNull(message = "Y coordinate cannot be null")
    @Max(value = 945, message = "Y coordinate must not exceed 945")
    private Long y;
}