package com.mealplanner.model.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ingredient with food information and quantity
 */
public class Ingredient {
    @JsonProperty("food_id")
    private String foodId;

    @JsonProperty("name_en")
    private String nameEn;

    @JsonProperty("quantity")
    private double quantityGrams;

    @JsonProperty("unit")
    private String unit;

    // Default constructor
    public Ingredient() {
    }

    // Constructor with all fields
    public Ingredient(String foodId, String nameEn, double quantityGrams, String unit) {
        this.foodId = foodId;
        this.nameEn = nameEn;
        this.quantityGrams = quantityGrams;
        this.unit = unit;
    }

    // Getters and Setters
    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public double getQuantityGrams() {
        return quantityGrams;
    }

    public void setQuantityGrams(double quantityGrams) {
        this.quantityGrams = quantityGrams;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "foodId='" + foodId + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", quantityGrams=" + quantityGrams +
                ", unit='" + unit + '\'' +
                '}';
    }
}
