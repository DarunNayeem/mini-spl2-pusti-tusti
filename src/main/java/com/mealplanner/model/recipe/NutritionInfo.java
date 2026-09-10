package com.mealplanner.model.recipe;

import java.util.Map;

/**
 * Nutrition information for a recipe or ingredient
 */
public class NutritionInfo {
    // Core nutritional values for quick display
    private double energyKcal;
    private double proteinG;
    private double fatG;
    private double carbohydrateAvailableG;
    private double totalDietaryFibreG;
    private double waterG;

    // Expandable map for all 27 nutrients from the database
    private Map<String, Double> allNutrients;

    // Default constructor
    public NutritionInfo() {
    }

    // Constructor with core nutrients
    public NutritionInfo(double energyKcal, double proteinG, double fatG,
            double carbohydrateAvailableG, double totalDietaryFibreG,
            double waterG) {
        this.energyKcal = energyKcal;
        this.proteinG = proteinG;
        this.fatG = fatG;
        this.carbohydrateAvailableG = carbohydrateAvailableG;
        this.totalDietaryFibreG = totalDietaryFibreG;
        this.waterG = waterG;
    }

    // Constructor with all fields
    public NutritionInfo(double energyKcal, double proteinG, double fatG,
            double carbohydrateAvailableG, double totalDietaryFibreG,
            double waterG, Map<String, Double> allNutrients) {
        this.energyKcal = energyKcal;
        this.proteinG = proteinG;
        this.fatG = fatG;
        this.carbohydrateAvailableG = carbohydrateAvailableG;
        this.totalDietaryFibreG = totalDietaryFibreG;
        this.waterG = waterG;
        this.allNutrients = allNutrients;
    }

    // Getters and Setters
    public double getEnergyKcal() {
        return energyKcal;
    }

    public void setEnergyKcal(double energyKcal) {
        this.energyKcal = energyKcal;
    }

    public double getProteinG() {
        return proteinG;
    }

    public void setProteinG(double proteinG) {
        this.proteinG = proteinG;
    }

    public double getFatG() {
        return fatG;
    }

    public void setFatG(double fatG) {
        this.fatG = fatG;
    }

    public double getCarbohydrateAvailableG() {
        return carbohydrateAvailableG;
    }

    public void setCarbohydrateAvailableG(double carbohydrateAvailableG) {
        this.carbohydrateAvailableG = carbohydrateAvailableG;
    }

    public double getTotalDietaryFibreG() {
        return totalDietaryFibreG;
    }

    public void setTotalDietaryFibreG(double totalDietaryFibreG) {
        this.totalDietaryFibreG = totalDietaryFibreG;
    }

    public double getWaterG() {
        return waterG;
    }

    public void setWaterG(double waterG) {
        this.waterG = waterG;
    }

    public Map<String, Double> getAllNutrients() {
        return allNutrients;
    }

    public void setAllNutrients(Map<String, Double> allNutrients) {
        this.allNutrients = allNutrients;
    }

    @Override
    public String toString() {
        return "NutritionInfo{" +
                "energyKcal=" + energyKcal +
                ", proteinG=" + proteinG +
                ", fatG=" + fatG +
                ", carbohydrateAvailableG=" + carbohydrateAvailableG +
                ", totalDietaryFibreG=" + totalDietaryFibreG +
                ", waterG=" + waterG +
                ", allNutrients=" + allNutrients +
                '}';
    }
}
