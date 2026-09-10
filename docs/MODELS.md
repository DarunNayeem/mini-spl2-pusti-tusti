# Data Models Documentation

## Overview
The Pusti-Tusti data model layer defines the core domain objects and data structures that represent users, recipes, meals, and nutrition information. The models follow POJO (Plain Old Java Object) conventions with proper encapsulation, validation, and comprehensive toString() methods for debugging.

## Package Structure

```
com.mealplanner.model/
├── Enumerations/
│   ├── Gender.java
│   ├── HealthGoal.java
│   ├── MealType.java
│   ├── MedicalCondition.java
│   └── DietaryPreference.java
├── user/
│   ├── UserProfile.java
│   ├── NutritionProfile.java
│   └── FamilyMember.java
├── recipe/
│   ├── Recipe.java
│   ├── Ingredient.java
│   └── NutritionInfo.java
└── Meal Planning Models/
    ├── WeeklyMealPlan.java
    ├── DailyMealPlan.java
    ├── Meal.java
    └── FamilyMealPlan.java
```

---

## Enumeration Types

### 1. Gender
**Package**: `com.mealplanner.model`  
**Purpose**: Represents user gender for nutritional calculations

```java
public enum Gender {
    MALE("Male"),
    FEMALE("Female");
    
    private final String displayName;
    
    Gender(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

**Usage**:
- BMR calculations differ by gender
- Nutritional requirements vary between male and female
- Used in user profile creation and validation

### 2. HealthGoal
**Package**: `com.mealplanner.model`  
**Purpose**: Defines user health objectives for meal planning

```java
public enum HealthGoal {
    WEIGHT_LOSS("Weight Loss", -500),           // 500 calorie deficit
    WEIGHT_GAIN("Weight Gain", 500),            // 500 calorie surplus
    MUSCLE_GAIN("Muscle Gain", 300),            // 300 calorie surplus + high protein
    MAINTENANCE("Maintenance", 0),              // No calorie adjustment
    GENERAL_HEALTH("General Health", 0);        // Focus on balanced nutrition
    
    private final String displayName;
    private final int calorieAdjustment;
    
    HealthGoal(String displayName, int calorieAdjustment) {
        this.displayName = displayName;
        this.calorieAdjustment = calorieAdjustment;
    }
    
    public String getDisplayName() { return displayName; }
    public int getCalorieAdjustment() { return calorieAdjustment; }
}
```

**Calorie Adjustments**:
- **WEIGHT_LOSS**: -500 calories/day (approximately 1 lb/week loss)
- **WEIGHT_GAIN**: +500 calories/day (approximately 1 lb/week gain)
- **MUSCLE_GAIN**: +300 calories/day with increased protein focus
- **MAINTENANCE**: No adjustment, maintain current weight
- **GENERAL_HEALTH**: Balanced nutrition without weight change goals

### 3. MealType
**Package**: `com.mealplanner.model`  
**Purpose**: Categorizes meals into daily eating periods

```java
public enum MealType {
    BREAKFAST("Breakfast", 0.25),    // 25% of daily calories
    LUNCH("Lunch", 0.35),            // 35% of daily calories
    DINNER("Dinner", 0.30),          // 30% of daily calories
    SNACKS("Snacks", 0.10);          // 10% of daily calories
    
    private final String displayName;
    private final double caloriePercentage;
    
    MealType(String displayName, double caloriePercentage) {
        this.displayName = displayName;
        this.caloriePercentage = caloriePercentage;
    }
    
