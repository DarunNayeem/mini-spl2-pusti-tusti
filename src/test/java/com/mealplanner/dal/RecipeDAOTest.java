package com.mealplanner.dal;

import com.mealplanner.model.MealType;
import com.mealplanner.model.recipe.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for RecipeDAO implementation
 */
public class RecipeDAOTest {

    private RecipeDAO recipeDAO;

    @BeforeEach
    public void setUp() {
        recipeDAO = new RecipeDAOImpl();
        // Test database connection
        assertTrue(DatabaseManager.testConnection(), "Database connection should be successful");
    }

    @AfterEach
    public void tearDown() {
        DatabaseManager.closeConnection();
    }

    @Test
    public void testDatabaseConnection() {
        assertTrue(DatabaseManager.isDatabaseAccessible(), "Database should be accessible");
    }

    @Test
    public void testGetRecipeCount() {
        long count = recipeDAO.getRecipeCount();
        assertTrue(count > 0, "Recipe count should be greater than 0");
        System.out.println("Total recipes in database: " + count);
    }

    @Test
    public void testFindRecipesByMealType() {
        List<Recipe> breakfastRecipes = recipeDAO.findRecipesByMealType(MealType.BREAKFAST);
        assertNotNull(breakfastRecipes, "Breakfast recipes should not be null");
        assertTrue(breakfastRecipes.size() > 0, "Should find some breakfast recipes");

        // Verify all returned recipes are breakfast
        for (Recipe recipe : breakfastRecipes) {
            assertEquals(MealType.BREAKFAST, recipe.getCategory(), "All recipes should be breakfast");
        }

        System.out.println("Found " + breakfastRecipes.size() + " breakfast recipes");
    }

    @Test
    public void testFindRecipesByFilter() {
        // Test with meal type filter
        List<Recipe> lunchRecipes = recipeDAO.findRecipesByFilter(MealType.LUNCH, null, null, null);
        assertNotNull(lunchRecipes, "Lunch recipes should not be null");
        assertTrue(lunchRecipes.size() > 0, "Should find some lunch recipes");

        System.out.println("Found " + lunchRecipes.size() + " lunch recipes");

        // Test with excluded allergens
        List<String> excludedAllergens = Arrays.asList("milk", "egg", "nuts");
        List<Recipe> nonAllergenicRecipes = recipeDAO.findRecipesByFilter(
                MealType.DINNER, null, null, excludedAllergens);
        assertNotNull(nonAllergenicRecipes, "Non-allergenic recipes should not be null");

        System.out.println("Found " + nonAllergenicRecipes.size() + " dinner recipes without common allergens");
    }

    @Test
    public void testFindRecipesByDietaryRequirements() {
        // Test vegetarian recipes
        List<Recipe> vegetarianRecipes = recipeDAO.findRecipesByDietaryRequirements(
                false, true, false, false);
        assertNotNull(vegetarianRecipes, "Vegetarian recipes should not be null");

        System.out.println("Found " + vegetarianRecipes.size() + " vegetarian recipes");

        // Test diabetic-friendly recipes
        List<Recipe> diabeticFriendlyRecipes = recipeDAO.findRecipesByDietaryRequirements(
                false, false, true, false);
        assertNotNull(diabeticFriendlyRecipes, "Diabetic-friendly recipes should not be null");

        System.out.println("Found " + diabeticFriendlyRecipes.size() + " diabetic-friendly recipes");
    }

    @Test
    public void testFindRecipesByNutrition() {
        // Test recipes with good protein, moderate calories, and fiber
        List<Recipe> proteinRichRecipes = recipeDAO.findRecipesByNutrition(10.0, 500.0, 5.0);
        assertNotNull(proteinRichRecipes, "Protein-rich recipes should not be null");

        System.out.println("Found " + proteinRichRecipes.size() + " protein-rich recipes");

        // Verify nutrition criteria
        for (Recipe recipe : proteinRichRecipes) {
            if (recipe.getNutritionPerServing() != null) {
                assertTrue(recipe.getNutritionPerServing().getProteinG() >= 10.0,
                        "Recipe should have at least 10g protein");
                assertTrue(recipe.getNutritionPerServing().getEnergyKcal() <= 500.0,
                        "Recipe should have at most 500 calories");
                assertTrue(recipe.getNutritionPerServing().getTotalDietaryFibreG() >= 5.0,
                        "Recipe should have at least 5g fiber");
            }
        }
    }

