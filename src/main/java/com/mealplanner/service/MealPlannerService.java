package com.mealplanner.service;

import com.mealplanner.dal.RecipeDAO;
import com.mealplanner.model.*;
import com.mealplanner.model.recipe.NutritionInfo;
import com.mealplanner.model.recipe.Recipe;
import com.mealplanner.model.user.NutritionProfile;
import com.mealplanner.model.user.UserProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enhanced meal planning service with improved efficiency, variety, and portion
 * control
 * Features:
 * - Intelligent portion calculation to meet user's nutritional needs
 * - Advanced recipe selection with better variety algorithms
 * - Efficient database operations with proper connection management
 * - Detailed nutritional matching and balancing
 */
public class MealPlannerService {

    private final RecipeDAO recipeDAO;

    // Enhanced meal distribution with flexible adjustment
    private static final double BREAKFAST_PERCENTAGE = 0.25;
    private static final double LUNCH_PERCENTAGE = 0.35;
    private static final double DINNER_PERCENTAGE = 0.30;
    private static final double SNACKS_PERCENTAGE = 0.10;

    // Improved variety control
    private static final int RECIPE_COOLDOWN_DAYS = 3;
    private static final int SNACKS_COOLDOWN_DAYS = 2;

    // Nutritional tolerance ranges
    private static final double CALORIE_TOLERANCE = 0.15; // ±15% acceptable variance
    private static final double MACRO_TOLERANCE = 0.20; // ±20% acceptable variance

    // Recipe cache for better performance
    private Map<MealType, List<Recipe>> recipeCache = new HashMap<>();
    private long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 300000; // 5 minutes

    public MealPlannerService(RecipeDAO recipeDAO) {
        this.recipeDAO = recipeDAO;
    }

    /**
     * Generate an enhanced weekly meal plan with proper portion control
     * Refurnished version that calls database 4 times to generate 7 days of meals
     * for each meal type
     */
    public WeeklyMealPlan generateNewWeeklyPlan(UserProfile user, NutritionProfile nutritionProfile) {
        System.out.println("=== GENERATING WEEKLY MEAL PLAN ===");
        System.out.println("User: " + user.getName() + ", Target Calories: " + nutritionProfile.getTargetCalories());

        WeeklyMealPlan weeklyPlan = new WeeklyMealPlan(user, nutritionProfile, new ArrayList<>());

        // Calculate enhanced daily targets once for all days
        EnhancedMealTargets dailyTargets = calculateEnhancedDailyTargets(nutritionProfile);

        // Generate 7 days of meals for each meal type separately
        List<EnhancedMeal> weeklyBreakfasts = generateWeeklyMealsForType(MealType.BREAKFAST,
                dailyTargets.breakfast, user, 7);

        // Generate lunch and dinner together to ensure 100% different recipes
        LunchDinnerMealPair lunchDinnerPair = generateWeeklyLunchAndDinnerMeals(
                dailyTargets.lunch, dailyTargets.dinner, user, 7);
        List<EnhancedMeal> weeklyLunches = lunchDinnerPair.lunchMeals;
        List<EnhancedMeal> weeklyDinners = lunchDinnerPair.dinnerMeals;

        List<EnhancedMeal> weeklySnacks = generateWeeklyMealsForType(MealType.SNACKS,
                dailyTargets.snacks, user, 7);

        // Assemble daily plans from the weekly meal lists
        LocalDate startDate = LocalDate.now();

        for (int day = 0; day < 7; day++) {
            LocalDate currentDate = startDate.plusDays(day);

            EnhancedMeal breakfast = weeklyBreakfasts.get(day);
            EnhancedMeal lunch = weeklyLunches.get(day);
            EnhancedMeal dinner = weeklyDinners.get(day);
            EnhancedMeal snacks = weeklySnacks.get(day);

            // Lunch and dinner are now guaranteed to be 100% different by design

            // Calculate accurate daily nutrition with portions
            NutritionInfo dailyTotalNutrition = calculateAccurateDailyNutrition(
                    breakfast, lunch, dinner, snacks);

            // Create daily plan with enhanced data
            DailyMealPlan dailyPlan = new DailyMealPlan(currentDate,
                    convertToRegularMeal(breakfast), convertToRegularMeal(lunch),
                    convertToRegularMeal(dinner), convertToRegularMeal(snacks),
                    dailyTotalNutrition);

            weeklyPlan.getDailyPlans().add(dailyPlan);

            System.out.println("Day " + (day + 1) + " - B: "
                    + breakfast.recipe.getNameEn().substring(0, Math.min(20, breakfast.recipe.getNameEn().length())) +
                    ", L: " + lunch.recipe.getNameEn().substring(0, Math.min(20, lunch.recipe.getNameEn().length())) +
                    ", D: " + dinner.recipe.getNameEn().substring(0, Math.min(20, dinner.recipe.getNameEn().length())) +
                    ", S: " + snacks.recipe.getNameEn().substring(0, Math.min(20, snacks.recipe.getNameEn().length())));
        }

        System.out.println("=== WEEKLY MEAL PLAN GENERATION COMPLETED ===");
        return weeklyPlan;
    }

    /**
     * Generate 7 days of meals for a specific meal type with variety control
     * This method makes ONE database call per meal type to ensure efficiency
     */
    private List<EnhancedMeal> generateWeeklyMealsForType(MealType mealType,
            EnhancedNutritionTargets targets, UserProfile user, int numDays) {

        System.out.println("Processing " + mealType + " meals for " + numDays + " days");

        List<EnhancedMeal> weeklyMeals = new ArrayList<>();
        Set<String> usedRecipeIds = new HashSet<>();

        // Single database call to get all candidate recipes for this meal type
        List<Recipe> allCandidateRecipes = getAllCandidateRecipesForMealType(mealType, user);

        // If we don't have enough recipes, use fallback strategy
        if (allCandidateRecipes.size() < numDays) {
            allCandidateRecipes = extendWithFallbackRecipes(mealType, user, allCandidateRecipes, numDays);
        }

        // Generate meals for each day ensuring variety
        for (int day = 0; day < numDays; day++) {
            // Filter out already used recipes for better variety
            List<Recipe> availableRecipes = allCandidateRecipes.stream()
                    .filter(recipe -> !usedRecipeIds.contains(recipe.getRecipeId()))
                    .collect(Collectors.toList());

            // If no unused recipes available, reset and use all recipes
            if (availableRecipes.isEmpty()) {
                availableRecipes = new ArrayList<>(allCandidateRecipes);
                usedRecipeIds.clear();
            }

            // Select best recipe for this day with variety consideration
            Recipe selectedRecipe = selectBestRecipeForDay(availableRecipes, targets, day, user, mealType);

            // Calculate optimal portion size
            double optimalPortion = calculateOptimalPortion(selectedRecipe, targets);

            // Calculate adjusted nutrition based on portion
            NutritionInfo adjustedNutrition = calculateAdjustedNutrition(selectedRecipe, optimalPortion);

            // Calculate nutrition score
            double nutritionScore = calculateNutritionScore(adjustedNutrition, targets);

            // Create enhanced meal and track usage
            EnhancedMeal meal = new EnhancedMeal(mealType, selectedRecipe, optimalPortion,
                    adjustedNutrition, nutritionScore);
            weeklyMeals.add(meal);
            usedRecipeIds.add(selectedRecipe.getRecipeId());
        }

        return weeklyMeals;
    }

    /**
     * Get all candidate recipes for a meal type in ONE database call
     */
    private List<Recipe> getAllCandidateRecipesForMealType(MealType mealType, UserProfile user) {
        // Get user's dietary preferences and restrictions
        List<String> subcategories = getMealSubcategories(mealType, user);
        TagPriority tagPriority = getTagPriority(user, mealType);
        List<String> allergies = user.getAllergies() != null ? user.getAllergies() : new ArrayList<>();

        // Single comprehensive database call
        List<Recipe> candidateRecipes = findCandidateRecipes(mealType, subcategories, tagPriority, allergies);

        // If no recipes found with strict criteria, relax constraints
        if (candidateRecipes.isEmpty()) {
            candidateRecipes = recipeDAO.findRecipesByMealType(mealType).stream()
                    .filter(recipe -> matchesDietaryPreferences(recipe, user))
                    .collect(Collectors.toList());
        }

        return candidateRecipes;
    }

