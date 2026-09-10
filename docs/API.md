# API Documentation

## Service Layer APIs

This document provides comprehensive documentation for all public APIs in the service layer of the Pusti-Tusti meal planning application.

---

## MealPlannerService

**Package**: `com.mealplanner.service`  
**Purpose**: Core meal planning logic and recipe selection service

### Constructor

#### `MealPlannerService(RecipeDAO recipeDAO)`
Initializes the meal planner service with a recipe data access object.

**Parameters**:
- `recipeDAO` - Recipe data access object for database operations

---

### Public Methods

#### `generateNewWeeklyPlan(UserProfile user, NutritionProfile nutritionProfile)`
Generates a personalized 7-day weekly meal plan based on user profile and nutrition requirements.

**Parameters**:
- `user` - Complete user profile with preferences and health goals
- `nutritionProfile` - Calculated nutritional requirements and targets

**Returns**: `WeeklyMealPlan` - Complete 7-day meal plan with breakfast, lunch, dinner, and snacks

**Algorithm Features**:
- Makes exactly 4 optimized database calls (one per meal type)
- Ensures recipe variety using intelligent cooldown mechanisms
- Prevents lunch/dinner repetition on the same day
- Calculates appropriate portion sizes for nutritional targets

**Example Usage**:
```java
WeeklyMealPlan plan = mealPlannerService.generateNewWeeklyPlan(userProfile, nutritionProfile);
```

#### `generateFamilyMealPlan(List<UserProfile> familyMembers, Map<UserProfile, NutritionProfile> nutritionProfiles)`
Creates a comprehensive meal plan for multiple family members with individual dietary requirements.

**Parameters**:
- `familyMembers` - List of all family member profiles
- `nutritionProfiles` - Map of nutrition profiles for each family member

**Returns**: `FamilyMealPlan` - Family-wide meal plan with individual member considerations

**Features**:
- Accommodates different dietary restrictions within the family
- Balances nutritional needs across family members
- Generates consolidated shopping lists
- Handles allergen conflicts between family members

---

## NutritionCalculatorService

**Package**: `com.mealplanner.service`  
**Purpose**: Nutritional calculations and personalized requirement computation

### Public Methods

#### `calculateNutritionProfile(UserProfile userProfile)`
Computes comprehensive nutritional requirements based on user demographics, health goals, and medical conditions.

**Parameters**:
- `userProfile` - Complete user profile with personal information and goals

**Returns**: `NutritionProfile` - Calculated nutritional targets and requirements

**Calculations Include**:
- Daily calorie requirements (BMR × activity factor ± goal adjustments)
- Macronutrient distribution (protein, carbohydrates, fats)
- Micronutrient goals (27+ vitamins and minerals)
- Medical condition-specific adjustments

**Example Usage**:
```java
NutritionProfile nutrition = nutritionCalculatorService.calculateNutritionProfile(userProfile);
```

#### `calculateBMI(double heightCm, double weightKg)`
Calculates Body Mass Index using standard formula.

**Parameters**:
- `heightCm` - Height in centimeters
- `weightKg` - Weight in kilograms

**Returns**: `double` - BMI value

**Formula**: `BMI = weight(kg) / (height(m))²`

#### `calculateBMR(UserProfile userProfile)`
Calculates Basal Metabolic Rate using age-appropriate formulas.

**Parameters**:
- `userProfile` - User profile with age, gender, height, and weight

**Returns**: `double` - BMR in calories per day

**Formulas**:
- **Ages ≤18**: Schofield equations
- **Ages >18**: Mifflin-St Jeor equations

**Schofield Equations (≤18 years)**:
- Males: `BMR = (17.5 × weight) + (651 × height) + 729`
- Females: `BMR = (12.2 × weight) + (746 × height) + 1553`

**Mifflin-St Jeor Equations (>18 years)**:
- Males: `BMR = (10 × weight) + (6.25 × height) - (5 × age) + 5`
- Females: `BMR = (10 × weight) + (6.25 × height) - (5 × age) - 161`

#### `calculateTDEE(double bmr, int activityLevel)`
Calculates Total Daily Energy Expenditure based on BMR and activity level.

**Parameters**:
- `bmr` - Basal Metabolic Rate
- `activityLevel` - Activity level (1-5 scale)

**Returns**: `double` - TDEE in calories per day

**Activity Multipliers**:
- **Level 1** (Sedentary): 1.2
- **Level 2** (Light): 1.375
- **Level 3** (Moderate): 1.55
- **Level 4** (Active): 1.725
- **Level 5** (Very Active): 1.9

#### `calculateCalorieAdjustment(HealthGoal healthGoal, double tdee)`
Adjusts daily calorie target based on health goals.

**Parameters**:
- `healthGoal` - User's health objective
- `tdee` - Total Daily Energy Expenditure

