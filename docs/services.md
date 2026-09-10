# Service Layer Documentation

## Overview

The service layer contains the core business logic for the Pusti-Tusti Meal Planner application. This layer orchestrates complex operations involving nutrition calculations, meal planning algorithms, and user profile management. The services act as intermediaries between the UI layer and the data access layer (DAL).

## Architecture

```
┌─────────────────────────────────────────────────┐
│             UI Layer (Controllers)              │
└─────────────────┬───────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────┐
│             Service Layer                       │
│  ┌─────────────────────────────────────────┐   │
│  │ ProfileService                          │   │
│  │ NutritionCalculatorService              │   │
│  │ MealPlannerService                      │   │
│  └─────────────────────────────────────────┘   │
└─────────────────┬───────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────┐
│         Data Access Layer (DAL)                 │
└─────────────────────────────────────────────────┘
```

---

## Table of Contents

1. [ProfileService](#profileservice)
2. [NutritionCalculatorService](#nutritioncalculatorservice)
3. [MealPlannerService](#mealplannerservice)
4. [Design Patterns](#design-patterns)
5. [Usage Examples](#usage-examples)

---

## ProfileService

### Purpose
Manages user profile persistence and retrieval, supporting both individual profiles and family profile management. Profiles are stored in SQLite through a repository layer.

### Key Features
- Single profile management (legacy support)
- Family profile management (multiple user profiles)
- SQLite-backed persistence using a repository layer
- JSON serialization/deserialization using Jackson for nested fields

### Core Methods

#### Single Profile Operations

##### `getCurrentProfile()`
```java
public Optional<UserProfile> getCurrentProfile()
```
**Description:** Retrieves the currently active user profile.  
**Returns:** `Optional<UserProfile>` - The current profile if exists  
**Usage:**
```java
ProfileService profileService = new ProfileService();
Optional<UserProfile> profile = profileService.getCurrentProfile();
profile.ifPresent(p -> System.out.println("Current user: " + p.getName()));
```

##### `setCurrentProfile(UserProfile profile)`
```java
public void setCurrentProfile(UserProfile profile)
```
**Description:** Sets the active user profile.  
**Parameters:**
- `profile` - The profile to set as current  

**Usage:**
```java
UserProfile newProfile = new UserProfile();
newProfile.setName("John Doe");
profileService.setCurrentProfile(newProfile);
```

##### `loadProfileFromFile()`
```java
public Optional<UserProfile> loadProfileFromFile()
```
**Description:** Removed. Profiles are now loaded from SQLite via `loadFamilyProfiles()`.

##### `saveProfileToFile(UserProfile profile)`
```java
public boolean saveProfileToFile(UserProfile profile)
```
**Description:** Removed. Use `addFamilyProfile()` or `updateFamilyProfile()`.

##### `deleteProfileFile()`
```java
public boolean deleteProfileFile()
```
**Description:** Removed. Use `deleteFamilyProfile()`.

#### Family Profile Operations

##### `loadFamilyProfiles()`
```java
public List<UserProfile> loadFamilyProfiles()
```
**Description:** Loads all family member profiles from SQLite.  
**Returns:** `List<UserProfile>` - List of all family profiles  
**Usage:**
```java
List<UserProfile> family = profileService.loadFamilyProfiles();
System.out.println("Family has " + family.size() + " members");
```

##### `saveFamilyProfiles(List<UserProfile> profiles)`
```java
public boolean saveFamilyProfiles(List<UserProfile> profiles)
```
**Description:** Saves all family profiles to the file system.  
**Parameters:**
- `profiles` - List of profiles to save  

**Returns:** `true` if saved successfully  
**Usage:**
```java
List<UserProfile> familyProfiles = new ArrayList<>();
familyProfiles.add(profile1);
familyProfiles.add(profile2);
profileService.saveFamilyProfiles(familyProfiles);
```

##### `addFamilyProfile(UserProfile profile)`
```java
public boolean addFamilyProfile(UserProfile profile)
```
**Description:** Adds a new profile to the family. Generates UUID if not present. Validates for duplicate names.  
**Parameters:**
- `profile` - The profile to add  

**Returns:** `true` if added successfully, `false` if duplicate name exists  
**Validation:** Checks for existing profiles with the same name (case-insensitive)  
**Usage:**
```java
UserProfile childProfile = new UserProfile();
childProfile.setName("Alice");
boolean added = profileService.addFamilyProfile(childProfile);
```

##### `updateFamilyProfile(UserProfile profile)`
```java
public boolean updateFamilyProfile(UserProfile profile)
```
**Description:** Updates an existing family member's profile. Adds as new if profile ID not found.  
**Parameters:**
- `profile` - The profile to update (must have userId set)  

**Returns:** `true` if updated successfully  
**Usage:**
```java
profile.setWeightKg(75.0);
profileService.updateFamilyProfile(profile);
```

##### `deleteFamilyProfile(String profileId)`
```java
public boolean deleteFamilyProfile(String profileId)
```
**Description:** Removes a family member's profile by ID.  
**Parameters:**
- `profileId` - UUID of the profile to delete  

**Returns:** `true` if deleted successfully  
**Usage:**
```java
profileService.deleteFamilyProfile("550e8400-e29b-41d4-a716-446655440000");
```

##### `getFamilyProfileById(String profileId)`
```java
public Optional<UserProfile> getFamilyProfileById(String profileId)
```
**Description:** Retrieves a specific family member's profile by ID.  
**Parameters:**
- `profileId` - UUID of the profile  

**Returns:** `Optional<UserProfile>` - The profile if found  

### Data Format
Profiles are stored as JSON with pretty-printing enabled:
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "John Doe",
  "age": 35,
  "gender": "MALE",
  "heightCm": 175.0,
  "weightKg": 75.0,
  "activityLevel": 3,
  "primaryHealthGoal": "WEIGHT_LOSS",
  "medicalConditions": ["NONE"],
  "allergies": ["peanuts"],
  "mealPreferences": {
    "BREAKFAST": ["RUTI_VEGETABLES", "FRUIT_MILK_YOGURT"],
    "LUNCH": ["RICE_FISH_VEGETABLES"],
    "SNACKS": ["Savoury_Fried", "Juice"]
  }
}
```

---

## NutritionCalculatorService

### Purpose
Calculates personalized nutritional requirements based on user demographics, health goals, and medical conditions. Implements scientifically-backed formulas for BMR, TDEE, and macronutrient distribution.

### Key Features
- BMI calculation
- Age-appropriate BMR calculation (Schofield for children, Mifflin-St Jeor for adults)
- Activity-adjusted TDEE calculation
- Goal-specific calorie adjustment
- Macronutrient distribution based on health goals
- Micronutrient goal setting based on conditions
- Medical condition-specific nutritional limits

### Core Method

##### `calculateNutritionalNeeds(UserProfile user)`
```java
public NutritionProfile calculateNutritionalNeeds(UserProfile user)
```
**Description:** Main entry point that calculates comprehensive nutritional requirements for a user.  
**Parameters:**
- `user` - Complete user profile with demographics and health information  

**Returns:** `NutritionProfile` object containing:
- BMI (Body Mass Index)
- BMR (Basal Metabolic Rate)
- TDEE (Total Daily Energy Expenditure)
- Target Calories
- Macronutrient goals (protein, carbs, fat in grams)
- Micronutrient goals (vitamins, minerals)
- Micronutrient limits (e.g., sodium restrictions)

**Calculation Flow:**
```
1. Calculate BMI
2. Calculate BMR (age-appropriate formula)
3. Calculate TDEE (BMR × activity multiplier)
4. Adjust calories for health goal
5. Distribute macronutrients
6. Set micronutrient goals
7. Set micronutrient limits
```

**Usage:**
```java
NutritionCalculatorService calculator = new NutritionCalculatorService();
UserProfile user = getUserProfile();
NutritionProfile nutrition = calculator.calculateNutritionalNeeds(user);

System.out.println("Daily Calories: " + nutrition.getTargetCalories());
System.out.println("Protein: " + nutrition.getProteinGrams() + "g");
System.out.println("Carbs: " + nutrition.getCarbsGrams() + "g");
System.out.println("Fat: " + nutrition.getFatGrams() + "g");
```

### Internal Calculation Methods

##### `calculateBMI(double heightCm, double weightKg)`
**Formula:** BMI = weight(kg) / [height(m)]²  
**Returns:** BMI value  

##### `calculateBMR(UserProfile user)`
**Description:** Selects appropriate BMR formula based on age.  
**Formulas Used:**
- **Children (≤18):** Schofield equation
- **Adults (>18):** Mifflin-St Jeor equation  

**Schofield Equation (Children):**
- Male 0-3: 59.512W - 30.4
- Male 3-10: 22.706W + 504.3
- Male 10-18: 17.686W + 658.2
- Female 0-3: 58.317W - 31.1
- Female 3-10: 20.315W + 485.9
- Female 10-18: 13.384W + 692.6

**Mifflin-St Jeor Equation (Adults):**
- Male: 10W + 6.25H - 5A + 5
- Female: 10W + 6.25H - 5A - 161
  - W = weight (kg), H = height (cm), A = age (years)

##### `calculateTDEE(double bmr, int activityLevel)`
**Description:** Calculates Total Daily Energy Expenditure by applying activity multiplier.  
**Activity Multipliers:**
- Level 1 (Sedentary): 1.2
- Level 2 (Lightly Active): 1.375
- Level 3 (Moderately Active): 1.55
- Level 4 (Very Active): 1.725
- Level 5 (Extremely Active): 1.9

**Formula:** TDEE = BMR × Activity Multiplier

##### `adjustCaloriesForHealthGoal(double tdee, HealthGoal healthGoal)`
**Description:** Adjusts TDEE based on health objectives.  
**Adjustments:**

| Health Goal | Adjustment | Calculation |
|------------|-----------|-------------|
| WEIGHT_LOSS | -20% | TDEE × 0.8 |
| WEIGHT_GAIN | +20% | TDEE × 1.2 |
| MUSCLE_GAIN | +10% + 300 | TDEE × 1.1 + 300 |
| DIABETES_CONTROL | -10% | TDEE × 0.9 |
| PREGNANCY_NUTRITION | +450 kcal | TDEE + 450 |
| HYPERTENSION_CONTROL | -10% | TDEE × 0.9 |
| MAINTENANCE | No change | TDEE × 1.0 |

##### `calculateMacronutrients(double targetCalories, HealthGoal healthGoal)`
**Description:** Distributes calories into macronutrient targets.  
**Macronutrient Percentages:**

| Health Goal | Protein | Fat | Carbs |
|------------|---------|-----|-------|
| WEIGHT_LOSS | 30% | 25% | 45% |
| WEIGHT_GAIN | 20% | 35% | 45% |
| MUSCLE_GAIN | 35% | 25% | 40% |
| DIABETES_CONTROL | 25% | 30% | 45% |
| PREGNANCY_NUTRITION | 20% | 30% | 50% |
| CHILD_NUTRITION | 17.5% | 32.5% | 52.5% |
| ELDERLY_NUTRITION | 25% | 30% | 45% |
| MAINTENANCE | 20% | 30% | 50% |

**Conversion to Grams:**
- Protein: calories / 4 (4 calories per gram)
- Fat: calories / 9 (9 calories per gram)
- Carbohydrates: calories / 4 (4 calories per gram)

##### `calculateMicronutrientGoals(UserProfile user)`
**Description:** Sets micronutrient targets based on health needs.  
**Base Requirements:**
- Vitamin C: 90 mg
- Calcium: 1000 mg (1200 mg for age ≥65)
- Iron: 18 mg (female), 8 mg (male)
- Magnesium: 420 mg
- Potassium: 4700 mg
- Zinc: 11 mg
- Vitamin B12: 2.4 mcg
- Folate: 400 mcg
- Dietary Fiber: 25 g

**Health Goal-Specific Additions:**
- **WEIGHT_LOSS:** Increased fiber (30g), standard iron
- **MUSCLE_GAIN:** Magnesium (400mg), Zinc (11mg)
- **DIABETES_CONTROL:** Chromium (35mcg), Magnesium (420mg)
- **PREGNANCY_NUTRITION:** Folate (600mcg), Iron (27mg), Calcium (1000mg)
- **ELDERLY_NUTRITION:** B12 (2.4mcg), Calcium (1200mg)
- **ANEMIA_CONTROL:** Iron (18mg female/8mg male), Vitamin C (90mg)

##### `calculateMicronutrientLimits(UserProfile user)`
**Description:** Sets upper limits for certain nutrients based on medical conditions.  
**Medical Condition Limits:**
- **HYPERTENSION:** Sodium ≤ 1500 mg/day (strict)
- **DIABETES:** Sodium ≤ 2300 mg/day (moderate)

### Example Calculation

```java
// Sample user profile
UserProfile user = new UserProfile();
user.setAge(30);
user.setGender(Gender.MALE);
user.setHeightCm(175.0);
user.setWeightKg(75.0);
user.setActivityLevel(3); // Moderately active
user.setPrimaryHealthGoal(HealthGoal.MUSCLE_GAIN);

// Calculate nutrition
NutritionCalculatorService calculator = new NutritionCalculatorService();
NutritionProfile nutrition = calculator.calculateNutritionalNeeds(user);

// Results:
// BMI: 24.49 (75 / (1.75)²)
// BMR: 1706 kcal (Mifflin-St Jeor)
// TDEE: 2644 kcal (1706 × 1.55)
// Target Calories: 3208 kcal (2644 × 1.1 + 300)
// Protein: 281g (35% of 3208)
// Fat: 89g (25% of 3208)
// Carbs: 321g (40% of 3208)
```

---

## MealPlannerService

### Purpose
The most complex service in the application. Generates intelligent, personalized weekly meal plans using advanced algorithms for recipe selection, portion control, nutritional balance, and variety management.

### Key Features
- **Intelligent Recipe Selection:** Multi-pass filtering with boolean tag optimization
- **Portion Control:** Dynamic portion calculation to meet nutritional targets
- **Variety Management:** Cooldown periods and rotation algorithms
- **Family Meal Planning:** Individual plans for each family member
- **Nutritional Accuracy:** Precise macro/micronutrient tracking
- **Efficiency:** Optimized database queries (4 queries for 7-day plan)
- **Compatibility Analysis:** Identifies shared meal opportunities

### Architecture

```
┌──────────────────────────────────────────────────┐
│      generateNewWeeklyPlan()                     │
│      (Entry Point - Individual)                  │
└───────────────┬──────────────────────────────────┘
                │
                ▼
┌──────────────────────────────────────────────────┐
│   calculateEnhancedDailyTargets()                │
│   (Splits daily nutrition into 4 meals)          │
└───────────────┬──────────────────────────────────┘
                │
                ▼
┌──────────────────────────────────────────────────┐
│   generateWeeklyMealsForType() × 3               │
│   (Breakfast, Snacks - Single DB call each)      │
└───────────────┬──────────────────────────────────┘
                │
                ▼
┌──────────────────────────────────────────────────┐
│   generateWeeklyLunchAndDinnerMeals()            │
│   (Ensures 100% different recipes)               │
└───────────────┬──────────────────────────────────┘
                │
                ▼
┌──────────────────────────────────────────────────┐
│   Assemble 7 DailyMealPlan objects               │
│   Return WeeklyMealPlan                          │
└──────────────────────────────────────────────────┘
```

### Constants

```java
// Meal Distribution Percentages
private static final double BREAKFAST_PERCENTAGE = 0.25;  // 25%
private static final double LUNCH_PERCENTAGE = 0.35;      // 35%
private static final double DINNER_PERCENTAGE = 0.30;     // 30%
private static final double SNACKS_PERCENTAGE = 0.10;     // 10%

// Variety Control
private static final int RECIPE_COOLDOWN_DAYS = 3;        // Main meals
private static final int SNACKS_COOLDOWN_DAYS = 2;        // Snacks

// Nutritional Tolerances
private static final double CALORIE_TOLERANCE = 0.15;     // ±15%
private static final double MACRO_TOLERANCE = 0.20;       // ±20%

// Performance Optimization
private static final long CACHE_DURATION = 300000;        // 5 minutes
```

### Core Methods

#### Weekly Meal Plan Generation

##### `generateNewWeeklyPlan(UserProfile user, NutritionProfile nutritionProfile)`
```java
public WeeklyMealPlan generateNewWeeklyPlan(UserProfile user, 
                                            NutritionProfile nutritionProfile)
```
**Description:** Main entry point for generating a complete 7-day meal plan for an individual user.  

**Algorithm:**
1. Calculate enhanced daily nutritional targets for each meal type
2. Generate 7 breakfast meals (1 DB call)
3. Generate 7 lunch and dinner meals together (1 DB call for 14 meals)
4. Generate 7 snack meals (1 DB call)
5. Assemble daily plans with accurate nutrition tracking
6. Return complete weekly plan

**Efficiency:** Total of 4 database calls for entire week

**Parameters:**
- `user` - User profile with preferences and restrictions
- `nutritionProfile` - Calculated nutritional requirements

**Returns:** `WeeklyMealPlan` containing 7 `DailyMealPlan` objects

**Example:**
```java
MealPlannerService planner = new MealPlannerService(recipeDAO);
UserProfile user = loadUserProfile();
NutritionProfile nutrition = calculator.calculateNutritionalNeeds(user);

WeeklyMealPlan weeklyPlan = planner.generateNewWeeklyPlan(user, nutrition);

for (DailyMealPlan day : weeklyPlan.getDailyPlans()) {
    System.out.println("Date: " + day.getDate());
    System.out.println("Breakfast: " + day.getBreakfast().getRecipes().get(0).getNameEn());
    System.out.println("Lunch: " + day.getLunch().getRecipes().get(0).getNameEn());
    System.out.println("Dinner: " + day.getDinner().getRecipes().get(0).getNameEn());
    System.out.println("Snacks: " + day.getSnacks().getRecipes().get(0).getNameEn());
    System.out.println("Total Calories: " + day.getTotalNutrition().getEnergyKcal());
}
```

##### `generateWeeklyMealsForType(MealType mealType, EnhancedNutritionTargets targets, UserProfile user, int numDays)`
```java
private List<EnhancedMeal> generateWeeklyMealsForType(
    MealType mealType,
    EnhancedNutritionTargets targets, 
    UserProfile user, 
    int numDays)
```
**Description:** Generates meals for a specific meal type for the entire week with a single database call.

**Algorithm:**
1. Fetch all candidate recipes for meal type (1 DB call)
2. Extend with fallback recipes if needed
3. For each day:
   - Filter out already-used recipes
   - Select best recipe using rotation strategy
   - Calculate optimal portion size
   - Adjust nutrition based on portion
   - Track recipe usage

**Parameters:**
- `mealType` - Type of meal (BREAKFAST, SNACKS)
- `targets` - Nutritional targets for this meal
- `user` - User profile with preferences
- `numDays` - Number of days (typically 7)

**Returns:** List of `EnhancedMeal` objects

**Variety Strategy:**
- Day % 3 == 0: Focus on nutrition match
- Day % 3 == 1: Focus on user preferences
- Day % 3 == 2: Weighted random selection

##### `generateWeeklyLunchAndDinnerMeals(EnhancedNutritionTargets lunchTargets, EnhancedNutritionTargets dinnerTargets, UserProfile user, int numDays)`
```java
private LunchDinnerMealPair generateWeeklyLunchAndDinnerMeals(
    EnhancedNutritionTargets lunchTargets,
    EnhancedNutritionTargets dinnerTargets, 
    UserProfile user, 
    int numDays)
```
**Description:** Special method that generates lunch and dinner meals together to ensure 100% different recipes between them.

**Algorithm:**
1. Fetch 14 unique recipes from lunch category (1 DB call)
2. Extend with dinner recipes if needed
3. Shuffle for variety
4. Split: First 7 for lunch, next 7 for dinner
5. For each day:
   - Select best lunch recipe from first half
   - Select best dinner recipe from second half (guaranteed different)
   - Calculate portions and nutrition for each

**Innovation:** Ensures lunch and dinner are always different recipes every day

**Returns:** `LunchDinnerMealPair` containing both lists

#### Family Meal Planning

##### `generateFamilyPlan(List<UserProfile> allMembers)`
```java
public FamilyMealPlan generateFamilyPlan(List<UserProfile> allMembers)
```
**Description:** Generates individualized meal plans for each family member, analyzes compatibility, and creates consolidated shopping lists.

**Algorithm:**
1. For each family member:
   - Calculate individual nutrition profile
   - Generate personalized weekly meal plan
   - Store individual plan
2. Analyze meal compatibility across family
3. Generate consolidated shopping list
4. Return complete family plan

**Parameters:**
- `allMembers` - List of all family member profiles

**Returns:** `FamilyMealPlan` with individual plans for each member

**Features:**
- Individual customization based on age, gender, goals, conditions
- Compatibility analysis (identifies shared meal opportunities)
- Consolidated shopping list (aggregates all ingredients)
- Member-specific notes and recommendations

**Example:**
```java
List<UserProfile> family = new ArrayList<>();
family.add(fatherProfile);    // MUSCLE_GAIN
family.add(motherProfile);    // PREGNANCY_NUTRITION
family.add(childProfile);     // CHILD_NUTRITION

FamilyMealPlan familyPlan = planner.generateFamilyPlan(family);

// Access individual plans
WeeklyMealPlan fatherPlan = familyPlan.getIndividualMealPlans().get("Father");
WeeklyMealPlan motherPlan = familyPlan.getIndividualMealPlans().get("Mother");

// Get shopping list
Map<String, Integer> shoppingList = familyPlan.getConsolidatedShoppingList();
```

### Recipe Selection Algorithm

#### Tag-Based Priority System

The service uses a sophisticated tag-based priority matrix for recipe selection:

**Tag Priority Classes:**
1. **Required Tags (Must Have):** Medical condition-specific tags
2. **Preferred Tags (High Priority):** Health goal-specific tags
3. **Prohibited Tags (Instant Disqualification):** Contraindicated ingredients
4. **Scoring Tags:** Weighted bonus system (0-3 points)

##### `getTagPriority(UserProfile user, MealType mealType)`
```java
private TagPriority getTagPriority(UserProfile user, MealType mealType)
```
**Description:** Builds tag priority structure based on user context.

**Medical Condition Tags:**

| Condition | Required Tags | Prohibited Tags | Score |
|-----------|--------------|-----------------|-------|
| DIABETES | is_diabetic_friendly, is_low_carb, is_high_fibre | is_high_carb, is_sweet | 3 |
| HYPERTENSION | is_healthy | - | 2 |
| PREGNANCY | is_vitamin_rich, is_omega_3, is_high_protein | - | 3 |
| ANEMIA | is_iron_rich, is_vitamin_rich | - | 3 |
| HIGH_CHOLESTEROL | is_low_fat, is_high_fibre | - | 3 |

**Health Goal Tags:**

| Goal | Preferred Tags | Score |
|------|---------------|-------|
| WEIGHT_LOSS | is_low_calorie, is_high_protein, is_low_carb | 3 |
| MUSCLE_GAIN | is_high_protein, is_milk_based, is_omega_3 | 3 |
| CHILD_NUTRITION | is_kid_friendly, is_milk_based, is_healthy | 3 |

#### Multi-Pass Recipe Filtering

##### `findCandidateRecipes(MealType mealType, List<String> subcategories, TagPriority tagPriority, List<String> allergies)`
```java
private List<Recipe> findCandidateRecipes(
    MealType mealType,
    List<String> subcategories,
    TagPriority tagPriority, 
    List<String> allergies)
```
**Description:** Uses multi-pass filtering strategy to find suitable recipes.

**Pass Hierarchy:**
1. **First Pass:** Medical-required tags (strict)
2. **Second Pass:** Goal-priority tags (relaxed)
3. **Third Pass:** Basic healthy options
4. **Fourth Pass:** Staple foods
5. **Fifth Pass:** Any recipe for meal type (last resort)

**Boolean Column Optimization:**  
Uses database boolean columns for efficient tag queries:
```sql
SELECT * FROM breakfast 
WHERE subcategory IN (?, ?, ...) 
AND is_diabetic_friendly = 1 
AND is_low_carb = 1
```

#### Recipe Scoring

##### `calculateOptimizedScore(Recipe recipe, NutritionTargets targets, Set<String> usedRecipeIds, Map<String, Integer> tagScores, int currentDay)`
```java
private double calculateOptimizedScore(
    Recipe recipe,
    NutritionTargets targets,
    Set<String> usedRecipeIds, 
    Map<String, Integer> tagScores, 
    int currentDay)
```
**Description:** Comprehensive scoring algorithm combining multiple factors.

**Score Components:**
```
Total Score = Nutrition Score 
            + Variety Penalty 
            - Tag Bonus 
            + Random Factor
```

**Component Details:**
- **Nutrition Score:** Weighted penalty for macro deviation
  ```
  Score = |calories_diff| × 2.0 
        + |protein_diff| × 1.5 
        + |carbs_diff| × 1.0 
        + |fat_diff| × 1.0
  ```
- **Variety Penalty:** 500 points if recipe recently used
- **Tag Bonus:** 10 points × tag_score for matching boolean tags
- **Random Factor:** Small Gaussian noise for variety

**Lower Score = Better Match**

### Portion Control

##### `calculateOptimalPortion(Recipe recipe, EnhancedNutritionTargets targets)`
```java
private double calculateOptimalPortion(Recipe recipe, 
                                       EnhancedNutritionTargets targets)
```
**Description:** Calculates precise portion size to match nutritional targets.

**Algorithm:**
```java
calorieMultiplier = targetCalories / recipeCalories;
calorieMultiplier = clamp(calorieMultiplier, 0.25, 4.0);  // Safety bounds
portionGrams = 100.0 * calorieMultiplier;
```

**Example:**
- Target: 500 kcal
- Recipe: 200 kcal per 100g
- Multiplier: 500 / 200 = 2.5
- Portion: 100g × 2.5 = 250g

##### `calculateAdjustedNutrition(Recipe recipe, double portionGrams)`
```java
private NutritionInfo calculateAdjustedNutrition(Recipe recipe, 
                                                 double portionGrams)
```
**Description:** Scales recipe nutrition based on calculated portion.

**Formula:**
```java
multiplier = portionGrams / 100.0;
adjustedCalories = baseCalories × multiplier;
adjustedProtein = baseProtein × multiplier;
// ... etc for all nutrients
```

### Variety Management

#### Cooldown System
Prevents recipe repetition by tracking usage:

```java
Map<String, Integer> recipeCooldown = new HashMap<>();

// After selecting a recipe:
int cooldownDays = (mealType == SNACKS) ? 2 : 3;
recipeCooldown.put(recipe.getRecipeId(), cooldownDays);

// Each day, decrement cooldowns:
cooldown.entrySet().forEach(entry -> entry.setValue(entry.getValue() - 1));
```

#### Subcategory Rotation

##### `getRotatedSubcategories(MealType mealType, UserProfile user, int dayIndex)`
```java
private List<String> getRotatedSubcategories(MealType mealType, 
                                             UserProfile user, 
                                             int dayIndex)
```
**Description:** Rotates through meal subcategories to ensure variety.

**Algorithm:**
```java
startIndex = dayIndex % subcategories.size();
rotated = [subcategories[startIndex], 
           subcategories[(startIndex+1) % size],
           subcategories[(startIndex+2) % size],
           ...];
```

**Example (Snacks):**
- Day 1: [Savoury_Fried, Steamed_Boiled, Juice, ...]
- Day 2: [Steamed_Boiled, Juice, Smoothie, ...]
- Day 3: [Juice, Smoothie, Sweet_Snacks_Pitha, ...]

### Family Features

#### Compatibility Analysis

##### `analyzeFamilyMealCompatibility(FamilyMealPlan familyPlan, List<UserProfile> allMembers)`
```java
private void analyzeFamilyMealCompatibility(
    FamilyMealPlan familyPlan,
    List<UserProfile> allMembers)
```
**Description:** Identifies meals that can be shared among family members.

**Process:**
1. For each day and meal type:
   - Collect each member's recipe
   - Group by recipe name
2. If 2+ members have same recipe:
   - Flag as "shared meal opportunity"
   - Add note to family plan

**Output Example:**
```
Day 1 - LUNCH: 'Rice with Fish Curry' can be shared by Father, Mother
Day 3 - DINNER: 'Chicken Biryani' can be shared by all members
```

#### Consolidated Shopping List

##### `generatePersonalizedShoppingList(FamilyMealPlan familyPlan)`
```java
private void generatePersonalizedShoppingList(FamilyMealPlan familyPlan)
```
**Description:** Aggregates ingredients from all family members' meal plans.

**Algorithm:**
```java
for each family member:
    for each daily plan:
        for each meal:
            for each recipe:
                for each ingredient:
                    shoppingList.merge(ingredientName, quantity, Integer::sum);
```

**Scaling:** Quantities automatically scaled by family size

**Example Output:**
```java
{
    "Rice": 7000,           // grams (7kg for family of 4 for week)
    "Chicken": 2100,        // grams
    "Tomatoes": 1400,       // grams
    "Spinach": 700,         // grams
    ...
}
```

---

## Design Patterns

### 1. Service Layer Pattern
All services act as facades to complex business logic, providing simple interfaces to UI layer.

### 2. Strategy Pattern
Different calculation strategies based on user attributes:
- BMR: Schofield vs. Mifflin-St Jeor
- Macros: Different distributions per health goal
- Recipe Selection: Multi-pass filtering strategies

### 3. Builder Pattern
Gradual construction of complex objects:
- `NutritionProfile` built step-by-step
- `WeeklyMealPlan` assembled from daily plans

### 4. Repository Pattern
`ProfileService` abstracts file storage details, providing clean CRUD operations.

### 5. Factory Pattern
`EnhancedMealTargets` and related classes constructed via factory methods.

---

## Usage Examples

### Complete Meal Planning Workflow

```java
// 1. Initialize services
RecipeDAO recipeDAO = DAOFactory.getRecipeDAO();
ProfileService profileService = new ProfileService();
NutritionCalculatorService nutritionService = new NutritionCalculatorService();
MealPlannerService mealPlanner = new MealPlannerService(recipeDAO);

// 2. Load or create user profile
UserProfile user = new UserProfile();
user.setName("John Doe");
user.setAge(35);
user.setGender(Gender.MALE);
user.setHeightCm(175.0);
user.setWeightKg(75.0);
user.setActivityLevel(3);  // Moderately active
user.setPrimaryHealthGoal(HealthGoal.WEIGHT_LOSS);
user.setMedicalConditions(Arrays.asList(MedicalCondition.NONE));
user.setAllergies(Arrays.asList("peanuts"));

// Set meal preferences
Map<MealType, List<String>> prefs = new HashMap<>();
prefs.put(MealType.BREAKFAST, Arrays.asList("RUTI_VEGETABLES", "FRUIT_MILK_YOGURT"));
prefs.put(MealType.LUNCH, Arrays.asList("RICE_FISH_VEGETABLES", "RICE_MEAT_VEGETABLES"));
prefs.put(MealType.SNACKS, Arrays.asList("Savoury_Fried", "Juice", "Fruits"));
user.setMealPreferences(prefs);

// 3. Save profile
profileService.saveProfileToFile(user);

// 4. Calculate nutrition requirements
NutritionProfile nutrition = nutritionService.calculateNutritionalNeeds(user);
System.out.println("BMI: " + nutrition.getBmi());
System.out.println("Daily Calories: " + nutrition.getTargetCalories());
System.out.println("Protein: " + nutrition.getProteinGrams() + "g");
System.out.println("Carbs: " + nutrition.getCarbsGrams() + "g");
System.out.println("Fat: " + nutrition.getFatGrams() + "g");

// 5. Generate weekly meal plan
WeeklyMealPlan weeklyPlan = mealPlanner.generateNewWeeklyPlan(user, nutrition);

// 6. Display meal plan
for (int i = 0; i < weeklyPlan.getDailyPlans().size(); i++) {
    DailyMealPlan day = weeklyPlan.getDailyPlans().get(i);
    System.out.println("\n=== Day " + (i + 1) + " ===");
    System.out.println("Breakfast: " + day.getBreakfast().getRecipes().get(0).getNameEn());
    System.out.println("  Calories: " + day.getBreakfast().getNutrition().getEnergyKcal());
    
    System.out.println("Lunch: " + day.getLunch().getRecipes().get(0).getNameEn());
    System.out.println("  Calories: " + day.getLunch().getNutrition().getEnergyKcal());
    
    System.out.println("Dinner: " + day.getDinner().getRecipes().get(0).getNameEn());
    System.out.println("  Calories: " + day.getDinner().getNutrition().getEnergyKcal());
    
    System.out.println("Snacks: " + day.getSnacks().getRecipes().get(0).getNameEn());
    System.out.println("  Calories: " + day.getSnacks().getNutrition().getEnergyKcal());
    
    System.out.println("Total Daily Calories: " + day.getTotalNutrition().getEnergyKcal());
}
```

### Family Meal Planning

```java
// 1. Create family profiles
UserProfile father = new UserProfile();
father.setName("Father");
father.setAge(40);
father.setGender(Gender.MALE);
father.setHeightCm(178);
father.setWeightKg(80);
father.setPrimaryHealthGoal(HealthGoal.MUSCLE_GAIN);

UserProfile mother = new UserProfile();
mother.setName("Mother");
mother.setAge(35);
mother.setGender(Gender.FEMALE);
mother.setHeightCm(165);
mother.setWeightKg(60);
mother.setPrimaryHealthGoal(HealthGoal.PREGNANCY_NUTRITION);
mother.setMedicalConditions(Arrays.asList(MedicalCondition.PREGNANCY));

UserProfile child = new UserProfile();
child.setName("Child");
child.setAge(8);
child.setGender(Gender.MALE);
child.setHeightCm(130);
child.setWeightKg(28);
child.setPrimaryHealthGoal(HealthGoal.CHILD_NUTRITION);
child.setMedicalConditions(Arrays.asList(MedicalCondition.CHILD));

// 2. Save family profiles
ProfileService profileService = new ProfileService();
profileService.addFamilyProfile(father);
profileService.addFamilyProfile(mother);
profileService.addFamilyProfile(child);

// 3. Generate family meal plan
List<UserProfile> family = Arrays.asList(father, mother, child);
FamilyMealPlan familyPlan = mealPlanner.generateFamilyPlan(family);

// 4. Access individual plans
WeeklyMealPlan fatherPlan = familyPlan.getIndividualMealPlans().get("Father");
WeeklyMealPlan motherPlan = familyPlan.getIndividualMealPlans().get("Mother");
WeeklyMealPlan childPlan = familyPlan.getIndividualMealPlans().get("Child");

System.out.println("Father's Day 1 Breakfast: " + 
    fatherPlan.getDailyPlans().get(0).getBreakfast().getRecipes().get(0).getNameEn());
System.out.println("Mother's Day 1 Breakfast: " + 
    motherPlan.getDailyPlans().get(0).getBreakfast().getRecipes().get(0).getNameEn());
System.out.println("Child's Day 1 Breakfast: " + 
    childPlan.getDailyPlans().get(0).getBreakfast().getRecipes().get(0).getNameEn());

// 5. Get consolidated shopping list
Map<String, Integer> shoppingList = familyPlan.getConsolidatedShoppingList();
System.out.println("\nWeekly Shopping List:");
shoppingList.forEach((ingredient, quantity) -> {
    System.out.println(ingredient + ": " + quantity + "g");
});

// 6. Check meal compatibility
System.out.println("\nShared Meal Opportunities:");
// (Compatibility notes are automatically added during generation)
```

### Profile Management

```java
// Individual profile
ProfileService service = new ProfileService();

// Load existing profile
Optional<UserProfile> existing = service.loadProfileFromFile();
if (existing.isPresent()) {
    System.out.println("Welcome back, " + existing.get().getName());
} else {
    // Create new profile
    UserProfile newUser = createUserProfile();
    service.saveProfileToFile(newUser);
}

// Family profiles
List<UserProfile> family = service.loadFamilyProfiles();
System.out.println("Family members: " + family.size());

// Add new family member
UserProfile newMember = new UserProfile();
newMember.setName("Grandparent");
newMember.setAge(70);
newMember.setPrimaryHealthGoal(HealthGoal.ELDERLY_NUTRITION);
service.addFamilyProfile(newMember);

// Update existing member
UserProfile toUpdate = family.get(0);
toUpdate.setWeightKg(toUpdate.getWeightKg() - 2);  // Lost 2kg!
service.updateFamilyProfile(toUpdate);

// Delete member
service.deleteFamilyProfile(toUpdate.getUserId());
```

---

## Performance Considerations

### Database Query Optimization
- **Recipe caching:** 5-minute cache to reduce repeated queries
- **Batch loading:** Single query per meal type instead of per meal
- **Boolean columns:** Direct database filtering instead of in-memory
- **Connection pooling:** Efficient database connection management

### Memory Management
- **Lazy loading:** Recipes loaded only when needed
- **Stream processing:** Large lists processed with streams
- **Cache invalidation:** Automatic cache refresh after timeout

### Scalability
- **Stateless services:** All services can be instantiated multiple times
- **Thread-safe operations:** Immutable data structures where possible
- **Efficient algorithms:** O(n) complexity for most operations

---

## Error Handling

### ProfileService Errors
- **File I/O exceptions:** Logged to stderr, returns empty Optional
- **Duplicate names:** Returns false with error message
- **Invalid JSON:** Handled gracefully, returns empty list

### NutritionCalculatorService Errors
- **Invalid input:** Defaults to safe values (e.g., sedentary activity)
- **Division by zero:** Protected by validation checks
- **Null references:** Defensive programming with null checks

### MealPlannerService Errors
- **No recipes found:** Multi-level fallback mechanism
- **Database errors:** Caught and logged, uses cached data
- **Insufficient variety:** Relaxes constraints progressively

---

## Testing Recommendations

### Unit Tests
```java
@Test
public void testBMICalculation() {
    NutritionCalculatorService service = new NutritionCalculatorService();
    // Test BMI = 75 / (1.75^2) = 24.49
    double bmi = service.calculateBMI(175, 75);
    assertEquals(24.49, bmi, 0.01);
}

@Test
public void testMealPlanGeneration() {
    UserProfile user = createTestUser();
    NutritionProfile nutrition = calculator.calculateNutritionalNeeds(user);
    WeeklyMealPlan plan = planner.generateNewWeeklyPlan(user, nutrition);
    
    assertEquals(7, plan.getDailyPlans().size());
    assertNotNull(plan.getDailyPlans().get(0).getBreakfast());
}
```

### Integration Tests
- Test complete workflow from profile to meal plan
- Verify database queries return expected results
- Test family plan generation with multiple profiles

### Performance Tests
- Measure meal plan generation time (should be < 5 seconds)
- Test with large recipe database (1000+ recipes)
- Verify cache effectiveness

---

## Future Enhancements

### Potential Improvements
1. **Machine Learning:** Learn from user preferences over time
2. **Seasonal Recipes:** Adjust recommendations based on season
3. **Budget Optimization:** Consider ingredient costs
4. **Leftover Management:** Suggest recipes using leftover ingredients
5. **Batch Cooking:** Identify recipes that can be prepared in bulk
6. **Nutritional Tracking:** Track actual vs. target consumption
7. **Recipe Substitutions:** Suggest alternatives for unavailable ingredients
8. **Collaborative Filtering:** Recommend based on similar users

---

## Glossary

- **BMI:** Body Mass Index - measure of body fat based on height and weight
- **BMR:** Basal Metabolic Rate - calories burned at rest
- **TDEE:** Total Daily Energy Expenditure - total calories burned per day
- **Macronutrients:** Protein, carbohydrates, and fats
- **Micronutrients:** Vitamins and minerals
- **Cooldown Period:** Time before recipe can be selected again
- **Enhanced Meal:** Meal with portion and nutrition calculations
- **Tag Priority:** System for ranking recipe suitability

---

## References

### Scientific Formulas
- Mifflin-St Jeor Equation: [Link](https://pubmed.ncbi.nlm.nih.gov/2305711/)
- Schofield Equation: [Link](https://pubmed.ncbi.nlm.nih.gov/4044297/)
- Macronutrient Guidelines: USDA Dietary Guidelines

### Dependencies
- Jackson (JSON processing): `com.fasterxml.jackson.databind`
- RecipeDAO: Custom data access layer
- Model classes: `com.mealplanner.model.*`

---

## Support

For questions or issues:
- Check code comments in source files
- Review test cases for usage examples
- Refer to model class documentation for data structures

---

**Last Updated:** November 2025  
**Version:** 1.0  
**Maintained by:** Pusti-Tusti Development Team