    /**
     * Extend recipe list with fallback recipes if we don't have enough variety
     */
    private List<Recipe> extendWithFallbackRecipes(MealType mealType, UserProfile user,
            List<Recipe> existingRecipes, int requiredCount) {

        List<Recipe> extendedList = new ArrayList<>(existingRecipes);
        Set<String> existingIds = existingRecipes.stream()
                .map(Recipe::getRecipeId)
                .collect(Collectors.toSet());

        // Get additional recipes from database
        List<Recipe> allMealTypeRecipes = recipeDAO.findRecipesByMealType(mealType);

        for (Recipe recipe : allMealTypeRecipes) {
            if (!existingIds.contains(recipe.getRecipeId()) &&
                    matchesDietaryPreferences(recipe, user)) {
                extendedList.add(recipe);
                if (extendedList.size() >= requiredCount * 2) { // Get extra for better variety
                    break;
                }
            }
        }

        return extendedList;
    }

    /**
     * Select the best recipe for a specific day considering variety and nutrition
     */
    private Recipe selectBestRecipeForDay(List<Recipe> candidateRecipes,
            EnhancedNutritionTargets targets, int dayIndex, UserProfile user, MealType mealType) {

        if (candidateRecipes.isEmpty()) {
            throw new IllegalArgumentException("No candidate recipes available for day " + dayIndex);
        }

        // Use different selection strategies for different days to ensure variety
        Recipe selectedRecipe;

        switch (dayIndex % 3) {
            case 0: // Focus on nutrition match
                selectedRecipe = selectBestRecipeWithNutrition(candidateRecipes, targets, dayIndex);
                break;
            case 1: // Focus on user preferences
                selectedRecipe = selectRecipeByPreference(candidateRecipes, user, dayIndex, mealType);
                break;
            case 2: // Random selection for variety
                selectedRecipe = selectRandomRecipeWithWeight(candidateRecipes, targets, dayIndex);
                break;
            default:
                selectedRecipe = candidateRecipes.get(0);
        }

        return selectedRecipe;
    }

    /**
     * Select recipe based on user preferences with some randomization
     */
    private Recipe selectRecipeByPreference(List<Recipe> candidateRecipes, UserProfile user, int dayIndex,
            MealType mealType) {
        TagPriority tagPriority = getTagPriority(user, mealType);

        // Score recipes based on user preferences
        Recipe bestRecipe = candidateRecipes.get(0);
        double bestScore = calculateUserPreferenceScore(bestRecipe, tagPriority);

        for (Recipe recipe : candidateRecipes) {
            double score = calculateUserPreferenceScore(recipe, tagPriority);
            // Add slight randomization based on day
            Random random = new Random(recipe.getRecipeId().hashCode() + dayIndex);
            score += random.nextGaussian() * 5; // Small random factor

            if (score > bestScore) {
                bestScore = score;
                bestRecipe = recipe;
            }
        }

        return bestRecipe;
    }

    /**
     * Determine meal type from recipe (helper method)
     * This is a workaround since Recipe doesn't have getMealType() method
     */
    private MealType determineMealTypeFromRecipe(Recipe recipe) {
        // Check which meal type table this recipe belongs to by checking database
        // For now, we'll try each meal type
        for (MealType mealType : MealType.values()) {
            List<Recipe> recipesForType = recipeDAO.findRecipesByMealType(mealType);
            if (recipesForType.stream().anyMatch(r -> r.getRecipeId().equals(recipe.getRecipeId()))) {
                return mealType;
            }
        }
        // Default fallback
        return MealType.LUNCH;
    }

    /**
     * Select recipe with weighted randomization
     */
    private Recipe selectRandomRecipeWithWeight(List<Recipe> candidateRecipes,
            EnhancedNutritionTargets targets, int dayIndex) {

        // Create weighted list based on nutrition scores
        List<Recipe> weightedList = new ArrayList<>();
        for (Recipe recipe : candidateRecipes) {
            double nutritionScore = calculateNutritionScore(recipe.getNutritionPerServing(), targets);
            // Invert score so lower nutrition difference = higher weight
            int weight = Math.max(1, (int) (100 / (nutritionScore + 1)));
            for (int i = 0; i < weight; i++) {
                weightedList.add(recipe);
            }
        }

        // Select random recipe from weighted list
        Random random = new Random(System.currentTimeMillis() + dayIndex);
        return weightedList.get(random.nextInt(weightedList.size()));
    }

    /**
     * Calculate user preference score based on tags
     */
    private double calculateUserPreferenceScore(Recipe recipe, TagPriority tagPriority) {
        double score = 0;

        // Check preferred tags using boolean column queries
        if (tagPriority.tagScores != null && !tagPriority.tagScores.isEmpty()) {
            score += calculateBooleanTagBonus(recipe.getRecipeId(), tagPriority.tagScores);
        }

        // Bonus for required tags
        if (recipe.getTags() != null && tagPriority.requiredTags != null) {
            for (String requiredTag : tagPriority.requiredTags) {
                if (recipe.getTags().contains(requiredTag)) {
                    score += 50; // High bonus for required tags
                }
            }
        }

        // Penalty for prohibited tags
        if (recipe.getTags() != null && tagPriority.prohibitedTags != null) {
            for (String prohibitedTag : tagPriority.prohibitedTags) {
                if (recipe.getTags().contains(prohibitedTag)) {
                    score -= 100; // High penalty for prohibited tags
                }
            }
        }

        return score;
    }

    /**
     * Update enhanced cooldown for all recipes
     */
    private void updateEnhancedCooldown(Map<String, Integer> recipeCooldown) {
        recipeCooldown.entrySet().removeIf(entry -> {
            int newCooldown = entry.getValue() - 1;
            if (newCooldown <= 0) {
                return true; // Remove from map
            } else {
                entry.setValue(newCooldown);
                return false;
            }
        });
    }

    /**
     * Enhanced meal targets with portion information
     */
    private static class EnhancedMealTargets {
        final EnhancedNutritionTargets breakfast;
        final EnhancedNutritionTargets lunch;
        final EnhancedNutritionTargets dinner;
        final EnhancedNutritionTargets snacks;

        EnhancedMealTargets(EnhancedNutritionTargets breakfast, EnhancedNutritionTargets lunch,
                EnhancedNutritionTargets dinner, EnhancedNutritionTargets snacks) {
            this.breakfast = breakfast;
            this.lunch = lunch;
            this.dinner = dinner;
            this.snacks = snacks;
        }
    }

    /**
     * Enhanced nutrition targets with tolerance ranges
     */
    private static class EnhancedNutritionTargets {
        final double targetCalories;
        final double targetProtein;
        final double targetCarbs;
        final double targetFat;
        final double minCalories;
        final double maxCalories;
        final double minProtein;
        final double maxProtein;
        final double minCarbs;
        final double maxCarbs;
        final double minFat;
        final double maxFat;

        EnhancedNutritionTargets(double calories, double protein, double carbs, double fat) {
            this.targetCalories = calories;
            this.targetProtein = protein;
            this.targetCarbs = carbs;
            this.targetFat = fat;

            // Calculate tolerance ranges
            this.minCalories = calories * (1 - CALORIE_TOLERANCE);
            this.maxCalories = calories * (1 + CALORIE_TOLERANCE);
            this.minProtein = protein * (1 - MACRO_TOLERANCE);
            this.maxProtein = protein * (1 + MACRO_TOLERANCE);
            this.minCarbs = carbs * (1 - MACRO_TOLERANCE);
            this.maxCarbs = carbs * (1 + MACRO_TOLERANCE);
            this.minFat = fat * (1 - MACRO_TOLERANCE);
            this.maxFat = fat * (1 + MACRO_TOLERANCE);
        }
    }

    /**
     * Enhanced meal with portion information
     */
    private static class EnhancedMeal {
        final MealType mealType;
        final Recipe recipe;
        final double portionGrams;
        final NutritionInfo adjustedNutrition;
        final double nutritionScore;

        EnhancedMeal(MealType mealType, Recipe recipe, double portionGrams,
                NutritionInfo adjustedNutrition, double nutritionScore) {
            this.mealType = mealType;
            this.recipe = recipe;
            this.portionGrams = portionGrams;
            this.adjustedNutrition = adjustedNutrition;
            this.nutritionScore = nutritionScore;
        }
    }

    /**
     * Helper class to hold lunch and dinner meal pairs
     */
    private static class LunchDinnerMealPair {
        final List<EnhancedMeal> lunchMeals;
        final List<EnhancedMeal> dinnerMeals;

        LunchDinnerMealPair(List<EnhancedMeal> lunchMeals, List<EnhancedMeal> dinnerMeals) {
            this.lunchMeals = lunchMeals;
            this.dinnerMeals = dinnerMeals;
        }
    }

