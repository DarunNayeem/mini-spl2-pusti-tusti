package com.mealplanner.ui;

import com.mealplanner.model.*;
import com.mealplanner.model.user.UserProfile;
import com.mealplanner.service.MealPlannerService;
import com.mealplanner.service.NutritionCalculatorService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Main View Controller for Family Meal Planning Application
 * Manages the tab-based interface where each tab represents a family member
 */
public class MainViewController {
    private static final Logger LOGGER = Logger.getLogger(MainViewController.class.getName());

    // Header Controls
    @FXML
    private Button generateShoppingListButton;
    @FXML
    private Button generatePlanButton;
    @FXML
    private Button generateFamilyPlanButton;
    @FXML
    private Button createEditProfileButton;
    @FXML
    private Button getStartedButton;

    // Main TabPane for family members
    @FXML
    private TabPane familyMembersTabPane;
    @FXML
    private Tab welcomeTab;

    // Data
    private List<UserProfile> familyMembers;
    private Map<UserProfile, FamilyMemberTabController> memberControllers;
    private FamilyMealPlan currentFamilyPlan;

    // Services
    private MealPlannerService mealPlannerService;
    private ProfileService profileService;
    private NutritionCalculatorService nutritionCalculatorService;

    /**
     * Initialize the controller
     */
    public void initialize() {
        familyMembers = new ArrayList<>();
        memberControllers = new HashMap<>();

        // Set up initial state
        showWelcomeMessage();
    }

    /**
     * Set services
     */
    public void setServices(MealPlannerService mealPlannerService, ProfileService profileService,
            NutritionCalculatorService nutritionCalculatorService) {
        this.mealPlannerService = mealPlannerService;
        this.profileService = profileService;
        this.nutritionCalculatorService = nutritionCalculatorService;

        // Load existing profiles
        loadExistingProfiles();
    }

