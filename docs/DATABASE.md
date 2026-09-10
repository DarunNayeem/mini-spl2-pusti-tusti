# Database Guide

## Overview
Pusti-Tusti uses SQLite as its database system for recipe storage, providing efficient local data access with JSON support for complex data structures. The database design emphasizes flexibility, performance, and easy maintenance.

## Database Architecture

### Database File
- **Location**: `src/main/resources/recipes.db`
- **Type**: SQLite 3.x
- **Size**: Optimized for thousands of recipes
- **Encoding**: UTF-8 for multilingual support

### Connection Management
- **Pattern**: Singleton (DatabaseManager)
- **Connection Pooling**: Single connection with proper lifecycle management
- **Transaction Support**: ACID compliance for data integrity
- **Error Handling**: Comprehensive exception management

## Table Structure

### Core Recipe Tables

#### 1. breakfast
Stores breakfast recipe data with complete nutritional information.

```sql
CREATE TABLE breakfast (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    recipe_id TEXT UNIQUE NOT NULL,
    recipe_name TEXT NOT NULL,
    recipe_name_bengali TEXT,
    ingredients TEXT, -- JSON array
    tags TEXT, -- JSON array
    instructions TEXT, -- JSON array
    cooking_time_minutes INTEGER,
    prep_time_minutes INTEGER,
    nutrition_info TEXT, -- JSON object
    subcategory TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 2. lunch
Lunch recipes with identical structure to breakfast table.

```sql
CREATE TABLE lunch (
    -- Same structure as breakfast
    -- Optimized for lunch-specific recipes
);
```

#### 3. dinner
Dinner recipes following the same schema pattern.

```sql
CREATE TABLE dinner (
    -- Same structure as breakfast
    -- Specialized for dinner recipes
);
```

#### 4. snacks
Snack and beverage recipes with consistent schema.

```sql
CREATE TABLE snacks (
    -- Same structure as breakfast
    -- Focused on snacks and beverages
);
```

### Unified Recipe View

#### all_recipes (View)
A unified view combining all meal type tables for efficient querying.

```sql
CREATE VIEW all_recipes AS
SELECT 
    recipe_id,
    recipe_name,
    recipe_name_bengali,
    'BREAKFAST' as meal_type,
    ingredients,
    tags,
    instructions,
    cooking_time_minutes,
    prep_time_minutes,
    nutrition_info,
    subcategory,
    created_at
FROM breakfast
UNION ALL
SELECT 
    recipe_id,
    recipe_name,
    recipe_name_bengali,
    'LUNCH' as meal_type,
    ingredients,
    tags,
    instructions,
    cooking_time_minutes,
    prep_time_minutes,
    nutrition_info,
    subcategory,
    created_at
FROM lunch
UNION ALL
SELECT 
    recipe_id,
    recipe_name,
    recipe_name_bengali,
    'DINNER' as meal_type,
    ingredients,
    tags,
    instructions,
    cooking_time_minutes,
    prep_time_minutes,
    nutrition_info,
    subcategory,
    created_at
FROM dinner
UNION ALL
SELECT 
    recipe_id,
    recipe_name,
    recipe_name_bengali,
    'SNACKS' as meal_type,
    ingredients,
    tags,
    instructions,
    cooking_time_minutes,
    prep_time_minutes,
    nutrition_info,
    subcategory,
    created_at