    /**
     * Generate lunch and dinner meals together ensuring 100% different recipes
     * This method fetches 14 unique recipes from lunch category and splits them:
     * - First 7 for lunch meals
     * - Remaining 7 for dinner meals
     */
    private LunchDinnerMealPair generateWeeklyLunchAndDinnerMeals(
            EnhancedNutritionTargets lunchTargets, EnhancedNutritionTargets dinnerTargets,
            UserProfile user, int numDays) {

        System.out.println("Processing lunch and dinner meals together for " + numDays + " days to ensure variety");

        // Get candidate recipes from lunch category (which typically has more variety
        // suitable for both meals)
        List<Recipe> allCandidateRecipes = getAllCandidateRecipesForMealType(MealType.LUNCH, user);

        // We need 14 unique recipes (7 for lunch, 7 for dinner)
        int requiredRecipes = numDays * 2;

        if (allCandidateRecipes.size() < requiredRecipes) {
            // Extend with dinner recipes if lunch doesn't have enough
            List<Recipe> dinnerRecipes = getAllCandidateRecipesForMealType(MealType.DINNER, user);

            // Add dinner recipes that are not already in lunch candidates
            Set<String> existingIds = allCandidateRecipes.stream()
                    .map(Recipe::getRecipeId)
                    .collect(Collectors.toSet());

            for (Recipe dinnerRecipe : dinnerRecipes) {
                if (!existingIds.contains(dinnerRecipe.getRecipeId()) &&
                        allCandidateRecipes.size() < requiredRecipes) {
                    allCandidateRecipes.add(dinnerRecipe);
                    existingIds.add(dinnerRecipe.getRecipeId());
                }
            }
        }

        // If still not enough, use fallback strategy
        if (allCandidateRecipes.size() < requiredRecipes) {
            allCandidateRecipes = extendWithFallbackRecipes(MealType.LUNCH, user, allCandidateRecipes, requiredRecipes);
        }

        // Shuffle to ensure variety and avoid any ordering bias
        Collections.shuffle(allCandidateRecipes);

        // Split recipes: first 7 for lunch, next 7 for dinner
        List<EnhancedMeal> lunchMeals = new ArrayList<>();
        List<EnhancedMeal> dinnerMeals = new ArrayList<>();

        for (int day = 0; day < numDays; day++) {
            // Select lunch recipe (from first half)
            Recipe lunchRecipe = selectBestRecipeForDay(
                    allCandidateRecipes.subList(0, Math.min(numDays, allCandidateRecipes.size())),
                    lunchTargets, day, user, MealType.LUNCH);

            // Calculate optimal portion and nutrition for lunch
            double lunchPortion = calculateOptimalPortion(lunchRecipe, lunchTargets);
            NutritionInfo lunchNutrition = calculateAdjustedNutrition(lunchRecipe, lunchPortion);
            double lunchScore = calculateNutritionScore(lunchNutrition, lunchTargets);

            EnhancedMeal lunchMeal = new EnhancedMeal(MealType.LUNCH, lunchRecipe, lunchPortion,
                    lunchNutrition, lunchScore);
            lunchMeals.add(lunchMeal);

            // Select dinner recipe (from second half, ensuring it's different from lunch)
            int dinnerStartIndex = Math.min(numDays, allCandidateRecipes.size());
            int dinnerEndIndex = Math.min(numDays * 2, allCandidateRecipes.size());

            Recipe dinnerRecipe;
            if (dinnerStartIndex < allCandidateRecipes.size() && dinnerStartIndex < dinnerEndIndex) {
                dinnerRecipe = selectBestRecipeForDay(
                        allCandidateRecipes.subList(dinnerStartIndex, dinnerEndIndex),
                        dinnerTargets, day, user, MealType.DINNER);
            } else {
                // Fallback: select from remaining recipes, ensuring it's different from lunch
                List<Recipe> remainingRecipes = allCandidateRecipes.stream()
                        .filter(recipe -> !recipe.getRecipeId().equals(lunchRecipe.getRecipeId()))
                        .collect(Collectors.toList());
                dinnerRecipe = selectBestRecipeForDay(remainingRecipes, dinnerTargets, day, user, MealType.DINNER);
            }

            // Calculate optimal portion and nutrition for dinner
            double dinnerPortion = calculateOptimalPortion(dinnerRecipe, dinnerTargets);
            NutritionInfo dinnerNutrition = calculateAdjustedNutrition(dinnerRecipe, dinnerPortion);
            double dinnerScore = calculateNutritionScore(dinnerNutrition, dinnerTargets);

            EnhancedMeal dinnerMeal = new EnhancedMeal(MealType.DINNER, dinnerRecipe, dinnerPortion,
                    dinnerNutrition, dinnerScore);
            dinnerMeals.add(dinnerMeal);

            System.out.println("Day " + (day + 1) + " - Lunch: " +
                    lunchRecipe.getNameEn().substring(0, Math.min(25, lunchRecipe.getNameEn().length())) +
                    ", Dinner: " +
                    dinnerRecipe.getNameEn().substring(0, Math.min(25, dinnerRecipe.getNameEn().length())));
        }

        System.out.println("✅ Generated " + numDays + " lunch and dinner meals with 100% different recipes");
        return new LunchDinnerMealPair(lunchMeals, dinnerMeals);
    }

    /**
     * Calculate enhanced daily meal targets with tolerance ranges
     */
    private EnhancedMealTargets calculateEnhancedDailyTargets(NutritionProfile nutritionProfile) {
        double totalCalories = nutritionProfile.getTargetCalories();
        double totalProtein = nutritionProfile.getProteinGrams();
        double totalCarbs = nutritionProfile.getCarbsGrams();
        double totalFat = nutritionProfile.getFatGrams();

        return new EnhancedMealTargets(
                new EnhancedNutritionTargets(totalCalories * BREAKFAST_PERCENTAGE, totalProtein * BREAKFAST_PERCENTAGE,
                        totalCarbs * BREAKFAST_PERCENTAGE, totalFat * BREAKFAST_PERCENTAGE),
                new EnhancedNutritionTargets(totalCalories * LUNCH_PERCENTAGE, totalProtein * LUNCH_PERCENTAGE,
                        totalCarbs * LUNCH_PERCENTAGE, totalFat * LUNCH_PERCENTAGE),
                new EnhancedNutritionTargets(totalCalories * DINNER_PERCENTAGE, totalProtein * DINNER_PERCENTAGE,
                        totalCarbs * DINNER_PERCENTAGE, totalFat * DINNER_PERCENTAGE),
                new EnhancedNutritionTargets(totalCalories * SNACKS_PERCENTAGE, totalProtein * SNACKS_PERCENTAGE,
                        totalCarbs * SNACKS_PERCENTAGE, totalFat * SNACKS_PERCENTAGE));
    }

