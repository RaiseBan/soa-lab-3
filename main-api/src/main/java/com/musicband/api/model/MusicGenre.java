package com.musicband.api.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Music genre enumeration
 * Order for comparison: PROGRESSIVE_ROCK (0), POP (1), MATH_ROCK (2)
 */
@XmlType(name = "musicGenre")
@XmlEnum
public enum MusicGenre {
    PROGRESSIVE_ROCK,
    POP,
    MATH_ROCK
}