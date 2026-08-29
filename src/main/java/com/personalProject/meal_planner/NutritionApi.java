package com.personalProject.meal_planner;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;

public class NutritionApi {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.nal.usda.gov/fdc/v1/foods/search?api_key=DEMO_KEY&query=banana&dataType=Foundation,SR%20Legacy"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        JsonNode foods = root.get("foods");

        for (JsonNode food : foods) {
            String description = food.get("description").asText();
            int fdcId = food.get("fdcId").asInt();
            String dataType = food.get("dataType").asText();
            System.out.println(fdcId + " | " + dataType + " | " + description);
        }
    }
}