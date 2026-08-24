package com.training.core

/**
 * Trainingsmathematik (Fusion Bauplan 5.4, E6): Volumen =
 * effektive Last mal Wiederholungen. Oeffentlich in kg; intern wird auf
 * ganze Millikilogramm gerundet und ganzzahlig gerechnet, damit keine
 * Gleitkommafehler in Summen und Vergleichen landen (E1).
 */
object WorkoutMath {
    /** Erlaubte Multiplikatoren: 1 oder 2, nie berechnet (5.4). */
    fun isValidLoadMultiplier(value: Int): Boolean = value == 1 || value == 2

    /** Effektive Last eines Satzes = Implementlast mal Multiplikator. */
    fun effectiveLoadKg(
        loadKg: Double,
        loadMultiplier: Int,
    ): Double {
        require(isValidLoadMultiplier(loadMultiplier)) {
            "loadMultiplier muss 1 oder 2 sein: $loadMultiplier"
        }
        return Units.milliToKg(Units.kgToMilli(loadKg) * loadMultiplier)
    }

    /** Satzvolumen = effektive Last mal Wiederholungen. */
    fun segmentVolumeKg(
        loadKg: Double,
        loadMultiplier: Int,
        reps: Int,
    ): Double {
        require(reps > 0) { "reps muss positiv sein: $reps" }
        return Units.milliToKg(Units.kgToMilli(loadKg) * loadMultiplier * reps)
    }

    /** Volumen einer Satzgruppe = Summe der Satzvolumina (Gramm-exakt). */
    fun clusterVolumeKg(segments: List<TrainedSegment>): Double =
        Units.milliToKg(
            segments.sumOf { Units.kgToMilli(it.loadKg) * it.loadMultiplier * it.reps },
        )

    /** Formelversion des geschaetzten 1RM, fuer Datenexporte. */
    const val ONE_RM_FORMULA_VERSION = 1

    /**
     * Geschaetztes 1RM: `load * (1 + reps / 30)` — nur Trendwert, nie
     * "PR". Gueltig fuer 1..10 Wiederholungen und positive Last; das
     * Ergebnis wird auf ganze Gramm gerundet.
     */
    fun estimatedOneRmKg(
        effectiveLoadKg: Double,
        reps: Int,
    ): Double? {
        if (reps !in 1..10 || effectiveLoadKg <= 0.0) return null
        val loadMilli = Units.kgToMilli(effectiveLoadKg)
        // (load * (30 + reps)) / 30, kaufmaennisch gerundet.
        val numerator = loadMilli * (30 + reps)
        return Units.milliToKg((numerator + 15) / 30)
    }
}
