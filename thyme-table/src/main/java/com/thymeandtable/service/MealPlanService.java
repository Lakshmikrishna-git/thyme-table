package com.thymeandtable.service;

import com.thymeandtable.db.DatabaseConnection;
import com.thymeandtable.model.*;
import java.sql.*;
import java.util.*;

public class MealPlanService {
    public String add(User u, int recipeId, String date, String type) throws SQLException {
        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}"))
            return "Use a date in YYYY-MM-DD format.";
        if (!List.of("Breakfast", "Lunch", "Dinner", "Snack").contains(type))
            return "Choose a valid meal type.";
        try (Connection c = DatabaseConnection.getConnection();
                PreparedStatement p = c.prepareStatement(
                        "INSERT INTO meal_plans(user_id,recipe_id,plan_date,meal_type) VALUES(?,?,?,?)")) {
            p.setInt(1, u.getId());
            p.setInt(2, recipeId);
            p.setString(3, date);
            p.setString(4, type);
            p.executeUpdate();
            return "";
        }
    }

    public List<MealPlan> list(User u) throws SQLException {
        List<MealPlan> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
                PreparedStatement p = c.prepareStatement(
                        "SELECT mp.id,mp.recipe_id,mp.plan_date,mp.meal_type,r.title FROM meal_plans mp JOIN recipes r ON r.id=mp.recipe_id WHERE mp.user_id=? ORDER BY mp.plan_date,mp.meal_type")) {
            p.setInt(1, u.getId());
            try (ResultSet r = p.executeQuery()) {
                while (r.next())
                    out.add(new MealPlan(r.getInt(1), r.getInt(2), r.getString(3), r.getString(4), r.getString(5)));
            }
        }
        return out;
    }

    public void delete(User u, int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
                PreparedStatement p = c.prepareStatement("DELETE FROM meal_plans WHERE id=? AND user_id=?")) {
            p.setInt(1, id);
            p.setInt(2, u.getId());
            p.executeUpdate();
        }
    }
}
