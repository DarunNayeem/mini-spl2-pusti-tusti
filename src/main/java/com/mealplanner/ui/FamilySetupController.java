package com.mealplanner.ui;

import com.mealplanner.model.*;
import com.mealplanner.model.user.UserProfile;
import com.mealplanner.service.MealPlannerService;
import com.mealplanner.service.NutritionCalculatorService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the Family Setup Dialog
 * Handles family member creation and family meal plan generation
 */
public class FamilySetupController {

    // UI Components
    @FXML
    private ListView<UserProfile> familyMembersListView;
    @FXML
    private Button addMemberButton;

    // Member Details Form
    @FXML
    private VBox memberDetailsSection;
    @FXML
    private TextField memberNameField;
    @FXML
    private TextField memberAgeField;
    @FXML
    private ComboBox<Gender> memberGenderCombo;
    @FXML
    private TextField memberHeightField;
    @FXML
    private TextField memberWeightField;
    @FXML
    private ComboBox<String> memberActivityCombo;
    @FXML
    private ComboBox<HealthGoal> memberHealthGoalCombo;

    // Allergies Section
    @FXML
    private TextField allergyInputField;
    @FXML
    private Button addAllergyButton;
    @FXML
    private FlowPane allergiesFlowPane;

    // Medical Conditions Section
    @FXML
    private ComboBox<MedicalCondition> medicalConditionCombo;
    @FXML
    private FlowPane medicalConditionsFlowPane;

    // Action Buttons
    @FXML
    private Button cancelMemberButton;
    @FXML
    private Button saveMemberButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button generateFamilyPlanButton;

    // Data
    private List<UserProfile> familyMembers;
    private List<String> currentAllergies;
    private List<MedicalCondition> currentMedicalConditions;
    private UserProfile editingMember;
    private boolean isEditMode;

    // Services
    private MealPlannerService mealPlannerService;

    // Callback for generated family plan
    private FamilyPlanGeneratedCallback callback;

    public interface FamilyPlanGeneratedCallback {
        void onFamilyPlanGenerated(FamilyMealPlan familyPlan);
    }

    /**
     * Initialize the controller
     */
    public void initialize() {
        familyMembers = new ArrayList<>();
        currentAllergies = new ArrayList<>();
        currentMedicalConditions = new ArrayList<>();

        setupUI();
        setupListeners();
    }

    /**
     * Set up the UI components
     */
    private void setupUI() {
        // Set up family members list view
        familyMembersListView.setCellFactory(listView -> new ListCell<UserProfile>() {
            @Override
            protected void updateItem(UserProfile member, boolean empty) {
                super.updateItem(member, empty);
                if (empty || member == null) {
                    setText(null);
                } else {
                    setText(member.getName() + " (" + member.getAge() + " years, " + member.getGender() + ")");
                }
            }
        });

        // Set up combo boxes
        memberGenderCombo.getItems().setAll(Gender.values());
        memberActivityCombo.getItems().addAll(
                "1 - Sedentary (little/no exercise)",
                "2 - Light (light exercise 1-3 days/week)",
                "3 - Moderate (moderate exercise 3-5 days/week)",
                "4 - Active (hard exercise 6-7 days/week)",
                "5 - Very Active (very hard exercise, physical job)");
        memberHealthGoalCombo.getItems().setAll(HealthGoal.values());
        medicalConditionCombo.getItems().setAll(MedicalCondition.values());

        // Set default selections
        memberGenderCombo.setValue(Gender.MALE);
        memberActivityCombo.setValue("3 - Moderate (moderate exercise 3-5 days/week)");
        memberHealthGoalCombo.setValue(HealthGoal.MAINTENANCE);
    }

