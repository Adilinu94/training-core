# training-core

Gemeinsamer Trainings-Kern fuer **Fusion** (DropSync/FlowRep) und
**Flowtimer** — Flowtimer-Integration design.md Abschnitt 1 (2026-08-22),
entschiedene offene Punkte 1/2/8 vom 2026-08-24 in Fusions CONTEXT.md.

## Inhalt

Pure Kotlin JVM, keine Android-Abhaengigkeiten, deterministisch testbar
(java.time mit injizierter Zone statt Calendar):

- **Units** — kg-Double an der Schnittstelle, ganze Millikilogramm intern;
  jede Gewichtseingabe rundet am Eingang auf 3 Dezimalstellen (E1)
- **WorkoutMath** — Multiplikator 1/2, Satz-/Gruppenvolumen, geschaetztes
  1RM `load * (1 + reps/30)` fuer 1..10 Wiederholungen (E6: Fusions
  Mathematik ist die Grundlage)
- **PrCalculator** — drei PR-Arten (hoechste Last, hoechstes Session-
  Volumen, meiste Reps bei Last) mit Gleichstandsregel „fruehester Satz
  haelt den Rekord"; `bestSet` bildet Flowtimers `bestFor` als Sonderfall ab
- **Streak** — Tages-Streak; `maxGapDays = 0` haelt Flowtimers Verhalten
  (heute optional), Fusion uebergibt 2 (E4b: erst drei zusammenhaengende
  Ruhetage brechen)
- **WeekAgg** — Montag-Wochenstart (ISO-8601), kalendarische Tag-Buckets
  (DST-sicher), Gramm-exakte Volumensummen
- **TargetMath** — Ziel erfuellt heisst: EIN Satz mit `Gewicht >= Ziel`
  UND `Reps >= Ziel` (E4/Entscheidung 14); maassgeblich ist der beste Satz
- **WeightFormat** — E4c: ganzzahlig ohne Dezimalstelle, krumme Werte mit
  einer, Volumen ab 1000 kg in Tonnen

## Einbindung (beide Apps)

Git-Submodule, gepinnter Stand; als gewoehnliches Subprojekt eingebunden:

```kotlin
// settings.gradle.kts der App
include(":training-core")
project(":training-core").projectDir = file("training-core")
```

```kotlin
// build.gradle.kts des Kerns deklariert KEINE Plugin-Version — die App
// liefert sie (Fusion: kotlin.jvm 2.4.10 apply-false im Root; Flowtimer
// ergaenzt 2.4.0 entsprechend). Standalone-Build nimmt den Default aus
// der settings.gradle.kts hier (2.4.10).
```

Toolchain: Java 17, `apiVersion`/`languageVersion` 2.0 (konservativ, damit
beide Apps den Kern konsumieren koennen). Der Architekturtest der Apps
(`ModuleDependencyRulesTest`) traversiert die Repo-Wurzel von innen nach
aussen und prueft nur `core/`/`domain/`/`data/`/`feature/` — der Kern an
der Wurzel bleibt unberuehrt.

## Tests

46 Tests: 13 aus Flowtimer portiert (Streak 4, Pr/bestSet 3, WeekAgg 3,
targetPct 2, e1rm 1) plus die Pflichtfaelle aus design.md Abschnitt 8
(maxGapDays-Luecken 1/2/3, Eingangs-Rundung, „hohes Volumen bei zu leichtem
Gewicht erfuellt nicht", Zahlenformate, PR-Gleichstandsregeln).

```
./gradlew test
```

## Versionsdisziplin

Aenderungen am Kern: Version bumpen (git tag) und den Submodule-Pointer in
BEIDEN Apps aktualisieren (design.md Entscheidung 7).