FROM snacks;
```

## JSON Data Structures

### 1. Ingredients JSON Schema
```json
[
  {
    "name": "Rice",
    "nameBengali": "ভাত",
    "quantity": 1.0,
    "unit": "cup",
    "calories": 205,
    "protein": 4.3,
    "carbohydrates": 45.0,
    "fat": 0.4,
    "fiber": 0.6,
    "micronutrients": {
      "iron": 0.8,
      "magnesium": 25.0,
      "phosphorus": 68.0
    }
  }
]
```

### 2. Tags JSON Schema
```json
[
  "vegetarian",
  "gluten-free",
  "high-protein",
  "low-carb",
  "dairy-free",
  "quick-meal"
]
```

### 3. Instructions JSON Schema
```json
[
  "Heat oil in a large pan over medium heat",
  "Add chopped onions and sauté until golden brown",
  "Add spices and cook for 1 minute until fragrant",
  "Add vegetables and cook until tender",
  "Serve hot with rice or bread"
]
```

### 4. Nutrition Info JSON Schema
```json
{
  "calories": 350.5,
  "protein": 12.3,
  "carbohydrates": 45.7,
  "fat": 8.9,
  "fiber": 5.2,
  "sugar": 8.1,
  "sodium": 680.0,
  "micronutrients": {
    "vitamin_a": 450.0,
    "vitamin_c": 15.2,
    "vitamin_d": 0.0,
    "vitamin_e": 2.1,
    "vitamin_k": 12.5,
    "vitamin_b1": 0.15,
    "vitamin_b2": 0.18,
    "vitamin_b3": 3.2,
    "vitamin_b6": 0.25,
    "vitamin_b12": 0.0,
    "folate": 45.0,
    "biotin": 2.1,
    "pantothenic_acid": 0.8,
    "calcium": 120.5,
    "iron": 2.8,
    "magnesium": 65.0,
    "phosphorus": 180.0,
    "potassium": 350.0,
    "zinc": 1.2,
    "copper": 0.15,
    "manganese": 0.8,
    "selenium": 12.5,
    "iodine": 8.0,
    "chromium": 2.1
  }
}
```

## Database Operations

### Connection Management (DatabaseManager)

```java
public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:src/main/resources/recipes.db";
    
    // Singleton pattern implementation
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    // Connection establishment with error handling
    private void connect() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
        connection.setAutoCommit(true);
        createUnifiedView(); // Ensure view exists
    }
}
```

### Query Patterns

#### 1. Simple Recipe Retrieval
```sql
SELECT * FROM all_recipes 
WHERE recipe_id = ?;
```

#### 2. Meal Type Filtering
```sql
SELECT * FROM all_recipes 
WHERE meal_type = ? 
ORDER BY recipe_name;
```

#### 3. Dietary Preference Filtering
```sql
SELECT * FROM all_recipes 
WHERE meal_type = ? 
AND json_extract(tags, '$') LIKE '%vegetarian%'
AND json_extract(tags, '$') NOT LIKE '%dairy%'
ORDER BY recipe_name;
```

#### 4. Nutritional Filtering
```sql
SELECT * FROM all_recipes 
WHERE meal_type = ?
AND CAST(json_extract(nutrition_info, '$.calories') AS REAL) BETWEEN ? AND ?
AND CAST(json_extract(nutrition_info, '$.protein') AS REAL) >= ?
ORDER BY CAST(json_extract(nutrition_info, '$.calories') AS REAL);
```

#### 5. Complex Multi-Criteria Filtering
```sql
SELECT * FROM all_recipes 
WHERE meal_type = ?
AND (? = '' OR json_extract(tags, '$') LIKE '%' || ? || '%')
AND (? = 0 OR json_extract(ingredients, '$') NOT LIKE '%' || ? || '%')
AND (? = 0 OR CAST(json_extract(nutrition_info, '$.calories') AS REAL) BETWEEN ? AND ?)
ORDER BY recipe_name
LIMIT ?;
```

### JSON Processing in SQL

#### Extracting JSON Array Elements
```sql
-- Check if tags contain specific value
json_extract(tags, '$') LIKE '%vegetarian%'

-- Extract specific nutrition value
CAST(json_extract(nutrition_info, '$.calories') AS REAL)

-- Extract micronutrient value
CAST(json_extract(nutrition_info, '$.micronutrients.iron') AS REAL)
```

#### Searching Within JSON Arrays
```sql
-- Search for allergen in ingredients
json_extract(ingredients, '$') LIKE '%' || ? || '%'

