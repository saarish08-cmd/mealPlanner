package com.personalProject.meal_planner;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;

public class DishDecomposer {
    public static void main(String[] args) throws Exception {
        List<IngredientEstimate> ingredients = decompose("Cajun Shrimp Pasta");
        for (IngredientEstimate ingredient : ingredients) {
            System.out.println(ingredient);
        }
    }

    // This method asks the AI for a list of ingredients for one dish.
    public static List<IngredientEstimate> decompose(String dishName) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String apiKey = System.getenv("GEMINI_API_KEY");

        // This is the question to the AI.
        String prompt = "List the main ingredients of a typical \"" + dishName +
                "\" dish, with an estimated gram weight for ONE realistic cafeteria-style " +
                "entrée portion — the amount one student would get from a single scoop or " +
                "serving spoon at a dining hall, NOT a home recipe batch or restaurant-size plate. " +
                "List each ingredient as its own separate entry — never combine two different foods " +
                "into one item (for example, write \"bell peppers\" and \"onions\" separately, " +
                "not \"bell peppers and onions\"). " +
                "Assume all ingredients are in their fully cooked, ready-to-eat state, as they would " +
                "be served on a plate — never raw or dry weight. Name each ingredient in a way that " +
                "reflects this (e.g. \"cooked pasta\", not \"pasta\"; \"cooked shrimp\", not \"shrimp\"). " +
                "Respond with ONLY a JSON array, no other text, in this exact format: " +
                "[{\"name\": \"shrimp\", \"grams\": 120}, {\"name\": \"pasta\", \"grams\": 150}]";

        // The AI needs the question wrapped in a specific JSON shape.
        // toJsonString makes sure the  question text is safe to put inside JSON.
        String requestBody = "{\"contents\": [{\"parts\": [{\"text\": " + toJsonString(prompt) + "}]}],"
                + "\"generationConfig\": {\"temperature\": 0.2}}";

        // This builds the actual request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent"))
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        /*cooked pasta -> Pasta, cooked, enriched, with added salt (140.0g)
        cooked shrimp -> Crustaceans, shrimp, cooked (60.0g)
        cooked cajun cream sauce -> Guava sauce, cooked (70.0g)
        cooked red bell peppers -> Peppers, sweet, red, cooked, boiled, drained, with salt (15.0g)
        cooked green bell peppers -> Peppers, sweet, green, cooked, boiled, drained, with salt (15.0g)
        cooked onions -> Onions, cooked, boiled, drained, with salt (15.0g)
        Cajun Shrimp Pasta TOTAL: 318.49999999999994 kcal, 23.224g protein, 315.0 g total weight

        cooked penne pasta -> Pasta, cooked, enriched, with added salt (180.0g)
cooked shrimp -> Crustaceans, shrimp, cooked (75.0g)
Cajun cream sauce -> Cream, fluid, light (coffee cream or table cream) (85.0g)
cooked red bell peppers -> Peppers, sweet, red, cooked, boiled, drained, with salt (20.0g)
cooked green bell peppers -> Peppers, sweet, green, cooked, boiled, drained, with salt (20.0g)
cooked onions -> Onions, cooked, boiled, drained, with salt (15.0g)
grated parmesan cheese -> Cheese, parmesan, grated (5.0g)
Cajun Shrimp Pasta TOTAL: 560.3000000000001 kcal, 32.948g protein, 400.0 g total weight*/

        // This sends the request to AI
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Print error details if the request failed
        if (response.statusCode() != 200) {
            System.err.println("Gemini API Error (HTTP " + response.statusCode() + "):");
            System.err.println(response.body());
            throw new IllegalStateException("API request failed with status code " + response.statusCode());
        }

        // This turns the raw text answer into something readable
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());

        // Digs out the information that is required from the full response
        String aiText = root.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();

        // The AI's plain text is itself a JSON list, so read it a second time.
        JsonNode ingredientsJson = mapper.readTree(aiText);

        List<IngredientEstimate> ingredients = new ArrayList<>();

        // Go through each ingredient the AI gave us, one at a time.
        for (JsonNode item : ingredientsJson) {
            // For each one, grab its name and its grams, and save it.
            ingredients.add(new IngredientEstimate(item.get("name").asText(), item.get("grams").asDouble()));
        }

        // Hand back the whole list once done.
        return ingredients;
    }

    // This method makes a piece of text safe to place inside JSON.
    private static String toJsonString(String raw) {
        return "\"" + raw.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}