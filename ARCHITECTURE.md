# Architecture Guide — Catalent OneHub

This document provides a technical deep-dive into the architecture of the Catalent OneHub Android application.

## High-Level Architecture
OneHub is built using **Multi-Module Clean Architecture** organized by **Feature**.

### Modularization Strategy
The project is split into several Gradle modules to ensure separation of concerns and build scalability:

1.  **`:app` (The Shell)**: The Android Application module. Responsible for Hilt initialization, Navigation, and Dependency Orchestration.
2.  **Feature Modules (`:auth`, `:shipment`)**: Each feature is further divided into:
    *   **`:domain`**: Pure Kotlin module. Contains Entities, Use Cases, and Repository Interfaces. No dependencies on Android or SDKs.
    *   **`:data`**: Android Library. Implements Repository interfaces, handles persistence, and integrates with the Salesforce SDK.
    *   **`:ui`**: Android Library. Contains ViewModels and Jetpack Compose screens.
3.  **`:core` (The Foundation)**: Shared infrastructure like Networking, Logging, and common utilities.

## Dependency Inversion & SOLID
We strictly follow SOLID principles to keep the codebase maintainable.

### The Dependency Rule
Dependencies only point inwards towards the **Domain** layer. 
> **UI Module** → **Domain Module** ← **Data Module**

### SDK Decoupling (DIP)
The `core` module is decoupled from specific SDKs (like Salesforce) using the `AppClient` abstraction:
*   `ClientProvider` (`core`) manages an `AppClient` flow.
*   `MainActivity` (`app`) wraps the platform's `RestClient` into a `RawClientWrapper`.
*   `SalesforceAuthRepository` (`data`) unwraps the client to perform operations.

This ensures that the `core` and `domain` modules remain "clean" and aren't recompiled when SDK versions change.

## Reactive Flow
The app uses a fully reactive data flow based on **Kotlin Coroutines and Flow**.

1.  **Platform Event**: Salesforce SDK provides a `RestClient` in `MainActivity.onResume`.
2.  **State Update**: `ClientProvider` emits the new client via `StateFlow`.
3.  **Domain Transformation**: `AuthRepository` maps the client flow to an `AuthState` flow.
4.  **UI Observation**: `AuthViewModel` converts the repository flow into a `StateFlow` for the Compose UI.

## Runtime Enforcement
We use Gradle's `runtimeOnly` configuration in the `:app` module for `:data` modules.
```kotlin
// app/build.gradle.kts
implementation(projects.auth.domain)
implementation(projects.auth.ui)
runtimeOnly(projects.auth.data) // Enforces that UI/App cannot access Data implementation classes
```
This ensures that the `app` and `ui` modules can only "see" the interfaces defined in the `domain`, while Hilt injects the implementations at runtime.

## Coding Patterns
*   **Single Source of Truth**: Data always flows from the Repository.
*   **State Containers**: ViewModels use `stateIn` to manage UI state with `WhileSubscribed(5000)`.
*   **No Listeners**: Prefer Flows over callback interfaces for inter-module communication.