-- Multiple tag matching
json_extract(tags, '$') LIKE '%dairy-free%' 
AND json_extract(tags, '$') LIKE '%gluten-free%'
```

## Data Access Layer Implementation

### RecipeDAOImpl Methods

#### 1. findRecipeById
```java
public Recipe findRecipeById(String recipeId) {
    String sql = "SELECT * FROM all_recipes WHERE recipe_id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        pstmt.setString(1, recipeId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return mapResultSetToRecipe(rs);
        }
    } catch (SQLException e) {
        System.err.println("Error finding recipe by ID: " + e.getMessage());
    }
    return null;
}
```

#### 2. findRecipesByFilter (Complex Filtering)
```java
public List<Recipe> findRecipesByFilter(MealType mealType, 
                                      Set<DietaryPreference> preferences,
                                      Set<String> allergens,
                                      Map<String, Double> nutritionCriteria) {
    StringBuilder sql = new StringBuilder("SELECT * FROM all_recipes WHERE 1=1");
    List<Object> parameters = new ArrayList<>();
    
    // Add meal type filter
    if (mealType != null) {
        sql.append(" AND meal_type = ?");
        parameters.add(mealType.toString());
    }
    
    // Add dietary preference filters
    for (DietaryPreference pref : preferences) {
        sql.append(" AND json_extract(tags, '$') LIKE ?");
        parameters.add("%" + pref.toString().toLowerCase() + "%");
    }
    
    // Add allergen exclusions
    for (String allergen : allergens) {
        sql.append(" AND json_extract(ingredients, '$') NOT LIKE ?");
        parameters.add("%" + allergen + "%");
    }
    
    // Add nutrition criteria
    for (Map.Entry<String, Double> criteria : nutritionCriteria.entrySet()) {
        sql.append(" AND CAST(json_extract(nutrition_info, '$.") 
           .append(criteria.getKey())
           .append("') AS REAL) >= ?");
        parameters.add(criteria.getValue());
    }
    
    sql.append(" ORDER BY recipe_name");
    
    // Execute query with parameters
    return executeQuery(sql.toString(), parameters);
}
```

### JSON Deserialization

#### Jackson ObjectMapper Configuration
```java
private static final ObjectMapper objectMapper = new ObjectMapper();

// Parse ingredients JSON
private List<Ingredient> parseIngredients(String ingredientsJson) {
    try {
        return objectMapper.readValue(ingredientsJson, 
               new TypeReference<List<Ingredient>>() {});
    } catch (JsonProcessingException e) {
        System.err.println("Error parsing ingredients JSON: " + e.getMessage());
        return new ArrayList<>();
    }
}

// Parse nutrition info JSON
private NutritionInfo parseNutritionInfo(String nutritionJson) {
    try {
        return objectMapper.readValue(nutritionJson, NutritionInfo.class);
    } catch (JsonProcessingException e) {
        System.err.println("Error parsing nutrition JSON: " + e.getMessage());
        return new NutritionInfo();
    }
}
```

## Performance Optimizations

### 1. Indexing Strategy
```sql
-- Primary indexes for fast lookups
CREATE INDEX idx_breakfast_recipe_id ON breakfast(recipe_id);
CREATE INDEX idx_lunch_recipe_id ON lunch(recipe_id);
CREATE INDEX idx_dinner_recipe_id ON dinner(recipe_id);
CREATE INDEX idx_snacks_recipe_id ON snacks(recipe_id);

-- Composite indexes for common queries
CREATE INDEX idx_breakfast_name_time ON breakfast(recipe_name, cooking_time_minutes);
CREATE INDEX idx_lunch_subcategory ON lunch(subcategory);
```

### 2. Query Optimization
- **Prepared Statements**: All queries use parameterized statements
- **Result Set Streaming**: Large result sets processed incrementally
- **Connection Reuse**: Single connection managed by singleton pattern
- **View Optimization**: Unified view reduces code complexity

### 3. JSON Processing Optimization
- **Selective Parsing**: Only parse JSON when needed
- **Caching**: Frequently accessed recipes cached in memory
- **Lazy Loading**: Complex objects loaded on demand

## Data Integrity

### 1. Constraints
```sql
-- Unique recipe IDs across all tables
ALTER TABLE breakfast ADD CONSTRAINT uk_breakfast_recipe_id UNIQUE (recipe_id);
ALTER TABLE lunch ADD CONSTRAINT uk_lunch_recipe_id UNIQUE (recipe_id);
ALTER TABLE dinner ADD CONSTRAINT uk_dinner_recipe_id UNIQUE (recipe_id);
ALTER TABLE snacks ADD CONSTRAINT uk_snacks_recipe_id UNIQUE (recipe_id);

-- Non-null essential fields
ALTER TABLE breakfast ADD CONSTRAINT nn_breakfast_name CHECK (recipe_name IS NOT NULL);
ALTER TABLE breakfast ADD CONSTRAINT nn_breakfast_ingredients CHECK (ingredients IS NOT NULL);
```

### 2. Data Validation
- **JSON Schema Validation**: Ingredients and nutrition data validated before insertion
- **Referential Integrity**: Recipe IDs must be unique across all meal types
- **Type Safety**: Numeric fields validated for appropriate ranges

## Backup and Migration

### 1. Database Backup
```bash
# Create backup
sqlite3 recipes.db ".backup backup_$(date +%Y%m%d).db"

