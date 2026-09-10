package com.mealplanner.dal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealplanner.model.MealType;
import com.mealplanner.model.recipe.Ingredient;
import com.mealplanner.model.recipe.NutritionInfo;
import com.mealplanner.model.recipe.Recipe;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of RecipeDAO interface for SQLite database operations
 */
public class RecipeDAOImpl implements RecipeDAO {

    private final ObjectMapper objectMapper;

    public RecipeDAOImpl() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Optional<Recipe> findRecipeById(String recipeId) {
        String sql = "SELECT * FROM recipe_all WHERE recipe_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, recipeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToRecipe(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error finding recipe by ID: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public List<Recipe> findRecipesByFilter(MealType mealType,
            List<String> subcategories,
            List<String> requiredTags,
            List<String> excludedAllergenicIngredientNames) {

        StringBuilder sql = new StringBuilder("SELECT * FROM recipe_all WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        // Add meal type filter
        if (mealType != null) {
            sql.append(" AND meal_type_derived = ?");
            parameters.add(mealType.name());
        }

        // Add subcategories filter
        if (subcategories != null && !subcategories.isEmpty()) {
            sql.append(" AND subcategory IN (");
            sql.append(subcategories.stream().map(s -> "?").collect(Collectors.joining(", ")));
            sql.append(")");
            parameters.addAll(subcategories);
        }

        // Add required tags filter using JSON search
        if (requiredTags != null && !requiredTags.isEmpty()) {
            for (String tag : requiredTags) {
                sql.append(" AND (tags LIKE ? OR tags LIKE ? OR tags LIKE ?)");
                parameters.add("%" + "\"" + tag + "\"" + "%");
                parameters.add("%" + "'" + tag + "'" + "%");
                parameters.add("%" + tag + "%");
            }
        }

        List<Recipe> recipes = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Recipe recipe = mapResultSetToRecipe(rs);

                // Filter out recipes with excluded allergenic ingredients
                if (excludedAllergenicIngredientNames != null && !excludedAllergenicIngredientNames.isEmpty()) {
                    boolean hasAllergen = recipe.getIngredients().stream()
                            .anyMatch(ingredient -> excludedAllergenicIngredientNames.stream()
                                    .anyMatch(allergen -> ingredient.getNameEn().toLowerCase()
                                            .contains(allergen.toLowerCase())));

                    if (!hasAllergen) {
                        recipes.add(recipe);
                    }
                } else {
                    recipes.add(recipe);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error finding recipes by filter: " + e.getMessage());
        }

        return recipes;
    }

    @Override
    public List<Recipe> findRecipesByMealType(MealType mealType) {
        return findRecipesByFilter(mealType, null, null, null);
    }

    @Override
    public List<Recipe> findRecipesByDietaryRequirements(boolean isVegan,
            boolean isVegetarian,
            boolean isDiabeticFriendly,
            boolean isLowCalorie) {

        StringBuilder sql = new StringBuilder("SELECT * FROM recipe_all WHERE 1=1");

        if (isVegan) {
            sql.append(" AND is_vegan = 1");
        }

        if (isVegetarian) {
            sql.append(" AND is_vegetarian = 1");
        }

        if (isDiabeticFriendly) {
            sql.append(" AND is_diabetic_friendly = 1");
        }

        if (isLowCalorie) {
            sql.append(" AND is_low_calorie = 1");
        }

        List<Recipe> recipes = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                recipes.add(mapResultSetToRecipe(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error finding recipes by dietary requirements: " + e.getMessage());
        }

        return recipes;
    }

    @Override
    public List<Recipe> findRecipesByNutrition(double minProtein, double maxCalories, double minFiber) {
        String sql = "SELECT * FROM recipe_all WHERE protein_g >= ? AND energy_kcal <= ? AND total_dietary_fibre_g >= ?";
        List<Recipe> recipes = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, minProtein);
            stmt.setDouble(2, maxCalories);
            stmt.setDouble(3, minFiber);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                recipes.add(mapResultSetToRecipe(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error finding recipes by nutrition: " + e.getMessage());
        }

        return recipes;
    }

    @Override
    public List<String> getSubcategoriesByMealType(MealType mealType) {
        String sql = "SELECT DISTINCT subcategory FROM recipe_all WHERE meal_type_derived = ? AND subcategory IS NOT NULL";
        List<String> subcategories = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, mealType.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                subcategories.add(rs.getString("subcategory"));
            }

        } catch (SQLException e) {
            System.err.println("Error getting subcategories: " + e.getMessage());
        }

        return subcategories;
    }

