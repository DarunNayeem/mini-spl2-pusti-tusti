package com.mealplanner.service;

import com.mealplanner.model.Gender;
import com.mealplanner.model.HealthGoal;
import com.mealplanner.model.MedicalCondition;
import com.mealplanner.model.user.NutritionProfile;
import com.mealplanner.model.user.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class NutritionCalculatorServiceTest {

    private NutritionCalculatorService nutritionCalculator;

    @BeforeEach
    void setUp() {
        nutritionCalculator = new NutritionCalculatorService();
    }

    private UserProfile createTestUser(String userId, String name, int age, Gender gender,
            double heightCm, double weightKg, HealthGoal primaryHealthGoal,
            List<MedicalCondition> medicalConditions, int activityLevel) {
        return new UserProfile(userId, name, age, heightCm, weightKg, gender, primaryHealthGoal,
                List.of(), // allergies
                new HashMap<>(), // mealPreferences
                medicalConditions, activityLevel);
    }

    @Test
    void testCalculateNutritionalNeedsForWeightLoss() {
        // Given: A 30-year-old female with weight loss goal
        UserProfile user = createTestUser("jane001", "Jane Doe", 30, Gender.FEMALE, 165.0, 70.0,
                HealthGoal.WEIGHT_LOSS, List.of(MedicalCondition.NONE), 3);

        // When: Calculate nutritional needs
        NutritionProfile profile = nutritionCalculator.calculateNutritionalNeeds(user);

        // Then: Verify calculations
        assertNotNull(profile);

        // BMI should be calculated correctly: 70 / (1.65^2) = 25.71
        assertEquals(25.71, profile.getBmi(), 0.01);

        // BMR should be calculated using Mifflin-St Jeor for age > 18
        // Female: BMR = 10 * 70 + 6.25 * 165 - 5 * 30 - 161 = 1420.25
        assertEquals(1420.25, profile.getBmr(), 0.01);

        // TDEE should be BMR * 1.55 (activity level 3)
        assertEquals(2201.39, profile.getTdee(), 0.01);

        // Target calories for weight loss: TDEE * 0.8
        assertEquals(1761.11, profile.getTargetCalories(), 0.01);

        // Macronutrients for weight loss: 30% protein, 25% fat, 45% carbs
        assertEquals(132.08, profile.getProteinGrams(), 0.01);
        assertEquals(48.92, profile.getFatGrams(), 0.01);
        assertEquals(198.12, profile.getCarbsGrams(), 0.01);

        // Micronutrients should include weight loss specific requirements
        Map<String, Double> goals = profile.getMicronutrientGoals();
        assertEquals(30.0, goals.get("total_dietary_fibre_g"));
        assertEquals(18.0, goals.get("iron_mg")); // Female requirement
    }

    @Test
    void testCalculateNutritionalNeedsForMuscleGain() {
        // Given: A 25-year-old male with muscle gain goal
        UserProfile user = createTestUser("john001", "John Doe", 25, Gender.MALE, 180.0, 80.0,
                HealthGoal.MUSCLE_GAIN, List.of(MedicalCondition.NONE), 4);

        // When: Calculate nutritional needs
        NutritionProfile profile = nutritionCalculator.calculateNutritionalNeeds(user);

        // Then: Verify calculations
        assertNotNull(profile);

        // BMI should be calculated correctly: 80 / (1.8^2) = 24.69
        assertEquals(24.69, profile.getBmi(), 0.01);

        // BMR should be calculated using Mifflin-St Jeor for age > 18
        // Male: BMR = 10 * 80 + 6.25 * 180 - 5 * 25 + 5 = 1805
        assertEquals(1805.0, profile.getBmr(), 0.01);

        // TDEE should be BMR * 1.725 (activity level 4)
        assertEquals(3113.63, profile.getTdee(), 0.01);

        // Target calories for muscle gain: TDEE * 1.1 + 300
        assertEquals(3724.99, profile.getTargetCalories(), 0.01);

        // Macronutrients for muscle gain: 35% protein, 25% fat, 40% carbs
        assertEquals(325.94, profile.getProteinGrams(), 0.01);
        assertEquals(103.47, profile.getFatGrams(), 0.01);
        assertEquals(372.50, profile.getCarbsGrams(), 0.01);

        // Micronutrients should include muscle gain specific requirements
        Map<String, Double> goals = profile.getMicronutrientGoals();
        assertEquals(400.0, goals.get("magnesium_mg"));
        assertEquals(11.0, goals.get("zinc_mg"));
        assertEquals(8.0, goals.get("iron_mg")); // Male requirement
    }

    @Test
    void testCalculateNutritionalNeedsForChild() {
        // Given: A 12-year-old male child
        UserProfile user = createTestUser("tommy001", "Tommy Smith", 12, Gender.MALE, 140.0, 40.0,
                HealthGoal.CHILD_NUTRITION, List.of(MedicalCondition.CHILD), 2);

        // When: Calculate nutritional needs
        NutritionProfile profile = nutritionCalculator.calculateNutritionalNeeds(user);

        // Then: Verify calculations
        assertNotNull(profile);

        // BMI should be calculated correctly: 40 / (1.4^2) = 20.41
        assertEquals(20.41, profile.getBmi(), 0.01);

        // BMR should be calculated using Schofield for age <= 18
        // Male 10-18 years: BMR = 17.686 * 40 + 658.2 = 1365.64
        assertEquals(1365.64, profile.getBmr(), 0.01);

        // TDEE should be BMR * 1.375 (activity level 2)
        assertEquals(1877.76, profile.getTdee(), 0.01);

        // Target calories for child nutrition: TDEE * 1.0
        assertEquals(1877.76, profile.getTargetCalories(), 0.01);

        // Macronutrients for child nutrition: 17.5% protein, 32.5% fat, 52.5% carbs
        assertEquals(82.15, profile.getProteinGrams(), 0.01);
        assertEquals(67.81, profile.getFatGrams(), 0.01);
        assertEquals(246.46, profile.getCarbsGrams(), 0.01);

        // Micronutrients should include child specific requirements
        Map<String, Double> goals = profile.getMicronutrientGoals();
        assertEquals(10.0, goals.get("iron_mg")); // Child requirement
    }

    @Test
    void testCalculateNutritionalNeedsWithHypertension() {
        // Given: A 45-year-old female with hypertension
        UserProfile user = createTestUser("mary001", "Mary Johnson", 45, Gender.FEMALE, 160.0, 65.0,
                HealthGoal.HYPERTENSION_CONTROL, List.of(MedicalCondition.HYPERTENSION), 3);

        // When: Calculate nutritional needs
        NutritionProfile profile = nutritionCalculator.calculateNutritionalNeeds(user);

        // Then: Verify calculations
        assertNotNull(profile);

        // Target calories for hypertension: TDEE * 0.9
        assertTrue(profile.getTargetCalories() < profile.getTdee());

        // Micronutrients should include hypertension specific requirements
        Map<String, Double> goals = profile.getMicronutrientGoals();
        assertEquals(4700.0, goals.get("potassium_mg"));
        assertEquals(420.0, goals.get("magnesium_mg"));

        // Limits should include sodium restriction
        Map<String, Double> limits = profile.getMicronutrientLimits();
        assertEquals(1500.0, limits.get("sodium_mg"));
    }

    @Test
    void testCalculateNutritionalNeedsWithPregnancy() {
        // Given: A 28-year-old female with pregnancy
        UserProfile user = createTestUser("sarah001", "Sarah Wilson", 28, Gender.FEMALE, 165.0, 70.0,
                HealthGoal.PREGNANCY_NUTRITION, List.of(MedicalCondition.PREGNANCY), 2);

        // When: Calculate nutritional needs
        NutritionProfile profile = nutritionCalculator.calculateNutritionalNeeds(user);

        // Then: Verify calculations
        assertNotNull(profile);

        // Target calories for pregnancy: TDEE + 450
        assertEquals(profile.getTdee() + 450, profile.getTargetCalories(), 0.01);

        // Micronutrients should include pregnancy specific requirements
        Map<String, Double> goals = profile.getMicronutrientGoals();
        assertEquals(600.0, goals.get("folate_mcg"));
        assertEquals(27.0, goals.get("iron_mg"));
        assertEquals(1000.0, goals.get("calcium_mg"));
    }

    @Test
    void testCalculateNutritionalNeedsWithDiabetes() {
        // Given: A 50-year-old male with diabetes
        UserProfile user = createTestUser("robert001", "Robert Brown", 50, Gender.MALE, 175.0, 85.0,
                HealthGoal.DIABETES_CONTROL, List.of(MedicalCondition.DIABETES), 2);

        // When: Calculate nutritional needs
        NutritionProfile profile = nutritionCalculator.calculateNutritionalNeeds(user);

        // Then: Verify calculations
        assertNotNull(profile);

        // Target calories for diabetes: TDEE * 0.9
        assertEquals(profile.getTdee() * 0.9, profile.getTargetCalories(), 0.01);

        // Micronutrients should include diabetes specific requirements
        Map<String, Double> goals = profile.getMicronutrientGoals();
        assertEquals(35.0, goals.get("chromium_mcg"));
        assertEquals(420.0, goals.get("magnesium_mg"));

        // Limits should include sodium restriction
        Map<String, Double> limits = profile.getMicronutrientLimits();
        assertEquals(2300.0, limits.get("sodium_mg"));
    }

    @Test
    void testCalculateNutritionalNeedsWithAnemia() {
        // Given: A 35-year-old female with anemia
        UserProfile user = createTestUser("lisa001", "Lisa Davis", 35, Gender.FEMALE, 160.0, 55.0,
                HealthGoal.ANEMIA_CONTROL, List.of(MedicalCondition.ANEMIA), 3);

        // When: Calculate nutritional needs
        NutritionProfile profile = nutritionCalculator.calculateNutritionalNeeds(user);

        // Then: Verify calculations
        assertNotNull(profile);

        // Micronutrients should include anemia specific requirements
        Map<String, Double> goals = profile.getMicronutrientGoals();
        assertEquals(18.0, goals.get("iron_mg")); // Female requirement
        assertEquals(90.0, goals.get("vitamin_c_mg"));
    }

    @Test
    void testCalculateNutritionalNeedsWithMultipleConditions() {
        // Given: A user with multiple conditions
        UserProfile user = createTestUser("michael001", "Michael Taylor", 60, Gender.MALE, 170.0, 75.0,
                HealthGoal.ELDERLY_NUTRITION,
                Arrays.asList(MedicalCondition.ELDERLY, MedicalCondition.HYPERTENSION, MedicalCondition.DIABETES),
                2);

        // When: Calculate nutritional needs
        NutritionProfile profile = nutritionCalculator.calculateNutritionalNeeds(user);

        // Then: Verify calculations
        assertNotNull(profile);

        // Should have requirements from all conditions
        Map<String, Double> goals = profile.getMicronutrientGoals();
        assertEquals(2.4, goals.get("vitamin_b12_mcg")); // Elderly
        assertEquals(1200.0, goals.get("calcium_mg")); // Elderly
        assertEquals(4700.0, goals.get("potassium_mg")); // Hypertension
        assertEquals(35.0, goals.get("chromium_mcg")); // Diabetes

        // Should have limits from relevant conditions
        Map<String, Double> limits = profile.getMicronutrientLimits();
        assertEquals(1500.0, limits.get("sodium_mg")); // Hypertension (stricter than diabetes)
    }
}
