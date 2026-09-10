package com.mealplanner.model.user;

import java.util.Map;

/**
 * Nutrition profile containing calculated nutritional requirements and goals
 */
public class NutritionProfile {
    private double bmi;
    private double bmr;
    private double tdee;
    private double targetCalories;
    private double proteinGrams;
    private double carbsGrams;
    private double fatGrams;
    private Map<String, Double> micronutrientGoals; // e.g., "iron_mg" -> 18.0
    private Map<String, Double> micronutrientLimits; // e.g., "sodium_mg" -> 1500.0

    // Default constructor
    public NutritionProfile() {
    }

    // Constructor with all fields
    public NutritionProfile(double bmi, double bmr, double tdee, double targetCalories,
            double proteinGrams, double carbsGrams, double fatGrams,
            Map<String, Double> micronutrientGoals, Map<String, Double> micronutrientLimits) {
        this.bmi = bmi;
        this.bmr = bmr;
        this.tdee = tdee;
        this.targetCalories = targetCalories;
        this.proteinGrams = proteinGrams;
        this.carbsGrams = carbsGrams;
        this.fatGrams = fatGrams;
        this.micronutrientGoals = micronutrientGoals;
        this.micronutrientLimits = micronutrientLimits;
    }

    // Getters and Setters
    public double getBmi() {
        return bmi;
    }

    public void setBmi(double bmi) {
        this.bmi = bmi;
    }

    public double getBmr() {
        return bmr;
    }

    public void setBmr(double bmr) {
        this.bmr = bmr;
    }

    public double getTdee() {
        return tdee;
    }

    public void setTdee(double tdee) {
        this.tdee = tdee;
    }

    public double getTargetCalories() {
        return targetCalories;
    }

    public void setTargetCalories(double targetCalories) {
        this.targetCalories = targetCalories;
    }

    public double getProteinGrams() {
        return proteinGrams;
    }

    public void setProteinGrams(double proteinGrams) {
        this.proteinGrams = proteinGrams;
    }

    public double getCarbsGrams() {
        return carbsGrams;
    }

    public void setCarbsGrams(double carbsGrams) {
        this.carbsGrams = carbsGrams;
    }

    public double getFatGrams() {
        return fatGrams;
    }

    public void setFatGrams(double fatGrams) {
        this.fatGrams = fatGrams;
    }

    public Map<String, Double> getMicronutrientGoals() {
        return micronutrientGoals;
    }

    public void setMicronutrientGoals(Map<String, Double> micronutrientGoals) {
        this.micronutrientGoals = micronutrientGoals;
    }

    public Map<String, Double> getMicronutrientLimits() {
        return micronutrientLimits;
    }

    public void setMicronutrientLimits(Map<String, Double> micronutrientLimits) {
        this.micronutrientLimits = micronutrientLimits;
    }

    @Override
    public String toString() {
        return "NutritionProfile{" +
                "bmi=" + bmi +
                ", bmr=" + bmr +
                ", tdee=" + tdee +
                ", targetCalories=" + targetCalories +
                ", proteinGrams=" + proteinGrams +
                ", carbsGrams=" + carbsGrams +
                ", fatGrams=" + fatGrams +
                ", micronutrientGoals=" + micronutrientGoals +
                ", micronutrientLimits=" + micronutrientLimits +
                '}';
    }
}
