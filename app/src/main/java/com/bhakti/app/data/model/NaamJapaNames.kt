package com.bhakti.app.data.model

/**
 * Simple Devanagari name for each deity - just the name itself (a proper
 * noun), not a mantra formula. Naam Japa is the practice of repeating a
 * deity's own name; keeping this separate from [Deity.displayName] (Latin
 * script) and from Mantra content (which pairs a full chant with meaning).
 */
fun naamJapaNameFor(deity: Deity): String = when (deity) {
    Deity.SHIVA -> "शिव"
    Deity.KRISHNA -> "कृष्ण"
    Deity.RAM -> "राम"
    Deity.HANUMAN -> "हनुमान"
    Deity.GANESH -> "गणेश"
    Deity.DURGA -> "दुर्गा"
    Deity.LAKSHMI -> "लक्ष्मी"
    Deity.SARASWATI -> "सरस्वती"
    Deity.VISHNU -> "विष्णु"
    Deity.RADHA -> "राधा"
    Deity.SAI_BABA -> "साईं"
    Deity.JAGANNATH -> "जगन्नाथ"
    Deity.SHANI_DEV -> "शनि"
    Deity.KALI -> "काली"
    Deity.BALAJI -> "बालाजी"
}
