package com.mealplanner.model;

import com.mealplanner.model.recipe.Recipe;
import com.mealplanner.model.user.UserProfile;
import com.mealplanner.model.user.NutritionProfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enhanced family meal plan with individual meal plans for each family member
 * Each family member gets their own personalized meal plan based on their
 * specific preferences, conditions, and nutritional needs
 */
public class FamilyMealPlan {
    private WeeklyMealPlan baseMealPlan; // For compatibility - primary user's plan
    private List<UserProfile> familyMembers;
    private Map<String, List<String>> memberSpecificNotes;
    private Map<String, List<Recipe>> memberAlternatives;
    private Map<String, Integer> consolidatedShoppingList; // ingredient -> total quantity

    // NEW: Individual meal plans for each family member
    private Map<String, WeeklyMealPlan> individualMealPlans; // memberName -> WeeklyMealPlan
    private Map<String, NutritionProfile> memberNutritionProfiles; // memberName -> NutritionProfile

    // Default constructor
    public FamilyMealPlan() {
        this.memberSpecificNotes = new HashMap<>();
        this.memberAlternatives = new HashMap<>();
        this.consolidatedShoppingList = new HashMap<>();
        this.individualMealPlans = new HashMap<>();
        this.memberNutritionProfiles = new HashMap<>();
    }

    // Constructor with all fields
    public FamilyMealPlan(WeeklyMealPlan baseMealPlan, List<UserProfile> familyMembers) {
        this.baseMealPlan = baseMealPlan;
        this.familyMembers = familyMembers;
        this.memberSpecificNotes = new HashMap<>();
        this.memberAlternatives = new HashMap<>();
        this.consolidatedShoppingList = new HashMap<>();
        this.individualMealPlans = new HashMap<>();
        this.memberNutritionProfiles = new HashMap<>();
    }

    // Getters and Setters
    public WeeklyMealPlan getBaseMealPlan() {
        return baseMealPlan;
    }

    public void setBaseMealPlan(WeeklyMealPlan baseMealPlan) {
        this.baseMealPlan = baseMealPlan;
    }

    public List<UserProfile> getFamilyMembers() {
        return familyMembers;
    }

    public void setFamilyMembers(List<UserProfile> familyMembers) {
        this.familyMembers = familyMembers;
    }

    public Map<String, List<String>> getMemberSpecificNotes() {
        return memberSpecificNotes;
    }

    public void setMemberSpecificNotes(Map<String, List<String>> memberSpecificNotes) {
        this.memberSpecificNotes = memberSpecificNotes;
    }

    public Map<String, List<Recipe>> getMemberAlternatives() {
        return memberAlternatives;
    }

    public void setMemberAlternatives(Map<String, List<Recipe>> memberAlternatives) {
        this.memberAlternatives = memberAlternatives;
    }

    public Map<String, Integer> getConsolidatedShoppingList() {
        return consolidatedShoppingList;
    }

    public void setConsolidatedShoppingList(Map<String, Integer> consolidatedShoppingList) {
        this.consolidatedShoppingList = consolidatedShoppingList;
    }

    // NEW: Getters and setters for individual meal plans and nutrition profiles
    public Map<String, WeeklyMealPlan> getIndividualMealPlans() {
        return individualMealPlans;
    }

    public void setIndividualMealPlans(Map<String, WeeklyMealPlan> individualMealPlans) {
        this.individualMealPlans = individualMealPlans;
    }

    public Map<String, NutritionProfile> getMemberNutritionProfiles() {
        return memberNutritionProfiles;
    }

    public void setMemberNutritionProfiles(Map<String, NutritionProfile> memberNutritionProfiles) {
        this.memberNutritionProfiles = memberNutritionProfiles;
    }

    /**
     * Get individual meal plan for a specific family member
     * 
     * @param memberName Family member name
     * @return WeeklyMealPlan for this member, or null if not found
     */
    public WeeklyMealPlan getMealPlanForMember(String memberName) {
        return individualMealPlans.get(memberName);
    }

    /**
     * Get nutrition profile for a specific family member
     * 
     * @param memberName Family member name
     * @return NutritionProfile for this member, or null if not found
     */
    public NutritionProfile getNutritionProfileForMember(String memberName) {
        return memberNutritionProfiles.get(memberName);
    }

    /**
     * Add a note for a specific family member for a specific meal
     * 
     * @param dayMealMember Format: "Day1-Lunch-ChildName"
     * @param notes         List of notes for this member and meal
     */
    public void addMemberNote(String dayMealMember, List<String> notes) {
        this.memberSpecificNotes.put(dayMealMember, notes);
    }

    /**
     * Add alternative recipe for a specific family member for a specific meal
     * 
     * @param dayMealMember Format: "Day1-Lunch-ChildName"
     * @param alternatives  List of alternative recipes for this member and meal
     */
    public void addMemberAlternative(String dayMealMember, List<Recipe> alternatives) {
        this.memberAlternatives.put(dayMealMember, alternatives);
    }

    /**
     * Get notes for a specific family member for a specific meal
     * 
     * @param dayNumber  Day number (1-7)
     * @param mealType   Meal type (BREAKFAST, LUNCH, DINNER, SNACKS)
     * @param memberName Family member name
     * @return List of notes for this combination
     */
    public List<String> getNotesForMember(int dayNumber, MealType mealType, String memberName) {
        String key = "Day" + dayNumber + "-" + mealType.toString() + "-" + memberName;
        return memberSpecificNotes.getOrDefault(key, List.of());
    }

    /**
     * Get alternative recipes for a specific family member for a specific meal
     * 
     * @param dayNumber  Day number (1-7)
     * @param mealType   Meal type (BREAKFAST, LUNCH, DINNER, SNACKS)
     * @param memberName Family member name
     * @return List of alternative recipes for this combination
     */
    public List<Recipe> getAlternativesForMember(int dayNumber, MealType mealType, String memberName) {
        String key = "Day" + dayNumber + "-" + mealType.toString() + "-" + memberName;
        return memberAlternatives.getOrDefault(key, List.of());
    }

    @Override
    public String toString() {
        return "FamilyMealPlan{" +
                "baseMealPlan=" + baseMealPlan +
                ", familyMembers=" + familyMembers.size() + " members" +
                ", individualMealPlans=" + individualMealPlans.size() + " personal plans" +
                ", memberNutritionProfiles=" + memberNutritionProfiles.size() + " profiles" +
                ", memberSpecificNotes=" + memberSpecificNotes.size() + " entries" +
                ", memberAlternatives=" + memberAlternatives.size() + " entries" +
                ", consolidatedShoppingList=" + consolidatedShoppingList.size() + " items" +
                '}';
    }
}
