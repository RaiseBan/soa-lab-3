package com.musicband.grammy.model;

import com.musicband.grammy.adapter.LocalDateAdapter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Single entity - represents a band's single
 */
@Entity
@Table(name = "singles")
@XmlRootElement(name = "single")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Single {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @XmlElement
    private Integer id;

    @NotBlank(message = "Title cannot be null or empty")
    @Column(nullable = false)
    @XmlElement(required = true)
    private String title;

    @NotNull(message = "Duration cannot be null")
    @Min(value = 1, message = "Duration must be greater than 0")
    @Column(nullable = false)
    @XmlElement(required = true)
    private Integer duration;

    @NotNull(message = "Release date cannot be null")
    @Column(nullable = false)
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate releaseDate;

    @Min(value = 1, message = "Chart position must be greater than 0 if specified")
    @XmlElement
    private Integer chartPosition;

    @Column(nullable = false, name = "band_id")
    @XmlTransient
    private Integer bandId;
}