package com.personalProject.meal_planner;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DishNutritionCalculator {

    private static final Map<String, DishNutritionResult> cache = new HashMap<>();

    public static DishNutritionResult getForDish(String dishName) throws Exception {
        if (cache.containsKey(dishName)) {
            System.out.println("(cache hit) " + dishName);
            return cache.get(dishName);
        }

        System.out.println("(computing fresh) " + dishName);
        DishNutritionResult result = calculate(dishName); // your existing method, slightly changed below
        cache.put(dishName, result);
        return result;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(getForDish("Cajun Shrimp Pasta"));
        System.out.println(getForDish("Cajun Shrimp Pasta")); // should be instant, no AI/USDA calls
    }

    // Calculates total calories/protein for one dish, ingredient by ingredient.
    public static DishNutritionResult calculate(String dishName) throws Exception {
        List<IngredientEstimate> ingredients = DishDecomposer.decompose(dishName);

        double totalCalories = 0;
        double totalProtein = 0;
        double totalGrams = 0;

        for (IngredientEstimate ingredient : ingredients) {
            NutritionMatch match = NutritionApi.matchIngredient(ingredient.getName());
            if (match == null) {
                System.out.println("No match for: " + ingredient.getName() + " (skipped)");
                continue;
            }
            double scale = ingredient.getGrams() / 100.0;
            totalCalories += match.getCaloriesPer100g() * scale;
            totalProtein += match.getProteinPer100g() * scale;
            totalGrams += ingredient.getGrams();
            System.out.println(ingredient.getName() + " -> " + match.getDescription() + " (" + ingredient.getGrams() + "g)");
        }

        return new DishNutritionResult(totalCalories, totalProtein, totalGrams);
    }
}