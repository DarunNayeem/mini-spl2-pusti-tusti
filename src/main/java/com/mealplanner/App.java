package com.mealplanner;

import com.mealplanner.dal.DAOFactory;
import com.mealplanner.dal.RecipeDAO;
import com.mealplanner.service.MealPlannerService;
import com.mealplanner.service.NutritionCalculatorService;
import com.mealplanner.service.ProfileService;
import com.mealplanner.ui.MainViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main application class for Pusti-Tusti meal planning application
 * Handles application initialization, service creation, and dependency
 * injection
 */
public class App extends Application {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    @Override
    public void start(Stage primaryStage) {
        try {
            if (!DAOFactory.initializeDatabase()) {
                throw new IllegalStateException("Could not initialize the application database");
            }

            RecipeDAO recipeDAO = DAOFactory.getRecipeDAO();
            NutritionCalculatorService nutritionCalculatorService = new NutritionCalculatorService();
            MealPlannerService mealPlannerService = new MealPlannerService(recipeDAO);
            ProfileService profileService = new ProfileService();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/mealplanner/ui/MainView.fxml"));
            BorderPane root = fxmlLoader.load();

            MainViewController mainController = fxmlLoader.getController();
            if (mainController == null) {
                throw new IllegalStateException("MainViewController could not be loaded from FXML");
            }
            mainController.setServices(mealPlannerService, profileService, nutritionCalculatorService);

            Scene scene = new Scene(root, 1200, 800);
            var css = getClass().getResource("/com/mealplanner/ui/meal-planner-styles.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            primaryStage.setTitle("Personalized Meal Planner");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.show();

            LOGGER.info("Application started successfully");

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading FXML", e);
            showCriticalError("Could not load application interface. Please check FXML files.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Critical startup error", e);
            String message = e.getMessage() == null ? "Unknown startup error" : e.getMessage();
            if (message.toLowerCase().contains("database") || message.contains("recipes.db") || message.contains("SQLite")) {
                showCriticalError("Critical Error: Could not start application. Check database file.");
            } else {
                showCriticalError("Critical Error: Could not start application. " + message);
            }
        }
    }

    /**
     * Display critical error alert and exit application
     */
    private void showCriticalError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Critical Error");
        alert.setHeaderText("Application Startup Failed");
        alert.setContentText(message);
        alert.showAndWait();
        System.exit(1);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
