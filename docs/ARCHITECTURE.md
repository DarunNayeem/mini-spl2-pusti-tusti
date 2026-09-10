# System Architecture Guide

## Overview
Pusti-Tusti follows a layered architecture pattern with clear separation of concerns, dependency injection, and modern design patterns to ensure maintainability, testability, and scalability.

## Architecture Layers

### 1. Presentation Layer (UI)
**Location**: `com.mealplanner.ui`

The presentation layer handles user interactions and displays data using JavaFX components.

#### Components:
- **MainViewController**: Primary application controller managing the main interface
- **ProfileEditorController**: Modal dialog for user profile management
- **RecipeCardController**: Individual recipe display components
- **FamilyMemberTabController**: Family member profile management
- **ShoppingListController**: Shopping list generation and display

#### Key Features:
- **MVC Pattern**: Clean separation between view logic and business logic
- **Dependency Injection**: Services injected into controllers for loose coupling
- **FXML-based UI**: Declarative UI design with FXML files
- **CSS Styling**: Professional appearance with custom stylesheets

#### UI Architecture:
```
MainView.fxml (BorderPane)
├── Top: Menu/Navigation
├── Center: TabPane (Nested Structure)
│   ├── Day Tabs (Monday-Sunday)
│   └── Meal Tabs (Breakfast, Lunch, Dinner, Snacks)
└── Right: Profile Information Panel
```

### 2. Service Layer (Business Logic)
**Location**: `com.mealplanner.service`

The service layer contains all business logic and orchestrates operations between the UI and data access layers.

#### MealPlannerService
**Purpose**: Core meal planning logic and recipe selection

**Key Methods**:
- `generateNewWeeklyPlan()`: Creates 7-day personalized meal plans
- `generateWeeklyMealsForType()`: Generates meals for specific meal types
- `generateFamilyMealPlan()`: Creates meal plans for entire families

**Algorithm Features**:
- **Optimized Database Access**: 4 database calls instead of 28 (7 days × 4 meals)
- **Recipe Variety Logic**: Prevents recipe repetition using cooldown mechanisms
- **Portion Control**: Automatic portion size calculation based on nutritional needs
- **Nutritional Balance**: Ensures meals meet daily nutritional requirements

#### NutritionCalculatorService
**Purpose**: Nutritional calculations and personalized requirements

**Key Methods**:
- `calculateNutritionProfile()`: Computes personalized nutrition requirements
- `calculateBMI()`: Body Mass Index calculations
- `calculateBMR()`: Basal Metabolic Rate using age-appropriate formulas
- `calculateTDEE()`: Total Daily Energy Expenditure based on activity level

**Calculation Standards**:
- **BMR Formulas**: Schofield (≤18 years), Mifflin-St Jeor (>18 years)
- **Activity Multipliers**: 5-level activity scale (1.2 - 1.9)
- **Medical Adjustments**: Condition-specific nutritional modifications
- **Macronutrient Distribution**: Balanced protein, carbohydrate, and fat ratios

#### ProfileService
**Purpose**: User profile management and persistence

**Key Methods**:
- `saveProfile()`: Persists user profiles to SQLite
- `loadProfile()`: Retrieves user profiles from SQLite storage
- `createDemoProfile()`: Generates demo profile for testing

### 3. Data Access Layer (DAL)
**Location**: `com.mealplanner.dal`

The data access layer provides abstraction for database operations and data persistence.

#### DatabaseManager
**Pattern**: Singleton
**Purpose**: Centralized database connection management

**Features**:
- **Connection Pooling**: Efficient database connection management
- **View Creation**: Automatic creation of unified recipe views
- **Error Handling**: Robust database error management
- **Resource Cleanup**: Automatic connection cleanup

#### RecipeDAO / RecipeDAOImpl
**Pattern**: Repository Pattern
**Purpose**: Recipe data access with complex filtering capabilities

**Key Methods**:
- `findRecipeById()`: Retrieve specific recipes
- `findRecipesByFilter()`: Complex multi-criteria filtering
- `findRecipesByMealType()`: Meal type-specific queries
- `findRecipesByDietaryRequirements()`: Dietary restriction filtering
- `findRecipesByNutrition()`: Nutritional value-based filtering

**Advanced Features**:
- **JSON Deserialization**: Automatic parsing of ingredient and nutrition data
- **Allergen Filtering**: Ingredient-level allergen detection
- **Tag-based Searching**: Flexible recipe categorization
- **Parameterized Queries**: SQL injection prevention

#### DAOFactory
**Pattern**: Factory Pattern
**Purpose**: DAO instance creation and management

### 4. Domain Model Layer
**Location**: `com.mealplanner.model`

The model layer defines the core domain objects and data structures.

