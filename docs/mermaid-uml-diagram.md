# Pusti-Tusti Meal Planning Application - UML Class Diagram

This Mermaid diagram represents the complete architecture of the Pusti-Tusti meal planning application, showing all layers, classes, and their relationships.

```mermaid
classDiagram
    %% ==============================================================
    %% PRESENTATION LAYER (UI Controllers)
    %% ==============================================================
    class App {
        +start(Stage primaryStage)
        +main(String[] args)
    }

    class MainViewController {
        -generateShoppingListButton: Button
        -generatePlanButton: Button
        -generateFamilyPlanButton: Button
        -createEditProfileButton: Button
        -familyMembersTabPane: TabPane
        -familyMembers: List~UserProfile~
        -memberControllers: Map~UserProfile, FamilyMemberTabController~
        -currentFamilyPlan: FamilyMealPlan
        -mealPlannerService: MealPlannerService
        -profileService: ProfileService
        -nutritionCalculatorService: NutritionCalculatorService
        +setServices(MealPlannerService, ProfileService, NutritionCalculatorService)
        +initialize()
        +createProfile()
        +generateNewPlan()
        +generateFamilyPlan()
        +generateShoppingList()
    }

    class FamilyMemberTabController {
        -dayTabPane: TabPane
        -userProfile: UserProfile
        -weeklyMealPlan: WeeklyMealPlan
        -mealPlannerService: MealPlannerService
        +setUserProfile(UserProfile)
        +setWeeklyMealPlan(WeeklyMealPlan)
        +populateWeeklyPlan()
        +generateNewPlan()
    }

    class RecipeCardController {
        -recipeNameLabel: Label
        -cuisineLabel: Label
        -cookingTimeLabel: Label
        -servingsLabel: Label
        -caloriesLabel: Label
        -proteinLabel: Label
        -carbsLabel: Label
        -fatLabel: Label
        -tagsContainer: FlowPane
        -ingredientsContainer: VBox
        -instructionsContainer: VBox
        +setRecipe(Recipe)
        +populateRecipeDetails()
        +formatNutritionValue(double)
        +createTagChip(String)
    }

    class ProfileEditorController {
        -nameField: TextField
        -ageField: TextField
        -heightField: TextField
        -weightField: TextField
        -genderComboBox: ComboBox~Gender~
        -healthGoalComboBox: ComboBox~HealthGoal~
        -activityLevelSlider: Slider
        -allergiesTextArea: TextArea
        -medicalConditionsListView: ListView~MedicalCondition~
        +populateProfile(UserProfile)
        +createUserProfile(): UserProfile
        +validateInputs(): boolean
        +saveProfile()
        +cancelEdit()
    }

    class FamilySetupController {
        -familyMembersListView: ListView~UserProfile~
        -addMemberButton: Button
        -removeMemberButton: Button
        -generateFamilyPlanButton: Button
        -familyMembers: List~UserProfile~
        -mealPlannerService: MealPlannerService
        -nutritionCalculatorService: NutritionCalculatorService
        +addFamilyMember()
        +removeFamilyMember()
        +generateFamilyPlan()
        +editFamilyMember(UserProfile)
    }

    class ShoppingListController {
        -shoppingItemsTableView: TableView~ShoppingItem~
        -ingredientColumn: TableColumn~ShoppingItem, String~
        -quantityColumn: TableColumn~ShoppingItem, String~
        -categoryColumn: TableColumn~ShoppingItem, String~
        -exportButton: Button
        -printButton: Button
        +populateShoppingList(List~ShoppingItem~)
        +exportToFile()
        +printShoppingList()
    }

    class ShoppingItem {
        -ingredient: String
        -quantity: String
        -category: String
        +getIngredient(): String
        +getQuantity(): String
        +getCategory(): String
    }

    class ProfileListController {
        -profilesListView: ListView~UserProfile~
        -addProfileButton: Button
        -editProfileButton: Button
        -deleteProfileButton: Button
        -selectProfileButton: Button
        +loadProfiles()
        +addProfile()
        +editProfile()
        +deleteProfile()
        +selectProfile()
    }

    %% ==============================================================
    %% SERVICE LAYER (Business Logic)
    %% ==============================================================
    class MealPlannerService {
        -recipeDAO: RecipeDAO
        -BREAKFAST_PERCENTAGE: double
        -LUNCH_PERCENTAGE: double
        -DINNER_PERCENTAGE: double
        -SNACKS_PERCENTAGE: double
        -RECIPE_COOLDOWN_DAYS: int
        -CALORIE_TOLERANCE: double
        +generateNewWeeklyPlan(UserProfile, NutritionProfile): WeeklyMealPlan
        +generateWeeklyMealsForType(MealType, NutritionProfile, Map): List~Meal~
        +generateFamilyMealPlan(List~UserProfile~): FamilyMealPlan
        +calculatePortionSize(Recipe, double): double
        +generateMealForDay(MealType, double, List~String~, List~String~): Meal
        -selectOptimalRecipes(List~Recipe~, double, Set~String~): List~Recipe~
        -calculateNutritionalScore(Recipe, double): double
        -addToUsedRecipes(Set~String~, List~Recipe~)
    }

    class NutritionCalculatorService {
        +calculateNutritionalNeeds(UserProfile): NutritionProfile
        +calculateBMI(double, double): double
        +calculateBMR(UserProfile): double
        +calculateTDEE(double, int): double
        +adjustCaloriesForHealthGoal(double, HealthGoal): double
        +calculateMacronutrients(double, HealthGoal): MacronutrientDistribution
        +calculateMicronutrientGoals(UserProfile): Map~String, Double~
        +calculateMicronutrientLimits(UserProfile): Map~String, Double~
        -getActivityMultiplier(int): double
        -adjustForMedicalConditions(UserProfile, Map): Map~String, Double~
    }

    class ProfileService {
        -PROFILE_DATA_FILE: String
        -FAMILY_PROFILES_FILE: String
        -objectMapper: ObjectMapper
        -currentProfile: UserProfile
        -familyProfiles: List~UserProfile~
        +saveProfile(UserProfile): boolean
        +loadProfile(): Optional~UserProfile~
        +createDemoProfile(): UserProfile
        +saveFamilyProfiles(List~UserProfile~): boolean
        +loadFamilyProfiles(): List~UserProfile~
        +deleteProfile(String): boolean
        +getAllProfiles(): List~UserProfile~
    }

    class MacronutrientDistribution {
        +proteinGrams: double
        +carbsGrams: double
        +fatGrams: double
    }

    %% ==============================================================
    %% DATA ACCESS LAYER (DAL)
    %% ==============================================================
    class DatabaseManager {
        -DATABASE_URL: String
        -connection: Connection
        +getConnection(): Connection
        +testConnection(): boolean
        +isDatabaseAccessible(): boolean
        +closeConnection()
        -createUnifiedView()
    }

    class DAOFactory {
        -recipeDAO: RecipeDAO
        +getRecipeDAO(): RecipeDAO
        +initializeDatabase(): boolean
        +cleanup()
    }

    class RecipeDAO {
        <<interface>>
        +findRecipeById(String): Optional~Recipe~
        +findRecipesByFilter(MealType, List~String~, List~String~, List~String~): List~Recipe~
        +findRecipesByMealType(MealType): List~Recipe~
        +findRecipesByDietaryRequirements(boolean, boolean, boolean, boolean): List~Recipe~
        +findRecipesByNutrition(double, double, double): List~Recipe~
        +getSubcategoriesByMealType(MealType): List~String~
        +getAllTags(): List~String~
        +getRecipeCount(): long
    }

    class RecipeDAOImpl {
        -objectMapper: ObjectMapper
        +findRecipeById(String): Optional~Recipe~
        +findRecipesByFilter(MealType, List~String~, List~String~, List~String~): List~Recipe~
        +findRecipesByMealType(MealType): List~Recipe~
        +findRecipesByDietaryRequirements(boolean, boolean, boolean, boolean): List~Recipe~
        +findRecipesByNutrition(double, double, double): List~Recipe~
        +getSubcategoriesByMealType(MealType): List~String~
        +getAllTags(): List~String~
        +getRecipeCount(): long
        -mapResultSetToRecipe(ResultSet): Recipe
        -parseJsonFields(ResultSet, Recipe)
        -createNutritionInfo(ResultSet): NutritionInfo
    }

    %% ==============================================================
    %% DOMAIN MODEL LAYER
    %% ==============================================================
    
    %% User Models
    class UserProfile {
        -userId: String
        -name: String
        -age: int
        -heightCm: double
        -weightKg: double
        -gender: Gender
        -primaryHealthGoal: HealthGoal
        -allergies: List~String~
        -mealPreferences: Map~MealType, List~String~~
        -medicalConditions: List~MedicalCondition~
        -activityLevel: int
        +getUserId(): String
        +getName(): String
        +getAge(): int
        +getHeightCm(): double
        +getWeightKg(): double
        +getGender(): Gender
        +getPrimaryHealthGoal(): HealthGoal
        +getAllergies(): List~String~
        +getMealPreferences(): Map~MealType, List~String~~
        +getMedicalConditions(): List~MedicalCondition~
        +getActivityLevel(): int
    }

    class NutritionProfile {
        -bmi: double
        -bmr: double
        -tdee: double
        -targetCalories: double
        -proteinGrams: double
        -carbsGrams: double
        -fatGrams: double
        -micronutrientGoals: Map~String, Double~
        -micronutrientLimits: Map~String, Double~
        +getBmi(): double
        +getBmr(): double
        +getTdee(): double
        +getTargetCalories(): double
        +getProteinGrams(): double
        +getCarbsGrams(): double
        +getFatGrams(): double
        +getMicronutrientGoals(): Map~String, Double~
        +getMicronutrientLimits(): Map~String, Double~
    }

    class FamilyMember {
        -memberName: String
        +getMemberName(): String
        +setMemberName(String)
    }

    %% Recipe Models
    class Recipe {
        -recipeId: String
        -nameEn: String
        -nameBn: String
        -servings: int
        -category: MealType
        -subcategory: String
        -cookingTimeMinutes: int
        -preparationTimeHours: int
        -yieldFactor: double
        -cuisine: String
        -foreignOrigin: String
        -tags: List~String~
        -ingredients: List~Ingredient~
        -nutritionPerServing: NutritionInfo
        -instructionsEn: List~String~
        -instructionsBn: List~String~
        +getRecipeId(): String
        +getNameEn(): String
        +getNameBn(): String
        +getServings(): int
        +getCategory(): MealType
        +getSubcategory(): String
        +getCookingTimeMinutes(): int
        +getPreparationTimeHours(): int
        +getYieldFactor(): double
        +getCuisine(): String
        +getForeignOrigin(): String
        +getTags(): List~String~
        +getIngredients(): List~Ingredient~
        +getNutritionPerServing(): NutritionInfo
        +getInstructionsEn(): List~String~
        +getInstructionsBn(): List~String~
    }

    class Ingredient {
        -foodId: String
        -nameEn: String
        -quantityGrams: double
        -unit: String
        +getFoodId(): String
        +getNameEn(): String
        +getQuantityGrams(): double
        +getUnit(): String
    }

    class NutritionInfo {
        -energyKcal: double
        -proteinG: double
        -fatG: double
        -carbohydrateAvailableG: double
        -totalDietaryFibreG: double
        -waterG: double
        -allNutrients: Map~String, Double~
        +getEnergyKcal(): double
        +getProteinG(): double
        +getFatG(): double
        +getCarbohydrateAvailableG(): double
        +getTotalDietaryFibreG(): double
        +getWaterG(): double
        +getAllNutrients(): Map~String, Double~
    }

    %% Meal Plan Models
    class WeeklyMealPlan {
        -user: UserProfile
        -nutritionProfile: NutritionProfile
        -dailyPlans: List~DailyMealPlan~
        +getUser(): UserProfile
        +getNutritionProfile(): NutritionProfile
        +getDailyPlans(): List~DailyMealPlan~
        +setUser(UserProfile)
        +setNutritionProfile(NutritionProfile)
        +setDailyPlans(List~DailyMealPlan~)
    }

    class DailyMealPlan {
        -date: LocalDate
        -breakfast: Meal
        -lunch: Meal
        -dinner: Meal
        -snacks: Meal
        -dailyTotalNutrition: NutritionInfo
        +getDate(): LocalDate
        +getBreakfast(): Meal
        +getLunch(): Meal
        +getDinner(): Meal
        +getSnacks(): Meal
        +getDailyTotalNutrition(): NutritionInfo
        +setDate(LocalDate)
        +setBreakfast(Meal)
        +setLunch(Meal)
        +setDinner(Meal)
        +setSnacks(Meal)
        +setDailyTotalNutrition(NutritionInfo)
    }

    class Meal {
        -mealType: MealType
        -recipes: List~Recipe~
        -mealNutrition: NutritionInfo
        +getMealType(): MealType
        +getRecipes(): List~Recipe~
        +getMealNutrition(): NutritionInfo
        +setMealType(MealType)
        +setRecipes(List~Recipe~)
        +setMealNutrition(NutritionInfo)
    }

    class FamilyMealPlan {
        -baseMealPlan: WeeklyMealPlan
        -familyMembers: List~UserProfile~
        -memberSpecificNotes: Map~String, List~String~~
        -memberAlternatives: Map~String, List~Recipe~~
        -consolidatedShoppingList: Map~String, Integer~
        -individualMealPlans: Map~String, WeeklyMealPlan~
        -memberNutritionProfiles: Map~String, NutritionProfile~
        +getBaseMealPlan(): WeeklyMealPlan
        +getFamilyMembers(): List~UserProfile~
        +getMemberSpecificNotes(): Map~String, List~String~~
        +getMemberAlternatives(): Map~String, List~Recipe~~
        +getConsolidatedShoppingList(): Map~String, Integer~
        +getIndividualMealPlans(): Map~String, WeeklyMealPlan~
        +getMemberNutritionProfiles(): Map~String, NutritionProfile~
        +addFamilyMember(UserProfile)
        +removeFamilyMember(String)
        +addMemberNote(String, String)
        +addMemberAlternative(String, Recipe)
        +generateShoppingList(): Map~String, Integer~
    }

    %% Enums
    class Gender {
        <<enumeration>>
        MALE
        FEMALE
    }

    class HealthGoal {
        <<enumeration>>
        WEIGHT_LOSS
        WEIGHT_GAIN
        MUSCLE_GAIN
        DIABETES_CONTROL
        CHILD_NUTRITION
        PREGNANCY_NUTRITION
        ELDERLY_NUTRITION
        HYPERTENSION_CONTROL
        ANEMIA_CONTROL
        MAINTENANCE
    }

    class MealType {
        <<enumeration>>
        BREAKFAST
        LUNCH
        DINNER
        SNACKS
    }

    class MedicalCondition {
        <<enumeration>>
        DIABETES
        HYPERTENSION
        PREGNANCY
        ELDERLY
        CHILD
        HIGH_CHOLESTEROL
        ANEMIA
        NONE
    }

    class DietaryPreference {
        <<enumeration>>
        VEGAN
        VEGETARIAN
        NON_VEGETARIAN
    }

    %% ==============================================================
    %% RELATIONSHIPS
    %% ==============================================================

    %% App Layer Dependencies
    App ..> MainViewController : creates/injects
    App ..> MealPlannerService : creates
    App ..> NutritionCalculatorService : creates
    App ..> ProfileService : creates
    App ..> RecipeDAOImpl : creates

    %% UI Layer Dependencies
    MainViewController --> MealPlannerService : uses
    MainViewController --> ProfileService : uses
    MainViewController --> NutritionCalculatorService : uses
    MainViewController --> FamilyMemberTabController : manages
    MainViewController --> FamilySetupController : creates
    MainViewController --> ShoppingListController : creates
    MainViewController --> ProfileEditorController : creates

    FamilyMemberTabController --> RecipeCardController : creates
    FamilyMemberTabController --> MealPlannerService : uses
    FamilySetupController --> MealPlannerService : uses
    FamilySetupController --> NutritionCalculatorService : uses
    ProfileEditorController --> UserProfile : creates/edits
    ShoppingListController --> ShoppingItem : manages

    %% Service Layer Dependencies
    MealPlannerService --> RecipeDAO : uses
    MealPlannerService --> WeeklyMealPlan : creates
    MealPlannerService --> FamilyMealPlan : creates
    MealPlannerService --> DailyMealPlan : creates
    MealPlannerService --> Meal : creates
    NutritionCalculatorService --> NutritionProfile : creates
    NutritionCalculatorService --> MacronutrientDistribution : creates
    ProfileService --> UserProfile : persists/loads

    %% DAL Layer Dependencies
    DAOFactory --> RecipeDAOImpl : creates
    DAOFactory --> DatabaseManager : uses
    RecipeDAOImpl ..|> RecipeDAO : implements
    RecipeDAOImpl --> DatabaseManager : uses
    RecipeDAOImpl --> Recipe : creates
    RecipeDAOImpl --> NutritionInfo : creates

    %% Domain Model Relationships
    UserProfile --> Gender : uses
    UserProfile --> HealthGoal : uses
    UserProfile --> MealType : uses
    UserProfile --> MedicalCondition : uses
    FamilyMember --|> UserProfile : extends

    Recipe --> MealType : uses
    Recipe --> Ingredient : contains
    Recipe --> NutritionInfo : contains

    WeeklyMealPlan --> UserProfile : for
    WeeklyMealPlan --> NutritionProfile : uses
    WeeklyMealPlan --> DailyMealPlan : contains

    DailyMealPlan --> Meal : contains
    DailyMealPlan --> NutritionInfo : aggregates

    Meal --> MealType : typed by
    Meal --> Recipe : contains
    Meal --> NutritionInfo : summarizes

    FamilyMealPlan --> WeeklyMealPlan : contains
    FamilyMealPlan --> UserProfile : manages
    FamilyMealPlan --> Recipe : alternatives
    FamilyMealPlan --> NutritionProfile : tracks

    %% Data Flow (dotted lines for key operations)
    UserProfile -.-> NutritionCalculatorService : "input to"
    NutritionProfile -.-> MealPlannerService : "guides"
    Recipe -.-> Meal : "assembled into"
    Meal -.-> DailyMealPlan : "grouped into"
    DailyMealPlan -.-> WeeklyMealPlan : "collected into"
    WeeklyMealPlan -.-> FamilyMealPlan : "combined into"

    %% Module Dependencies
    classDef presentationLayer fill:#e1f5fe
    classDef serviceLayer fill:#f3e5f5
    classDef dalLayer fill:#e8f5e8
    classDef modelLayer fill:#fff3e0
    classDef enumLayer fill:#fce4ec

    class App presentationLayer
    class MainViewController presentationLayer
    class FamilyMemberTabController presentationLayer
    class RecipeCardController presentationLayer
    class ProfileEditorController presentationLayer
    class FamilySetupController presentationLayer
    class ShoppingListController presentationLayer
    class ProfileListController presentationLayer
    class ShoppingItem presentationLayer
    
    class MealPlannerService serviceLayer
    class NutritionCalculatorService serviceLayer
    class ProfileService serviceLayer
    class MacronutrientDistribution serviceLayer
    
    class DatabaseManager dalLayer
    class DAOFactory dalLayer
    class RecipeDAO dalLayer
    class RecipeDAOImpl dalLayer
    
    class UserProfile modelLayer
    class NutritionProfile modelLayer
    class FamilyMember modelLayer
    class Recipe modelLayer
    class Ingredient modelLayer
    class NutritionInfo modelLayer
    class WeeklyMealPlan modelLayer
    class DailyMealPlan modelLayer
    class Meal modelLayer
    class FamilyMealPlan modelLayer
    
    class Gender enumLayer
    class HealthGoal enumLayer
    class MealType enumLayer
    class MedicalCondition enumLayer
    class DietaryPreference enumLayer
```

