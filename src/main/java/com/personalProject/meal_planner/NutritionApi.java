package com.personalProject.meal_planner;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
        return key;
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

    // Finds the single best real match for one plain ingredient name
    public static NutritionMatch matchIngredient(String ingredientName) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String encodedQuery = URLEncoder.encode(ingredientName, StandardCharsets.UTF_8);
        String url = "https://api.nal.usda.gov/fdc/v1/foods/search?api_key=DEMO_KEY&query="
                + encodedQuery + "&dataType=Foundation,SR%20Legacy";

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        JsonNode foods = root.get("foods");

        // Go through candidates in order, and return the first one (best match) with data
        for (JsonNode food : foods) {
            String description = food.get("description").asText();
            int fdcId = food.get("fdcId").asInt();

            double calories = -1;
            double protein = -1;
            for (JsonNode nutrient : food.get("foodNutrients")) {
                String nutrientNumber = nutrient.get("nutrientNumber").asText();
                if (nutrientNumber.equals("208")) calories = nutrient.get("value").asDouble();
                else if (nutrientNumber.equals("203")) protein = nutrient.get("value").asDouble();
            }

            if (calories == -1 || protein == -1) continue; // skip incomplete entries, keep looking
            return new NutritionMatch(fdcId, description, calories, protein);
        }
        return null; // nothing usable came back
    }
}