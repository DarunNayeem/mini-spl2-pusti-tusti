package com.mealplanner.ui;

import com.mealplanner.model.recipe.Ingredient;
import com.mealplanner.model.recipe.NutritionInfo;
import com.mealplanner.model.recipe.Recipe;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.text.DecimalFormat;
import java.util.Map;

/**
 * Controller for the RecipeCard FXML component
 * Handles the display of individual recipe information
 */
public class RecipeCardController {

    // Recipe Name Labels
    @FXML
    private Label recipeNameEnLabel;
    @FXML
    private Label recipeNameBnLabel;

    // Recipe Details Labels
    @FXML
    private Label servingsLabel;
    @FXML
    private Label prepTimeLabel;
    @FXML
    private Label cookTimeLabel;

    // Tags Section
    @FXML
    private FlowPane tagsFlowPane;

    // Nutrition Sections
    @FXML
    private GridPane basicNutritionGrid;
    @FXML
    private Hyperlink viewAllNutrientsLink;
    @FXML
    private VBox allNutrientsSection;
    @FXML
    private GridPane allNutrientsGrid;

    // Ingredients and Instructions
    @FXML
    private VBox ingredientsVBox;
    @FXML
    private VBox instructionsVBox;

    // Family Notes Section
    @FXML
    private TitledPane familyNotesSection;
    @FXML
    private VBox familyNotesVBox;

    // Data
    private Recipe recipe;
    private boolean showingAllNutrients = false;

    // Formatting
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    /**
     * Initialize the controller
     */
    public void initialize() {
        // Set up styles for nutrition grids
        basicNutritionGrid.getStyleClass().add("nutrition-grid");
        allNutrientsGrid.getStyleClass().add("nutrition-grid");

        // Set up ingredients container
        ingredientsVBox.getStyleClass().add("ingredients-container");

        // Set up instructions container
        instructionsVBox.getStyleClass().add("instructions-container");
    }

    /**
     * Set the recipe data and populate all UI components
     * 
     * @param recipe The recipe to display
     */
    public void setData(Recipe recipe) {
        this.recipe = recipe;
        populateRecipeInfo();
        populateTags();
        populateNutritionInfo();
        populateIngredients();
        populateInstructions();
        hideFamilyNotes(); // Hide family notes by default
    }

    /**
     * Set the recipe data with family notes and populate all UI components
     * 
     * @param recipe      The recipe to display
     * @param familyNotes List of family-specific notes for this recipe
     */
    public void setData(Recipe recipe, java.util.List<String> familyNotes) {
        this.recipe = recipe;
        populateRecipeInfo();
        populateTags();
        populateNutritionInfo();
        populateIngredients();
        populateInstructions();
        populateFamilyNotes(familyNotes);
    }

    /**
     * Populate basic recipe information
     */
    private void populateRecipeInfo() {
        if (recipe == null)
            return;

        // Recipe names
        recipeNameEnLabel.setText(recipe.getNameEn() != null ? recipe.getNameEn() : "Unknown Recipe");
        recipeNameBnLabel.setText(recipe.getNameBn() != null ? recipe.getNameBn() : "");

        // Recipe details
        servingsLabel.setText(String.valueOf(recipe.getServings()));

        // Prep time (convert hours to minutes if needed)
        int prepTimeMinutes = recipe.getPreparationTimeHours() * 60;
        prepTimeLabel.setText(formatTime(prepTimeMinutes));

        // Cook time
        cookTimeLabel.setText(formatTime(recipe.getCookingTimeMinutes()));
    }

    /**
     * Format time in minutes to readable format
     */
    private String formatTime(int minutes) {
        if (minutes == 0)
            return "Not specified";
        if (minutes < 60) {
            return minutes + " min";
        } else {
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            if (remainingMinutes == 0) {
                return hours + " hr";
            } else {
                return hours + " hr " + remainingMinutes + " min";
            }
        }
    }

    /**
     * Populate tags section
     */
    private void populateTags() {
        tagsFlowPane.getChildren().clear();

        if (recipe == null || recipe.getTags() == null)
            return;

        for (String tag : recipe.getTags()) {
            Label tagLabel = new Label(tag.replace("_", " "));
            tagLabel.getStyleClass().add("recipe-tag");
            tagsFlowPane.getChildren().add(tagLabel);
        }
    }