#### User Models (`com.mealplanner.model.user`)
- **UserProfile**: Complete user information with health goals and preferences
- **NutritionProfile**: Calculated nutritional requirements and goals
- **FamilyMember**: Extended user profile for family relationships

#### Recipe Models (`com.mealplanner.model.recipe`)
- **Recipe**: Complete recipe information with multilingual support
- **Ingredient**: Recipe components with quantities and nutritional data
- **NutritionInfo**: Comprehensive nutrition information (27+ nutrients)

#### Meal Plan Models
- **WeeklyMealPlan**: 7-day meal plan structure
- **DailyMealPlan**: Single day meal organization
- **Meal**: Individual meal with recipes and nutritional data
- **FamilyMealPlan**: Multi-member meal planning

#### Enums
- **Gender**: MALE, FEMALE
- **HealthGoal**: WEIGHT_LOSS, WEIGHT_GAIN, MUSCLE_GAIN, MAINTENANCE
- **MealType**: BREAKFAST, LUNCH, DINNER, SNACKS
- **MedicalCondition**: DIABETES, HYPERTENSION, HEART_DISEASE, etc.
- **DietaryPreference**: VEGAN, VEGETARIAN, NON_VEGETARIAN

## Design Patterns

### 1. Dependency Injection
**Implementation**: Constructor-based injection in services and controllers
**Benefits**: Loose coupling, testability, maintainability

```java
public class MainViewController {
    private MealPlannerService mealPlannerService;
    private ProfileService profileService;
    private NutritionCalculatorService nutritionCalculatorService;
    
    public void setServices(MealPlannerService mealPlannerService, 
                           ProfileService profileService,
                           NutritionCalculatorService nutritionCalculatorService) {
        // Dependency injection
    }
}
```

### 2. Singleton Pattern
**Implementation**: DatabaseManager for connection management
**Benefits**: Single point of database access, resource management

### 3. Factory Pattern
**Implementation**: DAOFactory for DAO creation
**Benefits**: Centralized object creation, easy testing and mocking

### 4. Repository Pattern
**Implementation**: RecipeDAO for data access abstraction
**Benefits**: Data access abstraction, testability, flexibility

### 5. MVC Pattern
**Implementation**: JavaFX controllers with FXML views
**Benefits**: Separation of concerns, maintainable UI code

## Data Flow Architecture

### Meal Plan Generation Flow
1. **UI Request**: User requests meal plan generation
2. **Service Orchestration**: MealPlannerService coordinates the process
3. **Nutrition Calculation**: NutritionCalculatorService computes requirements
4. **Data Access**: RecipeDAO queries database with filtering criteria
5. **Algorithm Processing**: Recipe selection and portion calculation
6. **Plan Assembly**: Weekly meal plan construction
7. **UI Update**: Display updated meal plan in interface

### Profile Management Flow
1. **UI Input**: User creates/edits profile information
2. **Validation**: Input validation and sanitization
3. **Service Processing**: ProfileService manages profile data
4. **Nutrition Calculation**: Automatic nutrition profile generation
5. **Persistence**: SQLite-backed profile storage in `user_profiles`
6. **UI Refresh**: Updated profile display

## Performance Optimizations

### Database Access Optimization
- **Batch Queries**: 4 database calls for weekly meal generation instead of 28
- **Connection Pooling**: Efficient database connection reuse
- **Prepared Statements**: SQL injection prevention and performance
- **View Optimization**: Unified recipe view for efficient querying

### Memory Management
- **Object Pooling**: Recipe caching for frequently accessed data
- **Lazy Loading**: On-demand data loading for large datasets
- **Garbage Collection**: Efficient object lifecycle management

### Algorithm Efficiency
- **Recipe Cooldown**: Prevents repetitive database queries for variety
- **Portion Calculation**: Mathematical optimization for portion sizing
- **Nutritional Scoring**: Efficient recipe ranking algorithms

## Security Considerations

### Data Protection
- **SQL Injection Prevention**: Parameterized queries throughout
- **Input Validation**: Comprehensive input sanitization
- **File System Security**: Secure profile file storage

### Database Security
- **Connection Security**: Secure database connection management
- **Access Control**: Proper database access patterns
- **Data Integrity**: Transaction management and rollback

## Scalability Design

### Horizontal Scaling
- **Service Layer**: Stateless services enable easy scaling
- **Database Layer**: SQLite suitable for single-user applications
- **UI Layer**: JavaFX desktop application architecture

### Vertical Scaling
- **Memory Efficiency**: Optimized object creation and management
- **Processing Efficiency**: Algorithmic optimizations
- **Storage Efficiency**: Compact data structures and JSON storage

## Testing Architecture

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
