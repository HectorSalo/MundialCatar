# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Android app (Kotlin) for a FIFA World Cup 2026 prediction/standings game, backed by a Firebase
project. Users predict match scores and earn points; group standings and knockout brackets are
computed server-side by Cloud Functions. The repo holds two codebases: the Android app under `app/`
and the TypeScript Cloud Functions under `functions/`.

Package: `com.skysam.hchirinos.mundial2026`. Most code comments and UI strings are in Spanish.

## Commands

### Android app (run from repo root)
- Build debug: `./gradlew assembleDebug` (Windows: `gradlew.bat assembleDebug`)
- Build release: `./gradlew assembleRelease`
- Unit tests: `./gradlew testDebugUnitTest`
- Single unit test: `./gradlew testDebugUnitTest --tests "com.skysam.hchirinos.mundial2026.ExampleUnitTest"`
- Instrumented tests (device/emulator required): `./gradlew connectedDebugAndroidTest`
- Lint: `./gradlew lint`

> Note: there is currently no real test coverage — only the generated `ExampleUnitTest` /
> `ExampleInstrumentedTest` stubs exist.

### Cloud Functions (run from `functions/`)
- Build: `npm run build` (runs `tsc`; output goes to `functions/lib/`)
- Watch build: `npm run build:watch`
- Local emulator: `npm run serve`
- Deploy: `npm run deploy` (or `firebase deploy --only functions`)
- Logs: `npm run logs`

Node 24 is required for functions (see `functions/package.json` engines).

## Build variants and tournament IDs

The `debug` and `release` build types map to **different Firestore tournaments**, controlled via
`BuildConfig.TOURNAMENT_ID` (defined in `app/build.gradle`):
- `release` → `world_cup_2026` (production data)
- `debug` → `world_cup_2026_demo` (demo/QA data; also applies `applicationIdSuffix ".demo"`)

Always query Firestore with `BuildConfig.TOURNAMENT_ID` (not a hardcoded constant) so debug and
release stay isolated. `Constants.REAL_TOURNAMENT_ID` / `DEMO_TOURNAMENT_ID` exist for the seed
tooling that explicitly targets one or the other.

## Architecture

### Android: Repository → ViewModel → Fragment, with Hilt DI
- **DI**: Hilt. `FirebaseModule` (`repositories/FirebaseModule.kt`) provides singleton
  `FirebaseFirestore`, `FirebaseAuth`, and `FirebaseFunctions`. The `Application` class is
  `common/Mundial.kt` (`@HiltAndroidApp`). Activities/Fragments are `@AndroidEntryPoint`; ViewModels
  are `@HiltViewModel` with constructor-injected repositories.
- **Repositories** (`repositories/`) are the only layer that touches Firestore. They expose realtime
  data as `kotlinx.coroutines.flow.Flow` built from `callbackFlow { ... addSnapshotListener ... }`,
  and writes as `suspend` functions using `.await()`. Each repo filters by
  `BuildConfig.TOURNAMENT_ID`.
- **ViewModels** convert repo `Flow`s to `LiveData` via `.asLiveData()` and launch writes in
  `viewModelScope`.
- **UI** is single-Activity-ish with Navigation Component + bottom navigation (`MainActivity`,
  nav graph + `BottomNavigationView`). ViewBinding is enabled (no Compose). Feature screens live
  under `ui/<feature>/` (gameday, groups, playoff, points, predicts, results, browse, settings,
  init, extras), each typically with a Fragment, ViewModel, RecyclerView Adapter, and DiffUtil.

### Data model: Entity vs Domain
Firestore documents deserialize into `*Entity` classes (`dataclass/GameEntity.kt`,
`TeamEntity.kt`, `MatchScheduleSlotEntity.kt`, etc.) via `doc.toObject(...)`. Repositories map these
to domain models (`Game`, `Team`, `MatchScheduleSlot`, ...) — see `GameEntity.toDomain()` in
`GamesRepository.kt`. Keep Firestore field names centralized in `common/Constants.kt` (collection
names, field keys, group/round display strings, special user emails).

### Server-side computation (source of truth)
Standings and brackets are **not** computed on the client. `functions/src/index.ts` contains:
- `onGameResultUpdated` — Firestore trigger on `games/{gameId}`. When a game gets a valid score /
  flips to FINISHED it: sends an FCM push, recomputes prediction points, recomputes group standings
  (+ best-thirds), and propagates knockout winners/losers to later rounds.
- `recomputeStandingsForTournament` — callable function (`onCall`) to force a recompute for a given
  `tournamentId`.
- Points algorithm (`computePoints`): exact score = +5, correct sign = +3, exact reversed score =
  −2, any other miss = −1.

The client reads computed standings from the `standings` collection
(`StandingsRepository`); it does not derive tables itself. `functions/src/thirdPlaceMatrix.ts`
encodes the FIFA best-third → knockout-slot assignment matrix.

### Demo seeding (`seeds/`)
The `seeds/` package + the commented block in `GamedayViewModel.init` are dev-only tools to populate
the demo tournament (predictions, simulated results, schedule slots). They are gated behind
`DEMO_TOURNAMENT_ID` — do not point them at the real tournament.

## Firebase collections
`games`, `gamesUsers` (predictions), `users`, `teams`, `standings`, `match_schedule_slots`,
`infoApp`, plus legacy `predict`. See `Constants.kt` for the canonical names and field keys.

## Notifications
FCM topic subscription is toggled by user preference (`Mundial.onCreate` observes
`Preferences.getNotificationStatus()`); functions publish results to a `results_<tournamentId>`
topic. Notification runtime permission is requested in `MainActivity` (Android 13+).

## Signing
Keystore files live at the repo root (`certificate.jks`, `certificateChampions.jks`,
`certificate2026`). There is **no** `signingConfigs` block in `app/build.gradle`, so signed
releases are produced via Android Studio's "Generate Signed Bundle / APK" dialog (or by adding a
signing config manually) rather than from a Gradle `assembleRelease`.
