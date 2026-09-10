package com.mealplanner.ui;

import com.mealplanner.model.*;
import com.mealplanner.model.recipe.Recipe;
import com.mealplanner.model.user.NutritionProfile;
import com.mealplanner.model.user.UserProfile;
import com.mealplanner.service.MealPlannerService;
import com.mealplanner.service.NutritionCalculatorService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Controller for individual family member tab
 */
public class FamilyMemberTabController {

    // Member Profile Components
    @FXML
    private Label memberNameLabel;
    @FXML
    private Label ageLabel;
    @FXML
    private Label genderLabel;
    @FXML
    private Label heightLabel;
    @FXML
    private Label weightLabel;
    @FXML
    private Label activityLabel;
    @FXML
    private Label healthGoalLabel;

    // Nutrition Profile Components
    @FXML
    private Label bmiLabel;
    @FXML
    private Label bmrLabel;
    @FXML
    private Label tdeeLabel;
    @FXML
    private Label targetCaloriesLabel;
    @FXML
    private Label targetProteinLabel;
    @FXML
    private Label targetCarbsLabel;
    @FXML
    private Label targetFatLabel;

    // Dietary Information Components
    @FXML
    private FlowPane allergiesFlowPane;
    @FXML
    private FlowPane medicalConditionsFlowPane;

    // Meal Plan Components
    @FXML
    private Button generatePlanButton;
    @FXML
    private VBox emptyStateVBox;
    @FXML
    private TabPane mealPlanTabPane;

    // Data
    private UserProfile userProfile;
    private NutritionProfile nutritionProfile;
    private WeeklyMealPlan weeklyMealPlan;

    // Services
    private MealPlannerService mealPlannerService;
    private NutritionCalculatorService nutritionCalculatorService;

    // Formatting
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    /**
     * Initialize the controller
     */
    public void initialize() {
        // Initialize components
    }

