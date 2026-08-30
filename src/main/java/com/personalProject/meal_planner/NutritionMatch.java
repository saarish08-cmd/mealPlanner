package com.personalProject.meal_planner;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class NutritionMatch {
    private int fdcId;
    private String description;
    private double caloriesPer100g;
    private double proteinPer100g;

    public NutritionMatch(int fdcId, String description, double caloriesPer100g, double proteinPer100g) {
        this.fdcId = fdcId;
        this.description = description;
        this.caloriesPer100g = caloriesPer100g;
        this.proteinPer100g = proteinPer100g;
    }

    public int getFdcId() { return fdcId; }
    public String getDescription() { return description; }
    public double getCaloriesPer100g() { return caloriesPer100g; }
    public double getProteinPer100g() { return proteinPer100g; }

    @Override
    public String toString() {
        return description + " (fdcId " + fdcId + "): " + caloriesPer100g + " kcal/100g, " + proteinPer100g + "g protein/100g";
    }

    public static NutritionMatch lookupByFdcId(int fdcId) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String url = "https://api.nal.usda.gov/fdc/v1/food/" + fdcId + "?api_key=DEMO_KEY";

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode food = mapper.readTree(response.body());

        String description = food.get("description").asText();
        JsonNode nutrients = food.get("foodNutrients");

        double calories = -1;
        double protein = -1;
        for (JsonNode nutrient : nutrients) {
            String nutrientNumber = nutrient.get("nutrientNumber").asText();
            if (nutrientNumber.equals("208")) calories = nutrient.get("value").asDouble();
            else if (nutrientNumber.equals("203")) protein = nutrient.get("value").asDouble();
        }

        return new NutritionMatch(fdcId, description, calories, protein);
    }
}

