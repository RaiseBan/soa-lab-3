package com.musicband.api.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.util.List;

@XmlRootElement(name = "bulkBandsResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class BulkBandsResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(name = "musicBand")
    private List<com.musicband.api.model.MusicBand> successful;

    @XmlElement(name = "error")
    private List<String> errors;

    @XmlElement
    private int totalProcessed;

    @XmlElement
    private int successCount;

    @XmlElement
    private int errorCount;

    public BulkBandsResponse() {
    }

    public List<com.musicband.api.model.MusicBand> getSuccessful() {
        return successful;
    }

    public void setSuccessful(List<com.musicband.api.model.MusicBand> successful) {
        this.successful = successful;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public int getTotalProcessed() {
        return totalProcessed;
    }

    public void setTotalProcessed(int totalProcessed) {
        this.totalProcessed = totalProcessed;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }
}
