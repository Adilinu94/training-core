package com.training.core

/**
 * Ein abgeschlossener, fuer Volumen und PRs qualifizierter Trainings-
 * satz. Gemeinsamer Nenner aus Fusions `QualifiedSegment` (Session- und
 * Cluster-Kontext) und Flowtimers `LoggedSet` (nur Gewicht und Reps):
 * Session- und Cluster-IDs sind optional — wer sie nicht hat, laesst sie
 * auf 0; die PR-Arten, die sie nicht brauchen, funktionieren weiterhin.
 *
 * [loadKg] ist die Last je Implement (Hantel), [loadMultiplier] 1 oder 2
 * (zwei gleich schwere Implementseiten, nie berechnet). Die effektive
 * Last ist `loadKg * loadMultiplier`.
 */
data class TrainedSegment(
    val loadKg: Double,
    val reps: Int,
    val completedAtEpochMs: Long,
    val loadMultiplier: Int = 1,
    val sessionId: Long = 0L,
    val sessionStartedAtEpochMs: Long = 0L,
    val clusterId: Long = 0L,
)

/** Die drei PR-Arten des Kerns (Fusion Bauplan 5.4; E6). */
enum class PrType { HIGHEST_LOAD, HIGHEST_SESSION_VOLUME, MOST_REPS_AT_LOAD }

/** Einheit des PR-Werts; [PrResult.value] traegt kg oder reine Reps. */
enum class PrValueUnit { KG, REPS }

/** Ergebnis der PR-Berechnung; Persistenz uebernimmt die jeweilige App. */
data class PrResult(
    val type: PrType,
    val achievedSessionId: Long,
    val achievedClusterId: Long?,
    val value: Double,
    val valueUnit: PrValueUnit,
    val comparableLoadKg: Double?,
    val achievedAtEpochMs: Long,
)
