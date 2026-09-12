# HabitFlow

A habit tracker (Kotlin, Jetpack Compose, Material 3, MVVM/Clean Architecture,
Room, Hilt) built against the HabitFlow product spec.

## Read this first

This project was authored in a sandbox with **no Android SDK, no Gradle, no
Kotlin compiler, and no network access to Google's Maven repo / Maven
Central** — only a bare JVM and access to npm/PyPI/GitHub. That means:

- Every file here is hand-written, real source intended to compile and run
  correctly in Android Studio — nothing is a mockup or a stub UI.
- **None of it has actually been compiled, linted, or run.** I could not
  execute `./gradlew assembleDebug`, run the unit tests, or produce an APK
  from where this was written. The very first thing to do with this project
  is open it in Android Studio, let Gradle sync, and fix whatever surfaces —
  treat it as a strong, deliberately-architected starting point rather than
  a verified-green build.
- There's no `gradlew`/`gradlew.bat` wrapper script or `gradle-wrapper.jar`
  binary included (I have no way to fetch or generate the wrapper jar
  offline). Open the project in a recent Android Studio (Ladybug/2024.2+)
  and it will offer to generate the wrapper for you, or run `gradle wrapper`
  once with a local Gradle 8.10+ install.

## What's actually built in this pass

Given the size of the full product spec (64 sections covering ~15 major
screens, a full animation system, 4 home-screen widgets, gamification,
challenges, and more), this iteration focused on what was asked for first:
**a project skeleton with correct Gradle/Hilt/Room wiring**, plus enough
real functionality end-to-end that it's provably not a static shell.

**Working, wired end-to-end:**
- Gradle Kotlin DSL + version catalog (`gradle/libs.versions.toml`) for AGP,
  Kotlin, Compose BOM, Hilt, Room, Navigation, DataStore, WorkManager, Glance.
- Room database (`HabitFlowDatabase`) with 9 real entities: `Habit`,
  `HabitCompletion`, `HabitPause`, `HabitReminder`, `Category`,
  `JournalEntry`, `JournalMedia`, `JournalHabitCrossRef`, `MoodEntry`.
- Full DAO layer with `Flow`-based reactive queries.
- Hilt DI graph (`di/DatabaseModule`, `di/RepositoryModule`) wiring Room ->
  Repository -> ViewModel with no manual singletons.
- **The streak engine** (`domain/streak/StreakEngine.kt`) — pure Kotlin, zero
  Android dependencies, fully unit-testable. Implements the core rule from
  the spec: it never counts consecutive calendar dates, it only evaluates
  *scheduled* days, and pause ranges are neutral rather than streak-breaking.
  Handles DAILY, SPECIFIC_WEEKDAYS, EVERY_N_DAYS, FLEXIBLE, TIMES_PER_WEEK,
  and TIMES_PER_MONTH frequency types.
- `StreakEngineTest.kt` — 7 real JUnit test cases covering the pause example
  from the spec verbatim, weekday-only scheduling, every-N-days, and the
  "in-progress period shouldn't count as a miss" edge case for weekly goals.
- `HabitDaoTest.kt` — an instrumented Room test (in-memory DB) covering
  insert/archive/completion-accumulation.
- Clean Architecture layering: `data` (Room) -> `domain` (models, streak
  engine) -> `ui` (Compose + ViewModels), with mappers between the Room
  entity and the domain model so the UI layer never touches Room types.
- Five real screens on real navigation: **Today** (progress card with
  animated percentage, habit list, tap-to-complete, streak display, empty
  state), **Habits** (list with pin/archive via long-press menu), a
  functional **habit creation** form that really inserts a row, and
  placeholder empty-state screens for Calendar/Journal/Insights.
- Original Material 3 theme (`ui/theme/`) — a calm teal palette, not a
  Material default, with dynamic color as an explicit opt-in rather than
  the default (per the "distinctive visual identity" requirement).
