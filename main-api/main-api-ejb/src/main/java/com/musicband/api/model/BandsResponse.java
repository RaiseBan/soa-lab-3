package com.musicband.api.model;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


@XmlRootElement(name = "bandsResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BandsResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    @XmlElementWrapper(name = "bands")
    @XmlElement(name = "musicBand")
    private List<MusicBand> bands;

    private Long totalElements;
    private Integer totalPages;
    private Integer currentPage;
    private Integer pageSize;
}
