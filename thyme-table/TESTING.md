# Manual test checklist

Run `mvn clean compile exec:java`, then open `http://localhost:8080`.

1. **Seed data:** Open Recipes and Influencers. Confirm the sample dishes and both creator profiles appear.
2. **Login:** Sign in as `demo` / `Demo@12345`. Confirm My Pantry and Meal Planner appear.
3. **Registration:** Create a new USER account with a 3–24 character username and an 8+ character password. Try the same username again; it should be rejected.
4. **Pantry persistence:** Add `Carrot`, quantity `250`, unit `g`, and an expiry date. Refresh the page and confirm it remains. Delete it and confirm it disappears.
5. **Recipe search:** Search for `tomato`, then filter by `Vegetarian`. Confirm matching recipes display.
6. **Recipe details:** Open a recipe while logged in. Check ingredient quantities and missing/in-pantry labels.
7. **Reviews:** As a regular user, submit a rating and review. Reload the recipe; the review and average rating should remain. Submit another review for the same recipe; it should update the existing review rather than duplicate it.
8. **Influencer account:** Sign in as `maya` / `Maya@12345`. Open Influencers → Maya's profile. Publish a recipe with ingredients formatted `Name,quantity,unit`, then edit or delete that recipe.
9. **Ownership:** Sign in as `arjun` and try to edit Maya's recipe by changing the `id` in the edit URL. The service should reject the edit.
10. **Meal planner:** As `demo`, add a recipe/date/meal type. Confirm it remains after refresh and can be removed.
11. **Admin:** Sign in as `admin` / `Admin@12345`. Open Admin, inspect accounts and recipes, and test account/recipe removal on test data.
12. **Restart persistence:** Stop the server with Ctrl+C, start it again, and confirm pantry items, reviews, recipes, accounts and meal plans remain.

For a clean demo database, stop the server and delete `data/thyme_table.db`; the next run recreates the seeded database. This deletes local project data, so do it only if you want to reset the demo.
