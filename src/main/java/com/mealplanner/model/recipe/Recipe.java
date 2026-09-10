package com.mealplanner.model.recipe;

import com.mealplanner.model.MealType;

import java.util.List;

/**
 * Recipe model containing all recipe information
 */
public class Recipe {
    private String recipeId;
    private String nameEn;
    private String nameBn;
    private int servings;
    private MealType category;
    private String subcategory;
    private int cookingTimeMinutes;
    private int preparationTimeHours;
    private double yieldFactor;
    private String cuisine;
    private String foreignOrigin;
    private List<String> tags;
    private List<Ingredient> ingredients;
    private NutritionInfo nutritionPerServing;
    private List<String> instructionsEn;
    private List<String> instructionsBn;

    // Default constructor
    public Recipe() {
    }

    // Constructor with all fields
    public Recipe(String recipeId, String nameEn, String nameBn, int servings,
            MealType category, String subcategory, int cookingTimeMinutes,
            int preparationTimeHours, double yieldFactor, String cuisine,
            String foreignOrigin, List<String> tags, List<Ingredient> ingredients,
            NutritionInfo nutritionPerServing, List<String> instructionsEn,
            List<String> instructionsBn) {
        this.recipeId = recipeId;
        this.nameEn = nameEn;
        this.nameBn = nameBn;
        this.servings = servings;
        this.category = category;
        this.subcategory = subcategory;
        this.cookingTimeMinutes = cookingTimeMinutes;
        this.preparationTimeHours = preparationTimeHours;
        this.yieldFactor = yieldFactor;
        this.cuisine = cuisine;
        this.foreignOrigin = foreignOrigin;
        this.tags = tags;
        this.ingredients = ingredients;
        this.nutritionPerServing = nutritionPerServing;
        this.instructionsEn = instructionsEn;
        this.instructionsBn = instructionsBn;
    }

    // Getters and Setters
    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameBn() {
        return nameBn;
    }

    public void setNameBn(String nameBn) {
        this.nameBn = nameBn;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public MealType getCategory() {
        return category;
    }

    public void setCategory(MealType category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public int getCookingTimeMinutes() {
        return cookingTimeMinutes;
    }

    public void setCookingTimeMinutes(int cookingTimeMinutes) {
        this.cookingTimeMinutes = cookingTimeMinutes;
    }

    public int getPreparationTimeHours() {
        return preparationTimeHours;
    }

    public void setPreparationTimeHours(int preparationTimeHours) {
        this.preparationTimeHours = preparationTimeHours;
    }

    public double getYieldFactor() {
        return yieldFactor;
    }

    public void setYieldFactor(double yieldFactor) {
        this.yieldFactor = yieldFactor;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public String getForeignOrigin() {
        return foreignOrigin;
    }

    public void setForeignOrigin(String foreignOrigin) {
        this.foreignOrigin = foreignOrigin;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public NutritionInfo getNutritionPerServing() {
        return nutritionPerServing;
    }

    public void setNutritionPerServing(NutritionInfo nutritionPerServing) {
        this.nutritionPerServing = nutritionPerServing;
    }

    public List<String> getInstructionsEn() {
        return instructionsEn;
    }

    public void setInstructionsEn(List<String> instructionsEn) {
        this.instructionsEn = instructionsEn;
    }

    public List<String> getInstructionsBn() {
        return instructionsBn;
    }

    public void setInstructionsBn(List<String> instructionsBn) {
        this.instructionsBn = instructionsBn;
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "recipeId='" + recipeId + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", nameBn='" + nameBn + '\'' +
                ", servings=" + servings +
                ", category=" + category +
                ", subcategory='" + subcategory + '\'' +
                ", cookingTimeMinutes=" + cookingTimeMinutes +
                ", preparationTimeHours=" + preparationTimeHours +
                ", yieldFactor=" + yieldFactor +
                ", cuisine='" + cuisine + '\'' +
                ", foreignOrigin='" + foreignOrigin + '\'' +
                ", tags=" + tags +
                ", ingredients=" + ingredients +
                ", nutritionPerServing=" + nutritionPerServing +
                ", instructionsEn=" + instructionsEn +
                ", instructionsBn=" + instructionsBn +
                '}';
    }
}
