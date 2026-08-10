# Startup profile generation and verification

This module owns the shared launcher-to-first-interactive-frame journey used by
both Baseline/Startup Profile generation and Macrobenchmark startup tests.

## Generate profiles safely

The default task uses an isolated AOSP Pixel 6 API 35 Gradle Managed Device, so
it cannot replace or clear the data of an app installed on a connected phone:

```powershell
.\gradlew.bat :app:generateBaselineProfile
```

The generated method-level files are copied under
`app/src/release/generated/baselineProfiles/`. Commit them with the release that
contains the matching code.

## Verify startup

Run the `StartupBenchmark` instrumented tests on a physical API 29+ device from
Android Studio. Compare `coldStartupWithoutProfile` with
`coldStartupWithBaselineProfile`; `StartupTimingMetric` reports TTID and, because
the app calls `reportFullyDrawn()`, TTFD.

Avoid benchmarking on an emulator for release decisions. Emulator results are
useful only as a functional smoke test and are not representative of user
hardware.
