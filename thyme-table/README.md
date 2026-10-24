# Thyme & Table — Smart Recipe Book & Kitchen Pantry Planner

A beginner-friendly Java 21 + JDBC + SQLite web application. It uses a lightweight Java `HttpServer` and HTML/CSS so it fits the project's existing browser-based direction; it does not require Spring Boot, AI, or external recipe APIs.

## Requirements
- JDK 21 (or a compatible newer JDK)
- Maven 3.9+
- VS Code + Extension Pack for Java (recommended)
- Internet access on the first Maven run to download SQLite JDBC

## Run
1. Open the `thyme-table` folder in VS Code.
2. Open a terminal in that folder.
3. Run `mvn clean compile exec:java`.
4. Open http://localhost:8080 in your browser.

Maven downloads SQLite JDBC automatically. The database `data/thyme_table.db` is created on first run and persists between restarts.

## Demo accounts
- Admin: `admin` / `Admin@12345`
- Influencer: `maya` / `Maya@12345`
- Regular user: `demo` / `Demo@12345`

Change demo passwords before using this beyond a classroom demo. Passwords are hashed using PBKDF2WithHmacSHA256.

## Features
- Register/login/logout and role-aware navigation
- Pantry CRUD with quantity and expiry date
- Recipe search by name or ingredient, recipe details, ingredients and cooking instructions
- Influencer discovery, profiles, tags, creator attribution and ratings/reviews
- Influencer-owned recipe creation and deletion; admin moderation controls
- Pantry match score and deterministic weighted recommendations
- Basic meal-plan creation and listing
- SQLite foreign keys, normalized recipe/ingredient join tables, prepared statements and unique review per user/recipe

## Recommendation formula
`score = 0.50 * pantryMatch + 0.20 * regularUserRating + 0.30 * influencerScore`.
All components are on a 0–100 scale. Pantry match considers ingredient coverage and quantity availability. Regular-user ratings are scaled from 1–5 to 0–100; unrated recipes receive a neutral `null` display and are not falsely displayed as highly rated. Influencer score is a seeded, explicitly editorial 1–5 score. The weights are configurable constants in `RecipeService` and are example project rules, not trained AI.

## OOP review notes
- Encapsulation: model fields are private and exposed through getters.
- Abstraction: services expose operations such as login, addRecipe and addPantryItem without exposing SQL to the web layer.
- Separation of concerns: `db`, `model`, `service`, and `web` packages have distinct jobs.
- Composition: a recipe contains ingredient requirements; services use `DatabaseConnection`.
- Role-based authorization is checked inside services, not just by hiding buttons.

## Important notes
- This is a college project starter suitable for a local demo, not a production internet service. It uses in-memory web sessions and a simple cookie; deploy only after adding HTTPS, CSRF protection, stronger session controls, and production configuration.
- Recipe images are intentionally not pulled from external services. The interface uses CSS food-themed cards and emoji so the project remains self-contained.
