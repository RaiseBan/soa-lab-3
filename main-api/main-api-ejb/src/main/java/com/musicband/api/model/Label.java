package com.musicband.api.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Positive;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Embeddable
@XmlRootElement(name = "label")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Label implements Serializable {
    private static final long serialVersionUID = 1L;
    @Positive(message = "Sales must be greater than 0")
    private Double sales;
}
