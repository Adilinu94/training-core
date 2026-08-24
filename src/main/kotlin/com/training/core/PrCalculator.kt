package com.training.core

/**
 * Vollstaendige PR-Neuberechnung aus der qualifizierten Historie einer
 * Uebung (Fusion Bauplan 5.4; E6 — dies ist die eine PR-Wahrheit des
 * Kerns, Flowtimers bestFor ist als [bestSet] darauf abgebildet).
 *
 * Eine inkrementelle Ruecknahme wird bewusst nicht angenommen: Jede
 * Aenderung berechnet die Uebung komplett neu; falsche alte PRs
 * verschwinden damit automatisch.
 *
 * Gleichstand erzeugt nie einen neuen Rekord: Bei mehreren Saetzen mit
 * demselben Wert haelt der FRUEHESTE (Abschlusszeit, dann Cluster-ID).
 */
object PrCalculator {
    fun computeAll(segments: List<TrainedSegment>): List<PrResult> {
        if (segments.isEmpty()) return emptyList()
        val records = mutableListOf<PrResult>()

        // 1. Hoechste Last: effektive Last strikt groesser als alles davor.
        val byLoad = segments.maxByOrNullStable { effectiveLoadMilli(it) }
        if (byLoad != null) {
            records +=
                PrResult(
                    type = PrType.HIGHEST_LOAD,
                    achievedSessionId = byLoad.sessionId,
                    achievedClusterId = byLoad.clusterId,
                    value = Units.milliToKg(effectiveLoadMilli(byLoad)),
                    valueUnit = PrValueUnit.KG,
                    comparableLoadKg = null,
                    achievedAtEpochMs = byLoad.completedAtEpochMs,
                )
        }

        // 2. Hoechstes Session-Volumen: Summe je Session; bei Gleichstand
        //    haelt die aeltere Session den Rekord.
        val volumeBySession =
            segments
                .groupBy { it.sessionId }
                .mapValues { (_, group) -> group.sumOf { effectiveLoadMilli(it) * it.reps } }
        val bestSession =
            volumeBySession.entries
                .sortedWith(
                    compareByDescending<Map.Entry<Long, Long>> { it.value }
                        .thenBy { sessionStart(segments, it.key) },
                ).first()
        records +=
            PrResult(
                type = PrType.HIGHEST_SESSION_VOLUME,
                achievedSessionId = bestSession.key,
                achievedClusterId = null,
                value = Units.milliToKg(bestSession.value),
                valueUnit = PrValueUnit.KG,
                comparableLoadKg = null,
                achievedAtEpochMs =
                    segments
                        .filter { it.sessionId == bestSession.key }
                        .maxOf { it.completedAtEpochMs },
            )

        // 3. Meiste Wiederholungen bei identischer effektiver Last:
        //    ein Rekord je Lastwert.
        segments
            .groupBy { effectiveLoadMilli(it) }
            .forEach { (load, group) ->
                val best = group.maxByOrNullStable { it.reps.toLong() } ?: return@forEach
                records +=
                    PrResult(
                        type = PrType.MOST_REPS_AT_LOAD,
                        achievedSessionId = best.sessionId,
                        achievedClusterId = best.clusterId,
                        value = best.reps.toDouble(),
                        valueUnit = PrValueUnit.REPS,
                        comparableLoadKg = Units.milliToKg(load),
                        achievedAtEpochMs = best.completedAtEpochMs,
                    )
            }

        return records
    }

    /**
     * Flowtimers `bestFor` als Sonderfall von [computeAll] (E6/9): Bester
     * Satz = hoechste effektive Last; Gleichstand entscheidet mehr Reps,
     * dann der fruehere Abschluss. Gewicht vergleicht in ganzen Gramm —
     * zwei als Double verschiedene Werte mit demselben Grammwert sind
     * Gleichstand.
     */
    fun bestSet(segments: List<TrainedSegment>): TrainedSegment? =
        segments.maxByOrNullStable { effectiveLoadMilli(it) * 1_000_000L + it.reps }

    private fun effectiveLoadMilli(segment: TrainedSegment): Long {
        require(WorkoutMath.isValidLoadMultiplier(segment.loadMultiplier)) {
            "loadMultiplier muss 1 oder 2 sein: ${segment.loadMultiplier}"
        }
        return Units.kgToMilli(segment.loadKg) * segment.loadMultiplier
    }

    private fun sessionStart(
        segments: List<TrainedSegment>,
        sessionId: Long,
    ): Long = segments.first { it.sessionId == sessionId }.sessionStartedAtEpochMs

    /**
     * Maximum mit stabiler Gleichstandsregel: Bei gleichem Wert gewinnt
     * das frueheste Segment (Abschlusszeit, dann Cluster-ID).
     */
    private fun List<TrainedSegment>.maxByOrNullStable(value: (TrainedSegment) -> Long): TrainedSegment? =
        sortedWith(
            compareByDescending(value)
                .thenBy { it.completedAtEpochMs }
                .thenBy { it.clusterId },
        ).firstOrNull()
}
