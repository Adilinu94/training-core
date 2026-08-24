package com.training.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Tages-Streak (E4b). Die vier ersten Faelle sind wortgleich aus
 * Flowtimers StreakTest portiert (UTC statt floorDayUtc); die restlichen
 * sind die Pflichtfaelle aus design.md Abschnitt 8 fuer maxGapDays.
 */
class StreakTest {
    private val DAY = 86_400_000L
    private val utc = ZoneOffset.UTC

    @Test fun empty_is_zero() {
        assertThat(streakCount(emptyList(), nowEpochMs = 10 * DAY, zone = utc)).isEqualTo(0)
    }

    @Test fun today_and_yesterday_is_two() {
        val now = 10 * DAY
        assertThat(streakCount(listOf(now, now - DAY), now, utc)).isEqualTo(2)
    }

    @Test fun missing_today_but_yesterday_counts() {
        val now = 10 * DAY
        assertThat(streakCount(listOf(now - DAY, now - 2 * DAY), now, utc)).isEqualTo(2)
    }

    @Test fun gap_breaks_streak() {
        val now = 10 * DAY
        assertThat(streakCount(listOf(now, now - 2 * DAY), now, utc)).isEqualTo(1)
    }

    // --- maxGapDays = 2 (Fusion, E4b: zwei Ruhetage sind frei) ---

    @Test fun gap_of_one_day_keeps_streak() {
        val now = 10 * DAY
        val days = listOf(now, now - 2 * DAY, now - 3 * DAY) // Luecke: gestern
        assertThat(streakCount(days, now, utc, maxGapDays = 2)).isEqualTo(3)
    }

    @Test fun gap_of_two_days_keeps_streak() {
        val now = 10 * DAY
        val days = listOf(now, now - 3 * DAY, now - 4 * DAY) // Luecke: gestern + vorgestern
        assertThat(streakCount(days, now, utc, maxGapDays = 2)).isEqualTo(3)
    }

    @Test fun gap_of_three_days_breaks_streak() {
        val now = 10 * DAY
        val days = listOf(now, now - 4 * DAY, now - 5 * DAY) // Luecke: 3 Tage
        assertThat(streakCount(days, now, utc, maxGapDays = 2)).isEqualTo(1)
    }

    @Test fun missing_today_consumes_one_grace_day_with_max_gap() {
        // Grenzfall aus design.md: heute noch kein Satz, gestern schon —
        // heute verbraechte einen der beiden Kulanztage, der Streak
        // zaehlt trotzdem weiter zurueck.
        val now = 10 * DAY
        val days = listOf(now - DAY, now - 2 * DAY, now - 3 * DAY)
        assertThat(streakCount(days, now, utc, maxGapDays = 2)).isEqualTo(3)
    }

    @Test fun missing_today_is_free_without_max_gap() {
        // Flowtimer-Modus (maxGapDays = 0): Ein heute ohne Satz kostet
        // nichts — die Zaehlung startet beim gestrigen Tag.
        val now = 10 * DAY
        val days = listOf(now - DAY, now - 2 * DAY)
        assertThat(streakCount(days, now, utc, maxGapDays = 0)).isEqualTo(2)
    }

    @Test fun zone_respects_local_midnight() {
        // 23:30 UTC ist in Berlin (UTC+1) schon der naechste Tag: Der
        // Streak muss den Berliner Tageswechsel sehen.
        val zone = ZoneId.of("Europe/Berlin")
        val now = 5 * DAY + 23 * 3_600_000L // 23:00 UTC = 00:00 Berlin
        val sessionAt2230Utc = now - 30 * 60_000L // 22:30 UTC = 23:30 Berlin (Vortag)
        val days = listOf(sessionAt2230Utc, sessionAt2230Utc - DAY)
        assertThat(streakCount(days, now, zone, maxGapDays = 0)).isEqualTo(2)
    }
}
