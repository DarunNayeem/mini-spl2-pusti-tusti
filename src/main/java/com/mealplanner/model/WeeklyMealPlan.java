package com.mealplanner.model;

import com.mealplanner.model.user.NutritionProfile;
import com.mealplanner.model.user.UserProfile;

import java.util.List;

/**
 * Represents a complete 7-day meal plan with user profile and nutrition targets
 */
public class WeeklyMealPlan {
    private UserProfile user;
    private NutritionProfile nutritionProfile;
    private List<DailyMealPlan> dailyPlans;

    // Default constructor
    public WeeklyMealPlan() {
    }

    // Constructor with all fields
    public WeeklyMealPlan(UserProfile user, NutritionProfile nutritionProfile,
            List<DailyMealPlan> dailyPlans) {
        this.user = user;
        this.nutritionProfile = nutritionProfile;
        this.dailyPlans = dailyPlans;
    }

    // Getters and Setters
    public UserProfile getUser() {
        return user;
    }

    public void setUser(UserProfile user) {
        this.user = user;
    }

    public NutritionProfile getNutritionProfile() {
        return nutritionProfile;
    }

    public void setNutritionProfile(NutritionProfile nutritionProfile) {
        this.nutritionProfile = nutritionProfile;
    }

    public List<DailyMealPlan> getDailyPlans() {
        return dailyPlans;
    }

    public void setDailyPlans(List<DailyMealPlan> dailyPlans) {
        this.dailyPlans = dailyPlans;
    }

    @Override
    public String toString() {
        return "WeeklyMealPlan{" +
                "user=" + user +
                ", nutritionProfile=" + nutritionProfile +
                ", dailyPlans=" + dailyPlans +
                '}';
    }
}
