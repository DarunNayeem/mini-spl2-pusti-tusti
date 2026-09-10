package com.mealplanner.model.user;

import com.mealplanner.model.Gender;
import com.mealplanner.model.HealthGoal;
import com.mealplanner.model.MealType;
import com.mealplanner.model.MedicalCondition;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * User profile containing personal information, health goals, and preferences
 */
public class UserProfile {
    private String userId;
    private String name;
    private int age;
    private double heightCm;
    private double weightKg;
    private Gender gender;
    private HealthGoal primaryHealthGoal;
    private List<String> allergies;
    private Map<MealType, List<String>> mealPreferences;
    private List<MedicalCondition> medicalConditions;
    private int activityLevel; // 1-5 scale

    // Default constructor
    public UserProfile() {
    }

    // Constructor with all fields
    public UserProfile(String userId, String name, int age, double heightCm, double weightKg,
            Gender gender, HealthGoal primaryHealthGoal, List<String> allergies,
            Map<MealType, List<String>> mealPreferences, List<MedicalCondition> medicalConditions,
            int activityLevel) {
        this.userId = userId;
        this.name = name;
        this.age = age;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.gender = gender;
        this.primaryHealthGoal = primaryHealthGoal;
        this.allergies = allergies;
        this.mealPreferences = mealPreferences;
        this.medicalConditions = medicalConditions;
        this.activityLevel = activityLevel;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(double heightCm) {
        this.heightCm = heightCm;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public HealthGoal getPrimaryHealthGoal() {
        return primaryHealthGoal;
    }

    public void setPrimaryHealthGoal(HealthGoal primaryHealthGoal) {
        this.primaryHealthGoal = primaryHealthGoal;
    }

    public List<String> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }

    public Map<MealType, List<String>> getMealPreferences() {
        return mealPreferences;
    }

    public void setMealPreferences(Map<MealType, List<String>> mealPreferences) {
        this.mealPreferences = mealPreferences;
    }

    public List<MedicalCondition> getMedicalConditions() {
        return medicalConditions;
    }

    public void setMedicalConditions(List<MedicalCondition> medicalConditions) {
        this.medicalConditions = medicalConditions;
    }

    public int getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(int activityLevel) {
        this.activityLevel = activityLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserProfile that)) {
            return false;
        }
        return Objects.equals(userId, that.userId)
                || (userId != null && userId.equals(that.userId))
                || (userId == null && Objects.equals(name, that.name));
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : (name != null ? name.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", heightCm=" + heightCm +
                ", weightKg=" + weightKg +
                ", gender=" + gender +
                ", primaryHealthGoal=" + primaryHealthGoal +
                ", allergies=" + allergies +
                ", mealPreferences=" + mealPreferences +
                ", medicalConditions=" + medicalConditions +
                ", activityLevel=" + activityLevel +
                '}';
    }
}
