package com.mealplanner.model.user;

import com.mealplanner.model.Gender;
import com.mealplanner.model.HealthGoal;
import com.mealplanner.model.MealType;
import com.mealplanner.model.MedicalCondition;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for UserProfile
 */
public class UserProfileTest {

    @Test
    public void testUserProfileCreation() {
        // Arrange
        List<String> allergies = Arrays.asList("nuts", "dairy");
        Map<MealType, List<String>> mealPreferences = new HashMap<>();
        mealPreferences.put(MealType.BREAKFAST, Arrays.asList("oatmeal", "eggs"));
        List<MedicalCondition> conditions = Arrays.asList(MedicalCondition.DIABETES);

        // Act
        UserProfile profile = new UserProfile(
                "user123",
                "John Doe",
                30,
                175.0,
                70.0,
                Gender.MALE,
                HealthGoal.WEIGHT_LOSS,
                allergies,
                mealPreferences,
                conditions,
                3);

        // Assert
        assertEquals("user123", profile.getUserId());
        assertEquals("John Doe", profile.getName());
        assertEquals(30, profile.getAge());
        assertEquals(175.0, profile.getHeightCm());
        assertEquals(70.0, profile.getWeightKg());
        assertEquals(Gender.MALE, profile.getGender());
        assertEquals(HealthGoal.WEIGHT_LOSS, profile.getPrimaryHealthGoal());
        assertEquals(allergies, profile.getAllergies());
        assertEquals(mealPreferences, profile.getMealPreferences());
        assertEquals(conditions, profile.getMedicalConditions());
        assertEquals(3, profile.getActivityLevel());
    }

    @Test
    public void testFamilyMemberCreation() {
        // Arrange
        UserProfile baseProfile = new UserProfile();
        baseProfile.setName("Jane Doe");
        baseProfile.setAge(25);

        // Act
        FamilyMember familyMember = new FamilyMember(baseProfile, "Wife");

        // Assert
        assertEquals("Wife", familyMember.getMemberName());
        assertEquals("Jane Doe", familyMember.getName());
        assertEquals(25, familyMember.getAge());
    }
}