    /**
     * Populate nutrition information
     */
    private void populateNutritionInfo() {
        if (recipe == null || recipe.getNutritionPerServing() == null) {
            basicNutritionGrid.getChildren().clear();
            basicNutritionGrid.add(new Label("No nutrition information available"), 0, 0, 2, 1);
            viewAllNutrientsLink.setVisible(false);
            return;
        }

        NutritionInfo nutrition = recipe.getNutritionPerServing();

        // Clear existing content
        basicNutritionGrid.getChildren().clear();

        // Basic nutrition info (6 default nutrients)
        addNutritionRow(basicNutritionGrid, 0, "Energy:", decimalFormat.format(nutrition.getEnergyKcal()) + " kcal");
        addNutritionRow(basicNutritionGrid, 1, "Protein:", decimalFormat.format(nutrition.getProteinG()) + " g");
        addNutritionRow(basicNutritionGrid, 2, "Fat:", decimalFormat.format(nutrition.getFatG()) + " g");
        addNutritionRow(basicNutritionGrid, 3, "Carbohydrates:",
                decimalFormat.format(nutrition.getCarbohydrateAvailableG()) + " g");
        addNutritionRow(basicNutritionGrid, 4, "Fiber:",
                decimalFormat.format(nutrition.getTotalDietaryFibreG()) + " g");
        addNutritionRow(basicNutritionGrid, 5, "Water:", decimalFormat.format(nutrition.getWaterG()) + " g");

        // Show/hide "View All Nutrients" link based on availability
        viewAllNutrientsLink.setVisible(nutrition.getAllNutrients() != null && !nutrition.getAllNutrients().isEmpty());
    }

    /**
     * Add a nutrition row to the grid
     */
    private void addNutritionRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("nutrition-label");

        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("nutrition-value");

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    /**
     * Populate ingredients list
     */
    private void populateIngredients() {
        // Clear existing ingredients
        ingredientsVBox.getChildren().clear();

        if (recipe == null) {
            Label errorLabel = new Label("Recipe is null");
            errorLabel.getStyleClass().add("error-label");
            ingredientsVBox.getChildren().add(errorLabel);
            return;
        }

        if (recipe.getIngredients() == null) {
            Label errorLabel = new Label("Ingredients list is null for recipe: " + recipe.getNameEn());
            errorLabel.getStyleClass().add("error-label");
            ingredientsVBox.getChildren().add(errorLabel);
            return;
        }

        if (recipe.getIngredients().isEmpty()) {
            Label emptyLabel = new Label("No ingredients found for recipe: " + recipe.getNameEn());
            emptyLabel.getStyleClass().add("empty-label");
            ingredientsVBox.getChildren().add(emptyLabel);
            return;
        }

        // Add each ingredient as a separate label
        for (int i = 0; i < recipe.getIngredients().size(); i++) {
            Ingredient ingredient = recipe.getIngredients().get(i);

            if (ingredient == null)
                continue;

            // Create ingredient label with bullet point
            String ingredientText = "• " + ingredient.getNameEn() + " - " +
                    decimalFormat.format(ingredient.getQuantityGrams()) + " " +
                    (ingredient.getUnit() != null ? ingredient.getUnit() : "g");

            Label ingredientLabel = new Label(ingredientText);
            ingredientLabel.getStyleClass().add("ingredient-item");
            ingredientLabel.setWrapText(true);

            // Add alternating background for better readability
            if (i % 2 == 1) {
                ingredientLabel.getStyleClass().add("ingredient-item-alt");
            }

            ingredientsVBox.getChildren().add(ingredientLabel);
        }
    }

    /**
     * Populate instructions section
     */
    private void populateInstructions() {
        instructionsVBox.getChildren().clear();

        if (recipe == null || recipe.getInstructionsEn() == null || recipe.getInstructionsEn().isEmpty()) {
            Label noInstructionsLabel = new Label("No instructions available");
            noInstructionsLabel.getStyleClass().add("no-instructions");
            instructionsVBox.getChildren().add(noInstructionsLabel);
            return;
        }

        int stepNumber = 1;
        for (String instruction : recipe.getInstructionsEn()) {
            Label stepLabel = new Label("Step " + stepNumber + ":");
            stepLabel.getStyleClass().add("instruction-step-number");

            Label instructionLabel = new Label(instruction);
            instructionLabel.getStyleClass().add("instruction-text");
            instructionLabel.setWrapText(true);

            instructionsVBox.getChildren().add(stepLabel);
            instructionsVBox.getChildren().add(instructionLabel);

            stepNumber++;
        }
    }

