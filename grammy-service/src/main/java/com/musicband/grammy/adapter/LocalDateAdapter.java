package com.musicband.grammy.adapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;

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