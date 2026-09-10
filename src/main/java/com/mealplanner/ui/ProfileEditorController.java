package com.mealplanner.ui;

import com.mealplanner.model.Gender;
import com.mealplanner.model.HealthGoal;
import com.mealplanner.model.MealType;
import com.mealplanner.model.MedicalCondition;
import com.mealplanner.model.user.UserProfile;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import java.net.URL;
import java.util.*;

/**
 * Controller for the Profile Editor view
 * Handles user input for creating and editing user profiles
 */
public class ProfileEditorController implements Initializable {

    // Demographics fields
    @FXML
    private TextField nameField;
    @FXML
    private TextField ageField;
    @FXML
    private TextField heightField;
    @FXML
    private TextField weightField;
    @FXML
    private ComboBox<Gender> genderComboBox;

    // Health goal fields
    @FXML
    private ComboBox<HealthGoal> healthGoalComboBox;
    @FXML
    private Slider activitySlider;
    @FXML
    private Label activityLevelLabel;

    // Allergies checkboxes
    @FXML
    private CheckBox nutsAllergy;
    @FXML
    private CheckBox dairyAllergy;
    @FXML
    private CheckBox shellfishAllergy;
    @FXML
    private CheckBox fishAllergy;
    @FXML
    private CheckBox eggsAllergy;
    @FXML
    private CheckBox wheatAllergy;
    @FXML
    private CheckBox soybeanAllergy;
    @FXML
    private CheckBox spicyAllergy;

    // Medical conditions checkboxes
    @FXML
    private CheckBox diabetesCondition;
    @FXML
    private CheckBox hypertensionCondition;
    @FXML
    private CheckBox pregnancyCondition;
    @FXML
    private CheckBox elderlyCondition;
    @FXML
    private CheckBox childCondition;
    @FXML
    private CheckBox highCholesterolCondition;
    @FXML
    private CheckBox anemiaCondition;
    @FXML
    private CheckBox noneCondition;

    // Breakfast preferences
    @FXML
    private CheckBox rutiVegetablesBreakfast;
    @FXML
    private CheckBox riceVegetablesBreakfast;
    @FXML
    private CheckBox fruitMilkYogurtBreakfast;
    @FXML
    private CheckBox teaCoffeeStreetFoodBreakfast;
    @FXML
    private CheckBox miscellaneousBreakfast;

    // Lunch preferences
    @FXML
    private CheckBox riceFishLunch;
    @FXML
    private CheckBox riceFishVegetablesLunch;
    @FXML
    private CheckBox riceMeatVegetablesLunch;
    @FXML
    private CheckBox riceVegetablesBhortaLunch;
    @FXML
    private CheckBox riceLentilBhortaLunch;
    @FXML
    private CheckBox internationalDishesLunch;

    // Dinner preferences
    @FXML
    private CheckBox riceFishDinner;
    @FXML
    private CheckBox riceFishVegetablesDinner;
    @FXML
    private CheckBox riceMeatVegetablesDinner;
    @FXML
    private CheckBox riceVegetablesBhortaDinner;
    @FXML
    private CheckBox riceLentilBhortaDinner;
    @FXML
    private CheckBox internationalDishesDinner;

    // Snacks preferences
    @FXML
    private CheckBox savouryFriedSnacks;
    @FXML
    private CheckBox steamedBoiledSnacks;
    @FXML
    private CheckBox juiceSmoothieSnacks;
    @FXML
    private CheckBox sweetSnacksPitha;
    @FXML
    private CheckBox soupSnacks;
    @FXML
    private CheckBox nutsSeedsSnacks;
    @FXML
    private CheckBox bakerySnacks;
    @FXML
    private CheckBox miscellaneousSnacks;

