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
                "\" dish, with an estimated gram weight for a standard single serving. " +
                "Respond with ONLY a JSON array, no other text, in this exact format: " +
                "[{\"name\": \"shrimp\", \"grams\": 120}, {\"name\": \"pasta\", \"grams\": 150}]";

        // The AI needs the question wrapped in a specific JSON shape.
        // toJsonString makes sure the  question text is safe to put inside JSON.
        String requestBody = "{\"contents\": [{\"parts\": [{\"text\": " + toJsonString(prompt) + "}]}]}";

        // This builds the actual request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent"))
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

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