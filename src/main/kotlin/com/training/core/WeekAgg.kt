package com.training.core

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

private fun Long.toLocalDate(zone: ZoneId): LocalDate = Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

private fun LocalDate.startOfDayEpochMs(zone: ZoneId): Long = atStartOfDay(zone).toInstant().toEpochMilli()

/**
 * Wochenaggregation (Montag-Wochenstart, Entscheidung 15): Verteilt die
 * Sessions eines Zeitraums auf die 7 Tage der Woche ab weekStartEpochMs.
 * Index 0 = Montag. Die Tag-Zuordnung laeuft kalendarisch ueber LocalDate
 * (DST-sicher — eine Herbstumstellungswoche hat 169 Stunden, eine fixe
 * Millisekunden-Differenz wuerde Sessions am Sonntag falsch einsortieren).
 */
data class WeekResult(
    val dayVolumeKg: DoubleArray,
    val dayHit: BooleanArray,
    val workouts: Int,
    val durationSec: Int,
    val totalVolumeKg: Double,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WeekResult) return false
        return dayVolumeKg.contentEquals(other.dayVolumeKg) && dayHit.contentEquals(other.dayHit) &&
            workouts == other.workouts && durationSec == other.durationSec &&
            totalVolumeKg == other.totalVolumeKg
    }

    override fun hashCode(): Int {
        var r = dayVolumeKg.contentHashCode()
        r = 31 * r + dayHit.contentHashCode()
        r = 31 * r + workouts
        r = 31 * r + durationSec
        r = 31 * r + totalVolumeKg.hashCode()
        return r
    }
}

/** Eine Trainingseinheit fuer die Aggregation: Uhrzeit, Dauer, Volumen. */
data class SessionSummary(
    val dateMs: Long,
    val durationSec: Int = 0,
    val volumeKg: Double = 0.0,
)

/** Montag 00:00 (lokal) der Woche, die nowEpochMs enthaelt (ISO-8601). */
fun weekStart(
    nowEpochMs: Long,
    zone: ZoneId,
): Long = nowEpochMs.toLocalDate(zone).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).startOfDayEpochMs(zone)

/** Lokale Tagesgrenze (Mitternacht) eines Zeitstempels. */
fun dayFloor(
    epochMs: Long,
    zone: ZoneId,
): Long = epochMs.toLocalDate(zone).startOfDayEpochMs(zone)

/**
 * Buckets die Sessions in die 7 Tage der Woche ab weekStartEpochMs.
 * Volumen wird in ganzen Gramm akkumuliert (E1) und erst am Ende als kg
 * zurueckgegeben. Sessions vor dem Wochenstart zaehlen nicht.
 */
fun weekAggregate(
    sessions: List<SessionSummary>,
    weekStartEpochMs: Long,
    zone: ZoneId,
): WeekResult {
    val start = Instant.ofEpochMilli(weekStartEpochMs).atZone(zone).toLocalDate()
    val volMilli = LongArray(7)
    val hit = BooleanArray(7)
    var workouts = 0
    var dur = 0
    var totalMilli = 0L
    for (s in sessions) {
        val date = s.dateMs.toLocalDate(zone)
        if (date < start) continue
        val index = ChronoUnit.DAYS.between(start, date).toInt()
        if (index in 0..6) {
            volMilli[index] += Units.kgToMilli(s.volumeKg)
            hit[index] = true
        }
        workouts++
        dur += s.durationSec
        totalMilli += Units.kgToMilli(s.volumeKg)
    }
    return WeekResult(
        dayVolumeKg = volMilli.map(Units::milliToKg).toDoubleArray(),
        dayHit = hit,
        workouts = workouts,
        durationSec = dur,
        totalVolumeKg = Units.milliToKg(totalMilli),
    )
}
