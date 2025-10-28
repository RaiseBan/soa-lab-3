package com.musicband.api.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Entity
@Table(name = "music_bands")
@XmlRootElement(name = "musicBand")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MusicBand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @XmlElement
    private Integer id;

    @NotBlank(message = "Name cannot be null or empty")
    @Column(nullable = false, columnDefinition = "TEXT")
    @XmlElement(required = true)
    private String name;

    @Embedded
    @Valid
    @NotNull(message = "Coordinates cannot be null")
    @XmlElement(required = true)
    private Coordinates coordinates;

    @Column(nullable = false, updatable = false)
    @XmlElement
    @XmlJavaTypeAdapter(com.musicband.api.adapter.LocalDateAdapter.class)
    private LocalDate creationDate;

    @NotNull(message = "Number of participants cannot be null")
    @Min(value = 1, message = "Number of participants must be greater than 0")
    @Column(nullable = false)
    @XmlElement(required = true)
    private Integer numberOfParticipants;

    @Min(value = 1, message = "Albums count must be greater than 0 if specified")
    @XmlElement
    private Integer albumsCount;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Genre cannot be null")
    @Column(nullable = false, length = 50)
    @XmlElement(required = true)
    private MusicGenre genre;

    @Embedded
    @Valid
    @XmlElement
    private Label label;

    @PrePersist
    protected void onCreate() {
        creationDate = LocalDate.now();
    }
}