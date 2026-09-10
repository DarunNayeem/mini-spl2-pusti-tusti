package com.mealplanner.service;

import com.mealplanner.model.Gender;
import com.mealplanner.model.HealthGoal;
import com.mealplanner.model.MedicalCondition;
import com.mealplanner.model.user.NutritionProfile;
import com.mealplanner.model.user.UserProfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for calculating personalized nutritional requirements
 * based on user profile, health goals, and medical conditions.
 */
public class NutritionCalculatorService {

    /**
     * Calculate comprehensive nutritional needs for a user
     * 
     * @param user User profile containing personal information and health goals
     * @return Complete nutrition profile with calculated requirements
     */
    public NutritionProfile calculateNutritionalNeeds(UserProfile user) {
        // Step 1: Calculate BMI
        double bmi = calculateBMI(user.getHeightCm(), user.getWeightKg());

        // Step 2: Calculate BMR (based on age)
        double bmr = calculateBMR(user);

        // Step 3: Calculate TDEE
        double tdee = calculateTDEE(bmr, user.getActivityLevel());

        // Step 4: Adjust calories for health goal
        double targetCalories = adjustCaloriesForHealthGoal(tdee, user.getPrimaryHealthGoal());

        // Step 5: Distribute macronutrients
        MacronutrientDistribution macros = calculateMacronutrients(targetCalories, user.getPrimaryHealthGoal());

        // Step 6: Set micronutrient goals and limits
        Map<String, Double> micronutrientGoals = calculateMicronutrientGoals(user);
        Map<String, Double> micronutrientLimits = calculateMicronutrientLimits(user);

        // Step 7: Return completed NutritionProfile
        return new NutritionProfile(
                bmi,
                bmr,
                tdee,
                targetCalories,
                macros.proteinGrams,
                macros.carbsGrams,
                macros.fatGrams,
                micronutrientGoals,
                micronutrientLimits);
    }

