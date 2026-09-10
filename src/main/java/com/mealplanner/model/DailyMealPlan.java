package com.mealplanner.model;

import com.mealplanner.model.recipe.NutritionInfo;

import java.time.LocalDate;

/**
 * Represents a complete day of meals with total nutrition
 */
public class DailyMealPlan {
    private LocalDate date;
    private Meal breakfast;
    private Meal lunch;
    private Meal dinner;
    private Meal snacks;
    private NutritionInfo dailyTotalNutrition;

    // Default constructor
    public DailyMealPlan() {
    }

    // Constructor with all fields
    public DailyMealPlan(LocalDate date, Meal breakfast, Meal lunch, Meal dinner,
            Meal snacks, NutritionInfo dailyTotalNutrition) {
        this.date = date;
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.dinner = dinner;
        this.snacks = snacks;
        this.dailyTotalNutrition = dailyTotalNutrition;
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Meal getBreakfast() {
        return breakfast;
    }

    public void setBreakfast(Meal breakfast) {
        this.breakfast = breakfast;
    }

    public Meal getLunch() {
        return lunch;
    }

    public void setLunch(Meal lunch) {
        this.lunch = lunch;
    }

    public Meal getDinner() {
        return dinner;
    }

    public void setDinner(Meal dinner) {
        this.dinner = dinner;
    }

    public Meal getSnacks() {
        return snacks;
    }

    public void setSnacks(Meal snacks) {
        this.snacks = snacks;
    }

    public NutritionInfo getDailyTotalNutrition() {
        return dailyTotalNutrition;
    }

    public void setDailyTotalNutrition(NutritionInfo dailyTotalNutrition) {
        this.dailyTotalNutrition = dailyTotalNutrition;
    }

    @Override
    public String toString() {
        return "DailyMealPlan{" +
                "date=" + date +
                ", breakfast=" + breakfast +
                ", lunch=" + lunch +
                ", dinner=" + dinner +
                ", snacks=" + snacks +
                ", dailyTotalNutrition=" + dailyTotalNutrition +
                '}';
    }
}
