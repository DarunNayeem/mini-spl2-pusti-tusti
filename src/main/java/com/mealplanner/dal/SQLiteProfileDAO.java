package com.mealplanner.dal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealplanner.model.Gender;
import com.mealplanner.model.HealthGoal;
import com.mealplanner.model.MealType;
import com.mealplanner.model.MedicalCondition;
import com.mealplanner.model.user.UserProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQLite-backed implementation of {@link ProfileDAO}.
 */
public class SQLiteProfileDAO implements ProfileDAO {

    private static final String TABLE_NAME = "user_profiles";
    private final ObjectMapper objectMapper;

    public SQLiteProfileDAO() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<UserProfile> findAllProfiles() {
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY name COLLATE NOCASE";
        List<UserProfile> profiles = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                profiles.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error loading profiles: " + e.getMessage());
        }

        return profiles;
    }

    @Override
    public Optional<UserProfile> findProfileById(String profileId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, profileId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding profile by ID: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public boolean saveProfile(UserProfile profile) {
        if (profile == null) {
            return false;
        }

        if (profile.getUserId() == null || profile.getUserId().isBlank()) {
            profile.setUserId(UUID.randomUUID().toString());
        }

        String sql = """
                INSERT INTO user_profiles (
                    user_id, name, age, height_cm, weight_kg, gender, primary_health_goal,
                    allergies, meal_preferences, medical_conditions, activity_level, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT(user_id) DO UPDATE SET
                    name = excluded.name,
                    age = excluded.age,
                    height_cm = excluded.height_cm,
                    weight_kg = excluded.weight_kg,
                    gender = excluded.gender,
                    primary_health_goal = excluded.primary_health_goal,
                    allergies = excluded.allergies,
                    meal_preferences = excluded.meal_preferences,
                    medical_conditions = excluded.medical_conditions,
                    activity_level = excluded.activity_level,
                    updated_at = CURRENT_TIMESTAMP
                """;

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            bindProfile(stmt, profile);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error saving profile: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean replaceAllProfiles(List<UserProfile> profiles) {
        List<UserProfile> safeProfiles = profiles == null ? List.of() : profiles;
        String deleteSql = "DELETE FROM " + TABLE_NAME;

        try (Connection conn = DatabaseManager.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                try (Statement deleteStatement = conn.createStatement()) {
                    deleteStatement.executeUpdate(deleteSql);
                }

                String insertSql = """
                    INSERT INTO user_profiles (
                        user_id, name, age, height_cm, weight_kg, gender, primary_health_goal,
                        allergies, meal_preferences, medical_conditions, activity_level, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                    ON CONFLICT(user_id) DO UPDATE SET
                        name = excluded.name,
                        age = excluded.age,
                        height_cm = excluded.height_cm,
                        weight_kg = excluded.weight_kg,
                        gender = excluded.gender,
                        primary_health_goal = excluded.primary_health_goal,
                        allergies = excluded.allergies,
                        meal_preferences = excluded.meal_preferences,
                        medical_conditions = excluded.medical_conditions,
                        activity_level = excluded.activity_level,
                        updated_at = CURRENT_TIMESTAMP
                    """;

                try (PreparedStatement insertStatement = conn.prepareStatement(insertSql)) {
                    for (UserProfile profile : safeProfiles) {
                        if (profile == null) {
                            continue;
                        }

                        if (profile.getUserId() == null || profile.getUserId().isBlank()) {
                            profile.setUserId(UUID.randomUUID().toString());
                        }

                        bindProfile(insertStatement, profile);
                        insertStatement.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error replacing profiles: " + e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            System.err.println("Error replacing profiles: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteProfile(String profileId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, profileId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting profile: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteAllProfiles() {
        String sql = "DELETE FROM " + TABLE_NAME;

        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting all profiles: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean existsByName(String name, String excludedProfileId) {
        StringBuilder sql = new StringBuilder("SELECT 1 FROM ").append(TABLE_NAME)
                .append(" WHERE LOWER(name) = LOWER(?)");

        if (excludedProfileId != null && !excludedProfileId.isBlank()) {
            sql.append(" AND user_id <> ?");
        }

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            stmt.setString(1, name);
            if (excludedProfileId != null && !excludedProfileId.isBlank()) {
                stmt.setString(2, excludedProfileId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking profile name uniqueness: " + e.getMessage());
            return false;
        }
    }

    private void bindProfile(PreparedStatement stmt, UserProfile profile) throws SQLException {
        stmt.setString(1, profile.getUserId());
        stmt.setString(2, profile.getName());
        stmt.setInt(3, profile.getAge());
        stmt.setDouble(4, profile.getHeightCm());
        stmt.setDouble(5, profile.getWeightKg());
        stmt.setString(6, profile.getGender() == null ? null : profile.getGender().name());
        stmt.setString(7, profile.getPrimaryHealthGoal() == null ? null : profile.getPrimaryHealthGoal().name());
        stmt.setString(8, serialize(profile.getAllergies()));
        stmt.setString(9, serialize(profile.getMealPreferences()));
        stmt.setString(10, serialize(profile.getMedicalConditions()));
        stmt.setInt(11, profile.getActivityLevel());
    }

    private UserProfile mapRow(ResultSet rs) throws SQLException {
        UserProfile profile = new UserProfile();
        profile.setUserId(rs.getString("user_id"));
        profile.setName(rs.getString("name"));
        profile.setAge(rs.getInt("age"));
        profile.setHeightCm(rs.getDouble("height_cm"));
        profile.setWeightKg(rs.getDouble("weight_kg"));
        profile.setGender(readEnum(Gender.class, rs.getString("gender")));
        profile.setPrimaryHealthGoal(readEnum(HealthGoal.class, rs.getString("primary_health_goal")));
        profile.setAllergies(deserialize(rs.getString("allergies"), new TypeReference<List<String>>() {
        }));
        profile.setMealPreferences(deserialize(rs.getString("meal_preferences"),
                new TypeReference<java.util.Map<MealType, List<String>>>() {
                }));
        profile.setMedicalConditions(deserialize(rs.getString("medical_conditions"),
                new TypeReference<List<MedicalCondition>>() {
                }));
        profile.setActivityLevel(rs.getInt("activity_level"));
        return profile;
    }

    private String serialize(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize profile data", e);
        }
    }

    private <T> T deserialize(String json, TypeReference<T> typeReference) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not deserialize profile data", e);
        }
    }

    private <E extends Enum<E>> E readEnum(Class<E> enumType, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}