**Returns**: `double` - Adjusted daily calorie target

**Adjustments**:
- **WEIGHT_LOSS**: -500 calories/day (1 lb/week loss)
- **WEIGHT_GAIN**: +500 calories/day (1 lb/week gain)
- **MUSCLE_GAIN**: +300 calories/day with increased protein
- **MAINTENANCE**: No adjustment

#### `calculateMacronutrientDistribution(double calories, HealthGoal healthGoal)`
Calculates optimal macronutrient distribution based on calories and health goals.

**Parameters**:
- `calories` - Daily calorie target
- `healthGoal` - User's health objective

**Returns**: `Map<String, Double>` - Macronutrient grams (protein, carbs, fat)

**Standard Distribution**:
- **Protein**: 25-30% of calories (higher for muscle gain)
- **Carbohydrates**: 45-50% of calories (lower for weight loss)
- **Fats**: 20-25% of calories

#### `calculateMicronutrientGoals(UserProfile userProfile)`
Computes personalized micronutrient targets based on demographics and medical conditions.

**Parameters**:
- `userProfile` - Complete user profile

**Returns**: `Map<String, Double>` - Daily micronutrient targets

**Includes**:
- **Vitamins**: A, B-complex, C, D, E, K
- **Minerals**: Calcium, Iron, Magnesium, Potassium, Zinc, etc.
- **Medical Adjustments**: Condition-specific modifications

#### `applyMedicalConditionAdjustments(NutritionProfile profile, Set<MedicalCondition> conditions)`
Applies nutritional modifications for medical conditions.

**Parameters**:
- `profile` - Base nutrition profile
- `conditions` - Set of medical conditions

**Returns**: `NutritionProfile` - Modified nutrition profile

**Condition Adjustments**:
- **DIABETES**: Reduced simple carbs, increased fiber
- **HYPERTENSION**: Reduced sodium, increased potassium
- **HEART_DISEASE**: Reduced saturated fat, increased omega-3
- **KIDNEY_DISEASE**: Reduced protein and phosphorus
- **PREGNANCY**: Increased folate, iron, and calories

---

## ProfileService

**Package**: `com.mealplanner.service`  
**Purpose**: User profile management and persistence operations

### Public Methods

#### `saveProfile(UserProfile profile)`
Persists user profile to SQLite storage.

**Parameters**:
- `profile` - Complete user profile to save

**Returns**: `boolean` - Success status of save operation

**Storage Format**: SQLite row in `user_profiles`
**Primary Key**: `user_id`

#### `loadProfile(String profileId)`
Retrieves user profile from SQLite storage.

**Parameters**:
- `profileId` - Unique profile identifier

**Returns**: `UserProfile` - Loaded user profile, or null if not found

#### `getAllProfiles()`
Retrieves all available user profiles from SQLite storage.

**Returns**: `List<UserProfile>` - List of all stored profiles

#### `deleteProfile(String profileId)`
Removes user profile from SQLite storage.

**Parameters**:
- `profileId` - Profile identifier to delete

**Returns**: `boolean` - Success status of delete operation

#### `createDemoProfile()`
Creates a demonstration profile with realistic data for testing.

**Returns**: `UserProfile` - Demo profile with sample data

**Demo Profile Characteristics**:
- **Name**: Demo User
- **Demographics**: 30 years old, Male, 175cm, 70kg
- **Goal**: Weight Loss
- **Activity**: Moderate (Level 3)
- **Preferences**: Realistic dietary preferences and allergies

---

## RecipeDAO Interface

**Package**: `com.mealplanner.dal`  
**Purpose**: Recipe data access interface for database operations

### Public Methods

#### `findRecipeById(String recipeId)`
Retrieves a specific recipe by its unique identifier.

**Parameters**:
- `recipeId` - Unique recipe identifier

**Returns**: `Recipe` - Complete recipe object, or null if not found

#### `findRecipesByFilter(MealType mealType, Set<DietaryPreference> preferences, Set<String> allergens, Map<String, Double> nutritionCriteria)`
Performs complex recipe filtering based on multiple criteria.

**Parameters**:
- `mealType` - Meal category filter (optional)
- `preferences` - Dietary preference filters
- `allergens` - Allergens to exclude
- `nutritionCriteria` - Nutritional value filters

**Returns**: `List<Recipe>` - Filtered recipe list

#### `findRecipesByMealType(MealType mealType)`
Retrieves all recipes for a specific meal type.

**Parameters**:
- `mealType` - Meal category (BREAKFAST, LUNCH, DINNER, SNACKS)

**Returns**: `List<Recipe>` - Meal type-specific recipes

#### `findRecipesByDietaryRequirements(Set<DietaryPreference> preferences, Set<String> allergens)`
Filters recipes based on dietary preferences and allergen restrictions.

