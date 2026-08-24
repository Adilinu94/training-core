package com.training.core

/**
 * Ziel-Fortschritt (E4): Fortschrittsanteil einer Dimension, auf 0..1
 * begrenzt; Nullbereich teilt nie durch null.
 */
fun targetPct(
    cur: Double,
    start: Double,
    target: Double,
): Double {
    val denom = (target - start).let { if (it == 0.0) 1.0 else it }
    return ((cur - start) / denom).coerceIn(0.0, 1.0)
}

/**
 * Ein Trainingsziel: Zielgewicht UND Ziel-Wiederholungen muessen von
 * EINEM einzelnen Satz erfuellt werden (E4/Entscheidung 14). Massgeblich
 * ist spaeter der beste Satz, nicht der letzte.
 */
data class TargetGoal(
    val weightKg: Double,
    val reps: Int,
) {
    init {
        require(reps > 0) { "targetReps muss positiv sein: $reps" }
    }
}

/**
 * Satz-Erfuellung (Entscheidung 14): `gewicht >= zielGewicht` UND
 * `reps >= zielReps`. Das Gewicht ist die effektive Last (Implementlast
 * mal Multiplikator), verglichen in ganzen Gramm (E1) — krumme Double-
 * Reste duerfen einen Treffer weder erzeugen noch verhindern.
 */
fun targetReachedBySet(
    effectiveLoadKg: Double,
    reps: Int,
    goal: TargetGoal,
): Boolean = Units.kgToMilli(effectiveLoadKg) >= Units.kgToMilli(goal.weightKg) && reps >= goal.reps

/**
 * Zielerreichung einer Uebung: erfuellt, wenn der BESTE Satz das Ziel
 * trifft (E4) — hohe Volumina ueber viele leichte Saetze erfuellen
 * nicht. Die effektive Last wird je Satz selbst berechnet.
 */
fun targetReached(
    segments: List<TrainedSegment>,
    goal: TargetGoal,
): Boolean =
    segments.any {
        targetReachedBySet(
            effectiveLoadKg = WorkoutMath.effectiveLoadKg(it.loadKg, it.loadMultiplier),
            reps = it.reps,
            goal = goal,
        )
    }
