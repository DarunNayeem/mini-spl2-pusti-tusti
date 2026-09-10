# User Interface Guide

## Overview
Pusti-Tusti features a modern JavaFX-based user interface designed for intuitive meal planning and nutrition management. The UI follows Material Design principles with a focus on usability, accessibility, and visual appeal.

## UI Architecture

### Design Pattern
- **MVC (Model-View-Controller)**: Clean separation between UI logic and business logic
- **Component-Based**: Reusable UI components and controllers
- **FXML-Based**: Declarative UI design with XML layouts
- **CSS Styling**: Professional appearance with custom stylesheets

### Technology Stack
- **JavaFX**: Modern GUI framework for desktop applications
- **FXML**: XML-based UI layout definition
- **CSS**: Custom styling and theming
- **Scene Builder**: Visual UI design tool compatibility

## Main Application Layout

### Application Window Structure
```
MainView.fxml (BorderPane - 1200x800px)
├── Top Region
│   ├── Application Title
│   ├── User Profile Display
│   └── Action Buttons
├── Center Region (Primary Content)
│   └── Nested TabPane Structure
│       ├── Day Tabs (Monday - Sunday)
│       └── Meal Tabs (Breakfast, Lunch, Dinner, Snacks)
├── Left Region
│   ├── Profile Management Panel
│   ├── Quick Actions
│   └── Statistics Summary
└── Bottom Region
    ├── Status Bar
    └── Application Information
```

## Core UI Components

### 1. MainViewController
**File**: `MainViewController.java` + `MainView.fxml`  
**Purpose**: Primary application controller managing the main interface

#### Key Features:
- **Service Integration**: Dependency injection for business logic services
- **Tab Management**: Dynamic creation and management of day/meal tabs
- **Profile Display**: Real-time user profile information display
- **Event Handling**: Central event coordination for user interactions

#### FXML Structure:
```xml
<BorderPane xmlns="http://javafx.com/javafx/11.0.1" 
            xmlns:fx="http://javafx.com/fxml/1" 
            fx:controller="com.mealplanner.ui.MainViewController">
    <top>
        <HBox styleClass="header-section">
            <Label fx:id="titleLabel" text="Pusti-Tusti Meal Planner" />
            <Button fx:id="generatePlanButton" text="Generate New Plan" />
            <Button fx:id="editProfileButton" text="Edit Profile" />
        </HBox>
    </top>
    
    <center>
        <TabPane fx:id="mainTabPane" styleClass="main-tabs">
            <!-- Dynamic day tabs created programmatically -->
        </TabPane>
    </center>
    
    <left>
        <VBox styleClass="sidebar">
            <Label text="Profile Information" styleClass="sidebar-title" />
            <VBox fx:id="profileInfoContainer" />
            <!-- Profile details dynamically populated -->
        </VBox>
    </left>
</BorderPane>
```

#### Controller Methods:
```java
@FXML
private void initialize() {
    // Initialize UI components and event handlers
    setupTabPane();
    loadUserProfile();
    updateProfileDisplay();
}

@FXML
private void handleGenerateNewPlan() {
    // Generate new meal plan and update UI
    WeeklyMealPlan plan = mealPlannerService.generateNewWeeklyPlan(
        currentUserProfile, currentNutritionProfile);
    updateMealPlanDisplay(plan);
}

@FXML
private void handleEditProfile() {
    // Open profile editor dialog
    showProfileEditor();
}
```

### 2. ProfileEditorController
**File**: `ProfileEditorController.java`  
**Purpose**: Modal dialog for user profile creation and editing

#### Features:
- **Form Validation**: Real-time input validation with error feedback
- **Health Goal Selection**: Interactive health goal configuration
- **Medical Condition Management**: Multiple condition selection with descriptions
- **Dietary Preference Setup**: Comprehensive dietary preference configuration
- **Allergen Management**: Custom allergen list management

#### UI Components:
```java
@FXML private TextField nameField;
@FXML private Spinner<Integer> ageSpinner;
@FXML private ComboBox<Gender> genderComboBox;
@FXML private TextField heightField;
@FXML private TextField weightField;
@FXML private ComboBox<HealthGoal> healthGoalComboBox;
@FXML private Slider activityLevelSlider;
@FXML private ListView<MedicalCondition> medicalConditionsList;
@FXML private CheckBox[] dietaryPreferenceCheckBoxes;
@FXML private ListView<String> allergensList;
```

