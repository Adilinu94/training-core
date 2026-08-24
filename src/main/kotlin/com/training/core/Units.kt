package com.training.core

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Einheitenvertrag des Kerns (E1): Die oeffentliche Schnittstelle spricht
 * Kilogramm als Double; intern wird ganzzahlig in Millikilogramm gerechnet.
 * Nur so sind Gleichheitsvergleiche auf Gewichte exakt — 0,001 kg ist als
 * Binaer-Double nicht darstellbar. Jede Gewichtseingabe wird am Kern-Eingang
 * auf 3 Dezimalstellen (ganze Gramm) gerundet, HALF_UP.
 */
object Units {
    private const val GRAMS_PER_KG = 1000L

    /** kg -> ganze Millikilogramm, kaufmaennisch auf ganze Gramm gerundet. */
    fun kgToMilli(kg: Double): Long =
        BigDecimal.valueOf(kg)
            .multiply(BigDecimal(GRAMS_PER_KG))
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()

    /** Millikilogramm -> kg. Mit [kgToMilli] zusammen verlustfrei: ganze
     *  Gramm ueberstehen den Rundweg immer exakt. */
    fun milliToKg(milli: Long): Double = milli / GRAMS_PER_KG.toDouble()
}
