package com.thymeandtable.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class DatabaseConnection {
  private static final String URL = "jdbc:sqlite:data/thyme_table.db";
  private static boolean initialized = false;

  private DatabaseConnection() {
  }

  public static synchronized Connection getConnection() throws SQLException {
    try {
      Files.createDirectories(Path.of("data"));
    } catch (Exception e) {
      throw new SQLException("Cannot create data folder", e);
    }
    Connection c = DriverManager.getConnection(URL);
    try (Statement s = c.createStatement()) {
      s.execute("PRAGMA foreign_keys = ON");
    }
    if (!initialized) {
      initialize(c);
      initialized = true;
    }
    return c;
  }

  private static void initialize(Connection c) throws SQLException {
    String[] ddl = {
        "CREATE TABLE IF NOT EXISTS users(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL UNIQUE COLLATE NOCASE, password_hash TEXT NOT NULL, role TEXT NOT NULL CHECK(role IN ('USER','INFLUENCER','ADMIN')), created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)",
        "CREATE TABLE IF NOT EXISTS influencer_profiles(user_id INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE, display_name TEXT NOT NULL, bio TEXT NOT NULL, speciality TEXT NOT NULL)",
        "CREATE TABLE IF NOT EXISTS recipes(id INTEGER PRIMARY KEY AUTOINCREMENT, owner_id INTEGER REFERENCES users(id) ON DELETE SET NULL, title TEXT NOT NULL, description TEXT NOT NULL, instructions TEXT NOT NULL, category TEXT NOT NULL, tags TEXT NOT NULL DEFAULT '', influencer_score REAL NOT NULL DEFAULT 3 CHECK(influencer_score BETWEEN 1 AND 5), created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)",
        "CREATE TABLE IF NOT EXISTS ingredients(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE COLLATE NOCASE, unit TEXT NOT NULL DEFAULT 'unit')",
        "CREATE TABLE IF NOT EXISTS recipe_ingredients(recipe_id INTEGER NOT NULL REFERENCES recipes(id) ON DELETE CASCADE, ingredient_id INTEGER NOT NULL REFERENCES ingredients(id) ON DELETE CASCADE, required_quantity REAL NOT NULL CHECK(required_quantity>0), unit TEXT NOT NULL, PRIMARY KEY(recipe_id,ingredient_id))",
        "CREATE TABLE IF NOT EXISTS pantry_items(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, ingredient_id INTEGER NOT NULL REFERENCES ingredients(id) ON DELETE CASCADE, quantity REAL NOT NULL CHECK(quantity>=0), unit TEXT NOT NULL, expiry_date TEXT, UNIQUE(user_id,ingredient_id))",
        "CREATE TABLE IF NOT EXISTS recipe_reviews(id INTEGER PRIMARY KEY AUTOINCREMENT, recipe_id INTEGER NOT NULL REFERENCES recipes(id) ON DELETE CASCADE, user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, rating INTEGER NOT NULL CHECK(rating BETWEEN 1 AND 5), review_text TEXT NOT NULL DEFAULT '', created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, UNIQUE(recipe_id,user_id))",
        "CREATE TABLE IF NOT EXISTS meal_plans(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, recipe_id INTEGER NOT NULL REFERENCES recipes(id) ON DELETE CASCADE, plan_date TEXT NOT NULL, meal_type TEXT NOT NULL CHECK(meal_type IN ('Breakfast','Lunch','Dinner','Snack')))"
    };
    try (Statement s = c.createStatement()) {
      for (String sql : ddl)
        s.execute(sql);
    }
    try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT COUNT(*) FROM users")) {
      if (r.next() && r.getInt(1) > 0)
        return;
    }
    addUser(c, "admin", "Admin@12345", "ADMIN");
    addUser(c, "maya", "Maya@12345", "INFLUENCER");
    addUser(c, "arjun", "Arjun@12345", "INFLUENCER");
    addUser(c, "demo", "Demo@12345", "USER");
    try (PreparedStatement p = c.prepareStatement(
        "INSERT OR IGNORE INTO influencer_profiles(user_id,display_name,bio,speciality) VALUES((SELECT id FROM users WHERE username=?),?,?,?)")) {
      p.setString(1, "maya");
      p.setString(2, "Maya Thomas");
      p.setString(3, "Home cook sharing simple, colourful meals for busy students.");
      p.setString(4, "Quick vegetarian meals");
      p.executeUpdate();
    }
    try (PreparedStatement p = c.prepareStatement(
        "INSERT OR IGNORE INTO influencer_profiles(user_id,display_name,bio,speciality) VALUES((SELECT id FROM users WHERE username=?),?,?,?)")) {
      p.setString(1, "arjun");
      p.setString(2, "Arjun Nair");
      p.setString(3, "Simple comfort food and practical student-budget recipes.");
      p.setString(4, "Budget-friendly cooking");
      p.executeUpdate();
    }
    String[] ing = { "Tomato", "Onion", "Rice", "Egg", "Spinach", "Pasta", "Garlic", "Olive oil", "Chickpeas",
        "Coconut milk", "Potato", "Lemon", "Flour", "Milk", "Cheese", "Bell pepper", "Lentils", "Cumin" };
    try (PreparedStatement p = c.prepareStatement("INSERT OR IGNORE INTO ingredients(name,unit) VALUES(?,?)")) {
      for (String n : ing) {
        p.setString(1, n);
        p.setString(2, (n.equals("Olive oil") || n.equals("Milk") || n.equals("Coconut milk")) ? "ml" : "g");
        p.addBatch();
      }
      p.executeBatch();
    }
    addRecipe(c, "maya", "Tomato Garlic Pasta", "A quick, comforting pasta with tomato, garlic and herbs.",
        "1. Boil pasta until tender.\n2. Sauté garlic and onion in olive oil.\n3. Add chopped tomato and simmer.\n4. Toss pasta with sauce and serve.",
        "Quick Meals", "Quick Meals,Vegetarian,Budget-Friendly", 4.7, new String[][] { { "Pasta", "100", "g" },
            { "Tomato", "120", "g" }, { "Garlic", "5", "g" }, { "Olive oil", "10", "ml" }, { "Onion", "30", "g" } });
    addRecipe(c, "maya", "Spinach Egg Rice Bowl", "A filling rice bowl with greens and egg.",
        "1. Cook rice.\n2. Sauté onion and spinach.\n3. Cook the egg to your liking.\n4. Serve over rice with lemon.",
        "Healthy", "Healthy,Quick Meals", 4.4, new String[][] { { "Rice", "150", "g" }, { "Spinach", "60", "g" },
            { "Egg", "1", "unit" }, { "Onion", "30", "g" }, { "Lemon", "10", "g" } });
    addRecipe(c, "maya", "Chickpea Coconut Curry", "A cosy pantry-friendly curry with chickpeas.",
        "1. Sauté onion with cumin.\n2. Add tomato and cook until soft.\n3. Add chickpeas and coconut milk.\n4. Simmer for 12 minutes and serve with rice.",
        "Vegetarian", "Vegetarian,Budget-Friendly,Healthy", 4.8,
        new String[][] { { "Chickpeas", "150", "g" }, { "Coconut milk", "100", "ml" }, { "Tomato", "100", "g" },
            { "Onion", "50", "g" }, { "Cumin", "3", "g" }, { "Rice", "100", "g" } });
    addRecipe(c, "admin", "Crispy Potato Pancakes", "Golden potato pancakes with a squeeze of lemon.",
        "1. Grate potato and squeeze out excess water.\n2. Mix with flour, salt and cumin.\n3. Pan-fry small portions until golden on both sides.\n4. Serve with lemon.",
        "Budget-Friendly", "Budget-Friendly,Vegetarian", 3.8, new String[][] { { "Potato", "200", "g" },
            { "Flour", "40", "g" }, { "Cumin", "2", "g" }, { "Lemon", "15", "g" }, { "Olive oil", "10", "ml" } });
    addRecipe(c, "arjun", "One-Pot Lentil Rice", "An easy lentil and rice bowl with warming cumin.",
        "1. Rinse rice and lentils.\n2. Sauté onion and cumin in a little oil.\n3. Add rice, lentils and water.\n4. Simmer until soft and finish with lemon.",
        "Budget-Friendly", "Budget-Friendly,Vegetarian,Healthy", 4.2,
        new String[][] { { "Rice", "100", "g" }, { "Lentils", "80", "g" }, { "Onion", "40", "g" },
            { "Cumin", "3", "g" }, { "Lemon", "10", "g" }, { "Olive oil", "8", "ml" } });
    try (PreparedStatement p = c.prepareStatement(
        "INSERT OR IGNORE INTO pantry_items(user_id,ingredient_id,quantity,unit,expiry_date) VALUES((SELECT id FROM users WHERE username='demo'),(SELECT id FROM ingredients WHERE name=?),?,?,?)")) {
      String[][] rows = { { "Tomato", "250", "g", "2027-12-30" }, { "Onion", "100", "g", "2027-12-30" },
          { "Rice", "300", "g", "2028-01-01" }, { "Garlic", "20", "g", "2027-12-30" },
          { "Olive oil", "50", "ml", "2028-01-01" }, { "Pasta", "150", "g", "2028-01-01" } };
      for (String[] x : rows) {
        p.setString(1, x[0]);
        p.setDouble(2, Double.parseDouble(x[1]));
        p.setString(3, x[2]);
        p.setString(4, x[3]);
        p.addBatch();
      }
      p.executeBatch();
    }
  }

  private static void addUser(Connection c, String name, String password, String role) throws SQLException {
    try (PreparedStatement p = c
        .prepareStatement("INSERT OR IGNORE INTO users(username,password_hash,role) VALUES(?,?,?)")) {
      p.setString(1, name);
      p.setString(2, hashPassword(password));
      p.setString(3, role);
      p.executeUpdate();
    }
  }

  private static void addRecipe(Connection c, String owner, String title, String desc, String instructions,
      String category, String tags, double score, String[][] ingredients) throws SQLException {
    int id;
    try (PreparedStatement p = c.prepareStatement(
        "INSERT INTO recipes(owner_id,title,description,instructions,category,tags,influencer_score) VALUES((SELECT id FROM users WHERE username=?),?,?,?,?,?,?)",
        Statement.RETURN_GENERATED_KEYS)) {
      p.setString(1, owner);
      p.setString(2, title);
      p.setString(3, desc);
      p.setString(4, instructions);
      p.setString(5, category);
      p.setString(6, tags);
      p.setDouble(7, score);
      p.executeUpdate();
      try (ResultSet r = p.getGeneratedKeys()) {
        r.next();
        id = r.getInt(1);
      }
    }
    for (String[] x : ingredients) {
      try (PreparedStatement p = c.prepareStatement("INSERT OR IGNORE INTO ingredients(name,unit) VALUES(?,?)")) {
        p.setString(1, x[0]);
        p.setString(2, x[2]);
        p.executeUpdate();
      }
      try (PreparedStatement p = c.prepareStatement(
          "INSERT OR IGNORE INTO recipe_ingredients(recipe_id,ingredient_id,required_quantity,unit) VALUES(?,(SELECT id FROM ingredients WHERE name=?),?,?)")) {
        p.setInt(1, id);
        p.setString(2, x[0]);
        p.setDouble(3, Double.parseDouble(x[1]));
        p.setString(4, x[2]);
        p.executeUpdate();
      }
    }
  }

  public static String hashPassword(String password) {
    try {
      byte[] salt = new byte[16];
      new SecureRandom().nextBytes(salt);
      PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 120000, 256);
      byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
      spec.clearPassword();
      return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public static boolean verifyPassword(String password, String stored) {
    try {
      String[] parts = stored.split(":");
      byte[] salt = Base64.getDecoder().decode(parts[0]);
      byte[] expected = Base64.getDecoder().decode(parts[1]);
      PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 120000, expected.length * 8);
      byte[] actual = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
      spec.clearPassword();
      if (actual.length != expected.length)
        return false;
      int diff = 0;
      for (int i = 0; i < actual.length; i++)
        diff |= actual[i] ^ expected[i];
      return diff == 0;
    } catch (Exception e) {
      return false;
    }
  }
}
