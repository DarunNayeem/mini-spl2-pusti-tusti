package com.mealplanner.dal;

import com.mealplanner.model.MealType;
import com.mealplanner.model.recipe.Recipe;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Recipe operations
 */
public interface RecipeDAO {

    /**
     * Finds a recipe by its unique ID
     * 
     * @param recipeId the unique recipe identifier
     * @return Optional containing the recipe if found, empty otherwise
     */
    Optional<Recipe> findRecipeById(String recipeId);

    /**
     * Finds recipes based on various filter criteria
     * 
     * @param mealType                          the meal type to filter by (can be
     *                                          null to include all)
     * @param subcategories                     list of subcategories to include
     *                                          (can be null or empty)
     * @param requiredTags                      list of tags that must be present
     *                                          (can be null or empty)
     * @param excludedAllergenicIngredientNames list of ingredient names to exclude
     *                                          (can be null or empty)
     * @return List of recipes matching the filter criteria
     */
    List<Recipe> findRecipesByFilter(MealType mealType,
            List<String> subcategories,
            List<String> requiredTags,
            List<String> excludedAllergenicIngredientNames);

    /**
     * Finds all recipes for a specific meal type
     * 
     * @param mealType the meal type to filter by
     * @return List of recipes for the specified meal type
     */
    List<Recipe> findRecipesByMealType(MealType mealType);

    /**
     * Finds recipes that are suitable for specific dietary requirements
     * 
     * @param isVegan            whether the recipe should be vegan
     * @param isVegetarian       whether the recipe should be vegetarian
     * @param isDiabeticFriendly whether the recipe should be diabetic-friendly
     * @param isLowCalorie       whether the recipe should be low-calorie
     * @return List of recipes matching dietary requirements
     */
    List<Recipe> findRecipesByDietaryRequirements(boolean isVegan,
            boolean isVegetarian,
            boolean isDiabeticFriendly,
            boolean isLowCalorie);

    /**
     * Finds recipes with specific nutritional characteristics
     * 
     * @param minProtein  minimum protein content in grams
     * @param maxCalories maximum calories per serving
     * @param minFiber    minimum fiber content in grams
     * @return List of recipes matching nutritional criteria
     */
    List<Recipe> findRecipesByNutrition(double minProtein, double maxCalories, double minFiber);

    /**
     * Gets all available subcategories for a given meal type
     * 
     * @param mealType the meal type to get subcategories for
     * @return List of unique subcategories
     */
    List<String> getSubcategoriesByMealType(MealType mealType);

    /**
     * Gets all available tags across all recipes
     * 
     * @return List of unique tags
     */
    List<String> getAllTags();

    /**
     * Gets the total count of recipes in the database
     * 
     * @return Total number of recipes
     */
    long getRecipeCount();
}
