package com.musicband.api.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

import java.io.Serializable;


@XmlType(name = "musicGenre")
@XmlEnum
public enum MusicGenre {
    PROGRESSIVE_ROCK,
    POP,
    MATH_ROCK
}
