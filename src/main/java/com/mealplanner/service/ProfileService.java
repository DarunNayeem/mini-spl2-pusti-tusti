package com.mealplanner.service;

import com.mealplanner.dal.DAOFactory;
import com.mealplanner.dal.ProfileDAO;
import com.mealplanner.model.user.UserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Service for managing user profiles through the SQLite-backed repository.
 */
public class ProfileService {
    private static final Logger LOGGER = Logger.getLogger(ProfileService.class.getName());

    private final ProfileDAO profileDAO;
    private List<UserProfile> familyProfiles;

    public ProfileService() {
        this(DAOFactory.getProfileDAO());
    }

    public ProfileService(ProfileDAO profileDAO) {
        this.profileDAO = profileDAO;
        this.familyProfiles = new ArrayList<>();
    }

    /**
     * Load all family profiles from the database.
     */
    public List<UserProfile> loadFamilyProfiles() {
        familyProfiles = new ArrayList<>(profileDAO.findAllProfiles());
        return new ArrayList<>(familyProfiles);
    }

    /**
     * Save all family profiles.
     */
    public boolean saveFamilyProfiles(List<UserProfile> profiles) {
        List<UserProfile> safeProfiles = profiles == null ? List.of() : new ArrayList<>(profiles);
        boolean saved = profileDAO.replaceAllProfiles(safeProfiles);
        if (saved) {
            familyProfiles = new ArrayList<>(safeProfiles);
        }
        return saved;
    }

    /**
     * Add a new family profile.
     */
    public boolean addFamilyProfile(UserProfile profile) {
        if (profile == null) {
            return false;
        }

        ensureProfileId(profile);

        if (isDuplicateProfileName(profile, null)) {
            LOGGER.warning("Profile with name '" + profile.getName() + "' already exists");
            return false;
        }

        boolean saved = profileDAO.saveProfile(profile);
        if (saved) {
            refreshCachedProfiles(profile);
        }
        return saved;
    }

    /**
     * Update an existing family profile.
     */
    public boolean updateFamilyProfile(UserProfile profile) {
        if (profile == null) {
            return false;
        }

        ensureProfileId(profile);

        if (isDuplicateProfileName(profile, profile.getUserId())) {
            LOGGER.warning("Profile with name '" + profile.getName() + "' already exists");
            return false;
        }

        boolean saved = profileDAO.saveProfile(profile);
        if (saved) {
            refreshCachedProfiles(profile);
        }
        return saved;
    }

    /**
     * Delete a family profile.
     */
    public boolean deleteFamilyProfile(String profileId) {
        boolean deleted = profileDAO.deleteProfile(profileId);
        if (deleted) {
            familyProfiles.removeIf(profile -> profileId != null && profileId.equals(profile.getUserId()));
        }
        return deleted;
    }

    /**
     * Get family profile by ID.
     */
    public Optional<UserProfile> getFamilyProfileById(String profileId) {
        return profileDAO.findProfileById(profileId);
    }

    private boolean isDuplicateProfileName(UserProfile profile, String excludedProfileId) {
        return profile != null
                && profile.getName() != null
                && !profile.getName().isBlank()
                && profileDAO.existsByName(profile.getName(), excludedProfileId);
    }

    private void ensureProfileId(UserProfile profile) {
        if (profile.getUserId() == null || profile.getUserId().isBlank()) {
            profile.setUserId(UUID.randomUUID().toString());
        }
    }

    private void refreshCachedProfiles(UserProfile profile) {
        if (familyProfiles == null) {
            familyProfiles = new ArrayList<>();
        }

        familyProfiles.removeIf(existing -> existing.getUserId() != null
                && existing.getUserId().equals(profile.getUserId()));
        familyProfiles.add(profile);
        familyProfiles.sort((left, right) -> left.getName().compareToIgnoreCase(right.getName()));
    }
}