    /**
     * Set the user profile and update all displays
     */
    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        if (userProfile != null) {
            updateProfileDisplay();
            calculateNutrition();
        }
    }

    /**
     * Set the services
     */
    public void setServices(MealPlannerService mealPlannerService,
            NutritionCalculatorService nutritionCalculatorService) {
        this.mealPlannerService = mealPlannerService;
        this.nutritionCalculatorService = nutritionCalculatorService;
    }

    /**
     * Update the profile display
     */
    private void updateProfileDisplay() {
        if (userProfile == null)
            return;

        memberNameLabel.setText(userProfile.getName());
        ageLabel.setText("Age: " + userProfile.getAge() + " years");
        genderLabel.setText("Gender: " + userProfile.getGender());
        heightLabel.setText("Height: " + decimalFormat.format(userProfile.getHeightCm()) + " cm");
        weightLabel.setText("Weight: " + decimalFormat.format(userProfile.getWeightKg()) + " kg");
        activityLabel.setText("Activity Level: " + userProfile.getActivityLevel() + "/5");
        healthGoalLabel.setText("Health Goal: " + userProfile.getPrimaryHealthGoal().toString().replace("_", " "));

        // Update allergies
        allergiesFlowPane.getChildren().clear();
        if (userProfile.getAllergies() != null) {
            for (String allergy : userProfile.getAllergies()) {
                Label allergyTag = new Label(allergy);
                allergyTag.getStyleClass().add("allergy-tag");
                allergiesFlowPane.getChildren().add(allergyTag);
            }
        }

        // Update medical conditions
        medicalConditionsFlowPane.getChildren().clear();
        if (userProfile.getMedicalConditions() != null) {
            for (MedicalCondition condition : userProfile.getMedicalConditions()) {
                Label conditionTag = new Label(condition.toString().replace("_", " "));
                conditionTag.getStyleClass().add("medical-condition-tag");
                medicalConditionsFlowPane.getChildren().add(conditionTag);
            }
        }
    }

    /**
     * Calculate and update nutrition information
     */
    private void calculateNutrition() {
        if (userProfile == null || nutritionCalculatorService == null)
            return;

        try {
            nutritionProfile = nutritionCalculatorService.calculateNutritionalNeeds(userProfile);
            updateNutritionDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update nutrition display with calculated values
     */
    private void updateNutritionDisplay() {
        if (nutritionProfile == null)
            return;

        try {
            // Update BMI
            bmiLabel.setText("BMI: " + decimalFormat.format(nutritionProfile.getBmi()));

            // Update BMR
            bmrLabel.setText("BMR: " + decimalFormat.format(nutritionProfile.getBmr()) + " cal");

            // Update TDEE
            tdeeLabel.setText("TDEE: " + decimalFormat.format(nutritionProfile.getTdee()) + " cal");

            // Update target calories
            targetCaloriesLabel
                    .setText("Target: " + decimalFormat.format(nutritionProfile.getTargetCalories()) + " cal");

            // Update macronutrients
            targetProteinLabel.setText("Protein: " + decimalFormat.format(nutritionProfile.getProteinGrams()) + " g");
            targetCarbsLabel.setText("Carbs: " + decimalFormat.format(nutritionProfile.getCarbsGrams()) + " g");
            targetFatLabel.setText("Fat: " + decimalFormat.format(nutritionProfile.getFatGrams()) + " g");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle generate plan button click
     */
    @FXML
    public void handleGeneratePlan() {
        if (userProfile == null || mealPlannerService == null || nutritionProfile == null) {
            showAlert("Error", "User profile or services not available.");
            return;
        }

        try {
            // Generate weekly meal plan for this member
            weeklyMealPlan = mealPlannerService.generateNewWeeklyPlan(userProfile, nutritionProfile);

            if (weeklyMealPlan != null) {
                displayWeeklyPlan();
            } else {
                showAlert("Error", "Failed to generate meal plan. Please try again.");
            }
        } catch (Exception e) {
            showAlert("Error", "An error occurred while generating the meal plan: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Display the weekly meal plan
     */
    private void displayWeeklyPlan() {
        if (weeklyMealPlan == null)
            return;

        // Hide empty state and show meal plan
        emptyStateVBox.setVisible(false);
        emptyStateVBox.setManaged(false);
        mealPlanTabPane.setVisible(true);
        mealPlanTabPane.setManaged(true);

        // Clear existing tabs
        mealPlanTabPane.getTabs().clear();

        // Create tabs for each day
        List<DailyMealPlan> dailyPlans = weeklyMealPlan.getDailyPlans();
        for (int i = 0; i < dailyPlans.size(); i++) {
            DailyMealPlan dailyPlan = dailyPlans.get(i);
            String dayName = getDayName(i);

            Tab dayTab = new Tab(dayName);
            dayTab.setClosable(false);

            // Create TabPane for meals within this day
            TabPane mealTabPane = new TabPane();
            mealTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

            // Create tabs for each meal
            createMealTab(mealTabPane, "Breakfast", dailyPlan.getBreakfast());
            createMealTab(mealTabPane, "Lunch", dailyPlan.getLunch());
            createMealTab(mealTabPane, "Dinner", dailyPlan.getDinner());
            createMealTab(mealTabPane, "Snacks", dailyPlan.getSnacks());

            dayTab.setContent(mealTabPane);
            mealPlanTabPane.getTabs().add(dayTab);
        }

        // Select the first tab
        if (!mealPlanTabPane.getTabs().isEmpty()) {
            mealPlanTabPane.getSelectionModel().select(0);
        }
    }

    /**
     * Create a meal tab
     */
    private void createMealTab(TabPane mealTabPane, String mealName, Meal meal) {
        Tab mealTab = new Tab(mealName);
        mealTab.setClosable(false);

        // Create ScrollPane for meal content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Create VBox for meal content
        VBox mealContent = new VBox(15);

        if (meal == null || meal.getRecipes() == null || meal.getRecipes().isEmpty()) {
            Label noMealLabel = new Label("No " + mealName.toLowerCase() + " planned");
            noMealLabel.getStyleClass().add("no-meal-label");
            mealContent.getChildren().add(noMealLabel);
        } else {
            // Add recipes for this meal
            for (Recipe recipe : meal.getRecipes()) {
                if (recipe == null)
                    continue;

                try {
                    // Load recipe card
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mealplanner/ui/RecipeCard.fxml"));
                    Node recipeCard = loader.load();

                    // Get controller and set data
                    RecipeCardController controller = loader.getController();
                    controller.setData(recipe);

                    mealContent.getChildren().add(recipeCard);

                } catch (IOException e) {
                    // Fallback to simple text display
                    Label recipeLabel = new Label(
                            "• " + (recipe.getNameEn() != null ? recipe.getNameEn() : "Unknown Recipe"));
                    mealContent.getChildren().add(recipeLabel);
                    e.printStackTrace();
                }
            }
        }

        scrollPane.setContent(mealContent);
        mealTab.setContent(scrollPane);
        mealTabPane.getTabs().add(mealTab);
    }

    /**
     * Get day name for index
     */
    private String getDayName(int index) {
        String[] dayNames = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
        return index < dayNames.length ? dayNames[index] : "Day " + (index + 1);
    }

    /**
     * Show alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Getters for external access
    public UserProfile getUserProfile() {
        return userProfile;
    }

    public NutritionProfile getNutritionProfile() {
        return nutritionProfile;
    }

    public WeeklyMealPlan getWeeklyMealPlan() {
        return weeklyMealPlan;
    }

    public void setWeeklyMealPlan(WeeklyMealPlan weeklyMealPlan) {
        setWeeklyMealPlan(weeklyMealPlan, false);
    }

    /**
     * Set weekly meal plan with option to indicate if it's personalized
     */
    public void setWeeklyMealPlan(WeeklyMealPlan weeklyMealPlan, boolean isPersonalized) {
        this.weeklyMealPlan = weeklyMealPlan;
        if (weeklyMealPlan != null) {
            displayWeeklyPlan();

            // Add visual indicator for personalized plans
            if (isPersonalized && memberNameLabel != null) {
                String currentText = memberNameLabel.getText();
                if (!currentText.contains("🎯")) {
                    memberNameLabel.setText("🎯 " + currentText + " (Personalized Plan)");
                    memberNameLabel.setStyle("-fx-text-fill: #2E8B57; -fx-font-weight: bold;");
                }
            }
        }
    }
}
