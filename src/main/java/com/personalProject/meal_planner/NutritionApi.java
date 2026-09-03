package com.personalProject.meal_planner;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.jspecify.annotations.Nullable;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;

public class NutritionApi {

    public static void main(String[] args) throws Exception {
        lookupFood("Pork Dumplings");
        lookupFood("Cajun Shrimp Pasta");
        lookupFood("Southwest Pork Tenderloin");
    }

    // Helper method to fetch the API key from system environment variables
    private static String getApiKey() {
        String key = System.getenv("USDA_API_KEY");
        if (key == null || key.isBlank()) {
            return "DEMO_KEY";
        }
        return null;
    }

    public static void lookupFood(String dishName) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String encodedQuery = URLEncoder.encode(dishName, StandardCharsets.UTF_8);//Makes it so that dishes with spaces are searched up through a URL in the correct format
        String url = "https://api.nal.usda.gov/fdc/v1/foods/search?api_key=" + getApiKey()
                + "&query=" + encodedQuery + "&dataType=Foundation,SR%20Legacy";

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        JsonNode foods = root.get("foods");

        System.out.println("=== " + dishName + " (" + foods.size() + " candidates) ===");

        /*ooked pasta -> Pasta, cooked, enriched, with added salt (130.0g)
cooked shrimp -> Crustaceans, shrimp, cooked (60.0g)
cajun cream sauce -> Cream, fluid, light (coffee cream or table cream) (65.0g)
cooked bell peppers -> Peppers, sweet, green, cooked, boiled, drained, with salt (20.0g)
cooked onions -> Onions, cooked, boiled, drained, with salt (15.0g)
grated parmesan cheese -> Cheese, parmesan, grated (10.0g)
Cajun Shrimp Pasta TOTAL: 443.75 kcal, 27.092g protein, 300.0 g total weight

cooked penne pasta -> Pasta, cooked, enriched, with added salt (140.0g)
cooked shrimp -> Crustaceans, shrimp, cooked (50.0g)
cajun cream sauce -> Cream, fluid, light (coffee cream or table cream) (60.0g)
cooked bell peppers -> Peppers, sweet, green, cooked, boiled, drained, with salt (20.0g)
cooked onions -> Onions, cooked, boiled, drained, with salt (15.0g)
No match for: cajun seasoning (skipped)
Cajun Shrimp Pasta TOTAL: 397.79999999999995 kcal, 22.284g protein, 285.0 g total weight
*/
        for (JsonNode food : foods) {
            String description = food.get("description").asText();
            int fdcId = food.get("fdcId").asInt();

            double calories = -1;
            double protein = -1;
            JsonNode nutrients = food.get("foodNutrients");
            for (JsonNode nutrient : nutrients) {
                String nutrientNumber = nutrient.get("nutrientNumber").asText();
                if (nutrientNumber.equals("208")) {
                    calories = nutrient.get("value").asDouble();
                } else if (nutrientNumber.equals("203")) {
                    protein = nutrient.get("value").asDouble();
                }
            }

            if (calories == -1 || protein == -1)
                continue;
            System.out.println(fdcId + " | " + description + " | " + calories + " kcal | " + protein + "g protein");
        }
    }

    // Skip results that look like a branded/restaurant item, not a plain ingredient.
    private static boolean looksLikeBrand(String description) {
        for (String word : description.split("\\s+")) {
            String cleaned = word.replaceAll("[^A-Za-z]", "");
            if (cleaned.length() > 2 && cleaned.equals(cleaned.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    // Finds the single best real match for one plain ingredient name
    public static NutritionMatch matchIngredient(String ingredientName) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String encodedQuery = URLEncoder.encode(ingredientName, StandardCharsets.UTF_8);

        if (ingredientName.toLowerCase().contains("seasoning") || ingredientName.toLowerCase().contains("spice")) {
            return null;
        }

        // Uses USDA_API_KEY if set, otherwise falls back to DEMO_KEY
        String apiKey = System.getenv("USDA_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = "DEMO_KEY";
        }

        String url = "https://api.nal.usda.gov/fdc/v1/foods/search?api_key=" + apiKey
                + "&query=" + encodedQuery + "&dataType=Foundation,SR%20Legacy";

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Check if the API returned an HTTP error
        if (response.statusCode() != 200) {
            System.err.println("USDA API Error for '" + ingredientName + "' (HTTP " + response.statusCode() + "): " + response.body());
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        JsonNode foods = root.get("foods");

        if (foods == null || !foods.isArray() || foods.isEmpty()) {
            System.out.println("No USDA matches found for: " + ingredientName);
            return null;
        }

        for (JsonNode food : foods) {
            if (!food.has("description") || !food.has("fdcId")) continue;

            String description = food.get("description").asText();
            int fdcId = food.get("fdcId").asInt();

            double calories = -1;
            double protein = -1;
            JsonNode nutrients = food.get("foodNutrients");

            if (nutrients != null && nutrients.isArray()) {
                for (JsonNode nutrient : nutrients) {
                    if (nutrient.has("nutrientNumber")) {
                        String nutrientNumber = nutrient.get("nutrientNumber").asText();
                        if (nutrientNumber.equals("208")) {
                            calories = nutrient.has("value") ? nutrient.get("value").asDouble() : -1;
                        } else if (nutrientNumber.equals("203")) {
                            protein = nutrient.has("value") ? nutrient.get("value").asDouble() : -1;
                        }
                    }
                }
            }

            if (calories == -1 || protein == -1) continue;
            if (looksLikeBrand(description)) continue;
            return new NutritionMatch(fdcId, description, calories, protein);
        }

        return null;
    }
}