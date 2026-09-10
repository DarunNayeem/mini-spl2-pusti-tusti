package com.mealplanner.ui;

import com.mealplanner.model.user.UserProfile;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for Shopping List Dialog
 */
public class ShoppingListController {

    @FXML
    private RadioButton familyRadio;
    @FXML
    private RadioButton individualRadio;
    @FXML
    private ToggleGroup listTypeGroup;
    @FXML
    private VBox individualSelectionBox;
    @FXML
    private ComboBox<UserProfile> memberComboBox;
    @FXML
    private Button generateButton;
    @FXML
    private VBox shoppingListSection;
    @FXML
    private Label shoppingListTitle;
    @FXML
    private TableView<ShoppingItem> shoppingListTable;
    @FXML
    private TableColumn<ShoppingItem, String> ingredientColumn;
    @FXML
    private TableColumn<ShoppingItem, String> quantityColumn;
    @FXML
    private TableColumn<ShoppingItem, String> categoryColumn;
    @FXML
    private Label totalItemsLabel;
    @FXML
    private Button exportButton;
    @FXML
    private Button closeButton;

    // Data
    private List<UserProfile> familyMembers;
    private Map<UserProfile, FamilyMemberTabController> memberControllers;

    /**
     * Initialize the controller
     */
    public void initialize() {
        // Set up radio button listeners
        listTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            individualSelectionBox.setDisable(newVal == familyRadio);
        });

        // Set up table columns
        ingredientColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIngredient()));
        quantityColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getQuantity()));
        categoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));

        // Set up combo box
        memberComboBox.setCellFactory(listView -> new ListCell<UserProfile>() {
            @Override
            protected void updateItem(UserProfile profile, boolean empty) {
                super.updateItem(profile, empty);
                if (empty || profile == null) {
                    setText(null);
                } else {
                    setText(profile.getName());
                }
            }
        });
        memberComboBox.setButtonCell(new ListCell<UserProfile>() {
            @Override
            protected void updateItem(UserProfile profile, boolean empty) {
                super.updateItem(profile, empty);
                if (empty || profile == null) {
                    setText("Choose family member...");
                } else {
                    setText(profile.getName());
                }
            }
        });
    }

    /**
     * Set family members
     */
    public void setFamilyMembers(List<UserProfile> familyMembers) {
        this.familyMembers = familyMembers;
        memberComboBox.getItems().setAll(familyMembers);
        if (!familyMembers.isEmpty()) {
            memberComboBox.setValue(familyMembers.get(0));
        }
    }

    /**
     * Set member controllers
     */
    public void setMemberControllers(Map<UserProfile, FamilyMemberTabController> memberControllers) {
        this.memberControllers = memberControllers;
    }

    /**
     * Handle generate shopping list
     */
    @FXML
    private void handleGenerate() {
        try {
            Map<String, Integer> shoppingList = new HashMap<>();

            if (familyRadio.isSelected()) {
                // Generate for entire family
                generateFamilyShoppingList(shoppingList);
                shoppingListTitle.setText("Family Shopping List");
            } else {
                // Generate for individual member
                UserProfile selectedMember = memberComboBox.getValue();
                if (selectedMember == null) {
                    showAlert("Selection Required", "Please select a family member.");
                    return;
                }
                generateIndividualShoppingList(selectedMember, shoppingList);
                shoppingListTitle.setText("Shopping List for " + selectedMember.getName());
            }

            displayShoppingList(shoppingList);

        } catch (Exception e) {
            showAlert("Error", "Failed to generate shopping list: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generate shopping list for entire family
     */
    private void generateFamilyShoppingList(Map<String, Integer> shoppingList) {
        for (UserProfile member : familyMembers) {
            FamilyMemberTabController controller = memberControllers.get(member);
            if (controller != null && controller.getWeeklyMealPlan() != null) {
                addMealPlanToShoppingList(controller.getWeeklyMealPlan(), shoppingList, member.getName());
            }
        }
    }

    /**
     * Generate shopping list for individual member
     */
    private void generateIndividualShoppingList(UserProfile member, Map<String, Integer> shoppingList) {
        FamilyMemberTabController controller = memberControllers.get(member);
        if (controller != null && controller.getWeeklyMealPlan() != null) {
            addMealPlanToShoppingList(controller.getWeeklyMealPlan(), shoppingList, member.getName());
        } else {
            showAlert("No Meal Plan",
                    "No meal plan found for " + member.getName() + ". Please generate a meal plan first.");
        }
    }

    /**
     * Add meal plan ingredients to shopping list
     */
    private void addMealPlanToShoppingList(com.mealplanner.model.WeeklyMealPlan weeklyPlan,
            Map<String, Integer> shoppingList, String memberName) {
        for (com.mealplanner.model.DailyMealPlan dailyPlan : weeklyPlan.getDailyPlans()) {
            addMealToShoppingList(dailyPlan.getBreakfast(), shoppingList);
            addMealToShoppingList(dailyPlan.getLunch(), shoppingList);
            addMealToShoppingList(dailyPlan.getDinner(), shoppingList);
            addMealToShoppingList(dailyPlan.getSnacks(), shoppingList);
        }
    }

    /**
     * Add meal ingredients to shopping list
     */
    private void addMealToShoppingList(com.mealplanner.model.Meal meal, Map<String, Integer> shoppingList) {
        if (meal == null || meal.getRecipes() == null)
            return;

        for (com.mealplanner.model.recipe.Recipe recipe : meal.getRecipes()) {
            if (recipe.getIngredients() != null) {
                for (com.mealplanner.model.recipe.Ingredient ingredient : recipe.getIngredients()) {
                    String ingredientName = ingredient.getNameEn();
                    int quantity = (int) Math.ceil(ingredient.getQuantityGrams());
                    shoppingList.merge(ingredientName, quantity, Integer::sum);
                }
            }
        }
    }

    /**
     * Display shopping list in table
     */
    private void displayShoppingList(Map<String, Integer> shoppingList) {
        shoppingListTable.getItems().clear();

        for (Map.Entry<String, Integer> entry : shoppingList.entrySet()) {
            String ingredient = entry.getKey();
            String quantity = entry.getValue() + " g";
            String category = categorizeIngredient(ingredient);

            shoppingListTable.getItems().add(new ShoppingItem(ingredient, quantity, category));
        }

        totalItemsLabel.setText("Total items: " + shoppingList.size());

        // Show the shopping list section
        shoppingListSection.setVisible(true);
        shoppingListSection.setManaged(true);
    }

    /**
     * Categorize ingredient (simple categorization)
     */
    private String categorizeIngredient(String ingredient) {
        String lower = ingredient.toLowerCase();

        if (lower.contains("rice") || lower.contains("bread") || lower.contains("flour")) {
            return "Grains";
        } else if (lower.contains("chicken") || lower.contains("fish") || lower.contains("meat")
                || lower.contains("egg")) {
            return "Protein";
        } else if (lower.contains("milk") || lower.contains("cheese") || lower.contains("yogurt")) {
            return "Dairy";
        } else if (lower.contains("apple") || lower.contains("banana") || lower.contains("orange")) {
            return "Fruits";
        } else if (lower.contains("onion") || lower.contains("tomato") || lower.contains("potato")) {
            return "Vegetables";
        } else {
            return "Other";
        }
    }

    /**
     * Handle export to file
     */
    @FXML
    private void handleExport() {
        if (shoppingListTable.getItems().isEmpty()) {
            showAlert("No Data", "No shopping list to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Shopping List");
        fileChooser.setInitialFileName("shopping_list.txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        Stage stage = (Stage) exportButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Shopping List\n");
                writer.write("=============\n\n");

                for (ShoppingItem item : shoppingListTable.getItems()) {
                    writer.write(String.format("• %s - %s (%s)\n",
                            item.getIngredient(), item.getQuantity(), item.getCategory()));
                }

                writer.write("\nTotal items: " + shoppingListTable.getItems().size());

                showInfoDialog("Export Successful", "Shopping list saved to " + file.getName());

            } catch (IOException e) {
                showAlert("Export Error", "Failed to save shopping list: " + e.getMessage());
            }
        }
    }

    /**
     * Handle close button
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
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
     * Shopping item data class
     */
    public static class ShoppingItem {
        private String ingredient;
        private String quantity;
        private String category;

        public ShoppingItem(String ingredient, String quantity, String category) {
            this.ingredient = ingredient;
            this.quantity = quantity;
            this.category = category;
        }

        public String getIngredient() {
            return ingredient;
        }

        public String getQuantity() {
            return quantity;
        }

        public String getCategory() {
            return category;
        }
    }
}
