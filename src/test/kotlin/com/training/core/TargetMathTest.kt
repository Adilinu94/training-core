package com.training.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Ziel-Mathematik (E4/Entscheidung 14). Die ersten beiden Faelle sind aus
 * Flowtimers TargetMathTest portiert; die satzbasierten Faelle sind die
 * Pflichttests aus design.md Abschnitt 8.
 */
class TargetMathTest {
    @Test fun progress_clamps_0_1() {
        assertThat(targetPct(cur = 105.0, start = 100.0, target = 110.0)).isWithin(0.001).of(0.5)
        assertThat(targetPct(cur = 130.0, start = 100.0, target = 110.0)).isEqualTo(1.0)
        assertThat(targetPct(cur = 90.0, start = 100.0, target = 110.0)).isEqualTo(0.0)
    }

    @Test fun zero_range_does_not_divide_by_zero() {
        assertThat(targetPct(cur = 100.0, start = 100.0, target = 100.0)).isEqualTo(0.0)
    }

    @Test fun reached_needs_weight_and_reps_in_one_set() {
        val goal = TargetGoal(weightKg = 100.0, reps = 5)
        assertThat(targetReachedBySet(effectiveLoadKg = 100.0, reps = 5, goal = goal)).isTrue()
        assertThat(targetReachedBySet(effectiveLoadKg = 120.0, reps = 4, goal = goal)).isFalse()
        assertThat(targetReachedBySet(effectiveLoadKg = 90.0, reps = 10, goal = goal)).isFalse()
    }

    @Test fun gram_rounding_decides_reach_not_double_remainder() {
        val goal = TargetGoal(weightKg = 100.0, reps = 1)
        // Als Double unter dem Ziel, als Gramm gleich: erfuellt (E1).
        assertThat(targetReachedBySet(effectiveLoadKg = 99.9999, reps = 1, goal = goal)).isTrue()
    }

    @Test fun high_volume_with_light_weight_does_not_fulfill() {
        // Pflichtfall design.md: hohes Volumen bei zu leichtem Gewicht
        // erfuellt nicht — die Bedingungen muessen in EINEM Satz stehen.
        val goal = TargetGoal(weightKg = 100.0, reps = 5)
        val segments =
            listOf(
                TrainedSegment(loadKg = 50.0, reps = 10, completedAtEpochMs = 1), // Volumen 500
                TrainedSegment(loadKg = 50.0, reps = 10, completedAtEpochMs = 2), // Volumen 500
            )
        assertThat(targetReached(segments, goal)).isFalse()
    }

    @Test fun best_set_decides_not_the_last() {
        val goal = TargetGoal(weightKg = 100.0, reps = 5)
        val segments =
            listOf(
                TrainedSegment(loadKg = 100.0, reps = 5, completedAtEpochMs = 1), // Ziel getroffen
                TrainedSegment(loadKg = 60.0, reps = 3, completedAtEpochMs = 2), // letzter Satz leicht
            )
        assertThat(targetReached(segments, goal)).isTrue()
    }

    @Test fun multiplier_counts_toward_the_goal() {
        val goal = TargetGoal(weightKg = 100.0, reps = 5)
        val segments = listOf(TrainedSegment(loadKg = 50.0, reps = 5, completedAtEpochMs = 1, loadMultiplier = 2))
        assertThat(targetReached(segments, goal)).isTrue()
    }
}