- Original adaptive launcher icon (simple abstract checkmark mark, not
  derived from any other app's assets).

**Explicitly not built yet** (see inline `// TODO(section N)` comments at
each call site for exactly where these plug in):
- Calendar, Journal, and Insights screens are on-brand empty-state
  placeholders. Their data-layer dependencies already exist and are
  documented in each file.
- The multi-step animated habit creation flow **(done in Phase B — see below)**.
- Habit stacking/dependency *UI* (the DB columns `stackAfterHabitId` /
  `dependsOnHabitId` and cycle-prevention responsibility exist on `Habit`,
  but nothing surfaces or edits them yet — still true after Phase B).
- Gamification, Challenges, Achievements, Tags — no entities yet.
- Widgets (Glance dependency is wired in Gradle; no widget code yet).
- Reminders/notifications (WorkManager dependency wired; no scheduling code).
- Import/export, Settings, onboarding, search, sample data.
- The full animation system beyond the Today screen's progress-percentage
  count-up and a `LinearProgressIndicator` animation.

This is intentionally scoped as an honest first slice rather than a rushed
attempt at all 64 sections with placeholder logic passed off as working.

## Getting it running

1. Open the `HabitFlow/` folder in Android Studio (Ladybug 2024.2 or newer
   recommended, given AGP 8.7 / Kotlin 2.0 / compileSdk 35 in the version
   catalog).
2. Let Android Studio generate the Gradle wrapper if prompted, or run
   `gradle wrapper --gradle-version 8.10.2` once with your own Gradle
   install.
3. Sync Gradle. Since this hasn't been synced/compiled anywhere yet, expect
   to fix at least a handful of small things on first sync — a version
   catalog entry that's since moved, an API signature (the `ExposedDropdownMenuBox`
   `menuAnchor()` call in `CreateHabitScreen.kt` is flagged inline as the
   most likely one, since that API has changed across recent Material3
   releases), or a missing import.
4. Run the unit tests first: `./gradlew testDebugUnitTest`. `StreakEngineTest`
   has zero Android dependencies and is the best signal of whether the core
   logic is sound.
5. `./gradlew connectedDebugAndroidTest` (needs a device/emulator) for
   `HabitDaoTest`.
6. `./gradlew assembleDebug` to build the APK.

## Architecture

```
ui (Compose + ViewModel)
  -> domain (Habit model, StreakEngine — no Android deps)
    -> data.repository (HabitRepository interface + impl)
      -> data.local (Room: entities, DAOs, HabitFlowDatabase)
```

Hilt wires all of it (`di/DatabaseModule.kt`, `di/RepositoryModule.kt`).
`HabitCompletionEntity` stores one row per logged completion rather than one
row per habit-day, which is what lets `StreakEngine` support
"additional completions beyond the target" for quantity/duration habits
without a schema change.

## Database

Schema v1, 9 entities (listed above). `exportSchema = true` is set in
`app/build.gradle.kts` (KSP `room.schemaLocation` arg), so the JSON schema
will land in `app/schemas/` after a real build — check that in for future
migration diffing. **`fallbackToDestructiveMigration` is deliberately never
used** here: any real schema change needs a hand-written `Migration`, since
silently wiping a user's habit history would violate the "never overwrite
user data" requirement from the product spec.

## Known limitations to be aware of

- `TodayViewModel` refreshes completion state via a manual trigger after
  each user action rather than a fully Room-reactive join across habits +
  completions + pauses. This is correct for everything the user does inside
  the app, but a completion logged from outside this ViewModel (e.g. a
  future widget tap) won't update an already-open Today screen until it's
  reopened. Documented inline; the fix is a dedicated reactive DAO query.
- The streak engine's week/month "is this period still in progress"
  boundary logic has been reasoned through carefully (see the test for the
  weekly-goal case) but, per the top note, has never actually been run.
  Prioritize getting `StreakEngineTest` green before trusting it, especially
  around ISO week-numbering at year boundaries — that's the trickiest part
  of `java.time.WeekFields` and the part I'd most want a real test run to
  confirm.
- No legacy (pre-API 26) launcher icon PNGs — fine given `minSdk = 26`
  (adaptive icons only), but flagging it as a deliberate choice, not an
  oversight.

## Phase A (foundation hardening) — completed

Applied on top of the original skeleton, in response to the audit:

- Added a real use-case layer (`domain/usecase/HabitUseCases.kt`) — every
  ViewModel now depends on use cases, not `HabitRepository` directly,
  matching the section 28 architecture diagram (UI → ViewModel → UseCase →
  Repository).
- `CreateHabitUseCase`/`UpdateHabitUseCase` now validate (blank name, a
  habit depending on/stacking after itself) and return a `Result` sealed
  interface instead of silently no-op'ing on bad input — the create-habit
  form now surfaces a real inline error instead of doing nothing.
- Wired the previously-dead `HABIT_DETAIL` route: added
  `ui/habits/detail/HabitDetailScreen.kt` + `HabitDetailViewModel.kt`, a
  proper `Long` nav argument in `HabitFlowNavHost`, and made tapping a row
  in `HabitsScreen` actually navigate there (long-press still opens the
  pin/archive menu).
- Replaced `ExposedDropdownMenuBox`/`menuAnchor()` in the habit-creation
  form with a plain `Box` + `DropdownMenu`, the same version-stable pattern
  already used in `HabitsScreen` — removes the single riskiest unverified
  API call flagged in the prior audit.
- Removed the empty, unused `ui/settings` directory (Settings is Phase J).

