package com.musicband.grammy.model;

import com.musicband.grammy.adapter.LocalDateAdapter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Participant entity - represents a band member
 */
@Entity
@Table(name = "participants")
@XmlRootElement(name = "participant")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @XmlElement
    private Integer id;

    @NotBlank(message = "Name cannot be null or empty")
    @Column(nullable = false)
    @XmlElement(name = "n", required = true)
    private String name;

    @NotBlank(message = "Role cannot be null or empty")
    @Column(nullable = false)
    @XmlElement(required = true)
    private String role;

    @NotNull(message = "Join date cannot be null")
    @Column(nullable = false)
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate joinDate;

    @XmlElement
    private String instrument;

    @Column(nullable = false, name = "band_id")
    @XmlTransient
    private Integer bandId;
}