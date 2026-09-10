package com.mealplanner.dal;

import com.mealplanner.model.user.UserProfile;

import java.util.List;
import java.util.Optional;

/**
 * DAO for storing and retrieving user profiles from SQLite.
 */
public interface ProfileDAO {

    List<UserProfile> findAllProfiles();

    Optional<UserProfile> findProfileById(String profileId);

    boolean saveProfile(UserProfile profile);

    boolean replaceAllProfiles(List<UserProfile> profiles);

    boolean deleteProfile(String profileId);

    boolean deleteAllProfiles();

    boolean existsByName(String name, String excludedProfileId);
}