#### Validation Logic:
```java
private boolean validateInput() {
    boolean valid = true;
    clearErrorMessages();
    
    // Name validation
    if (nameField.getText().trim().isEmpty()) {
        showFieldError(nameField, "Name is required");
        valid = false;
    }
    
    // Age validation
    if (ageSpinner.getValue() < 1 || ageSpinner.getValue() > 120) {
        showFieldError(ageSpinner, "Age must be between 1 and 120");
        valid = false;
    }
    
    // Height validation
    try {
        double height = Double.parseDouble(heightField.getText());
        if (height < 50 || height > 250) {
            showFieldError(heightField, "Height must be between 50-250 cm");
            valid = false;
        }
    } catch (NumberFormatException e) {
        showFieldError(heightField, "Invalid height format");
        valid = false;
    }
    
    return valid;
}
```

### 3. RecipeCardController
**File**: `RecipeCardController.java`  
**Purpose**: Individual recipe display component

#### Features:
- **Recipe Information Display**: Name, description, and cooking time
- **Nutrition Information**: Comprehensive nutritional data visualization
- **Ingredient List**: Detailed ingredient display with quantities
- **Cooking Instructions**: Step-by-step cooking guidance
- **Multilingual Support**: English and Bengali recipe names

#### Card Layout:
```xml
<VBox styleClass="recipe-card">
    <HBox styleClass="recipe-header">
        <VBox styleClass="recipe-title-section">
            <Label fx:id="recipeNameLabel" styleClass="recipe-name" />
            <Label fx:id="recipeNameBengaliLabel" styleClass="recipe-name-bengali" />
        </VBox>
        <VBox styleClass="recipe-meta">
            <Label fx:id="cookingTimeLabel" styleClass="cooking-time" />
            <Label fx:id="caloriesLabel" styleClass="calories" />
        </VBox>
    </HBox>
    
    <TabPane styleClass="recipe-details-tabs">
        <Tab text="Ingredients">
            <ListView fx:id="ingredientsList" />
        </Tab>
        <Tab text="Instructions">
            <ListView fx:id="instructionsList" />
        </Tab>
        <Tab text="Nutrition">
            <GridPane fx:id="nutritionGrid" />
        </Tab>
    </TabPane>
</VBox>
```

### 4. FamilyMemberTabController
**File**: `FamilyMemberTabController.java`  
**Purpose**: Family member profile and meal plan management

#### Features:
- **Member Profile Display**: Individual family member information
- **Meal Plan Generation**: Member-specific meal planning
- **Nutrition Tracking**: Individual nutritional requirement tracking
- **Dietary Conflict Resolution**: Handle conflicting dietary requirements

## Nested Tab Structure

### Day-Level Tabs
The main interface uses a two-level tab structure for intuitive navigation:

#### Primary Tabs (Days of the Week):
- **Monday Tab**: Complete Monday meal plan
- **Tuesday Tab**: Complete Tuesday meal plan
- **Wednesday Tab**: Complete Wednesday meal plan
- **Thursday Tab**: Complete Thursday meal plan
- **Friday Tab**: Complete Friday meal plan
- **Saturday Tab**: Complete Saturday meal plan
- **Sunday Tab**: Complete Sunday meal plan

#### Secondary Tabs (Meals within Each Day):
- **Breakfast Tab**: Morning meal recipes and nutrition
- **Lunch Tab**: Midday meal recipes and nutrition
- **Dinner Tab**: Evening meal recipes and nutrition
- **Snacks Tab**: Snack and beverage options

### Tab Creation Logic:
```java
private void setupTabPane() {
    mainTabPane.getTabs().clear();
    
    String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", 
                        "Friday", "Saturday", "Sunday"};
    
    for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
        Tab dayTab = new Tab(dayNames[dayIndex]);
        TabPane mealTabPane = new TabPane();
        
        // Create meal tabs for each day
        for (MealType mealType : MealType.values()) {
            Tab mealTab = new Tab(mealType.toString());
            ScrollPane scrollPane = new ScrollPane();
            VBox mealContainer = new VBox();
            mealContainer.setSpacing(10);
            scrollPane.setContent(mealContainer);
            mealTab.setContent(scrollPane);
            mealTabPane.getTabs().add(mealTab);
        }
        
        dayTab.setContent(mealTabPane);
        mainTabPane.getTabs().add(dayTab);
    }
}
```

## Styling and Theming

### CSS Stylesheet: `meal-planner-styles.css`

#### Color Palette:
```css
/* Primary Colors */
.root {
    -fx-primary-color: #2E7D32;      /* Forest Green */
    -fx-secondary-color: #FFC107;     /* Amber */
    -fx-accent-color: #FF7043;        /* Deep Orange */
    -fx-background-color: #F5F5F5;    /* Light Gray */
    -fx-text-color: #212121;          /* Dark Gray */
}
```

#### Component Styling:

##### Main Application Window:
```css
.root {
    -fx-font-family: "Segoe UI", Arial, sans-serif;
    -fx-font-size: 14px;
    -fx-background-color: #F5F5F5;
}

.main-window {
    -fx-background-color: white;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);
}
```

##### Header Section:
```css
.header-section {
    -fx-background-color: linear-gradient(to bottom, #2E7D32, #388E3C);
    -fx-padding: 15px 20px;
    -fx-spacing: 20px;
    -fx-alignment: center-left;
}

.header-section .label {
    -fx-text-fill: white;
    -fx-font-size: 18px;
    -fx-font-weight: bold;
}

.header-section .button {
    -fx-background-color: #FFC107;
    -fx-text-fill: #212121;
    -fx-font-weight: bold;
    -fx-background-radius: 20px;
    -fx-padding: 8px 16px;
}

.header-section .button:hover {
    -fx-background-color: #FFD54F;
    -fx-cursor: hand;
}
```

##### Tab Styling:
```css
.main-tabs .tab-pane {
    -fx-background-color: white;
}

.main-tabs .tab {
    -fx-background-color: #E8F5E8;
    -fx-text-base-color: #2E7D32;
    -fx-font-weight: bold;
}

.main-tabs .tab:selected {
    -fx-background-color: #2E7D32;
    -fx-text-base-color: white;
}

.meal-tabs .tab {
    -fx-background-color: #FFF3E0;
    -fx-text-base-color: #FF7043;
}

.meal-tabs .tab:selected {
    -fx-background-color: #FF7043;
    -fx-text-base-color: white;
}
```

##### Recipe Card Styling:
```css
.recipe-card {
    -fx-background-color: white;
    -fx-background-radius: 8px;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
    -fx-padding: 15px;
    -fx-spacing: 10px;
    -fx-margin: 10px;
}

.recipe-name {
    -fx-font-size: 16px;
    -fx-font-weight: bold;
    -fx-text-fill: #2E7D32;
}

.recipe-name-bengali {
    -fx-font-size: 14px;
    -fx-font-style: italic;
    -fx-text-fill: #666666;
}

.cooking-time {
    -fx-text-fill: #FF7043;
    -fx-font-weight: bold;
}

.calories {
    -fx-text-fill: #FFC107;
    -fx-font-weight: bold;
}
```

##### Sidebar Styling:
```css
.sidebar {
    -fx-background-color: #FAFAFA;
    -fx-padding: 20px;
    -fx-spacing: 15px;
    -fx-border-color: #E0E0E0;
    -fx-border-width: 0 1px 0 0;
}

.sidebar-title {
    -fx-font-size: 16px;
    -fx-font-weight: bold;
    -fx-text-fill: #2E7D32;
}

.profile-info-item {
    -fx-padding: 5px 0;
    -fx-border-color: #E8F5E8;
    -fx-border-width: 0 0 1px 0;
}
```

## User Interaction Patterns

### 1. Meal Plan Generation Flow
1. **User Action**: Click "Generate New Plan" button
2. **Loading State**: Show progress indicator
3. **Background Processing**: Service generates meal plan
4. **UI Update**: Populate tabs with new meal data
5. **User Feedback**: Show success notification

### 2. Profile Editing Flow
1. **User Action**: Click "Edit Profile" button
2. **Modal Dialog**: Open ProfileEditorController dialog
3. **Form Interaction**: User fills/modifies profile data
4. **Validation**: Real-time input validation
5. **Save Action**: Persist profile and update UI
6. **Dialog Close**: Return to main interface with updated data

### 3. Recipe Viewing Flow
1. **Recipe Selection**: Click on recipe card
2. **Detailed View**: Expand recipe information
3. **Tab Navigation**: Switch between ingredients, instructions, nutrition
4. **Action Options**: Add to favorites, modify portion, etc.

## Responsive Design

### Window Sizing:
- **Minimum Size**: 800x600 pixels
- **Default Size**: 1200x800 pixels
- **Maximum Size**: Full screen capability
- **Aspect Ratio**: 3:2 for optimal viewing

### Component Adaptation:
```java
// Responsive tab sizing
private void setupResponsiveLayout() {
    mainTabPane.widthProperty().addListener((obs, oldWidth, newWidth) -> {
        double tabWidth = (newWidth.doubleValue() - 50) / 7; // 7 days
        for (Tab tab : mainTabPane.getTabs()) {
            tab.getStyleClass().add("responsive-tab");
        }
    });
}
```

## Accessibility Features