    /**
     * Handle the "View All Nutrients" link click
     */
    @FXML
    private void toggleAllNutrients() {
        if (recipe == null || recipe.getNutritionPerServing() == null)
            return;

        showingAllNutrients = !showingAllNutrients;

        if (showingAllNutrients) {
            populateAllNutrients();
            allNutrientsSection.setVisible(true);
            allNutrientsSection.setManaged(true);
            viewAllNutrientsLink.setText("Hide All Nutrients");
        } else {
            allNutrientsSection.setVisible(false);
            allNutrientsSection.setManaged(false);
            viewAllNutrientsLink.setText("View All Nutrients");
        }
    }

    /**
     * Populate all nutrients section
     */
    private void populateAllNutrients() {
        allNutrientsGrid.getChildren().clear();

        NutritionInfo nutrition = recipe.getNutritionPerServing();
        Map<String, Double> allNutrients = nutrition.getAllNutrients();

        if (allNutrients == null || allNutrients.isEmpty()) {
            allNutrientsGrid.add(new Label("No additional nutrition information available"), 0, 0, 2, 1);
            return;
        }

        int row = 0;
        for (Map.Entry<String, Double> entry : allNutrients.entrySet()) {
            String nutrientName = formatNutrientName(entry.getKey());
            String nutrientValue = decimalFormat.format(entry.getValue()) + " " + extractUnit(entry.getKey());

            addNutritionRow(allNutrientsGrid, row, nutrientName + ":", nutrientValue);
            row++;
        }
    }

    /**
     * Format nutrient name for display
     */
    private String formatNutrientName(String nutrientKey) {
        // Convert from snake_case to Title Case
        String[] parts = nutrientKey.split("_");
        StringBuilder formatted = new StringBuilder();

        for (String part : parts) {
            if (formatted.length() > 0) {
                formatted.append(" ");
            }
            formatted.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
        }

        return formatted.toString();
    }

    /**
     * Extract unit from nutrient key
     */
    private String extractUnit(String nutrientKey) {
        if (nutrientKey.contains("_mg"))
            return "mg";
        if (nutrientKey.contains("_mcg"))
            return "mcg";
        if (nutrientKey.contains("_g"))
            return "g";
        if (nutrientKey.contains("_kcal"))
            return "kcal";
        if (nutrientKey.contains("_kj"))
            return "kJ";
        return "";
    }

    /**
     * Populate family notes section
     */
    private void populateFamilyNotes(java.util.List<String> familyNotes) {
        if (familyNotes == null || familyNotes.isEmpty()) {
            hideFamilyNotes();
            return;
        }

        // Clear existing content
        familyNotesVBox.getChildren().clear();

        // Add each note as a label
        for (String note : familyNotes) {
            Label noteLabel = new Label(note);
            noteLabel.setWrapText(true);
            noteLabel.getStyleClass().add("family-note");

            // Add different styling based on note type
            if (note.contains("⚠️")) {
                noteLabel.getStyleClass().add("warning-note");
            } else if (note.contains("✅")) {
                noteLabel.getStyleClass().add("success-note");
            } else if (note.contains("👶") || note.contains("🧒")) {
                noteLabel.getStyleClass().add("child-note");
            } else if (note.contains("👴")) {
                noteLabel.getStyleClass().add("elderly-note");
            } else if (note.contains("🍯")) {
                noteLabel.getStyleClass().add("health-note");
            }

            familyNotesVBox.getChildren().add(noteLabel);
        }

        // Show the section
        showFamilyNotes();
    }

    /**
     * Show family notes section
     */
    private void showFamilyNotes() {
        familyNotesSection.setVisible(true);
        familyNotesSection.setManaged(true);
    }

    /**
     * Hide family notes section
     */
    private void hideFamilyNotes() {
        familyNotesSection.setVisible(false);
        familyNotesSection.setManaged(false);
    }
}
