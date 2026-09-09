# Sanatan

An Android app for Sanatan Dharma daily practice: panchang, kundli, mantras,
and an AI astrologer you can ask questions in plain language.

## Stack

| Piece | Choice |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose + Material 3 |
| Build | AGP 8.13.2, Gradle 8.13 |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 |

Versions are pinned in `gradle/libs.versions.toml`. They match the toolchain
Android Studio on this machine already builds with. To move to AGP 9.x and
Compose BOM 2026.08.00, update Android Studio first, then run
**Tools > AGP Upgrade Assistant**.

## Getting started

```bash
git clone <repo-url>
cd Sanatan
```

Open the folder in Android Studio and let Gradle sync. `local.properties`
is generated for you and is intentionally not in Git.

## Layout

```
app/src/main/java/com/sanatan/app/
├── MainActivity.kt          entry point
├── core/navigation/         destinations + NavHost   (shared)
├── core/ui/                 shared composables       (shared)
├── data/model/              data classes
├── data/repository/         repository interfaces
├── feature/home/            Maneesha
├── feature/panchang/        Maneesha
├── feature/mantra/          Maneesha
├── feature/kundli/          Raman
├── feature/astrologer/      Raman
└── ui/theme/                colors, type, theme      (shared)
```

Read `CONTRIBUTING.md` before your first commit.