    @Test
    public void testGetSubcategoriesByMealType() {
        List<String> breakfastSubcategories = recipeDAO.getSubcategoriesByMealType(MealType.BREAKFAST);
        assertNotNull(breakfastSubcategories, "Breakfast subcategories should not be null");

        System.out.println("Breakfast subcategories: " + breakfastSubcategories);

        List<String> dinnerSubcategories = recipeDAO.getSubcategoriesByMealType(MealType.DINNER);
        assertNotNull(dinnerSubcategories, "Dinner subcategories should not be null");

        System.out.println("Dinner subcategories: " + dinnerSubcategories);
    }

    @Test
    public void testGetAllTags() {
        List<String> allTags = recipeDAO.getAllTags();
        assertNotNull(allTags, "All tags should not be null");
        assertTrue(allTags.size() > 0, "Should have some tags");

        System.out.println("Total unique tags: " + allTags.size());
        System.out.println("Sample tags: " + allTags.subList(0, Math.min(10, allTags.size())));
    }

    @Test
    public void testFindRecipeById() {
        // First get a recipe to test with
        List<Recipe> recipes = recipeDAO.findRecipesByMealType(MealType.BREAKFAST);
        assertTrue(recipes.size() > 0, "Should have at least one recipe");

        Recipe firstRecipe = recipes.get(0);
        String recipeId = firstRecipe.getRecipeId();

        // Now test finding by ID
        Optional<Recipe> foundRecipe = recipeDAO.findRecipeById(recipeId);
        assertTrue(foundRecipe.isPresent(), "Should find the recipe by ID");
        assertEquals(recipeId, foundRecipe.get().getRecipeId(), "Recipe IDs should match");

        System.out.println("Successfully found recipe by ID: " + foundRecipe.get().getNameEn());
    }

    @Test
    public void testRecipeDataIntegrity() {
        // Test that recipes have all required fields populated
        List<Recipe> recipes = recipeDAO.findRecipesByMealType(MealType.LUNCH);
        assertTrue(recipes.size() > 0, "Should have at least one recipe");

        Recipe recipe = recipes.get(0);

        // Test basic fields
        assertNotNull(recipe.getRecipeId(), "Recipe ID should not be null");
        assertNotNull(recipe.getNameEn(), "Recipe name (English) should not be null");
        assertNotNull(recipe.getCategory(), "Recipe category should not be null");

        // Test JSON parsing
        assertNotNull(recipe.getTags(), "Tags should not be null");
        assertNotNull(recipe.getIngredients(), "Ingredients should not be null");
        assertNotNull(recipe.getInstructionsEn(), "Instructions (English) should not be null");

        // Test nutrition info
        assertNotNull(recipe.getNutritionPerServing(), "Nutrition info should not be null");
        assertNotNull(recipe.getNutritionPerServing().getAllNutrients(), "All nutrients map should not be null");

        System.out.println("Recipe: " + recipe.getNameEn());
        System.out.println("Tags: " + recipe.getTags());
        System.out.println("Ingredients count: " + recipe.getIngredients().size());
        System.out.println("Instructions count: " + recipe.getInstructionsEn().size());
        System.out.println("Nutrition - Calories: " + recipe.getNutritionPerServing().getEnergyKcal());
        System.out.println("Nutrition - Protein: " + recipe.getNutritionPerServing().getProteinG());
        System.out.println("Total nutrients tracked: " + recipe.getNutritionPerServing().getAllNutrients().size());
    }
}