    public String getDisplayName() { return displayName; }
    public double getCaloriePercentage() { return caloriePercentage; }
}
```

**Calorie Distribution**:
- Breakfast: 25% (lighter start to the day)
- Lunch: 35% (main energy meal)
- Dinner: 30% (substantial evening meal)
- Snacks: 10% (healthy snacking throughout day)

### 4. MedicalCondition
**Package**: `com.mealplanner.model`  
**Purpose**: Medical conditions affecting nutritional requirements

```java
public enum MedicalCondition {
    DIABETES("Diabetes", "Requires controlled carbohydrate intake"),
    HYPERTENSION("Hypertension", "Requires low sodium diet"),
    HEART_DISEASE("Heart Disease", "Requires low saturated fat diet"),
    KIDNEY_DISEASE("Kidney Disease", "Requires controlled protein and phosphorus"),
    PREGNANCY("Pregnancy", "Requires increased folate, iron, and calories"),
    LACTATION("Lactation", "Requires increased calories and fluid intake"),
    CELIAC_DISEASE("Celiac Disease", "Requires gluten-free diet"),
    FOOD_ALLERGIES("Food Allergies", "Requires allergen avoidance"),
    ANEMIA("Anemia", "Requires increased iron intake"),
    OSTEOPOROSIS("Osteoporosis", "Requires increased calcium and vitamin D");
    
    private final String displayName;
    private final String description;
    
