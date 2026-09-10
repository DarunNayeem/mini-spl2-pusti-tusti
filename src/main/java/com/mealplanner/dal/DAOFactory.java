package com.mealplanner.dal;

/**
 * Factory class for creating DAO instances
 */
public class DAOFactory {

    private static RecipeDAO recipeDAO;
    private static ProfileDAO profileDAO;

    /**
     * Private constructor to prevent instantiation
     */
    private DAOFactory() {
    }

    /**
     * Gets the singleton instance of RecipeDAO
     * 
     * @return RecipeDAO instance
     */
    public static synchronized RecipeDAO getRecipeDAO() {
        if (recipeDAO == null) {
            recipeDAO = new RecipeDAOImpl();
        }
        return recipeDAO;
    }

    /**
     * Gets the singleton instance of ProfileDAO.
     *
     * @return ProfileDAO instance
     */
    public static synchronized ProfileDAO getProfileDAO() {
        if (profileDAO == null) {
            profileDAO = new SQLiteProfileDAO();
        }
        return profileDAO;
    }

    /**
     * Initializes the database and tests connectivity
     * 
     * @return true if initialization is successful
     */
    public static boolean initializeDatabase() {
        return DatabaseManager.testConnection();
    }

    /**
     * Closes all database connections
     */
    public static void cleanup() {
        DatabaseManager.closeConnection();
    }
}
