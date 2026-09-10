package com.mealplanner.model;

import com.mealplanner.model.recipe.NutritionInfo;
import com.mealplanner.model.recipe.Recipe;

import java.util.List;

/**
 * Represents a single mealtime with recipes and nutrition information
 */
public class Meal {
    private MealType mealType;
    private List<Recipe> recipes;
    private NutritionInfo mealNutrition;

    // Default constructor
    public Meal() {
    }

    // Constructor with all fields
    public Meal(MealType mealType, List<Recipe> recipes, NutritionInfo mealNutrition) {
        this.mealType = mealType;
        this.recipes = recipes;
        this.mealNutrition = mealNutrition;
    }

    // Getters and Setters
    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
    }

    public NutritionInfo getMealNutrition() {
        return mealNutrition;
    }

    public void setMealNutrition(NutritionInfo mealNutrition) {
        this.mealNutrition = mealNutrition;
    }

    @Override
    public String toString() {
        return "Meal{" +
                "mealType=" + mealType +
                ", recipes=" + recipes +
                ", mealNutrition=" + mealNutrition +
                '}';
    }
}