    /**
     * Load existing profiles
     */
    private void loadExistingProfiles() {
        if (profileService != null) {
            try {
                List<UserProfile> familyProfiles = profileService.loadFamilyProfiles();
                if (!familyProfiles.isEmpty()) {
                    familyMembers.addAll(familyProfiles);
                    updateFamilyTabs();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Show welcome message when no profiles exist
     */
    private void showWelcomeMessage() {
        if (familyMembers.isEmpty()) {
            if (!familyMembersTabPane.getTabs().contains(welcomeTab)) {
                familyMembersTabPane.getTabs().add(welcomeTab);
            }
            familyMembersTabPane.getSelectionModel().select(welcomeTab);
        }
    }

    /**
     * Hide welcome message and show family tabs
     */
    private void hideWelcomeMessage() {
        familyMembersTabPane.getTabs().remove(welcomeTab);
    }

    /**
     * Update family member tabs
     */
    private void updateFamilyTabs() {
        if (familyMembers.isEmpty()) {
            showWelcomeMessage();
            return;
        }

        hideWelcomeMessage();

        // Clear existing tabs (except welcome)
        familyMembersTabPane.getTabs().removeIf(tab -> tab != welcomeTab);
        memberControllers.clear();

        // Create tabs for each family member
        for (UserProfile member : familyMembers) {
            createFamilyMemberTab(member);
        }

        // Select first tab
        if (!familyMembersTabPane.getTabs().isEmpty()) {
            familyMembersTabPane.getSelectionModel().select(0);
        }
    }

    /**
     * Create a tab for a family member
     */
    private void createFamilyMemberTab(UserProfile member) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mealplanner/ui/FamilyMemberTab.fxml"));
            Parent content = loader.load();

            FamilyMemberTabController controller = loader.getController();
            controller.setServices(mealPlannerService, nutritionCalculatorService);
            controller.setUserProfile(member);

            Tab memberTab = new Tab(member.getName());
            memberTab.setClosable(false);
            memberTab.setContent(content);

            familyMembersTabPane.getTabs().add(memberTab);
            memberControllers.put(member, controller);

        } catch (IOException e) {
            showAlert("Error", "Failed to create tab for " + member.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle Generate Shopping List button click
     */
    @FXML
    private void handleGenerateShoppingList() {
        if (familyMembers.isEmpty()) {
            showAlert("No Profiles", "Please create family member profiles first.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mealplanner/ui/ShoppingListView.fxml"));
            Parent root = loader.load();

            ShoppingListController controller = loader.getController();
            controller.setFamilyMembers(familyMembers);
            controller.setMemberControllers(memberControllers);

            Stage dialog = new Stage();
            dialog.setTitle("Generate Shopping List");
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(generateShoppingListButton.getScene().getWindow());

            // Apply CSS styling
            dialog.getScene().getStylesheets()
                    .add(getClass().getResource("/com/mealplanner/ui/meal-planner-styles.css").toExternalForm());

            dialog.showAndWait();

        } catch (IOException e) {
            showAlert("Error", "Failed to open shopping list generator: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle Generate Individual Plan button click
     */
    @FXML
    private void handleGeneratePlan() {
        if (familyMembers.isEmpty()) {
            showAlert("No Profiles", "Please create family member profiles first.");
            return;
        }

        // Show profile selection dialog
        UserProfile selectedMember = showProfileSelectionDialog("Select Profile for Individual Meal Plan");
        if (selectedMember != null) {
            FamilyMemberTabController controller = memberControllers.get(selectedMember);
            if (controller != null) {
                controller.handleGeneratePlan();

                // Switch to that member's tab
                for (Tab tab : familyMembersTabPane.getTabs()) {
                    if (tab.getText().equals(selectedMember.getName())) {
                        familyMembersTabPane.getSelectionModel().select(tab);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Handle Generate Family Plan button click
     */
    @FXML
    private void handleGenerateFamilyPlan() {
        if (familyMembers.isEmpty()) {
            showAlert("No Profiles", "Please create family member profiles first.");
            return;
        }

        if (mealPlannerService == null) {
            showAlert("Service Error", "Meal planner service is not available.");
            return;
        }

        try {
            // Generate family meal plan
            currentFamilyPlan = mealPlannerService.generateFamilyPlan(familyMembers);

            if (currentFamilyPlan != null) {
                displayFamilyPlan();

                // Create informative message based on personalization level
                String message;
                if (currentFamilyPlan.getIndividualMealPlans() != null
                        && !currentFamilyPlan.getIndividualMealPlans().isEmpty()) {
                    message = String.format("✅ Generated PERSONALIZED meal plans for %d family members!\n\n" +
                            "🎯 Each member has their own customized meal plan based on:\n" +
                            "• Individual dietary preferences and restrictions\n" +
                            "• Personal health goals and medical conditions\n" +
                            "• Age-appropriate nutritional needs\n\n" +
                            "📊 Individual meal plans: %d\n" +
                            "🛒 Consolidated shopping items: %d\n" +
                            "🤝 Family compatibility notes: %d",
                            familyMembers.size(),
                            currentFamilyPlan.getIndividualMealPlans().size(),
                            currentFamilyPlan.getConsolidatedShoppingList() != null
                                    ? currentFamilyPlan.getConsolidatedShoppingList().size()
                                    : 0,
                            currentFamilyPlan.getMemberSpecificNotes().size());
                } else {
                    message = "Generated meal plan for " + familyMembers.size() + " family members with " +
                            currentFamilyPlan.getMemberSpecificNotes().size() + " special considerations.";
                }

                showInfoDialog("Family Plan Generated", message);
            } else {
                showAlert("Error", "Failed to generate family meal plan.");
            }

        } catch (Exception e) {
            showAlert("Error", "An error occurred while generating the family meal plan: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Display family meal plan across all member tabs
     * Now uses individual personalized meal plans for each family member
     */
    private void displayFamilyPlan() {
        if (currentFamilyPlan == null) {
            return;
        }

        LOGGER.info("Displaying personalized family meal plan in UI...");

        if (currentFamilyPlan.getIndividualMealPlans() != null
                && !currentFamilyPlan.getIndividualMealPlans().isEmpty()) {
            for (UserProfile member : familyMembers) {
                FamilyMemberTabController controller = memberControllers.get(member);
                if (controller == null) {
                    continue;
                }

                WeeklyMealPlan memberMealPlan = currentFamilyPlan.getMealPlanForMember(member.getName());
                if (memberMealPlan != null) {
                    controller.setWeeklyMealPlan(memberMealPlan, true);
                    LOGGER.fine(() -> "Set personalized meal plan for: " + member.getName());
                } else {
                    controller.setWeeklyMealPlan(currentFamilyPlan.getBaseMealPlan(), false);
                    LOGGER.warning("Using base plan for: " + member.getName() + " (individual plan not found)");
                }
            }
        } else {
            LOGGER.warning("Using legacy family meal plan approach (same plan for everyone)");
            applyLegacyFamilyPlan();
        }
    }

    private void applyLegacyFamilyPlan() {
        if (familyMembers.isEmpty() || currentFamilyPlan == null) {
            return;
        }

        UserProfile primaryUser = familyMembers.get(0);
        FamilyMemberTabController primaryController = memberControllers.get(primaryUser);
        if (primaryController != null) {
            primaryController.setWeeklyMealPlan(currentFamilyPlan.getBaseMealPlan(), false);
        }

        for (int i = 1; i < familyMembers.size(); i++) {
            UserProfile member = familyMembers.get(i);
            FamilyMemberTabController controller = memberControllers.get(member);
            if (controller != null) {
                controller.setWeeklyMealPlan(currentFamilyPlan.getBaseMealPlan(), false);
            }
        }
    }

    /**
     * Handle Create/Edit Profile button click
     */
    @FXML
    private void handleCreateEditProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mealplanner/ui/ProfileListView.fxml"));
            Parent root = loader.load();

            ProfileListController controller = loader.getController();
            controller.setProfileService(profileService);
            controller.setProfiles(familyMembers);
            controller.setCallback(this::updateFamilyMembers);

            Stage dialog = new Stage();
            dialog.setTitle("Manage Family Profiles");
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(createEditProfileButton.getScene().getWindow());

            // Apply CSS styling
            dialog.getScene().getStylesheets()
                    .add(getClass().getResource("/com/mealplanner/ui/meal-planner-styles.css").toExternalForm());

            dialog.showAndWait();

        } catch (IOException e) {
            showAlert("Error", "Failed to open profile manager: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update family members from profile list callback
     */
    private void updateFamilyMembers(List<UserProfile> updatedProfiles) {
        familyMembers.clear();
        familyMembers.addAll(updatedProfiles);
        updateFamilyTabs();
    }

    /**
     * Show profile selection dialog
     */
    private UserProfile showProfileSelectionDialog(String title) {
        if (familyMembers.size() == 1) {
            return familyMembers.get(0);
        }

        Dialog<UserProfile> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText("Choose a family member:");

        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        ListView<UserProfile> profilesList = new ListView<>();
        profilesList.getItems().setAll(familyMembers);
        profilesList.setCellFactory(listView -> new ListCell<UserProfile>() {
            @Override
            protected void updateItem(UserProfile profile, boolean empty) {
                super.updateItem(profile, empty);
                if (empty || profile == null) {
                    setText(null);
                } else {
                    setText(profile.getName() + " (" + profile.getAge() + " years)");
                }
            }
        });

        if (!familyMembers.isEmpty()) {
            profilesList.getSelectionModel().select(0);
        }

        dialog.getDialogPane().setContent(profilesList);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return profilesList.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
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
}
