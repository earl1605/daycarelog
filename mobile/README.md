# DaycareLog — Android

Kotlin + Jetpack Compose (Material 3) Android client for DaycareLog, a daycare management system for tracking children, attendance, health records, and reports. Talks to the same [Spring Boot backend](../backend) REST API as the [web client](../web), so data is shared across both.

## Tech stack

- **Kotlin** + **Jetpack Compose** (Material 3, fixed brand color scheme — no dynamic/wallpaper-based theming)
- **Navigation Compose** — in-app routing
- **Retrofit** + **Gson** — REST API client
- **Jetpack DataStore (Preferences)** — persisted auth token/user session
- **Coroutines** — async work

## Requirements

- Android Studio (or the Gradle wrapper + Android SDK on the command line)

## Run

Open `mobile/` in Android Studio and run the `app` configuration, or from the command line:

```bash
cd mobile
./gradlew assembleDebug
```

The app points at the production backend URL by default — see `RetrofitClient.kt` to point it at a local backend instead.

## Project structure

```
app/src/main/java/com/daycarelog/app/
├── data/
│   ├── api/           Retrofit service interface, client, auth token holder
│   ├── model/          Request/response DTOs
│   └── preferences/     DataStore-backed token/user persistence
├── navigation/          Top-level nav graph (Login/Register/Main)
├── ui/
│   ├── auth/            Login, Register, shared AuthViewModel
│   ├── main/             MainScreen — the Navigation Drawer shell wrapping every other screen
│   ├── dashboard/        Stat grid, weekly attendance chart, quick actions
│   ├── children/         Children list + add/edit form
│   ├── attendance/       Daily attendance
│   ├── health/           Health records list + add form
│   ├── reports/          Monthly report
│   ├── users/            Admin-only user management
│   ├── settings/         Profile (editable name fields) + sign out
│   ├── common/           Shared composables (date picker field)
│   └── theme/            Fixed Material 3 color scheme, typography
└── util/TextUtils.kt     Shared name-capitalization logic
```

## Notable behavior

- **Navigation**: `MainScreen.kt` wraps the app in a `ModalNavigationDrawer` grouped into Main / Management / Admin sections (Admin only shown for the `admin` role), matching the [web sidebar](../web)'s design. Every top-level screen has a menu icon to reopen the drawer.
- **Name fields**: First/Last/Middle Name inputs on Registration and the Settings profile form auto-capitalize each word as you type (`util/TextUtils.kt`, shared by both screens), via `KeyboardCapitalization.Words` plus a `capitalizeWords()` pass before saving — without overriding intentional casing like "McDonald" or disturbing cursor position mid-edit.
- **Auth**: JWT + user JSON persisted in DataStore; `NavGraph.kt` checks for a saved token on launch to decide between the Login screen and the main app.
- **Gotcha**: `painterResource()` cannot load `<adaptive-icon>` mipmap resources (e.g. `R.mipmap.ic_launcher_round`) — throws at runtime, not compile time. Use a plain vector `drawable` instead (see `R.drawable.ic_child`, used for the drawer logo and Login screen icon).
