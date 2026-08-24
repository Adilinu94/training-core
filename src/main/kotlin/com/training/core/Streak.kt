package com.training.core

import java.time.Instant
import java.time.ZoneId

private fun Long.toLocalDate(zone: ZoneId) = Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

/**
 * Tages-Streak (E4b, Entscheidung 20): Trainingstage in Folge; erst
 * maxGapDays + 1 zusammenhaengende Ruhetage brechen den Zaehler. Die
 * Tagesgrenze ist die lokale Mitternacht der uebergebenen Zone.
 *
 * Default maxGapDays = 0 haelt Flowtimers Verhalten unveraendert: Ein
 * heute ohne Satz startet die Zaehlung einfach beim gestrigen Tag
 * (heute ist optional), der erste Ruhetag danach bricht.
 *
 * Fusion uebergibt 2 (E4b: zwei freie Ruhetage — das 48-72-h-Fenster des
 * Krafttrainings darf den Zaehler nicht brechen). Dort verbraucht JEDER
 * Ruhetag, auch ein heute ohne Satz, einen der Kulanztage.
 *
 * Zeit wird als Epoch-Millisekunden plus [ZoneId] uebergeben, damit der
 * Kern deterministisch testbar ist (java.time statt Calendar).
 */
fun streakCount(
    sessionEpochMs: List<Long>,
    nowEpochMs: Long,
    zone: ZoneId,
    maxGapDays: Int = 0,
): Int {
    if (sessionEpochMs.isEmpty()) return 0
    val trainingDays = sessionEpochMs.map { it.toLocalDate(zone) }.toHashSet()
    val today = nowEpochMs.toLocalDate(zone)
    var cursor =
        if (maxGapDays == 0 && today !in trainingDays) today.minusDays(1) else today
    var streak = 0
    var restDays = 0
    while (restDays <= maxGapDays) {
        if (cursor in trainingDays) {
            streak++
            restDays = 0
        } else {
            restDays++
        }
        cursor = cursor.minusDays(1)
    }
    return streak
}