Not touched in this phase (by design, per "don't replace working code
unnecessarily"): `StreakEngine`, all Room entities/DAOs, `HabitRepositoryImpl`,
the Today screen's rendering code, the theme, and the DI modules' provider
methods are all unchanged from the original skeleton.

**Verification note**: as with the original skeleton, this could only be
traced statically (imports, type signatures, Hilt graph completeness) —
still no Gradle/Kotlin compiler available in the environment this was
written in. Run `./gradlew testDebugUnitTest` and then a real sync/build in
Android Studio before trusting this phase is actually green.

## Phase B (habit management depth) — completed

- **Multi-step habit creation wizard** (`ui/habits/create/CreateHabitScreen.kt`)
  replacing the Phase A single-step form: Basic Info → Schedule → Reminder →
  Finish, with `AnimatedContent` slide transitions and a step-progress bar,
  per spec section 53. `CreateHabitViewModel`'s contract barely changed —
  it still just takes a fully-built `Habit`.
- **The Schedule step now actually captures the parameters each frequency
  type needs** — weekday multi-select for `SPECIFIC_WEEKDAYS`, a count field
  for `TIMES_PER_WEEK`/`TIMES_PER_MONTH`, an interval field for
  `EVERY_N_DAYS`, plus value type (boolean/duration/quantity) with
  target+unit. Previously the creation form let you pick `SPECIFIC_WEEKDAYS`
  with no way to set *which* weekdays, which meant `weekdaysMask` stayed 0
  and the habit would never actually be scheduled — a real bug closed in
  this phase, not just missing polish.
- **`ui/habits/edit/`** — a real habit edit screen (`HabitEditScreen` +
  `HabitEditViewModel`), reusing the same `BasicInfoStep`/`ScheduleStep`
  composables as the creation wizard via a shared `HabitFormState`
  (`ui/habits/form/`), so the two surfaces can't drift apart. Backed by the
  new `UpdateHabitUseCase` validation path from Phase A.
- **Duplicate, Pause, Resume, Skip today** — all wired into `HabitDetailScreen`'s
  overflow menu. "Skip today" and "Pause" are both implemented as calls into
  the *existing* `HabitPauseEntity`/pause mechanism (a skip is just a
  single-day pause) rather than new concepts, so `StreakEngine`'s existing
  "pauses are neutral" behavior covers both for free — no engine changes
  needed.
- **Icon registry** (`ui/common/IconRegistry.kt`) — `iconKey` strings on
  habits/categories now actually render as icons; previously they were
  stored but never displayed anywhere. Deliberately a flat string→icon map
  rather than an enum on the entity, so adding icons later never needs a
  migration.
- **Shared weekday-bitmask utility** (`ui/common/WeekdaySelector.kt`'s
  `WeekdayMask` object) — closes audit item #6 (both `HabitEntity` and
  `HabitReminderEntity` previously hand-rolled the same bit convention
  independently).
- **Notes field** now has an actual UI (in the Reminder/Finish step of
  creation, and read-only display on the detail screen) — previously stored
  on the entity but never surfaced anywhere.
- Reminders: creating a habit with "Remind me" enabled now inserts a real
  `HabitReminderEntity` row via the new `AddHabitReminderUseCase` +
  `HabitRepository.addReminder`. **Scheduling the actual notification is
  still Phase H** — this phase only closes the "the data never gets saved"
  gap, not the "and a notification fires" gap.

**Not done in this phase** (deliberately out of scope): habit stacking/
dependency UI (the cycle-prevention checks from Phase A's `CreateHabitUseCase`/
`UpdateHabitUseCase` exist, but nothing in the UI lets you pick a
stack-after/depends-on habit yet), pause with a specific end date (current
UI only offers "pause indefinitely" / "resume" — a date-range picker is a
reasonable follow-up), and a snackbar/toast host for surfacing errors like
a failed duplicate (noted inline in `HabitDetailViewModel.onDuplicate`).

**Verification note**: static-traced only, same caveat as every prior
phase. This time I could at least mechanically verify one important thing:
every new `@Inject`-constructor class's dependencies resolve to something
Hilt can actually provide (checked by parsing every constructor + every
`@Binds`/`@Provides` in the DI modules and confirming the graph closes) —
that's a real, if partial, signal. It does not catch type mismatches inside
method bodies, so a real Gradle sync is still the next step before trusting
this.

## Phase C (Calendar) — completed

- **Month view** (`ui/calendar/CalendarScreen.kt` + `CalendarViewModel.kt`)
  replacing the empty-state placeholder: a 7-column grid with prev/next
  month navigation, an animated slide transition between months, today
  highlighted, and a colored dot per day reflecting that day's aggregate
  status across *every* active habit.
- **`DaySummary`** (new domain model) — the calendar needed a per-*day*
  aggregate across all habits, not the per-*habit* `DayStatus` the streak
  engine already returns. `DaySummary.overallStatus` reduces
  completed/missed/paused/pending counts into the single value each grid
  cell colors itself by.
