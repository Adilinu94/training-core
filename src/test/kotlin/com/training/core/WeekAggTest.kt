package com.training.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Wochenaggregation (Montags-Wochenstart). Die ersten drei Faelle sind
 * aus Flowtimers WeekAggTest portiert; die Zeitberechnung läuft jetzt
 * ueber java.time mit injizierter Zone.
 */
class WeekAggTest {
    private val DAY = 86_400_000L
    private val utc = ZoneOffset.UTC

    @Test fun buckets_volume_by_day_from_monday() {
        val weekStart = 0L
        val sessions =
            listOf(
                SessionSummary(weekStart + 0 * DAY + 3_600_000, durationSec = 1800, volumeKg = 1000.0),
                SessionSummary(weekStart + 2 * DAY + 3_600_000, durationSec = 1800, volumeKg = 500.0),
            )
        val agg = weekAggregate(sessions, weekStart, utc)
        assertThat(agg.dayVolumeKg[0]).isEqualTo(1000.0)
        assertThat(agg.dayVolumeKg[2]).isEqualTo(500.0)
        assertThat(agg.dayHit[0]).isTrue()
        assertThat(agg.dayHit[1]).isFalse()
        assertThat(agg.workouts).isEqualTo(2)
        assertThat(agg.totalVolumeKg).isEqualTo(1500.0)
    }

    @Test fun ignores_sessions_before_week_start() {
        val weekStart = 10 * DAY
        val sessions =
            listOf(
                SessionSummary(weekStart - DAY, durationSec = 1800, volumeKg = 999.0),
                SessionSummary(weekStart + DAY, durationSec = 1800, volumeKg = 200.0),
            )
        val agg = weekAggregate(sessions, weekStart, utc)
        assertThat(agg.workouts).isEqualTo(1)
        assertThat(agg.totalVolumeKg).isEqualTo(200.0)
    }

    @Test fun week_start_is_local_monday_midnight() {
        // 2026-08-19 ist ein Mittwoch; Montag war 2026-08-17.
        val monday = weekStart(1_755_000_000_000L, ZoneId.systemDefault())
        val mondayDate = java.time.Instant.ofEpochMilli(monday).atZone(ZoneId.systemDefault())
        assertThat(mondayDate.dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
        assertThat(mondayDate.hour).isEqualTo(0)
        assertThat(mondayDate.minute).isEqualTo(0)
    }

    @Test fun late_evening_and_next_morning_split_at_local_midnight() {
        val zone = ZoneId.of("Europe/Berlin")
        val monday = weekStart(1_755_000_000_000L, zone) // Woche um 2025-08-12
        val mondayEvening = monday + 23 * 3_600_000L // 23:00 Montag
        val tuesdayMorning = monday + 25 * 3_600_000L // 01:00 Dienstag
        val agg =
            weekAggregate(
                listOf(
                    SessionSummary(mondayEvening, volumeKg = 10.0),
                    SessionSummary(tuesdayMorning, volumeKg = 20.0),
                ),
                monday,
                zone,
            )
        assertThat(agg.dayVolumeKg[0]).isEqualTo(10.0)
        assertThat(agg.dayVolumeKg[1]).isEqualTo(20.0)
    }

    @Test fun day_floor_matches_week_start_monday() {
        val zone = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        assertThat(dayFloor(weekStart(now, zone), zone)).isEqualTo(weekStart(now, zone))
    }
}
