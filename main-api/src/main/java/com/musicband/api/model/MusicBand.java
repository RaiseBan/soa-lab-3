package com.musicband.api.model;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import java.time.LocalDate;

/**
 * MusicBand model
 */
@XmlRootElement(name = "musicBand")
@XmlAccessorType(XmlAccessType.FIELD)
public class MusicBand {
    
    @XmlElement
    private Integer id;
    
    @XmlElement(required = true)
    private String name;
    
    @XmlElement(required = true)
    private Coordinates coordinates;
    
    @XmlElement
    private LocalDate creationDate;
    
    @XmlElement(required = true)
    private Integer numberOfParticipants;
    
    @XmlElement
    private Integer albumsCount;
    
    @XmlElement(required = true)
    private MusicGenre genre;
    
    @XmlElement
    private Label label;
    
    public MusicBand() {
    }
    
    // Getters and Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Coordinates getCoordinates() {
        return coordinates;
    }
    
    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }
    
    public LocalDate getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }
    
    public Integer getNumberOfParticipants() {
        return numberOfParticipants;
    }
    
    public void setNumberOfParticipants(Integer numberOfParticipants) {
        this.numberOfParticipants = numberOfParticipants;
    }
    
    public Integer getAlbumsCount() {
        return albumsCount;
    }
    
    public void setAlbumsCount(Integer albumsCount) {
        this.albumsCount = albumsCount;
    }
    
    public MusicGenre getGenre() {
        return genre;
    }
    
    public void setGenre(MusicGenre genre) {
        this.genre = genre;
    }
    
    public Label getLabel() {
        return label;
    }
    
    public void setLabel(Label label) {
        this.label = label;
    }
}