    // Action buttons
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    // Result to return to calling code
    private UserProfile resultProfile;
    private boolean saved = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeComboBoxes();
        setupNumericFields();
        setupActivitySlider();
        setupMedicalConditionLogic();
    }

    /**
     * Initialize combo boxes with enum values
     */
    private void initializeComboBoxes() {
        genderComboBox.setItems(FXCollections.observableArrayList(Gender.values()));
        healthGoalComboBox.setItems(FXCollections.observableArrayList(HealthGoal.values()));
    }

    /**
     * Setup numeric text fields with formatters
     */
    private void setupNumericFields() {
        // Age field - integers only
        ageField.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));

        // Height field - decimal numbers
        heightField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*\\.?\\d*")) {
                return change;
            }
            return null;
        }));

        // Weight field - decimal numbers
        weightField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*\\.?\\d*")) {
                return change;
            }
            return null;
        }));
    }

    /**
     * Setup activity slider with label updates
     */
    private void setupActivitySlider() {
        activitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int level = newValue.intValue();
            String levelText = switch (level) {
                case 1 -> "Very Low";
                case 2 -> "Low";
                case 3 -> "Moderate";
                case 4 -> "High";
                case 5 -> "Very High";
                default -> "Moderate";
            };
            activityLevelLabel.setText("Activity Level: " + level + " (" + levelText + ")");
        });
    }

    /**
     * Setup medical condition logic (None checkbox disables others)
     */
    private void setupMedicalConditionLogic() {
        noneCondition.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // If "None" is selected, uncheck all other conditions
                diabetesCondition.setSelected(false);
                hypertensionCondition.setSelected(false);
                pregnancyCondition.setSelected(false);
                elderlyCondition.setSelected(false);
                childCondition.setSelected(false);
                highCholesterolCondition.setSelected(false);
                anemiaCondition.setSelected(false);
            }
        });

        // If any other condition is selected, uncheck "None"
        CheckBox[] otherConditions = { diabetesCondition, hypertensionCondition, pregnancyCondition,
                elderlyCondition, childCondition, highCholesterolCondition, anemiaCondition };

        for (CheckBox condition : otherConditions) {
            condition.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    noneCondition.setSelected(false);
                }
            });
        }
    }

    /**
     * Set profile data for editing
     * 
     * @param profile the UserProfile to edit
     */
    public void setProfileData(UserProfile profile) {
        if (profile == null)
            return;

        // Set demographics
        nameField.setText(profile.getName());
        ageField.setText(String.valueOf(profile.getAge()));
        heightField.setText(String.valueOf(profile.getHeightCm()));
        weightField.setText(String.valueOf(profile.getWeightKg()));
        genderComboBox.setValue(profile.getGender());

        // Set health goal and activity
        healthGoalComboBox.setValue(profile.getPrimaryHealthGoal());
        activitySlider.setValue(profile.getActivityLevel());

        // Set allergies
        setAllergiesFromProfile(profile.getAllergies());

        // Set medical conditions
        setMedicalConditionsFromProfile(profile.getMedicalConditions());

        // Set meal preferences
        setMealPreferencesFromProfile(profile.getMealPreferences());
    }

    /**
     * Set allergies checkboxes from profile
     */
    private void setAllergiesFromProfile(List<String> allergies) {
        if (allergies == null)
            return;

        nutsAllergy.setSelected(allergies.contains("nuts"));
        dairyAllergy.setSelected(allergies.contains("dairy"));
        shellfishAllergy.setSelected(allergies.contains("shellfish"));
        fishAllergy.setSelected(allergies.contains("fish"));
        eggsAllergy.setSelected(allergies.contains("eggs"));
        wheatAllergy.setSelected(allergies.contains("wheat"));
        soybeanAllergy.setSelected(allergies.contains("soybean"));
        spicyAllergy.setSelected(allergies.contains("spicy"));
    }

    /**
     * Set medical conditions checkboxes from profile
     */
    private void setMedicalConditionsFromProfile(List<MedicalCondition> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            noneCondition.setSelected(true);
            return;
        }

        diabetesCondition.setSelected(conditions.contains(MedicalCondition.DIABETES));
        hypertensionCondition.setSelected(conditions.contains(MedicalCondition.HYPERTENSION));
        pregnancyCondition.setSelected(conditions.contains(MedicalCondition.PREGNANCY));
        elderlyCondition.setSelected(conditions.contains(MedicalCondition.ELDERLY));
        childCondition.setSelected(conditions.contains(MedicalCondition.CHILD));
        highCholesterolCondition.setSelected(conditions.contains(MedicalCondition.HIGH_CHOLESTEROL));
        anemiaCondition.setSelected(conditions.contains(MedicalCondition.ANEMIA));
        noneCondition.setSelected(conditions.contains(MedicalCondition.NONE));
    }

    /**
     * Set meal preferences checkboxes from profile
     */
    private void setMealPreferencesFromProfile(Map<MealType, List<String>> preferences) {
        if (preferences == null)
            return;

        // Breakfast preferences
        List<String> breakfastPrefs = preferences.get(MealType.BREAKFAST);
        if (breakfastPrefs != null) {
            rutiVegetablesBreakfast.setSelected(breakfastPrefs.contains("RUTI_VEGETABLES"));
            riceVegetablesBreakfast.setSelected(breakfastPrefs.contains("RICE_VEGETABLES"));
            fruitMilkYogurtBreakfast.setSelected(breakfastPrefs.contains("FRUIT_MILK_YOGURT"));
            teaCoffeeStreetFoodBreakfast.setSelected(breakfastPrefs.contains("TEA_COFFEE_STREET_FOOD"));
            miscellaneousBreakfast.setSelected(breakfastPrefs.contains("MISCELLANEOUS"));
        }

        // Lunch preferences
        List<String> lunchPrefs = preferences.get(MealType.LUNCH);
        if (lunchPrefs != null) {
            riceFishLunch.setSelected(lunchPrefs.contains("RICE_FISH"));
            riceFishVegetablesLunch.setSelected(lunchPrefs.contains("RICE_FISH_VEGETABLES"));
            riceMeatVegetablesLunch.setSelected(lunchPrefs.contains("RICE_MEAT_VEGETABLES"));
            riceVegetablesBhortaLunch.setSelected(lunchPrefs.contains("RICE_VEGETABLES_BHORTA"));
            riceLentilBhortaLunch.setSelected(lunchPrefs.contains("RICE_LENTIL_BHORTA"));
            internationalDishesLunch.setSelected(lunchPrefs.contains("INTERNATIONAL_DISHES"));
        }

        // Dinner preferences
        List<String> dinnerPrefs = preferences.get(MealType.DINNER);
        if (dinnerPrefs != null) {
            riceFishDinner.setSelected(dinnerPrefs.contains("RICE_FISH"));
            riceFishVegetablesDinner.setSelected(dinnerPrefs.contains("RICE_FISH_VEGETABLES"));
            riceMeatVegetablesDinner.setSelected(dinnerPrefs.contains("RICE_MEAT_VEGETABLES"));
            riceVegetablesBhortaDinner.setSelected(dinnerPrefs.contains("RICE_VEGETABLES_BHORTA"));
            riceLentilBhortaDinner.setSelected(dinnerPrefs.contains("RICE_LENTIL_BHORTA"));
            internationalDishesDinner.setSelected(dinnerPrefs.contains("INTERNATIONAL_DISHES"));
        }

        // Snacks preferences
        List<String> snacksPrefs = preferences.get(MealType.SNACKS);
        if (snacksPrefs != null) {
            savouryFriedSnacks.setSelected(snacksPrefs.contains("Savoury Fried"));
            steamedBoiledSnacks.setSelected(snacksPrefs.contains("Steamed/Boiled"));
            juiceSmoothieSnacks.setSelected(snacksPrefs.contains("Juice/Smoothie"));
            sweetSnacksPitha.setSelected(snacksPrefs.contains("Sweet Snacks/Pitha"));
            soupSnacks.setSelected(snacksPrefs.contains("Soup"));
            nutsSeedsSnacks.setSelected(snacksPrefs.contains("Nuts/Seeds"));
            bakerySnacks.setSelected(snacksPrefs.contains("Bakery"));
            miscellaneousSnacks.setSelected(snacksPrefs.contains("Miscellaneous"));
        }
    }

    /**
     * Get profile data from form
     * 
     * @return UserProfile constructed from form data
     */
    public UserProfile getProfileFromFormData() {
        try {
            // Basic validation
            if (nameField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Name is required.");
                return null;
            }

            if (ageField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Age is required.");
                return null;
            }

            if (heightField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Height is required.");
                return null;
            }

            if (weightField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Weight is required.");
                return null;
            }

            if (genderComboBox.getValue() == null) {
                showAlert("Validation Error", "Gender is required.");
                return null;
            }

            if (healthGoalComboBox.getValue() == null) {
                showAlert("Validation Error", "Health goal is required.");
                return null;
            }

            // Parse numeric values
            int age = Integer.parseInt(ageField.getText().trim());
            double height = Double.parseDouble(heightField.getText().trim());
            double weight = Double.parseDouble(weightField.getText().trim());

            // Validate ranges
            if (age < 1 || age > 120) {
                showAlert("Validation Error", "Age must be between 1 and 120.");
                return null;
            }

            if (height < 50 || height > 250) {
                showAlert("Validation Error", "Height must be between 50 and 250 cm.");
                return null;
            }

            if (weight < 20 || weight > 300) {
                showAlert("Validation Error", "Weight must be between 20 and 300 kg.");
                return null;
            }

            // Get selected values
            String name = nameField.getText().trim();
            Gender gender = genderComboBox.getValue();
            HealthGoal healthGoal = healthGoalComboBox.getValue();
            int activityLevel = (int) activitySlider.getValue();

            // Get allergies
            List<String> allergies = getSelectedAllergies();

            // Get medical conditions
            List<MedicalCondition> medicalConditions = getSelectedMedicalConditions();

            // Get meal preferences
            Map<MealType, List<String>> mealPreferences = getSelectedMealPreferences();

            // Generate unique user ID
            String userId = "user_" + System.currentTimeMillis();

            // Create and return UserProfile
            return new UserProfile(userId, name, age, height, weight, gender, healthGoal,
                    allergies, mealPreferences, medicalConditions, activityLevel);

        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter valid numeric values for age, height, and weight.");
            return null;
        } catch (Exception e) {
            showAlert("Error", "An error occurred while processing the profile: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get selected allergies
     */
    private List<String> getSelectedAllergies() {
        List<String> allergies = new ArrayList<>();

        if (nutsAllergy.isSelected())
            allergies.add("nuts");
        if (dairyAllergy.isSelected())
            allergies.add("dairy");
        if (shellfishAllergy.isSelected())
            allergies.add("shellfish");
        if (fishAllergy.isSelected())
            allergies.add("fish");
        if (eggsAllergy.isSelected())
            allergies.add("eggs");
        if (wheatAllergy.isSelected())
            allergies.add("wheat");
        if (soybeanAllergy.isSelected())
            allergies.add("soybean");
        if (spicyAllergy.isSelected())
            allergies.add("spicy");

        return allergies;
    }

    /**
     * Get selected medical conditions
     */
    private List<MedicalCondition> getSelectedMedicalConditions() {
        List<MedicalCondition> conditions = new ArrayList<>();

        if (diabetesCondition.isSelected())
            conditions.add(MedicalCondition.DIABETES);
        if (hypertensionCondition.isSelected())
            conditions.add(MedicalCondition.HYPERTENSION);
        if (pregnancyCondition.isSelected())
            conditions.add(MedicalCondition.PREGNANCY);
        if (elderlyCondition.isSelected())
            conditions.add(MedicalCondition.ELDERLY);
        if (childCondition.isSelected())
            conditions.add(MedicalCondition.CHILD);
        if (highCholesterolCondition.isSelected())
            conditions.add(MedicalCondition.HIGH_CHOLESTEROL);
        if (anemiaCondition.isSelected())
            conditions.add(MedicalCondition.ANEMIA);
        if (noneCondition.isSelected())
            conditions.add(MedicalCondition.NONE);

        // If no conditions selected, default to NONE
        if (conditions.isEmpty()) {
            conditions.add(MedicalCondition.NONE);
        }

        return conditions;
    }

    /**
     * Get selected meal preferences
     */
    private Map<MealType, List<String>> getSelectedMealPreferences() {
        Map<MealType, List<String>> preferences = new HashMap<>();

        // Breakfast preferences
        List<String> breakfastPrefs = new ArrayList<>();
        if (rutiVegetablesBreakfast.isSelected())
            breakfastPrefs.add("RUTI_VEGETABLES");
        if (riceVegetablesBreakfast.isSelected())
            breakfastPrefs.add("RICE_VEGETABLES");
        if (fruitMilkYogurtBreakfast.isSelected())
            breakfastPrefs.add("FRUIT_MILK_YOGURT");
        if (teaCoffeeStreetFoodBreakfast.isSelected())
            breakfastPrefs.add("TEA_COFFEE_STREET_FOOD");
        if (miscellaneousBreakfast.isSelected())
            breakfastPrefs.add("MISCELLANEOUS");
        preferences.put(MealType.BREAKFAST, breakfastPrefs);

        // Lunch preferences
        List<String> lunchPrefs = new ArrayList<>();
        if (riceFishLunch.isSelected())
            lunchPrefs.add("RICE_FISH");
        if (riceFishVegetablesLunch.isSelected())
            lunchPrefs.add("RICE_FISH_VEGETABLES");
        if (riceMeatVegetablesLunch.isSelected())
            lunchPrefs.add("RICE_MEAT_VEGETABLES");
        if (riceVegetablesBhortaLunch.isSelected())
            lunchPrefs.add("RICE_VEGETABLES_BHORTA");
        if (riceLentilBhortaLunch.isSelected())
            lunchPrefs.add("RICE_LENTIL_BHORTA");
        if (internationalDishesLunch.isSelected())
            lunchPrefs.add("INTERNATIONAL_DISHES");
        preferences.put(MealType.LUNCH, lunchPrefs);

        // Dinner preferences
        List<String> dinnerPrefs = new ArrayList<>();
        if (riceFishDinner.isSelected())
            dinnerPrefs.add("RICE_FISH");
        if (riceFishVegetablesDinner.isSelected())
            dinnerPrefs.add("RICE_FISH_VEGETABLES");
        if (riceMeatVegetablesDinner.isSelected())
            dinnerPrefs.add("RICE_MEAT_VEGETABLES");
        if (riceVegetablesBhortaDinner.isSelected())
            dinnerPrefs.add("RICE_VEGETABLES_BHORTA");
        if (riceLentilBhortaDinner.isSelected())
            dinnerPrefs.add("RICE_LENTIL_BHORTA");
        if (internationalDishesDinner.isSelected())
            dinnerPrefs.add("INTERNATIONAL_DISHES");
        preferences.put(MealType.DINNER, dinnerPrefs);

        // Snacks preferences
        List<String> snacksPrefs = new ArrayList<>();
        if (savouryFriedSnacks.isSelected())
            snacksPrefs.add("Savoury Fried");
        if (steamedBoiledSnacks.isSelected())
            snacksPrefs.add("Steamed/Boiled");
        if (juiceSmoothieSnacks.isSelected())
            snacksPrefs.add("Juice/Smoothie");
        if (sweetSnacksPitha.isSelected())
            snacksPrefs.add("Sweet Snacks/Pitha");
        if (soupSnacks.isSelected())
            snacksPrefs.add("Soup");
        if (nutsSeedsSnacks.isSelected())
            snacksPrefs.add("Nuts/Seeds");
        if (bakerySnacks.isSelected())
            snacksPrefs.add("Bakery");
        if (miscellaneousSnacks.isSelected())
            snacksPrefs.add("Miscellaneous");
        preferences.put(MealType.SNACKS, snacksPrefs);

        return preferences;
    }

    /**
     * Handle save button action
     */
    @FXML
    private void handleSaveButtonAction() {
        UserProfile profile = getProfileFromFormData();
        if (profile != null) {
            this.resultProfile = profile;
            this.saved = true;
            closeDialog();
        }
    }

    /**
     * Handle cancel button action
     */
    @FXML
    private void handleCancelButtonAction() {
        this.saved = false;
        closeDialog();
    }

    /**
     * Close the dialog
     */
    private void closeDialog() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Show alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Get the result profile (after save)
     */
    public UserProfile getResultProfile() {
        return resultProfile;
    }

    /**
     * Check if profile was saved
     */
    public boolean isSaved() {
        return saved;
    }
}
