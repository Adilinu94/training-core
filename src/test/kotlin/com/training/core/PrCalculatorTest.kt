package com.training.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * PR-Berechnung (E6 — Fusions drei PR-Arten sind die eine Wahrheit).
 * Die bestSet-Faelle sind aus Flowtimers PrTest portiert (bestFor als
 * Sonderfall auf den PrCalculator abgebildet).
 */
class PrCalculatorTest {
    // proto SEED.bench: [[85,8],[92.5,6],[100,5]]
    private val benchSets =
        listOf(
            TrainedSegment(loadKg = 85.0, reps = 8, completedAtEpochMs = 0),
            TrainedSegment(loadKg = 92.5, reps = 6, completedAtEpochMs = 0),
            TrainedSegment(loadKg = 100.0, reps = 5, completedAtEpochMs = 0),
        )

    // --- bestSet (Flowtimers bestFor, portiert) ---

    @Test fun best_set_picks_max_weight() {
        val best = PrCalculator.bestSet(benchSets)!!
        assertThat(best.loadKg).isEqualTo(100.0)
        assertThat(best.reps).isEqualTo(5)
    }

    @Test fun best_set_tiebreaks_on_reps() {
        val sets =
            listOf(
                TrainedSegment(loadKg = 100.0, reps = 5, completedAtEpochMs = 0),
                TrainedSegment(loadKg = 100.0, reps = 8, completedAtEpochMs = 1),
            )
        assertThat(PrCalculator.bestSet(sets)!!.reps).isEqualTo(8)
    }

    @Test fun best_set_empty_is_null() {
        assertThat(PrCalculator.bestSet(emptyList())).isNull()
    }

    @Test fun best_set_treats_unequal_doubles_with_equal_grams_as_tie() {
        // Zwei Double, die ungleich sind, aber dasselbe Gramm ergeben:
        // Gleichstand — mehr Reps gewinnen, nicht das "hoehere" Double.
        val sets =
            listOf(
                TrainedSegment(loadKg = 100.0001, reps = 5, completedAtEpochMs = 0),
                TrainedSegment(loadKg = 100.0002, reps = 8, completedAtEpochMs = 0),
            )
        assertThat(PrCalculator.bestSet(sets)!!.reps).isEqualTo(8)
    }

    // --- computeAll (Fusions drei PR-Arten, neue Pflichttests) ---

    @Test fun highest_load_is_max_effective_load() {
        val segments =
            listOf(
                TrainedSegment(loadKg = 60.0, reps = 8, completedAtEpochMs = 1, loadMultiplier = 2),
                TrainedSegment(loadKg = 100.0, reps = 5, completedAtEpochMs = 2),
            )
        val records = PrCalculator.computeAll(segments)
        val highestLoad = records.first { it.type == PrType.HIGHEST_LOAD }
        assertThat(highestLoad.value).isEqualTo(120.0) // 60 kg * 2 > 100 kg
        assertThat(highestLoad.achievedAtEpochMs).isEqualTo(1)
    }

    @Test fun highest_load_tie_keeps_earliest_segment() {
        val segments =
            listOf(
                TrainedSegment(loadKg = 100.0, reps = 5, completedAtEpochMs = 10, clusterId = 1),
                TrainedSegment(loadKg = 100.0, reps = 3, completedAtEpochMs = 20, clusterId = 2),
            )
        val highestLoad = PrCalculator.computeAll(segments).first { it.type == PrType.HIGHEST_LOAD }
        assertThat(highestLoad.achievedClusterId).isEqualTo(1)
    }

    @Test fun highest_session_volume_sums_per_session() {
        val segments =
            listOf(
                TrainedSegment(loadKg = 100.0, reps = 5, completedAtEpochMs = 1, sessionId = 1, sessionStartedAtEpochMs = 0),
                TrainedSegment(loadKg = 100.0, reps = 5, completedAtEpochMs = 2, sessionId = 1, sessionStartedAtEpochMs = 0),
                TrainedSegment(loadKg = 140.0, reps = 6, completedAtEpochMs = 3, sessionId = 2, sessionStartedAtEpochMs = 1_000),
            )
        val volume = PrCalculator.computeAll(segments).first { it.type == PrType.HIGHEST_SESSION_VOLUME }
        assertThat(volume.achievedSessionId).isEqualTo(1)
        assertThat(volume.value).isEqualTo(1000.0) // 2 * 500 > 840
    }

    @Test fun most_reps_at_load_creates_one_record_per_load() {
        val segments =
            listOf(
                TrainedSegment(loadKg = 100.0, reps = 5, completedAtEpochMs = 1),
                TrainedSegment(loadKg = 100.0, reps = 8, completedAtEpochMs = 2),
                TrainedSegment(loadKg = 90.0, reps = 12, completedAtEpochMs = 3),
            )
        val atLoad = PrCalculator.computeAll(segments).filter { it.type == PrType.MOST_REPS_AT_LOAD }
        assertThat(atLoad).hasSize(2)
        assertThat(atLoad.first { it.comparableLoadKg == 100.0 }.value).isEqualTo(8.0)
        assertThat(atLoad.first { it.comparableLoadKg == 90.0 }.value).isEqualTo(12.0)
    }

    @Test fun empty_history_returns_no_records() {
        assertThat(PrCalculator.computeAll(emptyList())).isEmpty()
    }
}
