# Pusti-Tusti - Personalized Meal Planning Application

## Overview
Pusti-Tusti is a comprehensive meal planning application designed to provide personalized nutrition recommendations based on individual health goals, medical conditions, and dietary preferences. The application features a modern JavaFX interface and uses SQLite for data persistence.

## Key Features

### 🧑‍🤝‍🧑 User Management
- **Comprehensive User Profiles**: Personal information, health goals, medical conditions, and dietary preferences
- **Family Member Management**: Support for multiple family members with individual profiles
- **Activity Level Tracking**: 5-level activity scale for accurate calorie calculations
- **Allergy and Dietary Restriction Management**: Comprehensive allergen filtering

### 🍽️ Meal Planning
- **7-Day Weekly Meal Plans**: Personalized weekly meal planning with nutritional balance
- **4-Meal Structure**: Breakfast, Lunch, Dinner, and Snacks for complete daily nutrition
- **Recipe Variety**: Intelligent algorithm ensures meal variety and prevents repetition
- **Portion Control**: Automatic portion size calculation based on nutritional requirements

### 📊 Nutrition Tracking
- **BMI, BMR, and TDEE Calculations**: Industry-standard formulas for accurate metabolic calculations
- **Personalized Nutrition Goals**: Customized calorie and macronutrient targets
- **Medical Condition Awareness**: Nutrition adjustments for diabetes, hypertension, and other conditions
- **27+ Micronutrient Tracking**: Comprehensive vitamin and mineral monitoring

### 🥘 Recipe Management
- **Multilingual Support**: English and Bengali recipe content
- **Comprehensive Recipe Database**: Detailed ingredient lists with nutritional information
- **Advanced Filtering**: Filter by meal type, dietary requirements, allergens, and nutrition values
- **JSON-Based Data Structure**: Flexible ingredient and nutrition data storage

### 🎨 User Interface
- **Modern JavaFX Design**: Professional and intuitive user interface
- **Nested Tab Navigation**: Organized meal plan browsing by day and meal type
- **Modal Profile Editor**: Easy profile creation and editing
- **Recipe Cards**: Detailed recipe display with nutrition information

## Technology Stack

- **Java**: 17+ (Latest LTS version)
- **Build Tool**: Maven 3.6+
- **GUI Framework**: JavaFX (Controls and FXML)
- **Database**: SQLite with JDBC driver
- **JSON Processing**: Jackson Core and Databind
- **Testing**: JUnit 5
- **Module System**: Java 9+ modules for clean dependency management

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   ├── module-info.java
│   │   └── com/
│   │       └── mealplanner/
│   │           ├── App.java                    # Main application entry point
│   │           ├── dal/                        # Data Access Layer
│   │           │   ├── DatabaseManager.java   # Singleton database connection
│   │           │   ├── RecipeDAO.java          # Recipe data access interface
│   │           │   ├── RecipeDAOImpl.java      # Recipe DAO implementation
│   │           │   └── DAOFactory.java         # DAO factory pattern
│   │           ├── model/                      # Data Models
│   │           │   ├── *.java                  # Enums (Gender, HealthGoal, etc.)
│   │           │   ├── recipe/                 # Recipe models
│   │           │   ├── user/                   # User and nutrition models
│   │           │   └── *.java                  # Meal plan models
│   │           ├── service/                    # Business Logic Layer
│   │           │   ├── MealPlannerService.java # Meal planning logic
│   │           │   ├── NutritionCalculatorService.java # Nutrition calculations
│   │           │   └── ProfileService.java     # Profile management
│   │           └── ui/                         # User Interface
│   │               ├── *.java                  # JavaFX controllers
│   │               ├── *.fxml                  # JavaFX layouts
│   │               └── *.css                   # Stylesheets
│   └── resources/
│       ├── recipes.db                          # SQLite database
│       └── com/mealplanner/ui/                 # UI resources
└── test/
    └── java/
        └── com/mealplanner/                    # Unit tests
```

## Building and Running

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Build Commands
```bash
# Clean previous builds
mvn clean

# Compile the application
mvn compile

# Run tests
mvn test

# Run the application
mvn javafx:run
```

### VS Code Integration
Use the predefined task "Run Pusti-Tusti Application" or press `Ctrl+Shift+P` and run the task.

## Application Flow

1. **Application Launch**: 
   - Initialize services and database connections
   - Load or create user profile
   - Display main interface

2. **Profile Management**:
   - Create/edit user profiles with health goals
   - Set dietary preferences and medical conditions
   - Configure family member profiles

3. **Meal Plan Generation**:
   - Generate 7-day personalized meal plans
   - Calculate nutritional requirements based on user profile
   - Ensure recipe variety and prevent repetition

4. **Plan Navigation**:
   - Browse meal plans using nested day/meal tabs
   - View detailed recipe information
   - Review nutritional content

## Database Schema

The application uses SQLite with the following key tables:
- **breakfast**, **lunch**, **dinner**, **snacks**: Recipe data by meal type
- **Unified Recipe View**: Combined view of all meal types for efficient querying
- **JSON Columns**: Ingredients, tags, and instructions stored as JSON for flexibility

## Architecture Highlights

### Service Layer Architecture
- **Dependency Injection**: Clean separation of concerns with service injection
- **Factory Pattern**: DAO factory for database access
- **Strategy Pattern**: Multiple nutrition calculation strategies

### Data Access Layer
- **Singleton Pattern**: Single database connection manager
- **Repository Pattern**: Recipe DAO with comprehensive filtering
- **JSON Deserialization**: Automatic parsing of complex data structures

### User Interface Design
- **MVC Pattern**: Model-View-Controller architecture
- **Component-Based**: Reusable UI components and controllers
- **CSS Styling**: Professional appearance with custom stylesheets

## Demo Profile

The application includes a demo profile for immediate testing:
- **Name**: Demo User
- **Demographics**: 30 years old, Male, 175cm, 70kg
- **Goal**: Weight Loss with moderate activity level
- **Preferences**: Realistic dietary preferences and allergy information

## Future Enhancement Opportunities

- **Advanced Meal Planning**: Machine learning-based recipe recommendations
- **Shopping List Generation**: Automatic shopping list creation from meal plans
- **Nutrition Analytics**: Detailed nutrition analysis and reporting
- **Cloud Synchronization**: Multi-device profile synchronization
- **Mobile App**: Companion mobile application
- **Social Features**: Meal plan sharing and community features

## Development Standards

### Code Quality
- **Clean Code**: Consistent naming conventions and documentation
- **SOLID Principles**: Single responsibility and dependency inversion
- **Comprehensive Testing**: Unit tests for critical business logic
- **Error Handling**: Robust error management throughout the application

### Performance Optimizations
- **Database Efficiency**: Optimized query patterns (4 calls vs 28 for meal generation)
- **Memory Management**: Efficient object creation and garbage collection
- **Caching Strategy**: Recipe caching for improved performance

## Documentation Structure

This documentation is organized into the following sections:

- **[Architecture Guide](ARCHITECTURE.md)**: Detailed system architecture and design patterns
- **[API Documentation](API.md)**: Service layer APIs and method documentation
- **[Database Guide](DATABASE.md)**: Database schema and data access patterns
- **[UI Guide](UI.md)**: User interface components and JavaFX implementation
- **[Model Documentation](MODELS.md)**: Data models and domain objects
- **[Deployment Guide](DEPLOYMENT.md)**: Build, test, and deployment procedures

## License

This project is developed as part of a meal planning application development initiative.

---

**Version**: 1.0
**Last Updated**: July 2025
**Status**: Production Ready ✅