    /**
     * Set up event listeners
     */
    private void setupListeners() {
        // Family members list selection listener
        familyMembersListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                editMember(newVal);
            }
        });

        // Medical condition combo listener
        medicalConditionCombo.setOnAction(e -> {
            MedicalCondition selected = medicalConditionCombo.getValue();
            if (selected != null && !currentMedicalConditions.contains(selected)) {
                addMedicalConditionTag(selected);
                medicalConditionCombo.setValue(null);
            }
        });

        // Update generate button state based on family members
        familyMembersListView.itemsProperty()
                .addListener((obs, oldVal, newVal) -> generateFamilyPlanButton.setDisable(familyMembers.isEmpty()));
    }

    /**
     * Handle adding a new family member
     */
    @FXML
    private void handleAddMember() {
        clearMemberForm();
        isEditMode = false;
        editingMember = null;
        memberDetailsSection.setVisible(true);
    }

    /**
     * Handle adding an allergy
     */
    @FXML
    private void handleAddAllergy() {
        String allergy = allergyInputField.getText().trim();
        if (!allergy.isEmpty() && !currentAllergies.contains(allergy)) {
            currentAllergies.add(allergy);
            addAllergyTag(allergy);
            allergyInputField.clear();
        }
    }

    /**
     * Handle saving a family member
     */
    @FXML
    private void handleSaveMember() {
        if (validateMemberForm()) {
            UserProfile member = createMemberFromForm();

            if (isEditMode && editingMember != null) {
                // Replace existing member
                int index = familyMembers.indexOf(editingMember);
                if (index != -1) {
                    familyMembers.set(index, member);
                }
            } else {
                // Add new member
                familyMembers.add(member);
            }

            refreshFamilyMembersList();
            memberDetailsSection.setVisible(false);
            clearMemberForm();
        }
    }

    /**
     * Handle canceling member editing
     */
    @FXML
    private void handleCancelMember() {
        memberDetailsSection.setVisible(false);
        clearMemberForm();
        familyMembersListView.getSelectionModel().clearSelection();
    }

    /**
     * Handle canceling the entire dialog
     */
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Handle generating the family meal plan
     */
    @FXML
    private void handleGenerateFamilyPlan() {
        if (familyMembers.isEmpty()) {
            showAlert("Error", "Please add at least one family member before generating a meal plan.");
            return;
        }

        if (mealPlannerService == null) {
            showAlert("Error", "Meal planner service is not available.");
            return;
        }

        try {
            // Generate the family meal plan
            FamilyMealPlan familyPlan = mealPlannerService.generateFamilyPlan(familyMembers);

            // Call the callback if set
            if (callback != null) {
                callback.onFamilyPlanGenerated(familyPlan);
            }

            // Close the dialog
            Stage stage = (Stage) generateFamilyPlanButton.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to generate family meal plan: " + e.getMessage());
        }
    }

    /**
     * Edit an existing family member
     */
    private void editMember(UserProfile member) {
        isEditMode = true;
        editingMember = member;
        populateMemberForm(member);
        memberDetailsSection.setVisible(true);
    }

    /**
     * Populate the member form with data from an existing member
     */
    private void populateMemberForm(UserProfile member) {
        memberNameField.setText(member.getName());
        memberAgeField.setText(String.valueOf(member.getAge()));
        memberGenderCombo.setValue(member.getGender());
        memberHeightField.setText(String.valueOf(member.getHeightCm()));
        memberWeightField.setText(String.valueOf(member.getWeightKg()));
        memberActivityCombo
                .setValue(member.getActivityLevel() + " - " + getActivityDescription(member.getActivityLevel()));
        memberHealthGoalCombo.setValue(member.getPrimaryHealthGoal());

        // Populate allergies
        currentAllergies.clear();
        allergiesFlowPane.getChildren().clear();
        if (member.getAllergies() != null) {
            currentAllergies.addAll(member.getAllergies());
            for (String allergy : member.getAllergies()) {
                addAllergyTag(allergy);
            }
        }

        // Populate medical conditions
        currentMedicalConditions.clear();
        medicalConditionsFlowPane.getChildren().clear();
        if (member.getMedicalConditions() != null) {
            currentMedicalConditions.addAll(member.getMedicalConditions());
            for (MedicalCondition condition : member.getMedicalConditions()) {
                addMedicalConditionTag(condition);
            }
        }
    }

    /**
     * Clear the member form
     */
    private void clearMemberForm() {
        memberNameField.clear();
        memberAgeField.clear();
        memberGenderCombo.setValue(Gender.MALE);
        memberHeightField.clear();
        memberWeightField.clear();
        memberActivityCombo.setValue("3 - Moderate (moderate exercise 3-5 days/week)");
        memberHealthGoalCombo.setValue(HealthGoal.MAINTENANCE);

        currentAllergies.clear();
        allergiesFlowPane.getChildren().clear();

        currentMedicalConditions.clear();
        medicalConditionsFlowPane.getChildren().clear();
    }

    /**
     * Create a UserProfile from the form data
     */
    private UserProfile createMemberFromForm() {
        UserProfile member = new UserProfile();
        member.setUserId(UUID.randomUUID().toString());
        member.setName(memberNameField.getText().trim());
        member.setAge(Integer.parseInt(memberAgeField.getText()));
        member.setGender(memberGenderCombo.getValue());
        member.setHeightCm(Double.parseDouble(memberHeightField.getText()));
        member.setWeightKg(Double.parseDouble(memberWeightField.getText()));
        member.setActivityLevel(extractActivityLevel(memberActivityCombo.getValue()));
        member.setPrimaryHealthGoal(memberHealthGoalCombo.getValue());
        member.setAllergies(new ArrayList<>(currentAllergies));
        member.setMedicalConditions(new ArrayList<>(currentMedicalConditions));
        member.setMealPreferences(new HashMap<>()); // Empty for now

        return member;
    }

    /**
     * Validate the member form
     */
    private boolean validateMemberForm() {
        if (memberNameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter a name for the family member.");
            return false;
        }

        try {
            int age = Integer.parseInt(memberAgeField.getText());
            if (age < 0 || age > 120) {
                showAlert("Validation Error", "Please enter a valid age (0-120).");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a valid age.");
            return false;
        }

        try {
            double height = Double.parseDouble(memberHeightField.getText());
            if (height < 30 || height > 250) {
                showAlert("Validation Error", "Please enter a valid height (30-250 cm).");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a valid height.");
            return false;
        }

        try {
            double weight = Double.parseDouble(memberWeightField.getText());
            if (weight < 5 || weight > 500) {
                showAlert("Validation Error", "Please enter a valid weight (5-500 kg).");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a valid weight.");
            return false;
        }

        return true;
    }

    /**
     * Add an allergy tag to the flow pane
     */
    private void addAllergyTag(String allergy) {
        Button tag = createRemovableTag(allergy, () -> {
            currentAllergies.remove(allergy);
            refreshAllergyTags();
        });
        tag.getStyleClass().add("allergy-tag");
        allergiesFlowPane.getChildren().add(tag);
    }

    /**
     * Add a medical condition tag to the flow pane
     */
    private void addMedicalConditionTag(MedicalCondition condition) {
        currentMedicalConditions.add(condition);
        Button tag = createRemovableTag(condition.toString(), () -> {
            currentMedicalConditions.remove(condition);
            refreshMedicalConditionTags();
        });
        tag.getStyleClass().add("medical-condition-tag");
        medicalConditionsFlowPane.getChildren().add(tag);
    }

    /**
     * Create a removable tag button
     */
    private Button createRemovableTag(String text, Runnable onRemove) {
        Button tag = new Button(text + " ✕");
        tag.getStyleClass().add("tag-button");
        tag.setOnAction(e -> onRemove.run());
        return tag;
    }

    /**
     * Refresh allergy tags
     */
    private void refreshAllergyTags() {
        allergiesFlowPane.getChildren().clear();
        for (String allergy : currentAllergies) {
            addAllergyTag(allergy);
        }
    }

    /**
     * Refresh medical condition tags
     */
    private void refreshMedicalConditionTags() {
        medicalConditionsFlowPane.getChildren().clear();
        for (MedicalCondition condition : currentMedicalConditions) {
            addMedicalConditionTag(condition);
        }
    }

    /**
     * Refresh the family members list view
     */
    private void refreshFamilyMembersList() {
        familyMembersListView.getItems().setAll(familyMembers);
        generateFamilyPlanButton.setDisable(familyMembers.isEmpty());
    }

    /**
     * Extract activity level from combo box value
     */
    private int extractActivityLevel(String value) {
        if (value != null && !value.isEmpty()) {
            return Integer.parseInt(value.substring(0, 1));
        }
        return 3; // Default moderate activity
    }

    /**
     * Get activity description for a given level
     */
    private String getActivityDescription(int level) {
        switch (level) {
            case 1:
                return "Sedentary (little/no exercise)";
            case 2:
                return "Light (light exercise 1-3 days/week)";
            case 3:
                return "Moderate (moderate exercise 3-5 days/week)";
            case 4:
                return "Active (hard exercise 6-7 days/week)";
            case 5:
                return "Very Active (very hard exercise, physical job)";
            default:
                return "Moderate (moderate exercise 3-5 days/week)";
        }
    }

    /**
     * Show an alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Setters for dependency injection
    public void setMealPlannerService(MealPlannerService mealPlannerService) {
        this.mealPlannerService = mealPlannerService;
    }

    public void setCallback(FamilyPlanGeneratedCallback callback) {
        this.callback = callback;
    }
}
