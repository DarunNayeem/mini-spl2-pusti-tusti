package com.mealplanner.ui;

import com.mealplanner.model.user.UserProfile;
import com.mealplanner.service.ProfileService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for managing the list of family profiles
 */
public class ProfileListController {

    @FXML
    private ListView<UserProfile> profilesListView;
    @FXML
    private Button addProfileButton;
    @FXML
    private Button editProfileButton;
    @FXML
    private Button deleteProfileButton;
    @FXML
    private Button closeButton;
    @FXML
    private Label profileCountLabel;

    // Data
    private List<UserProfile> profiles;
    private ProfileService profileService;

    // Callback for profile changes
    private ProfileChangeCallback callback;

    public interface ProfileChangeCallback {
        void onProfilesChanged(List<UserProfile> profiles);
    }

    /**
     * Initialize the controller
     */
    public void initialize() {
        profiles = new ArrayList<>();
        setupListView();
        setupListeners();
    }

    /**
     * Set up the list view
     */
    private void setupListView() {
        profilesListView.setCellFactory(listView -> new ListCell<UserProfile>() {
            @Override
            protected void updateItem(UserProfile profile, boolean empty) {
                super.updateItem(profile, empty);
                if (empty || profile == null) {
                    setText(null);
                } else {
                    setText(profile.getName() + " (" + profile.getAge() + " years, " + profile.getGender() + ")");
                }
            }
        });
    }

    /**
     * Set up event listeners
     */
    private void setupListeners() {
        // Selection listener for enabling/disabling buttons
        profilesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            editProfileButton.setDisable(!hasSelection);
            deleteProfileButton.setDisable(!hasSelection);
        });
    }

    /**
     * Set the profile service
     */
    public void setProfileService(ProfileService profileService) {
        this.profileService = profileService;
        loadProfiles();
    }

    /**
     * Set the callback for profile changes
     */
    public void setCallback(ProfileChangeCallback callback) {
        this.callback = callback;
    }

    /**
     * Load profiles from service
     */
    private void loadProfiles() {
        if (profileService == null)
            return;

        try {
            profiles.clear();
            profiles.addAll(profileService.loadFamilyProfiles());

            updateProfilesList();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load profiles: " + e.getMessage());
        }
    }

    /**
     * Update the profiles list view
     */
    private void updateProfilesList() {
        profilesListView.getItems().setAll(profiles);
        profileCountLabel.setText(profiles.size() + " profile" + (profiles.size() != 1 ? "s" : ""));

        // Notify callback of changes
        if (callback != null) {
            callback.onProfilesChanged(new ArrayList<>(profiles));
        }
    }

    /**
     * Handle add profile button click
     */
    @FXML
    private void handleAddProfile() {
        openProfileEditor(null);
    }

    /**
     * Handle edit profile button click
     */
    @FXML
    private void handleEditProfile() {
        UserProfile selectedProfile = profilesListView.getSelectionModel().getSelectedItem();
        if (selectedProfile != null) {
            openProfileEditor(selectedProfile);
        }
    }

    /**
     * Handle delete profile button click
     */
    @FXML
    private void handleDeleteProfile() {
        UserProfile selectedProfile = profilesListView.getSelectionModel().getSelectedItem();
        if (selectedProfile == null)
            return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Profile");
        confirmAlert.setHeaderText("Delete " + selectedProfile.getName() + "'s Profile?");
        confirmAlert.setContentText("This action cannot be undone. Are you sure you want to delete this profile?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = profileService.deleteFamilyProfile(selectedProfile.getUserId());
            if (success) {
                profiles.remove(selectedProfile);
                updateProfilesList();
                showInfoDialog("Profile Deleted", "Profile for " + selectedProfile.getName() + " has been deleted.");
            } else {
                showAlert("Delete Error", "Failed to delete profile.");
            }
        }
    }

    /**
     * Handle close button click
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Open the profile editor
     */
    private void openProfileEditor(UserProfile profileToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mealplanner/ui/ProfileEditorView.fxml"));
            Parent root = loader.load();

            ProfileEditorController controller = loader.getController();

            if (profileToEdit != null) {
                controller.setProfileData(profileToEdit);
            }

            Stage dialog = new Stage();
            dialog.setTitle(profileToEdit == null ? "Create New Profile" : "Edit Profile - " + profileToEdit.getName());
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(addProfileButton.getScene().getWindow());

            // Apply CSS styling
            dialog.getScene().getStylesheets()
                    .add(getClass().getResource("/com/mealplanner/ui/meal-planner-styles.css").toExternalForm());

            dialog.showAndWait();

            // Check if profile was saved
            if (controller.isSaved()) {
                Optional<UserProfile> savedProfile = Optional.ofNullable(controller.getProfileFromFormData());
                if (savedProfile.isPresent()) {
                    boolean success = false;
                    if (profileToEdit == null) {
                        // New profile - add to family profiles
                        success = profileService.addFamilyProfile(savedProfile.get());
                        if (success) {
                            profiles.add(savedProfile.get());
                            showInfoDialog("Profile Created",
                                    "Profile for " + savedProfile.get().getName() + " has been created successfully.");
                        } else {
                            showAlert("Save Error",
                                    "Failed to add new profile. A profile with this name may already exist.");
                        }
                    } else {
                        // Update existing profile
                        success = profileService.updateFamilyProfile(savedProfile.get());
                        if (success) {
                            int index = profiles.indexOf(profileToEdit);
                            if (index != -1) {
                                profiles.set(index, savedProfile.get());
                            }
                            showInfoDialog("Profile Updated",
                                    "Profile for " + savedProfile.get().getName() + " has been updated successfully.");
                        } else {
                            showAlert("Save Error", "Failed to update profile.");
                        }
                    }

                    if (success) {
                        updateProfilesList();
                    }
                }
            }

        } catch (IOException e) {
            showAlert("Error", "Failed to open profile editor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show error alert
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show info dialog
     */
    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Get current profiles list
     */
    public List<UserProfile> getProfiles() {
        return new ArrayList<>(profiles);
    }

    /**
     * Set profiles list
     */
    public void setProfiles(List<UserProfile> profiles) {
        this.profiles = new ArrayList<>(profiles);
        updateProfilesList();
    }
}