## Architecture Overview

This UML diagram represents a **4-layer architecture** with clear separation of concerns:

### 1. **Presentation Layer** (Light Blue)
- **JavaFX Controllers**: Handle UI interactions and display
- **FXML-based**: Declarative UI components
- **MVC Pattern**: Clean separation between view and business logic

### 2. **Service Layer** (Light Purple)
- **Business Logic**: Core meal planning algorithms and calculations
- **Orchestration**: Coordinates between UI and data layers
- **Domain Services**: Specialized services for nutrition, profiles, and meal planning

### 3. **Data Access Layer** (Light Green)
- **Repository Pattern**: Abstract data access through interfaces
- **Factory Pattern**: Centralized DAO creation and management
- **Database Management**: SQLite connection and query handling

### 4. **Domain Model Layer** (Light Orange)
- **Rich Domain Objects**: User profiles, recipes, meal plans, nutrition info
- **Value Objects**: Enums for type safety and domain clarity
- **Aggregates**: Complex domain objects like family meal plans

### Key Design Patterns Used:
- **Dependency Injection**: Services injected into controllers
- **Factory Pattern**: DAOFactory for object creation
- **Singleton Pattern**: DatabaseManager for connection management
- **Repository Pattern**: RecipeDAO for data abstraction
- **Builder Pattern**: Complex object construction
- **MVC Pattern**: UI separation of concerns

### Data Flow:
1. **User Input** → UI Controllers
2. **Business Logic** → Service Layer
3. **Data Operations** → DAL Layer
4. **Domain Objects** → Model Layer
5. **Results** → Back through layers to UI

This architecture ensures **maintainability**, **testability**, and **scalability** while following modern software design principles.
