# SOLID Principles in OneHub

This document explains how the Five SOLID principles are applied in this project.

### 1. Single Responsibility Principle (SRP)
Each class has one reason to change.
*   **Example**: `LogoutUseCase` only handles the orchestration of a logout. It doesn't know about Salesforce SDK specifics or UI navigation.
*   **Example**: `ClientProvider` only manages the lifecycle of the network client.

### 2. Open/Closed Principle (OCP)
The system is open for extension but closed for modification.
*   **Implementation**: By using interfaces for Repositories, we can add a new authentication provider (e.g., `OktaAuthRepository`) without changing the code in `AuthViewModel` or `LogoutUseCase`.

### 3. Liskov Substitution Principle (LSP)
Objects should be replaceable with instances of their subtypes without altering correctness.
*   **Implementation**: Any implementation of `AuthRepository` (Salesforce, Mock, or Firebase) can be injected into the `LogoutUseCase` without changing the logic of the UseCase.

### 4. Interface Segregation Principle (ISP)
Clients should not be forced to depend on methods they do not use.
*   **Implementation**: Features are modularized (`:auth`, `:shipment`). A developer working on shipments doesn't see or depend on the internal authentication logic or interfaces.

### 5. Dependency Inversion Principle (DIP)
High-level modules should not depend on low-level modules. Both should depend on abstractions.
*   **Core Implementation**: The `core` module depends on the `AppClient` interface. The `app` module provides the implementation via `RawClientWrapper`.
*   **Module Implementation**: The `ui` layer depends on `AuthRepository` (interface in `:domain`), not `SalesforceAuthRepository` (implementation in `:data`).