### Keyboard Navigation:
- **Tab Navigation**: All components accessible via Tab key
- **Keyboard Shortcuts**: Ctrl+N (New Plan), Ctrl+E (Edit Profile)
- **Enter/Space Activation**: Buttons activated with Enter/Space
- **Arrow Key Navigation**: List and tree navigation

### Screen Reader Support:
```java
// Accessibility labels
generatePlanButton.setAccessibleText("Generate new weekly meal plan");
editProfileButton.setAccessibleText("Edit user profile information");
```

### High Contrast Support:
```css
/* High contrast mode */
.root:high-contrast {
    -fx-base: white;
    -fx-accent: blue;
    -fx-text-base-color: black;
}
```

## Animation and Transitions

### Tab Switching Animation:
```css
.tab-pane .tab-content-area {
    -fx-effect: null;
}

.tab-pane .tab-content-area .fade-transition {
    -fx-opacity: 0;
}

.tab-pane .tab-content-area .fade-transition:fade-in {
    -fx-opacity: 1;
    -fx-transition: opacity 0.3s ease-in-out;
}
```

### Button Hover Effects:
```css
.button {
    -fx-transition: all 0.2s ease-in-out;
}

.button:hover {
    -fx-scale-x: 1.05;
    -fx-scale-y: 1.05;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);
}
```

## Error Handling and User Feedback

### Error Display:
```java
private void showError(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}
```

### Success Notifications:
```java
private void showSuccess(String message) {
    // Custom notification component
    NotificationPane notification = new NotificationPane();
    notification.show(message, NotificationType.SUCCESS);
}
```

### Loading States:
```java
private void showLoadingState() {
    ProgressIndicator progress = new ProgressIndicator();
    progress.setPrefSize(50, 50);
    
    VBox loadingContainer = new VBox(progress, new Label("Generating meal plan..."));
    loadingContainer.setAlignment(Pos.CENTER);
    
    // Replace main content with loading state
    rootPane.setCenter(loadingContainer);
}
```

## Performance Optimization

### UI Threading:
```java
// Background task for heavy operations
Task<WeeklyMealPlan> generatePlanTask = new Task<WeeklyMealPlan>() {
    @Override
    protected WeeklyMealPlan call() throws Exception {
        return mealPlanService.generateNewWeeklyPlan(user, nutrition);
    }
    
    @Override
    protected void succeeded() {
        Platform.runLater(() -> updateUI(getValue()));
    }
};

new Thread(generatePlanTask).start();
```

### Memory Management:
- **Component Reuse**: Recipe cards pooled and reused
- **Lazy Loading**: Content loaded only when tabs are selected
- **Image Caching**: Recipe images cached for performance
- **Event Handler Cleanup**: Proper cleanup of event listeners

## Testing Strategy

### UI Unit Tests:
```java
@Test
public void testProfileEditorValidation() {
    ProfileEditorController controller = new ProfileEditorController();
    controller.nameField.setText("");
    assertFalse(controller.validateInput());
    
    controller.nameField.setText("John Doe");
    controller.ageSpinner.getValueFactory().setValue(25);
    assertTrue(controller.validateInput());
}
```

### Integration Tests:
```java
@Test
public void testMealPlanDisplay() {
    MainViewController controller = new MainViewController();
    WeeklyMealPlan testPlan = createTestMealPlan();
    
    Platform.runLater(() -> {
        controller.updateMealPlanDisplay(testPlan);
        assertEquals(7, controller.mainTabPane.getTabs().size());
    });
}
```

## Internationalization (i18n)

### Resource Bundles:
```properties
# messages_en.properties
app.title=Pusti-Tusti Meal Planner
button.generate.plan=Generate New Plan
button.edit.profile=Edit Profile
label.breakfast=Breakfast
label.lunch=Lunch
label.dinner=Dinner
label.snacks=Snacks

# messages_bn.properties
app.title=পুষ্টি-তুষ্টি খাবার পরিকল্পনাকারী
button.generate.plan=নতুন পরিকল্পনা তৈরি করুন
button.edit.profile=প্রোফাইল সম্পাদনা করুন
label.breakfast=নাস্তা
label.lunch=দুপুরের খাবার
label.dinner=রাতের খাবার
label.snacks=জলখাবার
```

### Dynamic Text Loading:
```java
private ResourceBundle messages = ResourceBundle.getBundle("messages", 
                                                          Locale.getDefault());

private void updateTexts() {
    titleLabel.setText(messages.getString("app.title"));
    generatePlanButton.setText(messages.getString("button.generate.plan"));
    editProfileButton.setText(messages.getString("button.edit.profile"));
}
```

---

This comprehensive UI guide provides detailed information about the Pusti-Tusti user interface, enabling developers to understand, maintain, and extend the application's visual and interactive components effectively.
