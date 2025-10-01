package com.musicband.api.model;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

/**
 * Response for bands list with pagination
 */
@XmlRootElement(name = "bandsResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class BandsResponse {
    
    @XmlElementWrapper(name = "bands")
    @XmlElement(name = "musicBand")
    private List<MusicBand> bands;
    
    private Long totalElements;
    private Integer totalPages;
    private Integer currentPage;
    private Integer pageSize;
    
    public BandsResponse() {
    }
    
    public BandsResponse(List<MusicBand> bands, Long totalElements, 
                         Integer totalPages, Integer currentPage, Integer pageSize) {
        this.bands = bands;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }
    
    // Getters and Setters
    
    public List<MusicBand> getBands() {
        return bands;
    }
    
    public void setBands(List<MusicBand> bands) {
        this.bands = bands;
    }
    
    public Long getTotalElements() {
        return totalElements;
    }
    
    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }
    
    public Integer getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
    
    public Integer getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }
    
    public Integer getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}