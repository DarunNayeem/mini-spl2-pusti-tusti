package com.mealplanner.service;

import com.mealplanner.dal.DatabaseManager;
import com.mealplanner.model.Gender;
import com.mealplanner.model.HealthGoal;
import com.mealplanner.model.MealType;
import com.mealplanner.model.MedicalCondition;
import com.mealplanner.model.user.UserProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProfileServiceTest {

    private Path tempDatabaseDirectory;
    private ProfileService profileService;

    @BeforeEach
    void setUp() throws IOException {
        tempDatabaseDirectory = Files.createTempDirectory("profile-service-test");
        String databaseUrl = "jdbc:sqlite:" + tempDatabaseDirectory.resolve("profiles.db").toAbsolutePath();
        System.setProperty("mealplanner.db.url", databaseUrl);
        DatabaseManager.closeConnection();
        profileService = new ProfileService();
    }

    @AfterEach
    void tearDown() throws IOException {
        DatabaseManager.closeConnection();
        System.clearProperty("mealplanner.db.url");
        if (tempDatabaseDirectory != null) {
            Files.walk(tempDatabaseDirectory)
                    .sorted((left, right) -> right.compareTo(left))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    @Test
    void saveLoadUpdateAndDeleteProfiles() {
        UserProfile firstProfile = createProfile("profile-1", "Amina");
        UserProfile secondProfile = createProfile("profile-2", "Karim");

        assertTrue(profileService.saveFamilyProfiles(List.of(firstProfile, secondProfile)));

        List<UserProfile> loadedProfiles = profileService.loadFamilyProfiles();
        assertEquals(2, loadedProfiles.size());
        assertEquals("Amina", loadedProfiles.get(0).getName());
        assertEquals("Karim", loadedProfiles.get(1).getName());

        Optional<UserProfile> foundProfile = profileService.getFamilyProfileById("profile-1");
        assertTrue(foundProfile.isPresent());
        assertEquals(29, foundProfile.get().getAge());

        UserProfile updatedProfile = foundProfile.get();
        updatedProfile.setName("Amina Updated");
        updatedProfile.setWeightKg(67.5);
        assertTrue(profileService.updateFamilyProfile(updatedProfile));

        Optional<UserProfile> reloadedProfile = profileService.getFamilyProfileById("profile-1");
        assertTrue(reloadedProfile.isPresent());
        assertEquals("Amina Updated", reloadedProfile.get().getName());
        assertEquals(67.5, reloadedProfile.get().getWeightKg());

        assertTrue(profileService.deleteFamilyProfile("profile-2"));
        assertEquals(1, profileService.loadFamilyProfiles().size());
        assertFalse(profileService.getFamilyProfileById("profile-2").isPresent());
    }

    private UserProfile createProfile(String userId, String name) {
        Map<MealType, List<String>> mealPreferences = new HashMap<>();
        mealPreferences.put(MealType.BREAKFAST, Arrays.asList("oatmeal", "eggs"));
        mealPreferences.put(MealType.LUNCH, Arrays.asList("rice", "lentils"));

        return new UserProfile(
                userId,
                name,
                29,
                170.0,
                65.0,
                Gender.FEMALE,
                HealthGoal.WEIGHT_LOSS,
                Arrays.asList("nuts"),
                mealPreferences,
                Arrays.asList(MedicalCondition.DIABETES),
                3);
    }
}
