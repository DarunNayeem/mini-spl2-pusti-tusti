# Pusti-Tusti - Personalized Meal Planning Application

## Overview
Pusti-Tusti is a JavaFX-based meal planning desktop application for generating personalized weekly nutrition plans for individuals and families. It combines user profile data, dietary constraints, medical conditions, and calorie targets to recommend recipes, generate meal plans, and produce consolidated shopping lists.

## Current Project Status

As of September 2026, the project is in a stable maintenance and cleanup phase.

- The codebase compiles successfully with Java 17 and Maven.
- The existing automated test suite passes: `mvn -q test` returned `EXIT:0`.
- Major code-smell cleanup has been applied around startup, database initialization, and profile identity handling.
- One notable architectural debt remains: the planner logic in `MealPlannerService` is still large and could be split into smaller specialist services in a future refactor.

## Key Features

### User and Family Management
- Create and manage multiple family member profiles
- Store age, weight, height, gender, allergens, medical conditions, and goals
- Support per-member meal preferences and nutrition targets
- Maintain unique profile identity across UI and persistence layers

### Meal Planning
- Generate personalized 7-day meal plans
- Split daily intake into breakfast, lunch, dinner, and snacks
- Consider nutrition goals, allergies, and medical context
- Try to avoid repetitive recipes and maintain variety over the week

### Nutrition Logic
- Calculate calorie and macronutrient targets from profile data
- Evaluate medical-condition-aware dietary preferences
- Support low-calorie, high-protein, diabetic-friendly, and healthy-fresh recommendations
- Assemble daily nutrition totals from selected meals

### Recipe and Data Layer
- SQLite-backed recipe data store
- Recipe filtering by meal type, dietary patterns, tags, and allergens
- JSON-based ingredient and nutrition parsing
- Unified recipe view for cross-table queries

### UI
- JavaFX desktop interface with FXML and CSS styling
- Family tab navigation and profile management dialogs
- Shopping list generation based on selected plans
- Family-level meal plan presentation with member-specific personalization

## Technology Stack

- Java 17
- Maven
- JavaFX 17.0.2
- SQLite JDBC
- Jackson JSON library
- JUnit 5

## Project Structure

```text
src/
├── main/
│   ├── java/
│   │   ├── module-info.java
│   │   └── com/
│   │       └── mealplanner/
│   │           ├── App.java
│   │           ├── dal/
│   │           │   ├── DAOFactory.java
│   │           │   ├── DatabaseManager.java
│   │           │   ├── ProfileDAO.java
│   │           │   ├── RecipeDAO.java
│   │           │   ├── RecipeDAOImpl.java
│   │           │   └── SQLiteProfileDAO.java
│   │           ├── model/
│   │           │   ├── DailyMealPlan.java
│   │           │   ├── FamilyMealPlan.java
│   │           │   ├── Meal.java
│   │           │   ├── MealType.java
│   │           │   ├── WeeklyMealPlan.java
│   │           │   ├── recipe/
│   │           │   └── user/
│   │           ├── service/
│   │           │   ├── MealPlannerService.java
│   │           │   ├── NutritionCalculatorService.java
│   │           │   └── ProfileService.java
│   │           └── ui/
│   │               ├── ... JavaFX controllers ...
│   │               ├── ... FXML files ...
│   │               └── ... CSS resources ...
│   └── resources/
│       └── com/mealplanner/ui/
├── test/
│   └── java/
│       └── com/mealplanner/
└── docs/
    └── ... documentation files ...
```

## Build and Run

### Prerequisites
- Java 17+
- Maven 3.6+

### Commands
```bash
mvn clean
mvn compile
mvn test
mvn javafx:run
```

## Verified Status

The project has been checked with the following command:

```bash
cd /media/nym/endeavouros/home/nym/Pictures/Java-Pusti-Tusti && mvn -q test; echo EXIT:$?
```

This currently returns:

```text
EXIT:0
```

## Current Health Summary

### Strengths
- Small but coherent desktop app architecture
- Functional SQLite persistence layer
- Core nutrition and planning logic working in tests
- Service/UI boundary mostly in place
- Improved error handling and safer database initialization

### Remaining Considerations
- The meal-planning engine is still large and could be decomposed further
- A few cross-cutting concerns remain in the planner service
- The app is best described as a working local desktop project rather than a full production platform

## Documentation

- [docs/INDEX.md](docs/INDEX.md)
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- [docs/API.md](docs/API.md)
- [docs/DATABASE.md](docs/DATABASE.md)
- [docs/UI.md](docs/UI.md)
- [docs/MODELS.md](docs/MODELS.md)
- [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)

## License

This project is maintained as a Java/JavaFX meal-planning application for local use and academic or personal development purposes.

---

**Version**: 1.1
**Last Updated**: September 2026
**Status**: Active project maintenance ✅