    MedicalCondition(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
```

### 5. DietaryPreference
**Package**: `com.mealplanner.model`  
**Purpose**: User dietary choices and restrictions

```java
public enum DietaryPreference {
    VEGAN("Vegan", "No animal products"),
    VEGETARIAN("Vegetarian", "No meat, fish, or poultry"),
    NON_VEGETARIAN("Non-Vegetarian", "Includes all food types"),
    PESCATARIAN("Pescatarian", "Vegetarian diet including fish"),
    KETO("Ketogenic", "Very low carb, high fat diet"),
    PALEO("Paleo", "Whole foods, no processed foods"),
    GLUTEN_FREE("Gluten-Free", "No wheat, barley, rye, or oats");
    
    private final String displayName;
    private final String description;
    
    DietaryPreference(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
```

---

## User Models

### 1. UserProfile
**Package**: `com.mealplanner.model.user`  
**Purpose**: Complete user information with health goals and preferences

#### Key Fields:
```java
public class UserProfile {
    // Personal Information
    private String name;
    private int age;
    private Gender gender;
    private double heightCm;
    private double weightKg;
    
    // Health and Fitness
    private HealthGoal healthGoal;
    private int activityLevel;        // 1-5 scale
    private Set<MedicalCondition> medicalConditions;
    
    // Dietary Information
    private Set<DietaryPreference> dietaryPreferences;
    private Set<String> allergens;
    private Set<String> dislikedFoods;
    
    // Family Information (Optional)
    private boolean isFamilyHead;
    private List<FamilyMember> familyMembers;
    
    // Metadata
    private String profileId;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
}
```

#### Constructor and Builder Pattern:
```java
// Primary constructor
public UserProfile(String name, int age, Gender gender, double heightCm, 
                  double weightKg, HealthGoal healthGoal, int activityLevel) {
    this.name = name;
    this.age = age;
    this.gender = gender;
    this.heightCm = heightCm;
    this.weightKg = weightKg;
    this.healthGoal = healthGoal;
    this.activityLevel = activityLevel;
    
    // Initialize collections
    this.medicalConditions = new HashSet<>();
    this.dietaryPreferences = new HashSet<>();
    this.allergens = new HashSet<>();
    this.dislikedFoods = new HashSet<>();
    this.familyMembers = new ArrayList<>();
    
    // Generate unique ID and timestamps
    this.profileId = UUID.randomUUID().toString();
    this.createdAt = LocalDateTime.now();
    this.lastModified = LocalDateTime.now();
}

// Builder pattern for complex construction
public static class Builder {
    private UserProfile profile;
    
    public Builder(String name, int age, Gender gender) {
        profile = new UserProfile();
        profile.name = name;
        profile.age = age;
        profile.gender = gender;
    }
    
    public Builder height(double heightCm) {
        profile.heightCm = heightCm;
        return this;
    }
    
    public Builder weight(double weightKg) {
        profile.weightKg = weightKg;
        return this;
    }
    
    public Builder healthGoal(HealthGoal goal) {
        profile.healthGoal = goal;
        return this;
    }
    
    public Builder activityLevel(int level) {
        profile.activityLevel = level;
        return this;
    }
    
    public Builder addMedicalCondition(MedicalCondition condition) {
        profile.medicalConditions.add(condition);
        return this;
    }
    
    public Builder addDietaryPreference(DietaryPreference preference) {
        profile.dietaryPreferences.add(preference);
        return this;
    }
    
    public Builder addAllergen(String allergen) {
        profile.allergens.add(allergen);
        return this;
    }
    
    public UserProfile build() {
        // Validation logic
        if (profile.name == null || profile.name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (profile.age < 1 || profile.age > 120) {
            throw new IllegalArgumentException("Age must be between 1 and 120");
        }
        
        return profile;
    }
}
```

#### Validation Methods:
```java
public boolean isValid() {
    return name != null && !name.trim().isEmpty() &&
           age > 0 && age <= 120 &&
           gender != null &&
           heightCm > 0 && heightCm <= 300 &&
           weightKg > 0 && weightKg <= 500 &&
           healthGoal != null &&
           activityLevel >= 1 && activityLevel <= 5;
}

public List<String> getValidationErrors() {
    List<String> errors = new ArrayList<>();
    
    if (name == null || name.trim().isEmpty()) {
        errors.add("Name is required");
    }
    if (age <= 0 || age > 120) {
        errors.add("Age must be between 1 and 120");
    }
    if (heightCm <= 0 || heightCm > 300) {
        errors.add("Height must be between 1 and 300 cm");
    }
    if (weightKg <= 0 || weightKg > 500) {
        errors.add("Weight must be between 1 and 500 kg");
    }
    if (activityLevel < 1 || activityLevel > 5) {
        errors.add("Activity level must be between 1 and 5");
    }
    
    return errors;
}
```

### 2. NutritionProfile
**Package**: `com.mealplanner.model.user`  
**Purpose**: Calculated nutritional requirements and goals

#### Key Fields:
```java
public class NutritionProfile {
    // Basic Calculations
    private double bmi;                    // Body Mass Index
    private double bmr;                    // Basal Metabolic Rate
    private double tdee;                   // Total Daily Energy Expenditure
    
    // Daily Targets
    private double targetCalories;         // Adjusted daily calories
    private double proteinGrams;           // Daily protein target
    private double carbsGrams;             // Daily carbohydrate target
    private double fatGrams;               // Daily fat target
    private double fiberGrams;             // Daily fiber target
    
    // Micronutrient Goals (expandable map)
    private Map<String, Double> micronutrientGoals;
    private Map<String, Double> micronutrientLimits;
    
    // Medical Condition Adjustments
    private Map<MedicalCondition, Map<String, Double>> conditionAdjustments;
    
    // Metadata
    private UserProfile associatedUser;
    private LocalDateTime calculatedAt;
    private String calculationVersion;     // For tracking calculation changes
}
```

#### Constructor and Calculation Methods:
```java
public NutritionProfile(UserProfile userProfile, double bmi, double bmr, double tdee) {
    this.associatedUser = userProfile;
    this.bmi = bmi;
    this.bmr = bmr;
    this.tdee = tdee;
    
    // Calculate adjusted calories based on health goal
    this.targetCalories = tdee + userProfile.getHealthGoal().getCalorieAdjustment();
    
    // Initialize micronutrient maps
    this.micronutrientGoals = new HashMap<>();
    this.micronutrientLimits = new HashMap<>();
    this.conditionAdjustments = new HashMap<>();
    
    // Calculate macronutrient distribution
    calculateMacronutrientTargets();
    calculateMicronutrientTargets();
    applyMedicalConditionAdjustments();
    
    this.calculatedAt = LocalDateTime.now();
    this.calculationVersion = "2025.1";
}

private void calculateMacronutrientTargets() {
    HealthGoal goal = associatedUser.getHealthGoal();
    
    // Protein calculation (higher for muscle gain)
    double proteinPercentage = goal == HealthGoal.MUSCLE_GAIN ? 0.30 : 0.25;
    this.proteinGrams = (targetCalories * proteinPercentage) / 4; // 4 cal/g
    
    // Fat calculation
    double fatPercentage = 0.25;
    this.fatGrams = (targetCalories * fatPercentage) / 9; // 9 cal/g
    
    // Carbohydrate calculation (remainder)
    double remainingCalories = targetCalories - (proteinGrams * 4) - (fatGrams * 9);
    this.carbsGrams = remainingCalories / 4; // 4 cal/g
    
    // Fiber target (14g per 1000 calories)
    this.fiberGrams = (targetCalories / 1000) * 14;
}

private void calculateMicronutrientTargets() {
    // Age and gender-based micronutrient requirements
    int age = associatedUser.getAge();
    Gender gender = associatedUser.getGender();
    
    // Vitamins
    micronutrientGoals.put("vitamin_a", getVitaminARequirement(age, gender));
    micronutrientGoals.put("vitamin_c", getVitaminCRequirement(age, gender));
    micronutrientGoals.put("vitamin_d", getVitaminDRequirement(age, gender));
    micronutrientGoals.put("vitamin_e", getVitaminERequirement(age, gender));
    micronutrientGoals.put("vitamin_k", getVitaminKRequirement(age, gender));
    
    // B Vitamins
    micronutrientGoals.put("vitamin_b1", getThiamineRequirement(age, gender));
    micronutrientGoals.put("vitamin_b2", getRiboflavinRequirement(age, gender));
    micronutrientGoals.put("vitamin_b3", getNiacinRequirement(age, gender));
    micronutrientGoals.put("vitamin_b6", getB6Requirement(age, gender));
    micronutrientGoals.put("vitamin_b12", getB12Requirement(age, gender));
    micronutrientGoals.put("folate", getFolateRequirement(age, gender));
    
    // Minerals
    micronutrientGoals.put("calcium", getCalciumRequirement(age, gender));
    micronutrientGoals.put("iron", getIronRequirement(age, gender));
    micronutrientGoals.put("magnesium", getMagnesiumRequirement(age, gender));
    micronutrientGoals.put("phosphorus", getPhosphorusRequirement(age, gender));
    micronutrientGoals.put("potassium", getPotassiumRequirement(age, gender));
    micronutrientGoals.put("zinc", getZincRequirement(age, gender));
    micronutrientGoals.put("selenium", getSeleniumRequirement(age, gender));
}
```

### 3. FamilyMember
**Package**: `com.mealplanner.model.user`  
**Purpose**: Extended user profile for family relationships

```java
public class FamilyMember extends UserProfile {
    private String relationshipToHead;     // "Spouse", "Child", "Parent", etc.
    private UserProfile familyHead;        // Reference to family head
    private boolean hasSpecialDiet;        // Requires special dietary attention
    private String specialDietNotes;       // Additional dietary notes
    
    // Child-specific fields
    private boolean isChild;
    private String schoolGrade;            // For children
    private Set<String> schoolFoodRestrictions;
    
    public FamilyMember(UserProfile familyHead, String relationshipToHead,
                       String name, int age, Gender gender, 
                       double heightCm, double weightKg,
                       HealthGoal healthGoal, int activityLevel) {
        super(name, age, gender, heightCm, weightKg, healthGoal, activityLevel);
        this.familyHead = familyHead;
        this.relationshipToHead = relationshipToHead;
        this.isChild = age < 18;
        this.schoolFoodRestrictions = new HashSet<>();
    }
}
```

---

## Recipe Models

### 1. Recipe
**Package**: `com.mealplanner.model.recipe`  
**Purpose**: Complete recipe information with multilingual support

#### Key Fields:
```java
public class Recipe {
    // Basic Information
    private String recipeId;               // Unique identifier
    private String recipeName;             // English name
    private String recipeNameBengali;      // Bengali translation
    private MealType mealType;             // Meal category
    private String subcategory;            // e.g., "Rice Dishes", "Curries"
    
    // Recipe Content
    private List<Ingredient> ingredients;   // Ingredient list with quantities
    private List<String> instructions;      // Step-by-step cooking instructions
    private Set<String> tags;              // Recipe categorization tags
    
    // Timing and Serving
    private int prepTimeMinutes;           // Preparation time
    private int cookingTimeMinutes;        // Cooking time
    private int servings;                  // Default serving size
    
    // Nutritional Information
    private NutritionInfo nutritionInfo;   // Complete nutrition data
    
    // Metadata
    private LocalDateTime createdAt;
    private String source;                 // Recipe source or author
    private double difficulty;             // 1-5 difficulty rating
}
```

#### Constructor and Methods:
```java
public Recipe(String recipeId, String recipeName, MealType mealType) {
    this.recipeId = recipeId;
    this.recipeName = recipeName;
    this.mealType = mealType;
    
    // Initialize collections
    this.ingredients = new ArrayList<>();
    this.instructions = new ArrayList<>();
    this.tags = new HashSet<>();
    
    this.createdAt = LocalDateTime.now();
    this.servings = 4; // Default serving size
}

// Calculate total cooking time
public int getTotalTimeMinutes() {
    return prepTimeMinutes + cookingTimeMinutes;
}

// Get calories per serving
public double getCaloriesPerServing() {
    return nutritionInfo != null ? nutritionInfo.getCalories() / servings : 0;
}

// Check if recipe matches dietary preferences
public boolean matchesDietaryPreferences(Set<DietaryPreference> preferences) {
    for (DietaryPreference pref : preferences) {
        String prefTag = pref.toString().toLowerCase();
        if (!tags.contains(prefTag)) {
            return false;
        }
    }
    return true;
}

// Check for allergens in ingredients
public boolean containsAllergen(String allergen) {
    return ingredients.stream()
            .anyMatch(ingredient -> 
                ingredient.getName().toLowerCase().contains(allergen.toLowerCase()));
}
```

### 2. Ingredient
**Package**: `com.mealplanner.model.recipe`  
**Purpose**: Recipe components with quantities and nutritional data

#### Key Fields:
```java
public class Ingredient {
    // Basic Information
    private String name;                   // English name
    private String nameBengali;            // Bengali translation
    private double quantity;               // Amount required
    private String unit;                   // Measurement unit
    
    // Nutritional Information (per quantity specified)
    private double calories;
    private double protein;
    private double carbohydrates;
    private double fat;
    private double fiber;
    private double sugar;
    private double sodium;
    
    // Micronutrients (expandable map)
    private Map<String, Double> micronutrients;
    
    // Metadata
    private boolean isOptional;            // Optional ingredient
    private String substitutions;          // Possible substitutions
    private String preparationNotes;       // Special preparation instructions
}
```

#### Constructor and Utility Methods:
```java
public Ingredient(String name, double quantity, String unit) {
    this.name = name;
    this.quantity = quantity;
    this.unit = unit;
    this.micronutrients = new HashMap<>();
    this.isOptional = false;
}

// Scale ingredient quantities for different serving sizes
public Ingredient scaleForServings(int originalServings, int targetServings) {
    double scaleFactor = (double) targetServings / originalServings;
    
    Ingredient scaled = new Ingredient(name, quantity * scaleFactor, unit);
    scaled.nameBengali = this.nameBengali;
    scaled.calories = this.calories * scaleFactor;
    scaled.protein = this.protein * scaleFactor;
    scaled.carbohydrates = this.carbohydrates * scaleFactor;
    scaled.fat = this.fat * scaleFactor;
    scaled.fiber = this.fiber * scaleFactor;
    scaled.sugar = this.sugar * scaleFactor;
    scaled.sodium = this.sodium * scaleFactor;
    
    // Scale micronutrients
    this.micronutrients.forEach((nutrient, value) -> 
        scaled.micronutrients.put(nutrient, value * scaleFactor));
    
    return scaled;
}

// Get formatted quantity string
public String getQuantityString() {
    if (quantity == (int) quantity) {
        return String.format("%d %s", (int) quantity, unit);
    } else {
        return String.format("%.1f %s", quantity, unit);
    }
}
```

### 3. NutritionInfo
**Package**: `com.mealplanner.model.recipe`  
**Purpose**: Comprehensive nutrition information with expandable nutrient map

#### Key Fields:
```java
public class NutritionInfo {
    // Macronutrients (per serving)
    private double calories;
    private double protein;                // grams
    private double carbohydrates;          // grams
    private double fat;                    // grams
    private double fiber;                  // grams
    private double sugar;                  // grams
    private double sodium;                 // milligrams
    
    // Expandable micronutrient map (allows for future additions)
    private Map<String, Double> micronutrients;
    
    // Nutritional Quality Indicators
    private double nutritionScore;         // 0-100 overall nutrition score
    private Map<String, String> nutritionGrades; // A-F grades for categories
}
```

#### Standard Micronutrients:
```java
// Initialize standard micronutrients
private void initializeStandardMicronutrients() {
    micronutrients = new HashMap<>();
    
    // Vitamins (in appropriate units)
    micronutrients.put("vitamin_a", 0.0);        // μg RAE
    micronutrients.put("vitamin_c", 0.0);        // mg
    micronutrients.put("vitamin_d", 0.0);        // μg
    micronutrients.put("vitamin_e", 0.0);        // mg
    micronutrients.put("vitamin_k", 0.0);        // μg
    micronutrients.put("vitamin_b1", 0.0);       // mg (Thiamine)
    micronutrients.put("vitamin_b2", 0.0);       // mg (Riboflavin)
    micronutrients.put("vitamin_b3", 0.0);       // mg (Niacin)
    micronutrients.put("vitamin_b6", 0.0);       // mg
    micronutrients.put("vitamin_b12", 0.0);      // μg
    micronutrients.put("folate", 0.0);           // μg DFE
    micronutrients.put("biotin", 0.0);           // μg
    micronutrients.put("pantothenic_acid", 0.0); // mg
    
    // Minerals (in appropriate units)
    micronutrients.put("calcium", 0.0);          // mg
    micronutrients.put("iron", 0.0);             // mg
    micronutrients.put("magnesium", 0.0);        // mg
    micronutrients.put("phosphorus", 0.0);       // mg
    micronutrients.put("potassium", 0.0);        // mg
    micronutrients.put("zinc", 0.0);             // mg
    micronutrients.put("copper", 0.0);           // mg
    micronutrients.put("manganese", 0.0);        // mg
    micronutrients.put("selenium", 0.0);         // μg
    micronutrients.put("iodine", 0.0);           // μg
    micronutrients.put("chromium", 0.0);         // μg
}

// Calculate nutrition score based on nutrient density
public double calculateNutritionScore() {
    double score = 0.0;
    int scoreComponents = 0;
    
    // Positive scoring factors
    if (protein > 0) {
        score += Math.min(protein / 25.0 * 20, 20); // Max 20 points for protein
        scoreComponents++;
    }
    
    if (fiber > 0) {
        score += Math.min(fiber / 10.0 * 15, 15); // Max 15 points for fiber
        scoreComponents++;
    }
    
    // Micronutrient scoring
    double micronutrientScore = micronutrients.values().stream()
            .mapToDouble(value -> Math.min(value / 100.0, 1.0)) // Normalize to daily needs
            .average().orElse(0.0) * 30; // Max 30 points for micronutrients
    
    score += micronutrientScore;
    scoreComponents++;
    
    // Negative scoring factors
    if (sodium > 600) { // High sodium penalty
        score -= Math.min((sodium - 600) / 1000.0 * 10, 10);
    }
    
    if (sugar > 15) { // High sugar penalty
        score -= Math.min((sugar - 15) / 25.0 * 10, 10);
    }
    
    // Ensure score is between 0 and 100
    this.nutritionScore = Math.max(0, Math.min(100, score));
    return this.nutritionScore;
}
```

---

## Meal Planning Models

### 1. WeeklyMealPlan
**Package**: `com.mealplanner.model`  
**Purpose**: 7-day meal plan structure with complete nutritional tracking

#### Key Fields:
```java
public class WeeklyMealPlan {
    // Associated User Information
    private UserProfile user;
    private NutritionProfile nutritionProfile;
    
    // Meal Plan Data
    private List<DailyMealPlan> dailyPlans;    // 7-day meal plans
    private LocalDate startDate;               // Week start date
    private LocalDate endDate;                 // Week end date
    
    // Weekly Nutrition Summary
    private NutritionInfo weeklyNutritionTotal;
    private NutritionInfo dailyNutritionAverage;
    
    // Plan Metadata
    private String planId;
    private LocalDateTime generatedAt;
    private String generationVersion;
    private Map<String, Object> generationParameters;
}
```

#### Constructor and Methods:
```java
public WeeklyMealPlan(UserProfile user, NutritionProfile nutritionProfile, 
                     List<DailyMealPlan> dailyPlans) {
    this.user = user;
    this.nutritionProfile = nutritionProfile;
    this.dailyPlans = dailyPlans != null ? dailyPlans : new ArrayList<>();
    
    this.planId = UUID.randomUUID().toString();
    this.startDate = LocalDate.now();
    this.endDate = startDate.plusDays(6);
    this.generatedAt = LocalDateTime.now();
    this.generationVersion = "2025.1";
    
    // Calculate nutrition summaries
    calculateWeeklyNutritionSummary();
}

// Calculate total weekly nutrition
private void calculateWeeklyNutritionSummary() {
    double totalCalories = 0, totalProtein = 0, totalCarbs = 0, totalFat = 0;
    Map<String, Double> totalMicronutrients = new HashMap<>();
    
    for (DailyMealPlan dailyPlan : dailyPlans) {
        NutritionInfo dailyNutrition = dailyPlan.getDailyNutritionTotal();
        if (dailyNutrition != null) {
            totalCalories += dailyNutrition.getCalories();
            totalProtein += dailyNutrition.getProtein();
            totalCarbs += dailyNutrition.getCarbohydrates();
            totalFat += dailyNutrition.getFat();
            
            // Aggregate micronutrients
            dailyNutrition.getMicronutrients().forEach((nutrient, value) ->
                totalMicronutrients.merge(nutrient, value, Double::sum));
        }
    }
    
    // Create weekly totals
    this.weeklyNutritionTotal = new NutritionInfo();
    weeklyNutritionTotal.setCalories(totalCalories);
    weeklyNutritionTotal.setProtein(totalProtein);
    weeklyNutritionTotal.setCarbohydrates(totalCarbs);
    weeklyNutritionTotal.setFat(totalFat);
    weeklyNutritionTotal.setMicronutrients(totalMicronutrients);
    
    // Calculate daily averages
    this.dailyNutritionAverage = new NutritionInfo();
    dailyNutritionAverage.setCalories(totalCalories / 7);
    dailyNutritionAverage.setProtein(totalProtein / 7);
    dailyNutritionAverage.setCarbohydrates(totalCarbs / 7);
    dailyNutritionAverage.setFat(totalFat / 7);
    
    Map<String, Double> avgMicronutrients = new HashMap<>();
    totalMicronutrients.forEach((nutrient, value) ->
        avgMicronutrients.put(nutrient, value / 7));
    dailyNutritionAverage.setMicronutrients(avgMicronutrients);
}

// Get specific day meal plan
public DailyMealPlan getDayPlan(int dayIndex) {
    if (dayIndex >= 0 && dayIndex < dailyPlans.size()) {
        return dailyPlans.get(dayIndex);
    }
    return null;
}

// Get meal plan for specific date
public DailyMealPlan getDayPlan(LocalDate date) {
    int dayIndex = (int) ChronoUnit.DAYS.between(startDate, date);
    return getDayPlan(dayIndex);
}
```

### 2. DailyMealPlan
**Package**: `com.mealplanner.model`  
**Purpose**: Single day meal organization with nutritional tracking

#### Key Fields:
```java
public class DailyMealPlan {
    // Date Information
    private LocalDate date;
    private String dayName;                // "Monday", "Tuesday", etc.
    
    // Daily Meals
    private Meal breakfast;
    private Meal lunch;
    private Meal dinner;
    private Meal snacks;
    
    // Daily Nutrition Summary
    private NutritionInfo dailyNutritionTotal;
    private NutritionInfo nutritionTargets;
    
    // Achievement Tracking
    private Map<String, Double> nutritionAchievementPercentages;
    private double overallNutritionScore;
}
```

### 3. Meal
**Package**: `com.mealplanner.model`  
**Purpose**: Individual meal with recipes and nutritional data

#### Key Fields:
```java
public class Meal {
    private MealType mealType;
    private List<Recipe> recipes;          // Multiple recipes per meal
    private List<Double> portionSizes;     // Portion size for each recipe
    private NutritionInfo totalNutrition;  // Combined nutrition from all recipes
    private LocalDateTime plannedTime;     // Suggested meal time
    
    // Meal Metadata
    private String mealNotes;              // Special preparation notes
    private boolean isPrepared;            // Meal preparation status
    private LocalDateTime preparedAt;      // When meal was prepared
}
```

### 4. FamilyMealPlan
**Package**: `com.mealplanner.model`  
**Purpose**: Multi-member meal planning with individual considerations

#### Key Fields:
```java
public class FamilyMealPlan {
    // Family Information
    private UserProfile familyHead;
    private List<FamilyMember> familyMembers;
    private Map<UserProfile, WeeklyMealPlan> individualPlans;
    
    // Consolidated Planning
    private WeeklyMealPlan consolidatedPlan;
    private Map<LocalDate, Map<MealType, List<Recipe>>> sharedMeals;
    
    // Shopping and Preparation
    private List<Ingredient> consolidatedShoppingList;
    private Map<String, Double> ingredientTotals;
    
    // Family Nutrition Summary
    private Map<UserProfile, NutritionProfile> memberNutritionProfiles;
    private NutritionInfo familyNutritionAverage;
}
```

---

## Model Relationships

### Entity Relationship Diagram
```
UserProfile (1) -----> (1..n) FamilyMember
     |
     v
NutritionProfile (1) -----> (1) WeeklyMealPlan
                                    |
                                    v
                            (7) DailyMealPlan
                                    |
                                    v
                            (4) Meal [Breakfast, Lunch, Dinner, Snacks]
                                    |
                                    v
                            (1..n) Recipe
                                    |
                                    v
                            (1..n) Ingredient
                                    |
                                    v
                            (1) NutritionInfo
```

### Dependency Flow
1. **UserProfile** → **NutritionProfile**: User data drives nutrition calculations
2. **NutritionProfile** → **WeeklyMealPlan**: Nutrition targets guide meal selection
3. **WeeklyMealPlan** → **DailyMealPlan**: Weekly structure broken into daily plans
4. **DailyMealPlan** → **Meal**: Daily structure organized by meal types
5. **Meal** → **Recipe**: Meals composed of specific recipes
6. **Recipe** → **Ingredient**: Recipes built from ingredient lists
7. **Ingredient** → **NutritionInfo**: Ingredients provide nutritional data

---

## Serialization and Persistence

### JSON Serialization
All models support JSON serialization using Jackson annotations:

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfile {
    @JsonProperty("profile_id")
    private String profileId;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonIgnore
    private transient String temporaryData; // Not serialized
}
```

### Database Mapping
Models can be mapped to database tables using appropriate annotations:

```java
// Example with JPA annotations (if needed in future)
@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @Column(name = "profile_id")
    private String profileId;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;
    
    @ElementCollection
    @CollectionTable(name = "user_allergens")
    private Set<String> allergens;
}
```

---

## Validation and Business Rules

### Model Validation
Each model implements comprehensive validation:

```java
public interface Validatable {
    boolean isValid();
    List<String> getValidationErrors();
    void validate() throws ValidationException;
}

// Implementation example
public class UserProfile implements Validatable {
    @Override
    public boolean isValid() {
        return getValidationErrors().isEmpty();
    }
    
    @Override
    public List<String> getValidationErrors() {
        List<String> errors = new ArrayList<>();
        
        if (StringUtils.isBlank(name)) {
            errors.add("Name is required");
        }
        
        if (age < 1 || age > 120) {
            errors.add("Age must be between 1 and 120");
        }
        
        return errors;
    }
    
    @Override
    public void validate() throws ValidationException {
        List<String> errors = getValidationErrors();
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed: " + String.join(", ", errors));
        }
    }
}
```

### Business Rules
- Users must have realistic physical measurements
- Activity levels must be within 1-5 range
- Nutrition profiles must have positive calorie targets
- Recipes must have at least one ingredient
- Meal plans must cover all seven days
- Family members must be associated with a family head

---

This comprehensive model documentation provides detailed information about all data structures in the Pusti-Tusti application, enabling developers to understand, extend, and maintain the domain model effectively.
