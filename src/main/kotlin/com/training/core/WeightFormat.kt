package com.training.core

import java.util.Locale

/**
 * Zahlenformate (E4c): Gewichte ganzzahlig ohne Dezimalstelle („95 kg",
 * nie „95,0 kg"); krumme Werte mit einer Stelle; Volumen unter 1000 kg
 * in kg, darueber in Tonnen mit einer Stelle. Formatierung ueber das
 * uebergebene Locale (Apps reichen Locale.getDefault() durch).
 */
object WeightFormat {
    /** Einzelgewicht: ganzahlig, nur bei Grammanteil eine Dezimalstelle. */
    fun kg(
        valueKg: Double,
        locale: Locale = Locale.getDefault(),
    ): String {
        val grams = Units.kgToMilli(valueKg)
        return if (grams % 1000 == 0L) {
            String.format(locale, "%d kg", grams / 1000)
        } else {
            String.format(locale, "%.1f kg", grams / 1000.0)
        }
    }

    /** Volumen: kg bis 999 kg, ab 1000 kg Tonnen mit einer Stelle. */
    fun volume(
        valueKg: Double,
        locale: Locale = Locale.getDefault(),
    ): String = if (valueKg < 1000.0) kg(valueKg, locale) else String.format(locale, "%.1f t", valueKg / 1000.0)
}