    /**
     * Refresh recipe cache for better performance
     */
    private void refreshRecipeCache() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheUpdate > CACHE_DURATION) {
            recipeCache.clear();
            for (MealType mealType : MealType.values()) {
                recipeCache.put(mealType, recipeDAO.findRecipesByMealType(mealType));
            }
            lastCacheUpdate = currentTime;
        }
    }

    /**
     * Generate enhanced meal with proper portion calculation
     */
    private EnhancedMeal generateEnhancedMeal(MealType mealType, EnhancedNutritionTargets targets,
            UserProfile user, Map<String, Integer> recipeCooldown,
            Map<MealType, Set<String>> dailyUsedRecipes, int currentDay) {

        // Get candidate recipes with improved efficiency
        List<Recipe> candidateRecipes = getFilteredCandidateRecipes(mealType, user, recipeCooldown,
                dailyUsedRecipes.get(mealType), currentDay);

        if (candidateRecipes.isEmpty()) {
            candidateRecipes = getFallbackRecipes(mealType, user);
        }

        // Select best recipe with nutrition scoring
        Recipe bestRecipe = selectBestRecipeWithNutrition(candidateRecipes, targets, currentDay);

        // Calculate optimal portion size
        double optimalPortion = calculateOptimalPortion(bestRecipe, targets);

        // Calculate adjusted nutrition based on portion
        NutritionInfo adjustedNutrition = calculateAdjustedNutrition(bestRecipe, optimalPortion);

        // Calculate nutrition score
        double nutritionScore = calculateNutritionScore(adjustedNutrition, targets);

        // Update tracking
        recipeCooldown.put(bestRecipe.getRecipeId(),
                mealType == MealType.SNACKS ? SNACKS_COOLDOWN_DAYS : RECIPE_COOLDOWN_DAYS);
        dailyUsedRecipes.get(mealType).add(bestRecipe.getRecipeId());

        return new EnhancedMeal(mealType, bestRecipe, optimalPortion, adjustedNutrition, nutritionScore);
    }

    /**
     * Calculate accurate daily nutrition from enhanced meals
     */
    private NutritionInfo calculateAccurateDailyNutrition(EnhancedMeal breakfast, EnhancedMeal lunch,
            EnhancedMeal dinner, EnhancedMeal snacks) {
        double totalCalories = breakfast.adjustedNutrition.getEnergyKcal() +
                lunch.adjustedNutrition.getEnergyKcal() +
                dinner.adjustedNutrition.getEnergyKcal() +
                snacks.adjustedNutrition.getEnergyKcal();

        double totalProtein = breakfast.adjustedNutrition.getProteinG() +
                lunch.adjustedNutrition.getProteinG() +
                dinner.adjustedNutrition.getProteinG() +
                snacks.adjustedNutrition.getProteinG();

        double totalCarbs = breakfast.adjustedNutrition.getCarbohydrateAvailableG() +
                lunch.adjustedNutrition.getCarbohydrateAvailableG() +
                dinner.adjustedNutrition.getCarbohydrateAvailableG() +
                snacks.adjustedNutrition.getCarbohydrateAvailableG();

        double totalFat = breakfast.adjustedNutrition.getFatG() +
                lunch.adjustedNutrition.getFatG() +
                dinner.adjustedNutrition.getFatG() +
                snacks.adjustedNutrition.getFatG();

        double totalFiber = breakfast.adjustedNutrition.getTotalDietaryFibreG() +
                lunch.adjustedNutrition.getTotalDietaryFibreG() +
                dinner.adjustedNutrition.getTotalDietaryFibreG() +
                snacks.adjustedNutrition.getTotalDietaryFibreG();

        double totalWater = breakfast.adjustedNutrition.getWaterG() +
                lunch.adjustedNutrition.getWaterG() +
                dinner.adjustedNutrition.getWaterG() +
                snacks.adjustedNutrition.getWaterG();

        return new NutritionInfo(totalCalories, totalProtein, totalFat, totalCarbs, totalFiber, totalWater);
    }

    /**
     * Convert enhanced meal to regular meal
     */
    private Meal convertToRegularMeal(EnhancedMeal enhancedMeal) {
        return new Meal(enhancedMeal.mealType,
                java.util.Arrays.asList(enhancedMeal.recipe),
                enhancedMeal.adjustedNutrition);
    }

    /**
     * Get filtered candidate recipes with improved efficiency
     */
    private List<Recipe> getFilteredCandidateRecipes(MealType mealType, UserProfile user,
            Map<String, Integer> recipeCooldown, Set<String> dailyUsedRecipes, int currentDay) {

        refreshRecipeCache();
        List<Recipe> allRecipes = recipeCache.get(mealType);

        if (allRecipes == null || allRecipes.isEmpty()) {
            return new ArrayList<>();
        }

        return allRecipes.stream()
                .filter(recipe -> {
                    // Check cooldown
                    if (recipeCooldown.containsKey(recipe.getRecipeId()) &&
                            recipeCooldown.get(recipe.getRecipeId()) > 0) {
                        return false;
                    }

                    // Check daily usage
                    if (dailyUsedRecipes.contains(recipe.getRecipeId())) {
                        return false;
                    }

                    // Check dietary preferences
                    return matchesDietaryPreferences(recipe, user);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get fallback recipes when no candidates are available
     */
    private List<Recipe> getFallbackRecipes(MealType mealType, UserProfile user) {
        refreshRecipeCache();
        List<Recipe> allRecipes = recipeCache.get(mealType);

        if (allRecipes == null || allRecipes.isEmpty()) {
            return new ArrayList<>();
        }

        return allRecipes.stream()
                .filter(recipe -> matchesDietaryPreferences(recipe, user))
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * Check if recipe matches dietary preferences
     */
    private boolean matchesDietaryPreferences(Recipe recipe, UserProfile user) {
        // Check meal preferences
        if (user.getMealPreferences() != null) {
            List<String> preferences = user.getMealPreferences().get(recipe.getCategory());
            if (preferences != null && !preferences.isEmpty()) {
                for (String preference : preferences) {
                    if (recipe.getTags() != null && recipe.getTags().contains(preference)) {
                        return true;
                    }
                }
            }
        }

        // Check primary health goal
        if (user.getPrimaryHealthGoal() != null) {
            String healthGoalTag = user.getPrimaryHealthGoal().toString();
            if (recipe.getTags() != null && recipe.getTags().contains(healthGoalTag)) {
                return true;
            }
        }

        // Check allergies (exclude recipes with allergens)
        if (user.getAllergies() != null) {
            for (String allergy : user.getAllergies()) {
                if (recipe.getTags() != null && recipe.getTags().contains(allergy)) {
                    return false;
                }
            }
        }

        // Default to true if no specific preferences
        return true;
    }

    /**
     * Select best recipe with nutrition scoring
     */
    private Recipe selectBestRecipeWithNutrition(List<Recipe> candidateRecipes,
            EnhancedNutritionTargets targets, int currentDay) {

        if (candidateRecipes.isEmpty()) {
            throw new IllegalArgumentException("No candidate recipes available");
        }

        Recipe bestRecipe = candidateRecipes.get(0);
        double bestScore = calculateNutritionScore(bestRecipe.getNutritionPerServing(), targets);

        for (Recipe recipe : candidateRecipes) {
            double score = calculateNutritionScore(recipe.getNutritionPerServing(), targets);
            if (score > bestScore) {
                bestScore = score;
                bestRecipe = recipe;
            }
        }

        return bestRecipe;
    }

    /**
     * Calculate optimal portion size for target nutrition
     */
    private double calculateOptimalPortion(Recipe recipe, EnhancedNutritionTargets targets) {
        NutritionInfo recipeNutrition = recipe.getNutritionPerServing();

        // Calculate portion multiplier based on calories
        double calorieMultiplier = targets.targetCalories / recipeNutrition.getEnergyKcal();

        // Ensure portion is within reasonable bounds
        calorieMultiplier = Math.max(0.25, Math.min(calorieMultiplier, 4.0));

        // Convert to grams (assuming base serving is 100g)
        return 100.0 * calorieMultiplier;
    }

    /**
     * Calculate adjusted nutrition based on portion
     */
    private NutritionInfo calculateAdjustedNutrition(Recipe recipe, double portionGrams) {
        NutritionInfo baseNutrition = recipe.getNutritionPerServing();
        double multiplier = portionGrams / 100.0; // Assuming base is per 100g

        return new NutritionInfo(
                baseNutrition.getEnergyKcal() * multiplier,
                baseNutrition.getProteinG() * multiplier,
                baseNutrition.getFatG() * multiplier,
                baseNutrition.getCarbohydrateAvailableG() * multiplier,
                baseNutrition.getTotalDietaryFibreG() * multiplier,
                baseNutrition.getWaterG() * multiplier);
    }

    /**
     * Calculate nutrition score for recipe matching
     */
    private double calculateNutritionScore(NutritionInfo nutrition, EnhancedNutritionTargets targets) {
        double calorieScore = calculateComponentScore(nutrition.getEnergyKcal(),
                targets.targetCalories, targets.minCalories, targets.maxCalories);

        double proteinScore = calculateComponentScore(nutrition.getProteinG(),
                targets.targetProtein, targets.minProtein, targets.maxProtein);

        double carbsScore = calculateComponentScore(nutrition.getCarbohydrateAvailableG(),
                targets.targetCarbs, targets.minCarbs, targets.maxCarbs);

        double fatScore = calculateComponentScore(nutrition.getFatG(),
                targets.targetFat, targets.minFat, targets.maxFat);

        // Weighted average (calories most important)
        return (calorieScore * 0.4) + (proteinScore * 0.25) + (carbsScore * 0.2) + (fatScore * 0.15);
    }

    /**
     * Calculate component score for nutrition matching
     */
    private double calculateComponentScore(double actual, double target, double min, double max) {
        if (actual >= min && actual <= max) {
            // Perfect match within tolerance
            return 1.0;
        } else if (actual < min) {
            // Below minimum
            return Math.max(0.0, 1.0 - (min - actual) / target);
        } else {
            // Above maximum
            return Math.max(0.0, 1.0 - (actual - max) / target);
        }
    }

    /**
     * Generate a single meal based on meal type and nutrition targets
     */
    private Meal generateMeal(MealType mealType, NutritionTargets targets, UserProfile user,
            Set<String> usedRecipeIds, Map<String, Integer> recipeCooldown, int currentDay) {

        // Step 4.2: Filter Recipes - Get user preferences with rotation for variety
        List<String> subcategories = getRotatedSubcategories(mealType, user, currentDay);

        // Get required and preferred tags based on user context
        TagPriority tagPriority = getTagPriority(user, mealType);

        // Get user allergies
        List<String> allergies = user.getAllergies() != null ? user.getAllergies() : new ArrayList<>();

        // Step 4.3: Execute Query - Multi-pass selection approach using optimized
        // boolean columns
        List<Recipe> candidateRecipes = findCandidateRecipes(mealType, subcategories, tagPriority, allergies);

        // Debug logging for snacks
        if (mealType == MealType.SNACKS) {
            System.out.println("DEBUG: Found " + candidateRecipes.size() + " candidate snack recipes");
            System.out.println("DEBUG: Recipes in cooldown: " + recipeCooldown.size());
            System.out.println("DEBUG: Used recipe IDs: " + usedRecipeIds.size());
        }

        // Step 4.4: Select Best Meal with variety constraints using optimized scoring
        Recipe selectedRecipe = selectBestRecipeOptimized(candidateRecipes, targets, usedRecipeIds, recipeCooldown,
                currentDay, tagPriority.tagScores);

        if (selectedRecipe == null) {
            // Fallback mechanism - relax constraints
            if (mealType == MealType.SNACKS) {
                System.out.println("DEBUG: No recipe found, using fallback mechanism");
            }
            selectedRecipe = selectFallbackRecipe(mealType, subcategories, allergies, usedRecipeIds, recipeCooldown,
                    currentDay);
        } else {
            if (mealType == MealType.SNACKS) {
                System.out.println("DEBUG: Recipe found through main selection");
            }
        }

        // Debug logging for snacks
        if (mealType == MealType.SNACKS && selectedRecipe != null) {
            System.out.println("DEBUG: Selected snack recipe: " + selectedRecipe.getNameEn() + " (ID: "
                    + selectedRecipe.getRecipeId() + ")");
        }

        // Step 4.5: Assemble the Meal
        List<Recipe> mealRecipes = Arrays.asList(selectedRecipe);
        NutritionInfo mealNutrition = selectedRecipe.getNutritionPerServing();

        // Track recipe usage with appropriate cooldown
        if (selectedRecipe != null) {
            usedRecipeIds.add(selectedRecipe.getRecipeId());
            // Use different cooldown periods for different meal types
            int cooldownDays = (mealType == MealType.SNACKS) ? SNACKS_COOLDOWN_DAYS : RECIPE_COOLDOWN_DAYS;
            recipeCooldown.put(selectedRecipe.getRecipeId(), cooldownDays);
        }

        return new Meal(mealType, mealRecipes, mealNutrition);
    }

    /**
     * Get default subcategories for meal types with proper rotation support
     */
    private List<String> getDefaultSubcategories(MealType mealType) {
        switch (mealType) {
            case BREAKFAST:
                return Arrays.asList("RUTI_VEGETABLES", "RICE_VEGETABLES", "FRUIT_MILK_YOGURT",
                        "TEA_COFFEE_STREET_FOOD", "MISCELLANEOUS");
            case LUNCH:
            case DINNER:
                return Arrays.asList("RICE_FISH", "RICE_FISH_VEGETABLES", "RICE_MEAT_VEGETABLES",
                        "RICE_VEGETABLES_BHORTA", "RICE_LENTIL_BHORTA", "INTERNATIONAL_DISHES");
            case SNACKS:
                return Arrays.asList("Savoury_Fried", "Steamed_Boiled", "Juice", "Smoothie",
                        "Sweet_Snacks_Pitha", "Soup", "Nuts_Seeds", "Bakery", "Miscellaneous", "Fruits",
                        "Noodles_Pasta");
            default:
                return new ArrayList<>();
        }
    }

    /**
     * Get subcategories based on meal type and user preferences with rotation
     * support
     */
    private List<String> getMealSubcategories(MealType mealType, UserProfile user) {
        Map<MealType, List<String>> mealPreferences = user.getMealPreferences();

        if (mealPreferences != null && mealPreferences.containsKey(mealType)) {
            List<String> userPrefs = mealPreferences.get(mealType);

            // For snacks, check if user preferences have old format names and update them
            if (mealType == MealType.SNACKS && userPrefs != null && !userPrefs.isEmpty()) {
                // If preferences contain old format names, replace with corrected ones
                if (userPrefs.contains("Savoury Fried") || userPrefs.contains("Steamed/Boiled") ||
                        userPrefs.contains("Juice/Smoothie") || userPrefs.contains("Sweet Snacks/Pitha") ||
                        userPrefs.contains("Nuts/Seeds")) {
                    System.out.println("DEBUG: Updating user preferences to use correct subcategory names");
                    // Use default subcategories with correct names
                    return getDefaultSubcategories(mealType);
                }
            }

            System.out.println("DEBUG: Using user preferences for " + mealType + ": " + userPrefs);
            return userPrefs;
        }

        // Default subcategories if no preferences specified
        List<String> defaults = getDefaultSubcategories(mealType);
        System.out.println("DEBUG: Using default subcategories for " + mealType + ": " + defaults);
        return defaults;
    }

    /**
     * Get subcategories with rotation for variety - rotate through user preferences
     * daily with better variety control
     */
    private List<String> getRotatedSubcategories(MealType mealType, UserProfile user, int dayIndex) {
        List<String> allSubcategories = getMealSubcategories(mealType, user);

        if (allSubcategories.isEmpty()) {
            return allSubcategories;
        }

        // For snacks, use more aggressive rotation to ensure variety
        if (mealType == MealType.SNACKS) {
            // Debug: Check what subcategories we have
            System.out.println("DEBUG: Available snack subcategories: " + allSubcategories);

            // Rotate through subcategories more aggressively for snacks
            List<String> rotatedSubcategories = new ArrayList<>();

            // Primary subcategory for this day
            int primaryIndex = dayIndex % allSubcategories.size();
            rotatedSubcategories.add(allSubcategories.get(primaryIndex));

            System.out.println(
                    "DEBUG: Day " + dayIndex + " primary snack subcategory: " + allSubcategories.get(primaryIndex));

            // Add secondary subcategories in rotated order
            for (int i = 1; i < allSubcategories.size(); i++) {
                int index = (primaryIndex + i) % allSubcategories.size();
                rotatedSubcategories.add(allSubcategories.get(index));
            }

            System.out.println("DEBUG: Rotated snack subcategories for day " + dayIndex + ": " + rotatedSubcategories);
            return rotatedSubcategories;
        }

        // For other meals, use standard rotation
        List<String> rotatedSubcategories = new ArrayList<>();
        int startIndex = dayIndex % allSubcategories.size();

        // Add rotated subcategories (prioritize different ones each day)
        for (int i = 0; i < allSubcategories.size(); i++) {
            int index = (startIndex + i) % allSubcategories.size();
            rotatedSubcategories.add(allSubcategories.get(index));
        }

        return rotatedSubcategories;
    }

    /**
     * Get tag priority based on user context (medical conditions, health goals,
     * dietary preferences)
     * Enhanced with comprehensive Tag-Driven Priority Matrix
     */
    private TagPriority getTagPriority(UserProfile user, MealType mealType) {
        List<String> requiredTags = new ArrayList<>();
        List<String> preferredTags = new ArrayList<>();
        List<String> prohibitedTags = new ArrayList<>();
        Map<String, Integer> tagScores = new HashMap<>();

        // Hard Requirements (Instant Disqualification) - Prohibited Tags
        if (user.getMedicalConditions() != null) {
            for (MedicalCondition condition : user.getMedicalConditions()) {
                switch (condition) {
                    case DIABETES:
                        // Hard Requirements: Critical medical tags
                        requiredTags.addAll(Arrays.asList("is_diabetic_friendly", "is_low_carb", "is_high_fibre"));
                        tagScores.put("is_diabetic_friendly", 3);
                        tagScores.put("is_low_carb", 3);
                        tagScores.put("is_high_fibre", 3);
                        tagScores.put("is_low_fat", 2);
                        tagScores.put("is_medicinal", 1);

                        // Prohibited Tags (Instant Disqualification)
                        prohibitedTags.addAll(Arrays.asList("is_high_carb", "is_sweet"));
                        break;
                    case HYPERTENSION:
                        preferredTags.addAll(Arrays.asList("is_healthy"));
                        tagScores.put("is_healthy", 2);
                        break;
                    case CHILD:
                        preferredTags.addAll(Arrays.asList("is_kid_friendly", "is_milk_based", "is_healthy"));
                        tagScores.put("is_kid_friendly", 3);
                        tagScores.put("is_milk_based", 2);
                        tagScores.put("is_healthy", 2);
                        break;
                    case PREGNANCY:
                        preferredTags.addAll(Arrays.asList("is_vitamin_rich", "is_omega_3", "is_high_protein"));
                        tagScores.put("is_vitamin_rich", 3);
                        tagScores.put("is_omega_3", 3);
                        tagScores.put("is_high_protein", 3);
                        tagScores.put("is_detox", 2);
                        tagScores.put("is_healthy", 2);
                        break;
                    case ANEMIA:
                        preferredTags.addAll(Arrays.asList("is_iron_rich", "is_vitamin_rich"));
                        tagScores.put("is_iron_rich", 3);
                        tagScores.put("is_vitamin_rich", 2);
                        break;
                    case ELDERLY:
                        preferredTags.addAll(Arrays.asList("is_healthy", "is_high_protein"));
                        tagScores.put("is_healthy", 3);
                        tagScores.put("is_high_protein", 2);
                        break;
                    case HIGH_CHOLESTEROL:
                        preferredTags.addAll(Arrays.asList("is_low_fat", "is_high_fibre", "is_healthy"));
                        tagScores.put("is_low_fat", 3);
                        tagScores.put("is_high_fibre", 2);
                        tagScores.put("is_healthy", 2);
                        break;
                    case NONE:
                        // No special requirements
                        break;
                }
            }
        }

        // Health Goal-Specific Tags (Scoring System: 0-3 points per tag)
        if (user.getPrimaryHealthGoal() == HealthGoal.WEIGHT_LOSS) {
            preferredTags.addAll(Arrays.asList("is_low_calorie", "is_high_protein", "is_low_carb", "is_high_fibre",
                    "is_healthy_fat", "is_detox"));
            tagScores.put("is_low_calorie", 3);
            tagScores.put("is_high_protein", 3);
            tagScores.put("is_low_carb", 3);
            tagScores.put("is_high_fibre", 2);
            tagScores.put("is_healthy_fat", 2);
            tagScores.put("is_detox", 1);
        } else if (user.getPrimaryHealthGoal() == HealthGoal.MUSCLE_GAIN) {
            preferredTags.addAll(Arrays.asList("is_high_protein", "is_milk_based", "is_omega_3", "is_healthy_fat"));
            tagScores.put("is_high_protein", 3);
            tagScores.put("is_milk_based", 2);
            tagScores.put("is_omega_3", 2);
            tagScores.put("is_healthy_fat", 2);
        } else if (user.getPrimaryHealthGoal() == HealthGoal.CHILD_NUTRITION) {
            preferredTags.addAll(Arrays.asList("is_kid_friendly", "is_milk_based", "is_healthy"));
            tagScores.put("is_kid_friendly", 3);
            tagScores.put("is_milk_based", 2);
            tagScores.put("is_healthy", 2);
        } else if (user.getPrimaryHealthGoal() == HealthGoal.PREGNANCY_NUTRITION) {
            preferredTags.addAll(
                    Arrays.asList("is_vitamin_rich", "is_omega_3", "is_high_protein", "is_detox", "is_healthy"));
            tagScores.put("is_vitamin_rich", 3);
            tagScores.put("is_omega_3", 3);
            tagScores.put("is_high_protein", 3);
            tagScores.put("is_detox", 2);
            tagScores.put("is_healthy", 2);
        }

        // Hard Requirements for Dietary Preferences (Must Required Tags)
        // This should be checked from UserProfile - assuming it has dietary preferences
        // For now, we'll use a placeholder approach

        // Add staple and healthy as base fallback
        preferredTags.addAll(Arrays.asList("is_staple", "is_healthy"));
        tagScores.put("is_staple", 1);
        tagScores.put("is_healthy", 1);

        return new TagPriority(requiredTags, preferredTags, prohibitedTags, tagScores);
    }

    /**
     * Find candidate recipes using optimized boolean column queries
     */
    private List<Recipe> findCandidateRecipes(MealType mealType, List<String> subcategories,
            TagPriority tagPriority, List<String> allergies) {

        // First pass: Medical-required tags using boolean columns
        List<Recipe> recipes = new ArrayList<>();
        if (!tagPriority.requiredTags.isEmpty()) {
            recipes = findRecipesByBooleanTags(mealType, subcategories, tagPriority.requiredTags, allergies, true);
        }

        // Second pass: Goal-priority tags using boolean columns
        if (recipes.isEmpty() && !tagPriority.preferredTags.isEmpty()) {
            recipes = findRecipesByBooleanTags(mealType, subcategories, tagPriority.preferredTags, allergies, false);
        }

        // Third pass: Basic healthy options using boolean columns
        if (recipes.isEmpty()) {
            recipes = findRecipesByBooleanTags(mealType, subcategories, Arrays.asList("is_healthy"), allergies, false);
        }

        // Fourth pass: Staple foods using boolean columns
        if (recipes.isEmpty()) {
            recipes = findRecipesByBooleanTags(mealType, subcategories, Arrays.asList("is_staple"), allergies, false);
        }

        // Fifth pass: Any recipe for this meal type (last resort)
        if (recipes.isEmpty()) {
            recipes = recipeDAO.findRecipesByMealType(mealType);
        }

        // Filter out prohibited tags using boolean columns
        if (!tagPriority.prohibitedTags.isEmpty() && recipes.size() > 1) {
            List<Recipe> filteredRecipes = recipes.stream()
                    .filter(recipe -> !hasProhibitedBooleanTags(recipe, tagPriority.prohibitedTags))
                    .collect(Collectors.toList());

            // Only use filtered results if we have alternatives
            if (!filteredRecipes.isEmpty()) {
                recipes = filteredRecipes;
            }
        }

        return recipes;
    }

    /**
     * Find recipes using boolean column queries for efficient tag filtering
     */
    private List<Recipe> findRecipesByBooleanTags(MealType mealType, List<String> subcategories,
            List<String> requiredTags, List<String> allergies, boolean requireAllTags) {

        // Get the correct table name for the meal type
        String tableName = mealType.name().toLowerCase();

        StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName + " WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        // Add subcategories filter
        if (subcategories != null && !subcategories.isEmpty()) {
            sql.append(" AND subcategory IN (");
            sql.append(subcategories.stream().map(s -> "?").collect(Collectors.joining(", ")));
            sql.append(")");
            parameters.addAll(subcategories);
        }

        // Add boolean tag filters
        if (requiredTags != null && !requiredTags.isEmpty()) {
            if (requireAllTags) {
                // AND logic - all tags must be present
                for (String tag : requiredTags) {
                    sql.append(" AND ").append(tag).append(" = 1");
                }
            } else {
                // OR logic - any tag can be present
                sql.append(" AND (");
                for (int i = 0; i < requiredTags.size(); i++) {
                    if (i > 0)
                        sql.append(" OR ");
                    sql.append(requiredTags.get(i)).append(" = 1");
                }
                sql.append(")");
            }
        }

        List<Recipe> recipes = new ArrayList<>();

        // Debug logging for snacks
        if (mealType == MealType.SNACKS) {
            System.out.println("DEBUG: SQL Query: " + sql.toString());
            System.out.println("DEBUG: Parameters: " + parameters);
        }

        try (Connection conn = com.mealplanner.dal.DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Recipe recipe = mapResultSetToRecipe(rs);

                // Filter out recipes with excluded allergenic ingredients
                if (allergies != null && !allergies.isEmpty()) {
                    boolean hasAllergen = recipe.getIngredients().stream()
                            .anyMatch(ingredient -> allergies.stream()
                                    .anyMatch(allergen -> ingredient.getNameEn().toLowerCase()
                                            .contains(allergen.toLowerCase())));

                    if (!hasAllergen) {
                        recipes.add(recipe);
                    }
                } else {
                    recipes.add(recipe);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error finding recipes by boolean tags: " + e.getMessage());
        }

        return recipes;
    }

    /**
     * Check if recipe has any prohibited boolean tags
     * Optimized version with error handling for missing columns
     */
    private boolean hasProhibitedBooleanTags(Recipe recipe, List<String> prohibitedTags) {
        if (prohibitedTags == null || prohibitedTags.isEmpty()) {
            return false;
        }

        // Use the regular tags system as fallback if boolean columns don't exist
        if (recipe.getTags() != null) {
            for (String prohibitedTag : prohibitedTags) {
                if (recipe.getTags().contains(prohibitedTag)) {
                    return true;
                }
            }
        }

        // Try boolean columns approach with error handling
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM recipe_all WHERE recipe_id = ?");

        // Add checks for prohibited tags - only if columns exist
        List<String> validColumns = new ArrayList<>();
        for (String tag : prohibitedTags) {
            // Only include commonly available boolean columns
            if (tag.equals("is_diabetic_friendly") || tag.equals("is_healthy") ||
                    tag.equals("is_low_calorie") || tag.equals("is_high_protein")) {
                validColumns.add(tag);
            }
        }

        if (validColumns.isEmpty()) {
            return false; // No valid columns to check
        }

        for (String tag : validColumns) {
            sql.append(" AND ").append(tag).append(" = 1");
        }

        try (Connection conn = com.mealplanner.dal.DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            stmt.setString(1, recipe.getRecipeId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            // Fallback to tag-based checking if SQL fails
            return false;
        }

        return false;
    }

    /**
     * Calculate nutrition score - how well recipe matches targets
     */
    /**
     * Calculate nutrition score using the standardized approach
     * Returns a penalty-based score (lower is better)
     */
    private double calculateNutritionScore(Recipe recipe, NutritionTargets targets) {
        if (recipe.getNutritionPerServing() == null)
            return 1000.0;

        NutritionInfo nutrition = recipe.getNutritionPerServing();

        double caloriesDiff = Math.abs(nutrition.getEnergyKcal() - targets.calories);
        double proteinDiff = Math.abs(nutrition.getProteinG() - targets.protein);
        double carbsDiff = Math.abs(nutrition.getCarbohydrateAvailableG() - targets.carbs);
        double fatDiff = Math.abs(nutrition.getFatG() - targets.fat);

        // Weighted penalty score (calories are most important) - lower is better
        return caloriesDiff * 2.0 + proteinDiff * 1.5 + carbsDiff * 1.0 + fatDiff * 1.0;
    }

    /**
     * Select best recipe based on nutrition targets, variety constraints, and
     * optimized tag scoring
     */
    private Recipe selectBestRecipeOptimized(List<Recipe> candidates, NutritionTargets targets,
            Set<String> usedRecipeIds, Map<String, Integer> recipeCooldown, int currentDay,
            Map<String, Integer> tagScores) {

        if (candidates.isEmpty())
            return null;

        // Filter out recipes in cooldown period
        List<Recipe> availableRecipes = candidates.stream()
                .filter(recipe -> !recipeCooldown.containsKey(recipe.getRecipeId())
                        || recipeCooldown.get(recipe.getRecipeId()) <= 0)
                .collect(Collectors.toList());

        if (availableRecipes.isEmpty()) {
            // If all recipes are in cooldown, use those with lowest cooldown
            availableRecipes = candidates.stream()
                    .sorted((r1, r2) -> {
                        int cooldown1 = recipeCooldown.getOrDefault(r1.getRecipeId(), 0);
                        int cooldown2 = recipeCooldown.getOrDefault(r2.getRecipeId(), 0);
                        return Integer.compare(cooldown1, cooldown2);
                    })
                    .collect(Collectors.toList());
        }

        // Score recipes based on multiple factors using optimized boolean tag scoring
        Recipe bestRecipe = null;
        double bestScore = Double.MAX_VALUE;

        // Add randomization for variety - shuffle the list before scoring
        List<Recipe> shuffledRecipes = new ArrayList<>(availableRecipes);
        Collections.shuffle(shuffledRecipes);

        for (Recipe recipe : shuffledRecipes) {
            double score = calculateOptimizedScore(recipe, targets, usedRecipeIds, tagScores, currentDay);

            if (score < bestScore) {
                bestScore = score;
                bestRecipe = recipe;
            }
        }

        return bestRecipe;
    }

    /**
     * Fallback recipe selection when no ideal matches found
     */
    private Recipe selectFallbackRecipe(MealType mealType, List<String> subcategories,
            List<String> allergies, Set<String> usedRecipeIds,
            Map<String, Integer> recipeCooldown, int currentDay) {

        // Try with basic healthy tag
        List<Recipe> fallbackRecipes = recipeDAO.findRecipesByFilter(mealType, subcategories,
                Arrays.asList("is_healthy"), allergies);

        if (fallbackRecipes.isEmpty()) {
            // Last resort - any recipe for this meal type
            fallbackRecipes = recipeDAO.findRecipesByMealType(mealType);
        }

        if (!fallbackRecipes.isEmpty()) {
            // Select recipe not in cooldown
            for (Recipe recipe : fallbackRecipes) {
                if (!recipeCooldown.containsKey(recipe.getRecipeId())
                        || recipeCooldown.get(recipe.getRecipeId()) <= 0) {
                    return recipe;
                }
            }
            // If all in cooldown, return first one
            return fallbackRecipes.get(0);
        }

        return null;
    }

    /**
     * Calculate tag bonus using efficient boolean column query
     */
    private double calculateBooleanTagBonus(String recipeId, Map<String, Integer> tagScores) {
        if (tagScores == null || tagScores.isEmpty()) {
            return 0;
        }

        double bonus = 0;

        // Build query to check all boolean tags at once
        StringBuilder sql = new StringBuilder("SELECT ");
        List<String> tagColumns = new ArrayList<>(tagScores.keySet());
        sql.append(String.join(", ", tagColumns));
        sql.append(" FROM recipe_all WHERE recipe_id = ?");

        try (Connection conn = com.mealplanner.dal.DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            stmt.setString(1, recipeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                for (String tag : tagColumns) {
                    if (rs.getInt(tag) == 1) {
                        bonus += tagScores.get(tag) * 10; // Scale the bonus
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error calculating boolean tag bonus: " + e.getMessage());
        }

        return bonus;
    }

    /**
     * Map ResultSet to Recipe object (helper method for boolean tag queries)
     */
    private Recipe mapResultSetToRecipe(ResultSet rs) throws SQLException {
        // This should delegate to the RecipeDAO's mapping method
        // For now, we'll use a simplified approach
        Optional<Recipe> optionalRecipe = recipeDAO.findRecipeById(rs.getString("recipe_id"));
        return optionalRecipe.orElse(null);
    }

    // Helper classes
    private static class NutritionTargets {
        final double calories;
        final double protein;
        final double carbs;
        final double fat;

        NutritionTargets(double calories, double protein, double carbs, double fat) {
            this.calories = calories;
            this.protein = protein;
            this.carbs = carbs;
            this.fat = fat;
        }
    }

    private static class TagPriority {
        final List<String> requiredTags;
        final List<String> preferredTags;
        final List<String> prohibitedTags;
        final Map<String, Integer> tagScores;

        TagPriority(List<String> requiredTags, List<String> preferredTags, List<String> prohibitedTags,
                Map<String, Integer> tagScores) {
            this.requiredTags = requiredTags;
            this.preferredTags = preferredTags;
            this.prohibitedTags = prohibitedTags;
            this.tagScores = tagScores;
        }
    }

    /**
     * Calculate comprehensive score with optimized boolean tag scoring and variety
     * control
     */
    private double calculateOptimizedScore(Recipe recipe, NutritionTargets targets,
            Set<String> usedRecipeIds, Map<String, Integer> tagScores, int currentDay) {

        double nutritionScore = calculateNutritionScore(recipe, targets);
        double varietyPenalty = 0;
        double tagBonus = 0;

        // Variety penalty for recently used recipes
        if (usedRecipeIds.contains(recipe.getRecipeId())) {
            varietyPenalty = 500; // Moderate penalty for variety
        }

        // Boolean tag bonus using database query
        if (tagScores != null && !tagScores.isEmpty()) {
            tagBonus = calculateBooleanTagBonus(recipe.getRecipeId(), tagScores);
        }

        // Add randomization factor for variety (small random adjustment)
        Random random = new Random(recipe.getRecipeId().hashCode() + currentDay);
        double randomFactor = random.nextGaussian() * 10; // Small random adjustment

        return nutritionScore + varietyPenalty - tagBonus + randomFactor; // Subtract bonus to reduce score
    }

    /**
     * Generate a personalized family meal plan with individual meal suggestions for
     * each family member
     * Each member gets their own customized meal plan based on their specific
     * preferences, conditions, and nutritional needs
     *
     * @param allMembers List of all family members
     * @return FamilyMealPlan with individual meal plans for each member
     */
    public FamilyMealPlan generateFamilyPlan(List<UserProfile> allMembers) {
        if (allMembers == null || allMembers.isEmpty()) {
            throw new IllegalArgumentException("Family members list cannot be empty");
        }

        System.out.println("\n=== GENERATING PERSONALIZED FAMILY MEAL PLAN ===");
        System.out.println("Creating individual meal plans for " + allMembers.size() + " family members:");
        for (UserProfile member : allMembers) {
            System.out.println("- " + member.getName() + " (Age: " + member.getAge() +
                    ", Goal: " + member.getPrimaryHealthGoal() +
                    ", Conditions: " + member.getMedicalConditions() + ")");
        }

        NutritionCalculatorService nutritionCalculator = new NutritionCalculatorService();

        // Generate individual meal plans for each family member
        Map<String, WeeklyMealPlan> individualMealPlans = new HashMap<>();
        Map<String, NutritionProfile> memberNutritionProfiles = new HashMap<>();

        for (UserProfile member : allMembers) {
            System.out.println("\n--- Generating personalized plan for " + member.getName() + " ---");

            // Calculate individual nutrition profile
            NutritionProfile nutritionProfile = nutritionCalculator.calculateNutritionalNeeds(member);
            memberNutritionProfiles.put(member.getName(), nutritionProfile);

            System.out.println("Target calories: " + nutritionProfile.getTargetCalories() + " kcal/day");
            System.out.println("Dietary preferences: "
                    + (member.getMealPreferences() != null ? member.getMealPreferences().keySet() : "Standard"));
            System.out.println("Allergies: " + (member.getAllergies() != null ? member.getAllergies() : "None"));

            // Generate individual weekly meal plan
            WeeklyMealPlan individualPlan = generateNewWeeklyPlan(member, nutritionProfile);
            individualMealPlans.put(member.getName(), individualPlan);

            System.out.println("✅ Personalized plan generated for " + member.getName());
        }

        // Create family meal plan with the primary user's plan as base (for
        // compatibility)
        UserProfile primaryUser = allMembers.get(0);
        WeeklyMealPlan baseMealPlan = individualMealPlans.get(primaryUser.getName());
        FamilyMealPlan familyPlan = new FamilyMealPlan(baseMealPlan, allMembers);

        // Add individual meal plans to the family plan
        familyPlan.setIndividualMealPlans(individualMealPlans);
        familyPlan.setMemberNutritionProfiles(memberNutritionProfiles);

        // Analyze family meal preferences and potential conflicts
        analyzeFamilyMealCompatibility(familyPlan, allMembers);

        // Generate enhanced consolidated shopping list from all individual plans
        generatePersonalizedShoppingList(familyPlan);

        System.out.println("\n=== PERSONALIZED FAMILY MEAL PLAN COMPLETED ===");
        System.out.println("✅ Individual meal plans created for each family member");
        System.out.println("✅ Each member has meals tailored to their specific needs");
        System.out.println("✅ Family meal compatibility analyzed");
        System.out.println("✅ Consolidated shopping list generated from all individual plans");

        return familyPlan;
    }

    /**
     * Analyze meal compatibility across family members and suggest shared meals
     * where possible
     */
    private void analyzeFamilyMealCompatibility(FamilyMealPlan familyPlan, List<UserProfile> allMembers) {
        System.out.println("\n--- Analyzing Family Meal Compatibility ---");

        Map<String, WeeklyMealPlan> individualPlans = familyPlan.getIndividualMealPlans();

        for (int day = 0; day < 7; day++) {
            System.out.println("Day " + (day + 1) + " compatibility analysis:");

            // Analyze each meal type for potential shared meals
            for (MealType mealType : MealType.values()) {
                analyzeSharedMealPossibility(individualPlans, allMembers, day, mealType, familyPlan);
            }
        }
    }

    /**
     * Analyze if a specific meal can be shared among family members
     */
    private void analyzeSharedMealPossibility(Map<String, WeeklyMealPlan> individualPlans,
            List<UserProfile> allMembers, int day, MealType mealType, FamilyMealPlan familyPlan) {

        Map<String, String> memberRecipes = new HashMap<>();

        // Get each member's recipe for this meal
        for (UserProfile member : allMembers) {
            WeeklyMealPlan memberPlan = individualPlans.get(member.getName());
            if (memberPlan != null && memberPlan.getDailyPlans().size() > day) {
                DailyMealPlan dailyPlan = memberPlan.getDailyPlans().get(day);
                Meal meal = getMealByType(dailyPlan, mealType);
                if (meal != null && !meal.getRecipes().isEmpty()) {
                    memberRecipes.put(member.getName(), meal.getRecipes().get(0).getNameEn());
                }
            }
        }

        // Check for shared recipes
        Map<String, List<String>> recipeToMembers = new HashMap<>();
        for (Map.Entry<String, String> entry : memberRecipes.entrySet()) {
            String memberName = entry.getKey();
            String recipeName = entry.getValue();
            recipeToMembers.computeIfAbsent(recipeName, k -> new ArrayList<>()).add(memberName);
        }

        // Report shared meals
        for (Map.Entry<String, List<String>> entry : recipeToMembers.entrySet()) {
            if (entry.getValue().size() > 1) {
                String sharedRecipe = entry.getKey();
                List<String> sharingMembers = entry.getValue();
                System.out.println(
                        "  " + mealType + ": '" + sharedRecipe.substring(0, Math.min(25, sharedRecipe.length())) +
                                "...' can be shared by " + String.join(", ", sharingMembers));

                // Add compatibility note
                String compatibilityKey = "Day" + (day + 1) + "-" + mealType + "-SHARED";
                familyPlan.addMemberNote(compatibilityKey,
                        Arrays.asList("🍽️ Shared meal possible: " + sharedRecipe + " for "
                                + String.join(", ", sharingMembers)));
            }
        }
    }

    /**
     * Get meal by type from daily plan
     */
    private Meal getMealByType(DailyMealPlan dailyPlan, MealType mealType) {
        switch (mealType) {
            case BREAKFAST:
                return dailyPlan.getBreakfast();
            case LUNCH:
                return dailyPlan.getLunch();
            case DINNER:
                return dailyPlan.getDinner();
            case SNACKS:
                return dailyPlan.getSnacks();
            default:
                return null;
        }
    }

    /**
     * Generate consolidated shopping list from all individual meal plans
     */
    private void generatePersonalizedShoppingList(FamilyMealPlan familyPlan) {
        System.out.println("\n--- Generating Consolidated Shopping List ---");
        Map<String, Integer> consolidatedList = new HashMap<>();

        // Add ingredients from all individual meal plans
        for (Map.Entry<String, WeeklyMealPlan> entry : familyPlan.getIndividualMealPlans().entrySet()) {
            String memberName = entry.getKey();
            WeeklyMealPlan memberPlan = entry.getValue();

            System.out.println("Adding ingredients for " + memberName + "...");

            for (DailyMealPlan dailyPlan : memberPlan.getDailyPlans()) {
                addMealIngredientsToShoppingList(dailyPlan.getBreakfast(), consolidatedList, 1);
                addMealIngredientsToShoppingList(dailyPlan.getLunch(), consolidatedList, 1);
                addMealIngredientsToShoppingList(dailyPlan.getDinner(), consolidatedList, 1);
                addMealIngredientsToShoppingList(dailyPlan.getSnacks(), consolidatedList, 1);
            }
        }

        familyPlan.setConsolidatedShoppingList(consolidatedList);
        System.out.println(
                "✅ Consolidated shopping list created with " + consolidatedList.size() + " unique ingredients");
    }

    /**
     * Add ingredients from a meal to the shopping list, scaled by family size
     */
    private void addMealIngredientsToShoppingList(Meal meal, Map<String, Integer> shoppingList, int familySize) {
        if (meal == null || meal.getRecipes() == null) {
            return;
        }

        for (Recipe recipe : meal.getRecipes()) {
            if (recipe.getIngredients() != null) {
                for (com.mealplanner.model.recipe.Ingredient ingredient : recipe.getIngredients()) {
                    String ingredientName = ingredient.getNameEn();
                    int quantity = (int) Math.ceil(ingredient.getQuantityGrams() * familySize);
                    shoppingList.merge(ingredientName, quantity, Integer::sum);
                }
            }
        }
    }

}