# Restore from backup
sqlite3 recipes.db ".restore backup_20250729.db"
```

### 2. Schema Migration
```sql
-- Example migration script
-- Add new column to existing tables
ALTER TABLE breakfast ADD COLUMN servings INTEGER DEFAULT 4;
ALTER TABLE lunch ADD COLUMN servings INTEGER DEFAULT 4;
ALTER TABLE dinner ADD COLUMN servings INTEGER DEFAULT 4;
ALTER TABLE snacks ADD COLUMN servings INTEGER DEFAULT 1;

-- Update unified view
DROP VIEW IF EXISTS all_recipes;
CREATE VIEW all_recipes AS
-- Updated view definition with new column
```

## Testing Strategy

### 1. Unit Tests
```java
@Test
public void testRecipeRetrieval() {
    RecipeDAO dao = new RecipeDAOImpl();
    Recipe recipe = dao.findRecipeById("breakfast_001");
    assertNotNull(recipe);
    assertEquals("Oatmeal with Fruits", recipe.getRecipeName());
}

@Test
public void testComplexFiltering() {
    Set<DietaryPreference> prefs = Set.of(DietaryPreference.VEGETARIAN);
    Set<String> allergens = Set.of("nuts");
    List<Recipe> recipes = dao.findRecipesByFilter(MealType.LUNCH, prefs, allergens, null);
    assertTrue(recipes.size() > 0);
}
```

### 2. Integration Tests
```java
@Test
public void testDatabaseConnection() {
    DatabaseManager manager = DatabaseManager.getInstance();
    Connection conn = manager.getConnection();
    assertNotNull(conn);
    assertFalse(conn.isClosed());
}
```

## Common Query Examples

### 1. Find High-Protein Breakfast Recipes
```sql
SELECT recipe_name, 
       CAST(json_extract(nutrition_info, '$.protein') AS REAL) as protein
FROM all_recipes 
WHERE meal_type = 'BREAKFAST'
AND CAST(json_extract(nutrition_info, '$.protein') AS REAL) >= 20
ORDER BY protein DESC;
```

### 2. Find Vegetarian Recipes Under 400 Calories
```sql
SELECT recipe_name, recipe_name_bengali,
       CAST(json_extract(nutrition_info, '$.calories') AS REAL) as calories
FROM all_recipes 
WHERE json_extract(tags, '$') LIKE '%vegetarian%'
AND CAST(json_extract(nutrition_info, '$.calories') AS REAL) < 400
ORDER BY calories;
```

### 3. Find Recipes with Specific Ingredients
```sql
SELECT recipe_name, ingredients
FROM all_recipes 
WHERE json_extract(ingredients, '$') LIKE '%chicken%'
AND meal_type = 'DINNER'
ORDER BY recipe_name;
```

### 4. Get Nutrition Summary by Meal Type
```sql
SELECT meal_type,
       COUNT(*) as recipe_count,
       AVG(CAST(json_extract(nutrition_info, '$.calories') AS REAL)) as avg_calories,
       AVG(CAST(json_extract(nutrition_info, '$.protein') AS REAL)) as avg_protein
FROM all_recipes 
GROUP BY meal_type
ORDER BY meal_type;
```

## Troubleshooting

### Common Issues

#### 1. Database Connection Errors
```java
// Error: Database is locked
// Solution: Ensure proper connection closing
try (Connection conn = DatabaseManager.getInstance().getConnection()) {
    // Database operations
} // Connection automatically closed
```

#### 2. JSON Parsing Errors
```java
// Error: Invalid JSON format
// Solution: Validate JSON before database insertion
private boolean isValidJson(String jsonString) {
    try {
        objectMapper.readTree(jsonString);
        return true;
    } catch (JsonProcessingException e) {
        return false;
    }
}
```

#### 3. Performance Issues
```sql
-- Problem: Slow JSON queries
-- Solution: Use selective filtering and indexing
EXPLAIN QUERY PLAN 
SELECT * FROM all_recipes 
WHERE meal_type = 'LUNCH'
AND json_extract(tags, '$') LIKE '%vegetarian%';
```

---

This database guide provides comprehensive information for understanding, maintaining, and extending the Pusti-Tusti database system, ensuring efficient and reliable data management for the meal planning application.
