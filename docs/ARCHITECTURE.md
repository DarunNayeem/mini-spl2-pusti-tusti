# System Architecture Guide

## Overview
Pusti-Tusti is structured as a layered Java desktop application with a JavaFX UI, a service layer for nutrition logic and plan generation, and a data-access layer backed by SQLite.

## Current State

The project is in a stable cleanup phase. The app currently compiles and the automated test suite passes. Several cleanup steps have already reduced brittle startup and persistence issues, but the meal-planning engine still has architectural complexity and remains the main refactor target.

## Architecture Layers

### 1. Presentation Layer
**Location**: `com.mealplanner.ui`

This layer handles UI flow and user interaction through JavaFX controls and FXML screens.

Key controllers include:
- `MainViewController`
- `ProfileEditorController`
- `ProfileListController`
- `FamilyMemberTabController`
- `ShoppingListController`

Responsibilities:
- show family profiles and plans
- trigger generation flows
- open profile management forms
- present shopping list output

### 2. Service Layer
**Location**: `com.mealplanner.service`

The service layer contains the application logic that coordinates profile data, nutrition targets, and recipe selection.

#### `MealPlannerService`
This is the main orchestration component for plan generation. It creates weekly plans, calculates portions, selects recipes, and assembles family-level meal suggestions.

Current characteristics:
- functional and tested
- large and multi-responsibility in nature
- still a consolidation point for scoring, filtering, and recommendation logic

#### `NutritionCalculatorService`
Responsible for converting profile data into daily nutrition targets and related metrics.

#### `ProfileService`
Handles profile persistence and cache management for family members.

### 3. Data Access Layer
**Location**: `com.mealplanner.dal`

This layer encapsulates the SQLite interactions.

Implemented components:
- `DatabaseManager`: connection lifecycle and schema bootstrap
- `DAOFactory`: creation of DAO instances
- `RecipeDAO` / `RecipeDAOImpl`: recipe retrieval and filtering
- `ProfileDAO` / `SQLiteProfileDAO`: profile persistence

Recent improvements include:
- safer default DB path resolution
- directory initialization before database creation
- explicit exception flow during startup and schema setup
- logging instead of ad hoc print statements

### 4. Domain Model Layer
**Location**: `com.mealplanner.model`

This layer models the domain objects used by the planner and UI.

Core categories:
- user profile models
- recipe models
- meal plan models
- enums for goals, conditions, genders, and meal types

## Design Characteristics

### Dependency injection
The app uses service injection into controllers and relies on constructor-based wiring for the main runtime setup.

### Repository / DAO pattern
The repository layer abstracts database access and keeps SQL-specific logic away from UI and business responsibilities.

### MVC-inspired UI structure
The UI follows a JavaFX-based controller pattern, where FXML defines the screen and controllers respond to events.

## Runtime Flow

1. Application starts through `App`.
2. Database is initialized.
3. Services are created.
4. Main JavaFX scene is loaded.
5. Family profiles are loaded and UI tabs are created.
6. Meal generation is triggered from the controller layer.
7. The planner uses recipe + profile data to build plans and shopping lists.

## Observed Strengths

- Clear project boundaries between UI, services, DAL, and model
- Portable SQLite local storage model
- Good fit for a personal or academic desktop app
- Sufficient test coverage for core repository behavior

## Observed Limitations

- `MealPlannerService` still acts as a large orchestration class and is the main source of architectural complexity
- Some business logic is still embedded in the same class as selection scoring and fallback behaviors
- Debug logging was previously heavy; cleanup reduced it, but a formal logger strategy could still be improved across all classes

## Recommendations

### Short-term
- Keep the current cleanup direction
- Centralize logging in a standard logger strategy
- Continue reducing duplicate logic in planner-related classes

### Medium-term
- Split `MealPlannerService` into dedicated helpers for:
  - recipe filtering
  - score calculation
  - fallback selection
  - family plan aggregation

### Long-term
- Consider a dedicated recommendation engine or strategy interface
- Move more selection rules into domain-specific components

## Status

**Current state**: stable working app with verified tests and a visible refactor opportunity in the planner service.

**Documentation note**: this architecture guide reflects the real current state, not an idealized target architecture.

### Unit Testing
- **Service Layer Testing**: Comprehensive business logic testing
- **DAO Layer Testing**: Database access testing with test data
- **Model Testing**: Domain object validation and behavior testing

### Integration Testing
- **Service Integration**: End-to-end service workflow testing
- **Database Integration**: Complete database operation testing
- **UI Integration**: JavaFX component testing

## Module System

### Java 9+ Modules
The application uses the Java module system for clean dependency management:

```java
module com.mealplanner {
    requires java.desktop;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    
    exports com.mealplanner;
    opens com.mealplanner.ui to javafx.fxml;
}
```

### Benefits:
- **Explicit Dependencies**: Clear dependency declarations
- **Encapsulation**: Module-level encapsulation
- **Compile-time Checks**: Dependency validation at compile time
- **Performance**: Improved application startup and runtime performance

## Error Handling Strategy

### Exception Hierarchy
- **Business Logic Exceptions**: Custom exceptions for business rule violations
- **Data Access Exceptions**: Database and file access error handling
- **UI Exceptions**: User interface error management

### Error Recovery
- **Graceful Degradation**: Application continues functioning with limited features
- **User Feedback**: Clear error messages and recovery suggestions
- **Logging**: Comprehensive error logging for debugging

## Configuration Management

### Application Configuration
- **Database Configuration**: Connection string and parameters
- **UI Configuration**: Window size, styling, and layout preferences
- **Business Logic Configuration**: Nutritional constants and algorithms

### Environment Support
- **Development**: Debug logging and development database
- **Production**: Optimized performance and minimal logging
- **Testing**: Test database and mock configurations

---

This architecture ensures a maintainable, scalable, and robust meal planning application that can evolve with changing requirements while maintaining high code quality and user experience.