**Parameters**:
- `preferences` - Dietary preferences to match
- `allergens` - Allergens to exclude

**Returns**: `List<Recipe>` - Filtered recipe list

#### `findRecipesByNutrition(Map<String, Double> minValues, Map<String, Double> maxValues)`
Filters recipes based on nutritional value ranges.

**Parameters**:
- `minValues` - Minimum nutritional values
- `maxValues` - Maximum nutritional values

**Returns**: `List<Recipe>` - Nutritionally filtered recipes

#### `getSubcategoriesByMealType(MealType mealType)`
Retrieves available subcategories for a meal type.

**Parameters**:
- `mealType` - Meal category

**Returns**: `Set<String>` - Available subcategories

#### `getAllTags()`
Extracts all unique tags from the recipe database.

**Returns**: `Set<String>` - All available recipe tags

#### `getRecipeCount()`
Returns the total number of recipes in the database.

**Returns**: `int` - Total recipe count

---

## Data Models

### UserProfile
**Package**: `com.mealplanner.model.user`

**Key Fields**:
- `String name` - User's full name
- `int age` - Age in years
- `Gender gender` - Gender enumeration
- `double heightCm` - Height in centimeters
- `double weightKg` - Weight in kilograms
- `HealthGoal healthGoal` - Primary health objective
- `int activityLevel` - Activity level (1-5)
- `Set<MedicalCondition> medicalConditions` - Medical conditions
- `Set<DietaryPreference> dietaryPreferences` - Dietary preferences
- `Set<String> allergens` - Known allergens

### NutritionProfile
**Package**: `com.mealplanner.model.user`

**Key Fields**:
- `double bmi` - Body Mass Index
- `double bmr` - Basal Metabolic Rate
- `double tdee` - Total Daily Energy Expenditure
- `double targetCalories` - Daily calorie target
- `double proteinGrams` - Daily protein target
- `double carbsGrams` - Daily carbohydrate target
- `double fatGrams` - Daily fat target
- `Map<String, Double> micronutrientGoals` - Vitamin/mineral targets

### Recipe
**Package**: `com.mealplanner.model.recipe`

**Key Fields**:
- `String recipeId` - Unique identifier
- `String recipeName` - Recipe name
- `String recipeNameBengali` - Bengali translation
- `MealType mealType` - Meal category
- `List<Ingredient> ingredients` - Recipe components
- `NutritionInfo nutritionInfo` - Nutritional data
- `int cookingTimeMinutes` - Preparation time
- `Set<String> tags` - Recipe categories

### WeeklyMealPlan
**Package**: `com.mealplanner.model`

**Key Fields**:
- `UserProfile user` - Associated user
- `NutritionProfile nutritionProfile` - Nutrition targets
- `List<DailyMealPlan> dailyPlans` - 7-day meal plan
- `LocalDate startDate` - Plan start date

---

## Error Handling

### Common Exceptions
- `IllegalArgumentException` - Invalid parameter values
- `SQLException` - Database access errors
- `IOException` - File system operations
- `JsonProcessingException` - JSON serialization errors

### Error Response Patterns
- **Null Returns**: Methods return null for "not found" scenarios
- **Empty Collections**: Empty lists/sets for no results
- **Boolean Status**: Success/failure indicators for operations

---

## Usage Examples

### Complete Meal Planning Workflow
```java
// 1. Initialize services
RecipeDAO recipeDAO = new RecipeDAOImpl();
NutritionCalculatorService nutritionService = new NutritionCalculatorService();
MealPlannerService mealPlannerService = new MealPlannerService(recipeDAO);
ProfileService profileService = new ProfileService();

// 2. Create or load user profile
UserProfile user = profileService.createDemoProfile();

// 3. Calculate nutrition requirements
NutritionProfile nutrition = nutritionService.calculateNutritionProfile(user);

// 4. Generate weekly meal plan
WeeklyMealPlan plan = mealPlannerService.generateNewWeeklyPlan(user, nutrition);

// 5. Save profile for future use
profileService.saveProfile(user);
```

### Advanced Recipe Filtering
```java
// Set up filter criteria
Set<DietaryPreference> preferences = Set.of(DietaryPreference.VEGETARIAN);
Set<String> allergens = Set.of("nuts", "dairy");
Map<String, Double> nutritionCriteria = Map.of(
    "calories", 400.0,
    "protein", 20.0
);

// Apply complex filtering
List<Recipe> filteredRecipes = recipeDAO.findRecipesByFilter(
    MealType.LUNCH, 
    preferences, 
    allergens, 
    nutritionCriteria
);
```

---

This API documentation provides comprehensive coverage of all public interfaces in the Pusti-Tusti meal planning application, enabling developers to effectively integrate and extend the system's functionality.