    /**
     * Calculate Body Mass Index (BMI)
     * Formula: BMI = weight / (height in meters)^2
     */
    private double calculateBMI(double heightCm, double weightKg) {
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    /**
     * Calculate Basal Metabolic Rate (BMR)
     * Uses Schofield equation for age <= 18, Mifflin-St Jeor for age > 18
     */
    private double calculateBMR(UserProfile user) {
        double weight = user.getWeightKg();
        double height = user.getHeightCm();
        int age = user.getAge();
        Gender gender = user.getGender();

        if (age <= 18) {
            return calculateSchofield(weight, age, gender);
        } else {
            return calculateMifflinStJeor(weight, height, age, gender);
        }
    }

    /**
     * Calculate BMR using Schofield equation (for age <= 18)
     */
    private double calculateSchofield(double weight, int age, Gender gender) {
        if (gender == Gender.MALE) {
            if (age <= 3) {
                return 59.512 * weight - 30.4;
            } else if (age <= 10) {
                return 22.706 * weight + 504.3;
            } else { // 10-18 years
                return 17.686 * weight + 658.2;
            }
        } else { // FEMALE
            if (age <= 3) {
                return 58.317 * weight - 31.1;
            } else if (age <= 10) {
                return 20.315 * weight + 485.9;
            } else { // 10-18 years
                return 13.384 * weight + 692.6;
            }
        }
    }

    /**
     * Calculate BMR using Mifflin-St Jeor equation (for age > 18)
     */
    private double calculateMifflinStJeor(double weight, double height, int age, Gender gender) {
        double bmr = 10 * weight + 6.25 * height - 5 * age;

        if (gender == Gender.MALE) {
            return bmr + 5;
        } else {
            return bmr - 161;
        }
    }

    /**
     * Calculate Total Daily Energy Expenditure (TDEE)
     * Multiply BMR by activity level multiplier
     */
    private double calculateTDEE(double bmr, int activityLevel) {
        double multiplier = switch (activityLevel) {
            case 1 -> 1.2; // Sedentary
            case 2 -> 1.375; // Lightly active
            case 3 -> 1.55; // Moderately active
            case 4 -> 1.725; // Very active
            case 5 -> 1.9; // Extremely active
            default -> 1.2; // Default to sedentary
        };

        return bmr * multiplier;
    }

    /**
     * Adjust calories based on health goal
     */
    private double adjustCaloriesForHealthGoal(double tdee, HealthGoal healthGoal) {
        return switch (healthGoal) {
            case WEIGHT_LOSS -> tdee * 0.8;
            case WEIGHT_GAIN -> tdee * 1.2;
            case MUSCLE_GAIN -> tdee * 1.1 + 300;
            case DIABETES_CONTROL -> tdee * 0.9;
            case PREGNANCY_NUTRITION -> tdee + 450;
            case ELDERLY_NUTRITION -> tdee * 1.0;
            case HYPERTENSION_CONTROL -> tdee * 0.9;
            case CHILD_NUTRITION -> tdee * 1.0; // Already calculated with child BMR
            case ANEMIA_CONTROL -> tdee * 1.0;
            case MAINTENANCE -> tdee * 1.0;
        };
    }

    /**
     * Calculate macronutrient distribution based on health goal
     */
    private MacronutrientDistribution calculateMacronutrients(double targetCalories, HealthGoal healthGoal) {
        MacronutrientPercentages percentages = getMacronutrientPercentages(healthGoal);

        // Calculate calories for each macronutrient
        double proteinCalories = targetCalories * percentages.protein;
        double fatCalories = targetCalories * percentages.fat;
        double carbsCalories = targetCalories * percentages.carbohydrates;

        // Convert to grams (protein: 4 cal/g, fat: 9 cal/g, carbs: 4 cal/g)
        double proteinGrams = proteinCalories / 4.0;
        double fatGrams = fatCalories / 9.0;
        double carbsGrams = carbsCalories / 4.0;

        return new MacronutrientDistribution(proteinGrams, fatGrams, carbsGrams);
    }

    /**
     * Get macronutrient percentages based on health goal
     */
    private MacronutrientPercentages getMacronutrientPercentages(HealthGoal healthGoal) {
        return switch (healthGoal) {
            case WEIGHT_LOSS -> new MacronutrientPercentages(0.30, 0.25, 0.45);
            case WEIGHT_GAIN -> new MacronutrientPercentages(0.20, 0.35, 0.45);
            case MUSCLE_GAIN -> new MacronutrientPercentages(0.35, 0.25, 0.40);
            case DIABETES_CONTROL -> new MacronutrientPercentages(0.25, 0.30, 0.45);
            case PREGNANCY_NUTRITION -> new MacronutrientPercentages(0.20, 0.30, 0.50);
            case CHILD_NUTRITION -> new MacronutrientPercentages(0.175, 0.325, 0.525); // Average of 15-20%, 30-35%,
                                                                                       // 50-55%
            case ELDERLY_NUTRITION -> new MacronutrientPercentages(0.25, 0.30, 0.45);
            case HYPERTENSION_CONTROL -> new MacronutrientPercentages(0.20, 0.30, 0.50);
            case ANEMIA_CONTROL -> new MacronutrientPercentages(0.20, 0.30, 0.50);
            case MAINTENANCE -> new MacronutrientPercentages(0.20, 0.30, 0.50);
        };
    }

    /**
     * Calculate micronutrient goals based on health conditions and goals
     */
    private Map<String, Double> calculateMicronutrientGoals(UserProfile user) {
        Map<String, Double> goals = new HashMap<>();

        // Base requirements for all users
        setBaseRequirements(goals, user);

        // Health goal specific requirements
        setHealthGoalRequirements(goals, user.getPrimaryHealthGoal(), user.getGender());

        // Medical condition specific requirements
        List<MedicalCondition> conditions = user.getMedicalConditions();
        if (conditions != null) {
            for (MedicalCondition condition : conditions) {
                setMedicalConditionRequirements(goals, condition, user.getGender());
            }
        }

        return goals;
    }

    /**
     * Calculate micronutrient limits based on health conditions
     */
    private Map<String, Double> calculateMicronutrientLimits(UserProfile user) {
        Map<String, Double> limits = new HashMap<>();

        // Medical condition specific limits
        List<MedicalCondition> conditions = user.getMedicalConditions();
        if (conditions != null) {
            boolean hasHypertension = conditions.contains(MedicalCondition.HYPERTENSION);
            boolean hasDiabetes = conditions.contains(MedicalCondition.DIABETES);

            // Hypertension has stricter sodium limits than diabetes
            if (hasHypertension) {
                limits.put("sodium_mg", 1500.0);
            } else if (hasDiabetes) {
                limits.put("sodium_mg", 2300.0);
            }
        }

        return limits;
    }

    /**
     * Set base micronutrient requirements for all users
     */
    private void setBaseRequirements(Map<String, Double> goals, UserProfile user) {
        Gender gender = user.getGender();
        int age = user.getAge();

        // Basic requirements that apply to everyone
        goals.put("vitamin_c_mg", 90.0);
        goals.put("calcium_mg", age >= 65 ? 1200.0 : 1000.0);
        goals.put("iron_mg", gender == Gender.FEMALE ? 18.0 : 8.0);
        goals.put("magnesium_mg", 420.0);
        goals.put("potassium_mg", 4700.0);
        goals.put("zinc_mg", 11.0);
        goals.put("vitamin_b12_mcg", 2.4);
        goals.put("folate_mcg", 400.0);
        goals.put("total_dietary_fibre_g", 25.0);
    }

    /**
     * Set health goal specific requirements
     */
    private void setHealthGoalRequirements(Map<String, Double> goals, HealthGoal healthGoal, Gender gender) {
        switch (healthGoal) {
            case WEIGHT_LOSS:
                goals.put("total_dietary_fibre_g", 30.0);
                goals.put("iron_mg", gender == Gender.FEMALE ? 18.0 : 8.0);
                break;

            case WEIGHT_GAIN:
                // No specific additional requirements beyond base
                break;

            case MUSCLE_GAIN:
                goals.put("magnesium_mg", 400.0);
                goals.put("zinc_mg", 11.0);
                break;

            case DIABETES_CONTROL:
                goals.put("chromium_mcg", 35.0);
                goals.put("magnesium_mg", 420.0);
                break;

            case PREGNANCY_NUTRITION:
                goals.put("folate_mcg", 600.0);
                goals.put("iron_mg", 27.0);
                goals.put("calcium_mg", 1000.0);
                break;

            case ELDERLY_NUTRITION:
                goals.put("vitamin_b12_mcg", 2.4);
                goals.put("calcium_mg", 1200.0);
                break;

            case CHILD_NUTRITION:
                goals.put("iron_mg", 10.0); // Average for children
                break;

            case HYPERTENSION_CONTROL:
                goals.put("potassium_mg", 4700.0);
                goals.put("magnesium_mg", 420.0);
                break;

            case ANEMIA_CONTROL:
                goals.put("iron_mg", gender == Gender.FEMALE ? 18.0 : 8.0);
                goals.put("vitamin_c_mg", 90.0);
                break;

            case MAINTENANCE:
                // No specific additional requirements beyond base
                break;
        }
    }

    /**
     * Set medical condition specific requirements
     */
    private void setMedicalConditionRequirements(Map<String, Double> goals, MedicalCondition condition, Gender gender) {
        switch (condition) {
            case DIABETES:
                goals.put("chromium_mcg", 35.0);
                goals.put("magnesium_mg", 420.0);
                break;

            case HYPERTENSION:
                goals.put("potassium_mg", 4700.0);
                goals.put("magnesium_mg", 420.0);
                break;

            case PREGNANCY:
                goals.put("folate_mcg", 600.0);
                goals.put("iron_mg", 27.0);
                goals.put("calcium_mg", 1000.0);
                break;

            case ELDERLY:
                goals.put("vitamin_b12_mcg", 2.4);
                goals.put("calcium_mg", 1200.0);
                break;

            case CHILD:
                goals.put("iron_mg", 10.0);
                break;

            case HIGH_CHOLESTEROL:
                goals.put("total_dietary_fibre_g", 30.0);
                break;

            case ANEMIA:
                goals.put("iron_mg", gender == Gender.FEMALE ? 18.0 : 8.0);
                goals.put("vitamin_c_mg", 90.0);
                break;

            case NONE:
                // No specific additional requirements
                break;
        }
    }

    /**
     * Helper class for macronutrient percentages
     */
    private record MacronutrientPercentages(double protein, double fat, double carbohydrates) {
    }

    /**
     * Helper class for macronutrient distribution in grams
     */
    private record MacronutrientDistribution(double proteinGrams, double fatGrams, double carbsGrams) {
    }
}
