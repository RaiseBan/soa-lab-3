package com.musicband.api.adapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;

/**
 * XML Adapter for LocalDate serialization/deserialization
 */
public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {
    
    @Override
    public LocalDate unmarshal(String v) {
        return v != null ? LocalDate.parse(v) : null;
    }

    @Override
    public String marshal(LocalDate v) {
        return v != null ? v.toString() : null;
    }
}