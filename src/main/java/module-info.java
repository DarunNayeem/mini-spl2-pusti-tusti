module com.mealplanner {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive java.sql;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    exports com.mealplanner;
    exports com.mealplanner.model;
    exports com.mealplanner.model.user;
    exports com.mealplanner.model.recipe;
    exports com.mealplanner.dal;
    exports com.mealplanner.ui;
    exports com.mealplanner.service;

    opens com.mealplanner.ui to javafx.fxml;
    opens com.mealplanner.service;
}
