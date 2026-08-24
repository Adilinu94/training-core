package com.training.core

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

/** Trainingsmathematik (Fusion Bauplan 5.4) — neue Pflichttests des Kerns. */
class WorkoutMathTest {
    @Test fun multiplier_must_be_one_or_two() {
        assertThat(WorkoutMath.isValidLoadMultiplier(1)).isTrue()
        assertThat(WorkoutMath.isValidLoadMultiplier(2)).isTrue()
        assertThat(WorkoutMath.isValidLoadMultiplier(0)).isFalse()
        assertThat(WorkoutMath.isValidLoadMultiplier(3)).isFalse()
    }

    @Test fun effective_load_multiplies_implement_load() {
        assertThat(WorkoutMath.effectiveLoadKg(60.0, 2)).isEqualTo(120.0)
        assertThat(WorkoutMath.effectiveLoadKg(60.0, 1)).isEqualTo(60.0)
    }

    @Test fun invalid_multiplier_rejects() {
        assertThrows(IllegalArgumentException::class.java) {
            WorkoutMath.effectiveLoadKg(60.0, 3)
        }
    }

    @Test fun segment_volume_multiplies_load_times_reps() {
        assertThat(WorkoutMath.segmentVolumeKg(100.0, 1, 5)).isEqualTo(500.0)
        assertThat(WorkoutMath.segmentVolumeKg(50.0, 2, 10)).isEqualTo(1000.0)
    }

    @Test fun negative_reps_reject() {
        assertThrows(IllegalArgumentException::class.java) {
            WorkoutMath.segmentVolumeKg(100.0, 1, 0)
        }
    }

    @Test fun cluster_volume_sums_gram_exact() {
        val segments =
            listOf(
                TrainedSegment(loadKg = 0.1, reps = 3, completedAtEpochMs = 0),
                TrainedSegment(loadKg = 0.2, reps = 3, completedAtEpochMs = 0),
            )
        // 0.1 und 0.2 sind als Binaer-Double unvermeidlich unexakt; die
        // Gramm-Summe muss dennoch exakt 900 g ergeben (E1).
        assertThat(WorkoutMath.clusterVolumeKg(segments)).isEqualTo(0.9)
    }

    @Test fun one_rm_formula_matches_fusion() {
        // (100 * 35) / 30 = 116,67 -> 116,67 kg auf Gramm gerundet.
        assertThat(WorkoutMath.estimatedOneRmKg(100.0, 5)).isEqualTo(116.667)
        assertThat(WorkoutMath.estimatedOneRmKg(60.0, 8)).isEqualTo(76.0)
    }

    @Test fun one_rm_out_of_range_is_null() {
        assertThat(WorkoutMath.estimatedOneRmKg(100.0, 0)).isNull()
        assertThat(WorkoutMath.estimatedOneRmKg(100.0, 11)).isNull()
        assertThat(WorkoutMath.estimatedOneRmKg(0.0, 5)).isNull()
    }

    @Test fun inputs_round_to_whole_grams_half_up() {
        assertThat(Units.kgToMilli(100.0004)).isEqualTo(100_000)
        assertThat(Units.kgToMilli(100.0005)).isEqualTo(100_001)
        assertThat(Units.kgToMilli(100.9995)).isEqualTo(101_000)
    }

    @Test fun kg_milli_roundtrip_is_lossless_for_whole_grams() {
        assertThat(Units.kgToMilli(Units.milliToKg(123_456))).isEqualTo(123_456)
    }
}