    @Override
    public List<String> getAllTags() {
        String sql = "SELECT DISTINCT tags FROM recipe_all WHERE tags IS NOT NULL";
        Set<String> allTags = new HashSet<>();

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String tagsJson = rs.getString("tags");
                if (tagsJson != null && !tagsJson.trim().isEmpty()) {
                    try {
                        List<String> tags = objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {
                        });
                        allTags.addAll(tags);
                    } catch (JsonProcessingException e) {
                        System.err.println("Error parsing tags JSON: " + e.getMessage());
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting all tags: " + e.getMessage());
        }

        return new ArrayList<>(allTags);
    }

    @Override
    public long getRecipeCount() {
        String sql = "SELECT COUNT(*) FROM recipe_all";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }

        } catch (SQLException e) {
            System.err.println("Error getting recipe count: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Maps a ResultSet row to a Recipe object with proper JSON parsing
     */
    private Recipe mapResultSetToRecipe(ResultSet rs) throws SQLException {
        Recipe recipe = new Recipe();

        // Basic fields
        recipe.setRecipeId(rs.getString("recipe_id"));
        recipe.setNameEn(rs.getString("recipe_name_en"));
        recipe.setNameBn(rs.getString("recipe_name_bn"));
        recipe.setServings(rs.getInt("servings"));
        recipe.setCookingTimeMinutes(rs.getInt("cooking_time_minutes"));
        recipe.setPreparationTimeHours(rs.getInt("preparation_time_hours"));
        recipe.setYieldFactor(rs.getDouble("yield_factor"));
        recipe.setCuisine(rs.getString("cuisine"));
        recipe.setForeignOrigin(rs.getString("foreign_origin"));
        recipe.setSubcategory(rs.getString("subcategory"));

        // Parse MealType from meal_type_derived column
        try {
            String mealTypeStr = rs.getString("meal_type_derived");
            if (mealTypeStr != null) {
                recipe.setCategory(MealType.valueOf(mealTypeStr));
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid meal type: " + e.getMessage());
        }

        // Parse JSON fields
        parseJsonFields(rs, recipe);

        // Create nutrition info from individual columns
        NutritionInfo nutritionInfo = createNutritionInfo(rs);
        recipe.setNutritionPerServing(nutritionInfo);

        return recipe;
    }

    /**
     * Parses JSON fields from the ResultSet
     */
    private void parseJsonFields(ResultSet rs, Recipe recipe) throws SQLException {
        // Parse tags
        String tagsJson = rs.getString("tags");
        if (tagsJson != null && !tagsJson.trim().isEmpty()) {
            try {
                List<String> tags = objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {
                });
                recipe.setTags(tags);
            } catch (JsonProcessingException e) {
                System.err.println("Error parsing tags JSON: " + e.getMessage());
                recipe.setTags(new ArrayList<>());
            }
        } else {
            recipe.setTags(new ArrayList<>());
        }

        // Parse ingredients
        String ingredientsJson = rs.getString("ingredients");
        if (ingredientsJson != null && !ingredientsJson.trim().isEmpty()) {
            try {
                List<Ingredient> ingredients = objectMapper.readValue(ingredientsJson,
                        new TypeReference<List<Ingredient>>() {
                        });
                recipe.setIngredients(ingredients);
            } catch (JsonProcessingException e) {
                System.err.println("Error parsing ingredients JSON: " + e.getMessage());
                System.err.println("JSON was: " + ingredientsJson);
                recipe.setIngredients(new ArrayList<>());
            }
        } else {
            recipe.setIngredients(new ArrayList<>());
        }

        // Parse instructions
        String instructionsEnJson = rs.getString("instructions_en");
        if (instructionsEnJson != null && !instructionsEnJson.trim().isEmpty()) {
            try {
                List<String> instructionsEn = objectMapper.readValue(instructionsEnJson,
                        new TypeReference<List<String>>() {
                        });
                recipe.setInstructionsEn(instructionsEn);
            } catch (JsonProcessingException e) {
                System.err.println("Error parsing English instructions JSON: " + e.getMessage());
                recipe.setInstructionsEn(new ArrayList<>());
            }
        } else {
            recipe.setInstructionsEn(new ArrayList<>());
        }

        String instructionsBnJson = rs.getString("instructions_bn");
        if (instructionsBnJson != null && !instructionsBnJson.trim().isEmpty()) {
            try {
                List<String> instructionsBn = objectMapper.readValue(instructionsBnJson,
                        new TypeReference<List<String>>() {
                        });
                recipe.setInstructionsBn(instructionsBn);
            } catch (JsonProcessingException e) {
                System.err.println("Error parsing Bengali instructions JSON: " + e.getMessage());
                recipe.setInstructionsBn(new ArrayList<>());
            }
        } else {
            recipe.setInstructionsBn(new ArrayList<>());
        }
    }

    /**
     * Creates NutritionInfo object from individual nutrition columns
     */
    private NutritionInfo createNutritionInfo(ResultSet rs) throws SQLException {
        NutritionInfo nutritionInfo = new NutritionInfo();

        // Set core nutrients
        nutritionInfo.setEnergyKcal(rs.getDouble("energy_kcal"));
        nutritionInfo.setProteinG(rs.getDouble("protein_g"));
        nutritionInfo.setFatG(rs.getDouble("fat_g"));
        nutritionInfo.setCarbohydrateAvailableG(rs.getDouble("carbohydrate_available_g"));
        nutritionInfo.setTotalDietaryFibreG(rs.getDouble("total_dietary_fibre_g"));
        nutritionInfo.setWaterG(rs.getDouble("water_g"));

        // Create allNutrients map with all available nutrients
        Map<String, Double> allNutrients = new HashMap<>();

        // Add all nutrient columns to the map
        allNutrients.put("energy_kcal", rs.getDouble("energy_kcal"));
        allNutrients.put("energy_kj", rs.getDouble("energy_kj"));
        allNutrients.put("protein_g", rs.getDouble("protein_g"));
        allNutrients.put("fat_g", rs.getDouble("fat_g"));
        allNutrients.put("carbohydrate_available_g", rs.getDouble("carbohydrate_available_g"));
        allNutrients.put("total_dietary_fibre_g", rs.getDouble("total_dietary_fibre_g"));
        allNutrients.put("water_g", rs.getDouble("water_g"));
        allNutrients.put("ash_g", rs.getDouble("ash_g"));

        // Minerals
        allNutrients.put("calcium_mg", rs.getDouble("calcium_mg"));
        allNutrients.put("iron_mg", rs.getDouble("iron_mg"));
        allNutrients.put("magnesium_mg", rs.getDouble("magnesium_mg"));
        allNutrients.put("phosphorus_mg", rs.getDouble("phosphorus_mg"));
        allNutrients.put("potassium_mg", rs.getDouble("potassium_mg"));
        allNutrients.put("sodium_mg", rs.getDouble("sodium_mg"));
        allNutrients.put("zinc_mg", rs.getDouble("zinc_mg"));
        allNutrients.put("copper_mg", rs.getDouble("copper_mg"));

        // Vitamins
        allNutrients.put("vitamin_a_mcg", rs.getDouble("vitamin_a_mcg"));
        allNutrients.put("vitamin_b1_mg", rs.getDouble("vitamin_b1_mg"));
        allNutrients.put("vitamin_b2_mg", rs.getDouble("vitamin_b2_mg"));
        allNutrients.put("vitamin_b3_mg", rs.getDouble("vitamin_b3_mg"));
        allNutrients.put("vitamin_b6_mg", rs.getDouble("vitamin_b6_mg"));
        allNutrients.put("vitamin_c_mg", rs.getDouble("vitamin_c_mg"));
        allNutrients.put("vitamin_d_mcg", rs.getDouble("vitamin_d_mcg"));
        allNutrients.put("vitamin_e_mg", rs.getDouble("vitamin_e_mg"));
        allNutrients.put("folate_mcg", rs.getDouble("folate_mcg"));
        allNutrients.put("retinol_mcg", rs.getDouble("retinol_mcg"));
        allNutrients.put("beta_carotene_equivalents_mcg", rs.getDouble("beta_carotene_equivalents_mcg"));

        nutritionInfo.setAllNutrients(allNutrients);

        return nutritionInfo;
    }
}