- **Efficient by construction**: `HabitRepository.statusesForRange()` (new)
  builds one habit's `StreakInput` once and evaluates every date in the
  requested range against it, instead of re-reading that habit's full
  completion history once per date. `GetCalendarMonthUseCase` is therefore
  O(habit count) database reads for an entire month, not O(habits × days).
- Tapping a date shows that day's actual habit list below the grid
  (`GetDayDetailUseCase`), with the same tap-to-complete interaction as the
  Today screen — reuses `ToggleHabitCompletionUseCase` directly, no new
  completion-logging code path.

**Not done in this phase**: week/day views (section 12 also asks for
these; month view was the highest-value one to build first, and both
would sit on the same `CalendarViewModel`/use cases with a narrower date
range — not a rewrite). Journal entries, photos, and mood aren't shown in
the day detail yet since those screens don't exist until Phase D.

**Verification note**: same static-only tracing as every phase, but this
time backed by two mechanical checks I re-ran after writing the code: (1)
every `import com.habitflow.app....` across all 49 Kotlin files resolves to
a symbol actually declared somewhere in the project (0 unresolved), and (2)
every `@Inject`-constructor class's dependencies resolve through the Hilt
graph (0 unresolved). Neither check catches logic bugs or type mismatches
inside a function body — a real Gradle sync is still the honest next step.

## Phase D (Journal + Mood) — completed

- **Real Journal timeline** (`ui/journal/JournalScreen.kt`) replacing the
  placeholder: newest-first entry list, title/body/mood/photo thumbnails,
  long-press to delete.
- **Entry composer** (`ui/journal/compose/`) — a real screen, not a stub:
  title, body, mood picker, and multi-photo attach via the system Photo
  Picker (`ActivityResultContracts.PickMultipleVisualMedia`). Worth calling
  out explicitly: I deliberately did **not** call
  `takePersistableUriPermission` on the picked URIs, because Photo Picker
  URIs (unlike `ACTION_OPEN_DOCUMENT` ones) already get durable read access
  automatically and calling that API on them can throw — a detail that's
  easy to get wrong copying older "how to persist a picked image URI"
  guidance, so I'm flagging the reasoning rather than just the code.
- **Mood check-in** — a 5-emoji strip at the top of the Journal screen,
  independent of writing a journal entry (section 14), backed by the
  existing `MoodEntryEntity`/`MoodDao` that had no UI before this phase.
- **New `JournalRepository`** (interface + impl) and use cases
  (`domain/usecase/JournalUseCases.kt`) — same UI→ViewModel→UseCase→Repository
  shape as the habit side, following the pattern established in Phase A
  rather than inventing a different one for Journal.
- **Added Coil** (`coil-compose`) as a new dependency — there was no
  image-loading library in the project, and rendering a real thumbnail from
  a `content://` URI needs one; hand-rolling bitmap decoding without a
  compiler to check it against seemed like the wrong tradeoff.
- Genuinely improved on the "manual refresh trigger" pattern documented as
  a limitation in every earlier phase: `JournalViewModel` is **fully
  Room-reactive** with no trigger hack needed, because `Flow.map`'s
  transform lambda can itself be `suspend` — `JournalRepositoryImpl` uses
  that directly to join in each entry's photos without leaving Room's Flow
  machinery. Worth revisiting `TodayViewModel`/`CalendarViewModel`/
  `HabitDetailViewModel` to use the same trick instead of their trigger
  flows, as a small follow-up cleanup rather than new functionality.

**Not done in this phase**: linking a journal entry to specific habits in
the composer UI (the `JournalHabitCrossRef` table and
`JournalRepository.createEntry`'s `linkedHabitIds` parameter both support
it already; just no habit-picker control in the composer yet), editing an
existing entry (only create/delete), and showing journal entries/mood in
the Calendar day-detail view from Phase C (noted as a gap there, still true).

**Verification note**: same two mechanical checks as Phase C, re-run after
this phase's code — 0 unresolved internal imports across all 56 files, 0
unresolved Hilt dependencies. The Photo Picker / Coil integration in
particular is the part of this phase I'd most want a real device run to
confirm, since URI permission behavior is exactly the kind of thing that
looks right on paper and misbehaves on an actual device/OS version.

## Future cloud-sync architecture (not implemented)


The repository layer already isolates all persistence behind the
`HabitRepository` interface. A future sync layer would sit as a decorator
around `HabitRepositoryImpl` (or a `SyncingHabitRepository` wrapping it),
push/pull through a `WorkManager`-scheduled job, and use `updatedAtEpochMillis`
-style columns (already present on `JournalEntryEntity`, and the natural
next addition to `HabitEntity`/`HabitCompletionEntity`) for last-write-wins
or conflict resolution. No entity needs to change shape for this — DTOs
would map from the same domain models the UI already uses.
