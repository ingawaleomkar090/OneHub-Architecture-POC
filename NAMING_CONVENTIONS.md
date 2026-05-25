# Naming Conventions — Catalent OneHub

This document defines the naming standards for the project. Adhering to these rules ensures consistency across the multi-module architecture and helps AI agents (like GitHub Copilot) generate code that matches our style.

## 1. Kotlin & Java Code

### Classes & Interfaces
| Type | Convention | Example |
|---|---|---|
| **ViewModel** | `{Feature}ViewModel` | `AuthViewModel`, `ShipmentListViewModel` |
| **Repository Interface** | `{Entity}Repository` | `AuthRepository`, `SObjectRepository` |
| **Repository Implementation** | `{Platform}{Entity}Repository` | `SalesforceAuthRepository` |
| **Use Case** | `{Action}{Entity}UseCase` | `LogoutUseCase`, `GetContactsUseCase` |
| **Data Model (Domain)** | `{Entity}Data` or `{Entity}` | `SObjectData`, `User` |
| **Data Model (Data Layer)** | `{Entity}Dto` or `{Entity}Entity` | `SalesforceUserDto` |
| **UI State** | `{Screen}State` | `SObjectListState` |
| **Mapper** | `{From}To{To}Mapper` | `AuthExceptionMapper` |

### Variables & Properties
*   **Mutable Private State**: Prefix with an underscore (`_`) and use camelCase.
*   **Immutable Public State**: No prefix, same name as private counterpart.
*   **Booleans**: Use `is`, `has`, or `should` prefixes.
*   **Collections**: Use plural nouns.

```kotlin
// Private mutable state
private val _items = MutableStateFlow<List<SObjectData>>(emptyList())
// Public immutable state
val items: StateFlow<List<SObjectData>> = _items.asStateFlow()

var isLoading by mutableStateOf(false)
var hasError by mutableStateOf(false)
```

### Functions
*   **Actions**: Use verbs. (`fetchData`, `saveContact`, `logout`)
*   **Flow Transformations**: Use `get` prefix for streams. (`getSObjects`)
*   **Event Handlers**: Use `on` prefix. (`onSearchQueryChanged`, `onBackClicked`)

---

## 2. Resources (XML & Compose)

### Compose Components
*   **Screens**: `{Feature}Screen` (e.g., `HomeScreen.kt`)
*   **Components**: `{Purpose}{Type}` (e.g., `NetworkBanner.kt`, `SObjectRow.kt`)
*   **Previews**: `{Component}Preview`

### XML Resources
| Type | Convention | Example |
|---|---|---|
| **Strings** | `{module}_{purpose}` | `auth_login_button`, `core_network_error` |
| **Drawables** | `ic_{name}` (icons) or `bg_{name}` | `ic_logout.xml`, `bg_button_rounded.xml` |
| **Colors** | `{purpose}_{shade}` | `primary_500`, `surface_variant` |

---

## 3. Package Structure
We follow a feature-based package structure within modules:
`com.catalent.{feature}.{layer}.{sub-package}`

*   **Correct**: `com.catalent.auth.ui.presentation.viewmodel`
*   **Correct**: `com.catalent.auth.data.repository`

---

## 4. Git Branching
*   **Features**: `feature/JIRA-123-short-description`
*   **Bug Fixes**: `fix/JIRA-456-issue-description`
*   **Hotfix**: `hotfix/urgent-fix-name`
*   **Refactor**: `refactor/cleanup-logic`

---

## 5. Module Naming (Gradle)
*   **Format**: `:feature:layer`
*   **Examples**: `:auth:domain`, `:auth:data`, `:auth:ui`
*   **Core**: `:core`
