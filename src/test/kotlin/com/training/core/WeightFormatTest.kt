package com.training.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Locale

/** Zahlenformate (E4c) — Pflichttests aus design.md Abschnitt 8. */
class WeightFormatTest {
    private val locale = Locale.GERMANY

    @Test fun whole_weights_have_no_decimal() {
        assertThat(WeightFormat.kg(95.0, locale)).isEqualTo("95 kg")
    }

    @Test fun odd_weights_show_one_decimal() {
        assertThat(WeightFormat.kg(97.5, locale)).isEqualTo("97,5 kg")
    }

    @Test fun sub_gram_remainders_round_away() {
        // Eingangs-Rundung auf ganze Gramm: 82.5004 kg ist 82,5 kg.
        assertThat(WeightFormat.kg(82.5004, locale)).isEqualTo("82,5 kg")
    }

    @Test fun volume_below_1000_kg_stays_kg() {
        assertThat(WeightFormat.volume(999.9, locale)).isEqualTo("999,9 kg")
    }

    @Test fun volume_from_1000_kg_becomes_tons() {
        assertThat(WeightFormat.volume(1500.0, locale)).isEqualTo("1,5 t")
        assertThat(WeightFormat.volume(12_345.0, locale)).isEqualTo("12,3 t")
    }
}
