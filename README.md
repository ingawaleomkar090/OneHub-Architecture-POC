# Catalent OneHub

Enterprise Android application for Catalent field agents, built on the Salesforce Mobile SDK with offline-first data access.

---

## Table of contents

- [Overview](#overview)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Module structure](#module-structure)
- [Module dependency rules](#module-dependency-rules)
- [Getting started](#getting-started)
- [Build variants](#build-variants)
- [Key patterns](#key-patterns)
- [Logging](#logging)
- [Error handling](#error-handling)
- [CI/CD & Security](#cicd--security)
- [Development guidelines](#development-guidelines)
- [Adding a new feature module](#adding-a-new-feature-module)

---

## Overview

OneHub gives Catalent field agents access to Salesforce contacts and accounts on mobile. It works offline using Salesforce SmartStore for local caching and syncs with the server when connectivity is available.

**Key capabilities:**
- Salesforce OAuth login via the Mobile SDK
- Offline-first data access with SmartStore
- Real-time connectivity monitoring
- Modular, feature-based architecture ready to scale

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt (Dagger) + KSP |
| Async | Kotlin Coroutines + Flow |
| Salesforce | Mobile SDK 13.2 + MobileSync |
| Logging | Timber via custom `AppLogger` |
| Build | Gradle KTS + Version Catalogs |

---

## Architecture

OneHub follows Clean Architecture with feature-based modularization. Each feature is a folder containing three Gradle modules — domain, data, and ui.

```
:app                     ← entry point, Hilt root, build variants
  ↓ depends on
:auth:ui                 ← AuthViewModel, HiddenLoginViewModel
:auth:data (runtimeOnly) ← SalesforceAuthRepository, AuthDataModule
:auth:domain             ← AuthRepository, AuthState, AuthException, LogoutUseCase
:shipment:ui             ← SObjectListViewModel, MainScreen
:shipment:data(runtimeOnly) ← SalesforceSObjectRepository, ShipmentDataModule
:shipment:domain         ← SObjectRepository, SObjectData, SObjectListState
:core                    ← AppLogger, NetworkManager, ClientProvider, Result
```

**Auth state machine:**

```
Unauthenticated
      │
      │  ClientProvider.onClientAvailable(client)
      ▼
  Authenticated  ──── logout() ────▶  Loading ────▶  Unauthenticated
      │
      │  error during session
      ▼
   Error(AuthException)
```

**Data flow:**

```
MainActivity
    │
    ├── ClientProvider.onClientAvailable(client)   ← stores RestClient
    │       ↓
    │   SalesforceAuthRepository (listener)        ← updates AuthState
    │
    └── setContent { }
            ↓
        AuthViewModel.authState                    ← UI reacts to state
            ↓
        MainScreen → SObjectListViewModel
            ↓
        SalesforceSObjectRepository                ← uses ClientProvider.client
```

---

## Module structure

```
CatalentOneHub/
│
├── app/
│   └── src/main/java/com/catalent/onehub/
│       ├── MainApplication.kt
│       ├── presentation/
│       │   ├── ui/MainActivity.kt
│       │   ├── screen/
│       │   │   ├── MainScreen.kt
│       │   │   └── ErrorScreen.kt
│       │   ├── components/
│       │   │   ├── SObjectList.kt
│       │   │   ├── SearchBar.kt
│       │   │   ├── NetworkBanner.kt
│       │   │   └── EmptyErrorState.kt
│       │   └── error/
│       │       └── AuthExceptionUI.kt
│       └── ui/theme/
│
├── auth/
│   ├── domain/                           ← pure Kotlin, no Android, no Hilt
│   │   └── com/catalent/auth/domain/
│   │       ├── AuthRepository.kt
│   │       ├── AuthState.kt
│   │       ├── SessionHandler.kt
│   │       ├── usecase/LogoutUseCase.kt
│   │       └── exceptions/AuthException.kt
│   │
│   ├── data/                             ← Android library, Hilt
│   │   └── com/catalent/auth/data/
│   │       ├── SalesforceAuthRepository.kt
│   │       ├── mapper/AuthExceptionMapper.kt
│   │       └── di/AuthDataModule.kt
│   │
│   └── ui/                               ← Android library, Compose, Hilt
│       └── com/catalent/auth/ui/
│           ├── AuthViewModel.kt
│           └── HiddenLoginViewModel.kt
│
├── shipment/
│   ├── domain/                           ← pure Kotlin, no Android, no Hilt
│   │   └── com/catalent/shipment/domain/
│   │       ├── model/
│   │       │   ├── SObjectData.kt
│   │       │   └── SObjectListState.kt
│   │       └── repository/SObjectRepository.kt
│   │
│   ├── data/                             ← Android library, Hilt
│   │   └── com/catalent/shipment/data/
│   │       ├── SalesforceSObjectRepository.kt
│   │       └── di/ShipmentDataModule.kt
│   │
│   └── ui/                               ← Android library, Compose, Hilt
│       └── com/catalent/shipment/ui/
│           └── SObjectListViewModel.kt
│
└── core/                                 ← Android library, shared infrastructure
    └── com/catalent/onehub/core/
        ├── logging/
        │   ├── AppLogger.kt
        │   ├── CatalentLogger.kt
        │   ├── ConsoleLogger.kt
        │   ├── Loggable.kt
        │   └── LogDestination.kt
        ├── network/
        │   ├── NetworkManager.kt
        │   └── ClientProvider.kt
        └── common/
            └── Result.kt
```

---

## Module dependency rules

| Module | Can depend on | Cannot depend on |
|---|---|---|
| `:auth:domain` | nothing | everything |
| `:auth:data` | `:auth:domain`, `:core` | `:auth:ui`, `:app`, `:shipment:*` |
| `:auth:ui` | `:auth:domain`, `:core` | `:auth:data`, `:app` |
| `:shipment:domain` | nothing | everything |
| `:shipment:data` | `:shipment:domain`, `:core` | `:shipment:ui`, `:app`, `:auth:*` |
| `:shipment:ui` | `:shipment:domain`, `:core` | `:shipment:data`, `:app` |
| `:core` | nothing | `:auth:*`, `:shipment:*`, `:app` |
| `:app` | `:auth:ui`, `:auth:domain`, `:shipment:ui`, `:shipment:domain`, `:core` | `:auth:data` (compile), `:shipment:data` (compile) |

> `:auth:data` and `:shipment:data` are `runtimeOnly` in `:app` — Hilt sees the modules at runtime but `:app` cannot import their classes at compile time. This enforces the boundary.

---

## Getting started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 11
- Salesforce Connected App with OAuth configured
- Access to a Salesforce org (sandbox or production)

### Setup

**1. Clone the repository**

```bash
Yet to be added
```

**2. Configure Local Secrets**
The app uses `local.properties` to manage sensitive Salesforce credentials during development. These keys are injected into the build and are not committed to Git.

Create or update `local.properties` in the root folder:
```properties
# local.properties — never commit this file
salesforce.consumer.key=YOUR_CONSUMER_KEY
salesforce.redirect.uri=fieldagentapp://mobile/oauth/onehub
```

**3. Configure Salesforce Connected App**
In your Salesforce org go to `Setup → App Manager → New Connected App`:
* **Callback URL**: `fieldagentapp://mobile/oauth/onehub`
* **OAuth Scopes**: `api`, `web`, `refresh_token`, `openid`
* **Permitted Users**: All users may self-authorize
* **IP Relaxation**: Relax IP restrictions

---

## CI/CD & Security

### Production Secrets
For release builds and CI/CD pipelines (GitHub Actions, Azure DevOps, etc.), secrets should be managed as environment variables rather than a physical `local.properties` file.

The Gradle build script is configured to check for these **Environment Variables** if the local file is missing:

1. `SALESFORCE_CONSUMER_KEY`
2. `SALESFORCE_REDIRECT_URI`

### Setting up a Pipeline
1. Go to your CI/CD platform's **Secrets** or **Variable Groups** section.
2. Add the two variables mentioned above.
3. Paste the production values.
4. The build task `./gradlew assembleRelease` will automatically pick them up and inject them into the APK.

---

## Build variants

Three product flavors × two build types = six variants:

| Variant | App ID suffix | Login host | Logging |
|---|---|---|---|
| `devDebug` | `.dev.debug` | `test.salesforce.com` | ✅ |
| `devRelease` | `.dev` | `test.salesforce.com` | ✅ |
| `stagingDebug` | `.staging.debug` | `test.salesforce.com` | ✅ |
| `stagingRelease` | `.staging` | `test.salesforce.com` | ✅ |
| `prodDebug` | `.debug` | `login.salesforce.com` |  |
| `prodRelease` | _(none)_ | `login.salesforce.com` |  |

**Available `BuildConfig` fields:**

```kotlin
BuildConfig.ENABLE_CONSOLE_LOGGING  // Boolean — controls AppLogger
BuildConfig.SALESFORCE_LOGIN_HOST   // String — sandbox vs production
```

---

## Key patterns

### ClientProvider — session lifecycle

`ClientProvider` in `:core` holds a `StateFlow<AppClient?>`. The `MainActivity` provides the platform-specific client, and repositories transform this flow into domain-specific states.

```kotlin
// MainActivity — provides the client
override fun onResume(client: RestClient) {
    clientProvider.onClientAvailable(RawClientWrapper(client))
}
```

```kotlin
// SalesforceAuthRepository — transforms the client flow
override val authState: Flow<AuthState> = clientProvider.client.map { client ->
    // map to AuthState...
}
```

### Variable declaration order

Inside any class follow this order:

```kotlin
class MyViewModel @Inject constructor(...) : ViewModel() {
    // 1. Constants
    private val pageSize = 20

    // 2. Private mutable state
    private val _state = MutableStateFlow(MyState())

    // 3. Public immutable state
    val state: StateFlow<MyState> = _state.asStateFlow()

    // 4. Derived values
    val tabs = listOf("Contacts", "Accounts")

    // 5. init block
    init { }

    // 6. Public functions
    fun load() { }

    // 7. Private functions
    private suspend fun fetch() { }
}
```

**Naming rules:**

```kotlin
private val _authState = MutableStateFlow(...)  // mutable private → underscore prefix
val authState: StateFlow<...> = ...             // public immutable → no underscore
var isRefreshing = false                        // boolean → is/has prefix
val items: List<SObjectData>                    // collections → plural
private val pageSize = 20                       // class constant → camelCase
companion object { const val PAGE_SIZE = 20 }  // companion constant → SCREAMING_SNAKE
```

### runtimeOnly for data modules

`:auth:data` and `:shipment:data` are `runtimeOnly` in `:app`. This means:

- Hilt finds `AuthDataModule` and `ShipmentDataModule` at runtime ✅
- `:app` cannot import `SalesforceAuthRepository` or `SalesforceSObjectRepository` at compile time ✅
- Architecture boundary enforced by the compiler, not convention ✅

```kotlin
// app/build.gradle.kts
runtimeOnly(project(":auth:data"))      // ← never implementation
runtimeOnly(project(":shipment:data"))  // ← never implementation
```

---

## Logging

All classes use the `Loggable` interface from `:core`. Tag is auto-derived from class name.

```kotlin
class SalesforceAuthRepository @Inject constructor(...) : AuthRepository, Loggable {

    fun onClientAvailable(client: RestClient) {
        d("onClientAvailable", "user=${client.clientInfo.username}")
        // ...
    }

    override suspend fun logout() {
        d("logout", "initiated")
        try {
            SalesforceSDKManager.getInstance().logout(null)
            d("logout", "success")
        } catch (e: Throwable) {
            e("logout", "failed", e)
            throw e
        }
    }
}
```

**Logcat format:**

```
D/SalesforceAuthRepository: [onClientAvailable] user=john@catalent.com
D/AuthViewModel: [logout] initiated
E/SalesforceAuthRepository: [logout] failed — Session expired
```

**Log levels:**

```kotlin
d("method", "message")              // debug — verbose flow tracing
i("method", "message")              // info — key lifecycle events
w("method", "message")              // warn — unexpected but recoverable
e("method", "message", throwable)   // error — always include throwable
```

**Adding a log destination (e.g. Crashlytics):**

1. Create `CrashlyticsLogger : CatalentLogger` in `:core/logging/`
2. Register in `AppLogger.init()` — no other changes needed

**Rules:**
- Never use `Log.d()` or `println()` directly — always use `Loggable`
- Never log PII — no usernames, tokens, or SOQL with user input in production
- `LogDestination.ConsoleOnly` for verbose debug logs you don't want in Crashlytics

---

## Error handling

### Auth errors

Typed via `AuthException` in `:auth:domain`:

```kotlin
sealed class AuthException(message: String) : Exception(message) {
    class InvalidCredentials(...) : AuthException(...)
    class SessionExpired(...) : AuthException(...)
    class NetworkError(...) : AuthException(...)
    class LogoutFailed(...) : AuthException(...)
    class UnknownError(...) : AuthException(...)
}
```

Mapped from Salesforce SDK errors in `AuthExceptionMapper` in `:auth:data`. User-facing strings in `res/values/strings_errors.xml` resolved via `AuthException.toStringRes()` in `:app`.

### Error surfaces

| Error type | Surface | Component |
|---|---|---|
| Fatal auth failure | Full screen | `ErrorScreen` |
| Recoverable (logout fail) | Dialog | `AlertDialog` in `MainActivity` |
| No data offline | Empty state | `EmptyErrorState` in `SObjectList` |
| Network unavailable | Banner | `NetworkBanner` in `MainScreen` |

### Auth state vs error state

```
AuthState.Error        → fatal, full screen, blocks the app
AuthViewModel.errorState → transient, dialog, dismissable
```

---

## Development guidelines

### Before every PR

```bash
Yet to define
```

### Branch naming

```
feature/JIRA-123-short-description
fix/JIRA-456-what-was-broken
```

### What to check when adding code

**Imports** — if you're importing from a module that shouldn't be a dependency per the table above, stop and reconsider the design.

**RestClient** — must never appear in `:auth:domain`, `:auth:ui`, `:shipment:*`, or Composables. Only in `MainActivity` and `:auth:data`/`:shipment:data` via `ClientProvider`.

**Salesforce SDK classes** — must never appear in domain modules or Composables.

**`Log.d()`** — never use directly. Always implement `Loggable` and use `d()`, `e()` etc.

**Hardcoded strings** — never in Kotlin files. Always in `strings.xml` with `@StringRes`.

**`Dispatchers.IO`** — never hardcoded. Use `AppDispatchers` (pending implementation).

**SOQL injection** — never interpolate `searchQuery` directly into SOQL strings. Use parameterized queries or strict escaping.

---

## Adding a new feature module

Follow the `shipment` pattern exactly:

**1. Create folders:**

```
feature/
├── domain/
├── data/
└── ui/
```

**2. Register in `settings.gradle.kts`:**

```kotlin
include(":feature:domain")
include(":feature:data")
include(":feature:ui")
```

**3. `feature/domain/build.gradle.kts`:**

```kotlin
plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}
dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
```

**4. `feature/data/build.gradle.kts`:**

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}
android {
    namespace = "com.catalent.feature.data"
    compileSdk = 36
    defaultConfig { minSdk = 32 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation(project(":feature:domain"))
    implementation(project(":core"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
```

**5. `feature/ui/build.gradle.kts`:**

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}
android {
    namespace = "com.catalent.feature.ui"
    compileSdk = 36
    defaultConfig { minSdk = 32 }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation(project(":feature:domain"))
    implementation(project(":core"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
}
```

**6. Wire in `:app/build.gradle.kts`:**

```kotlin
runtimeOnly(project(":feature:data"))
implementation(project(":feature:ui"))
implementation(project(":feature:domain"))
```

---