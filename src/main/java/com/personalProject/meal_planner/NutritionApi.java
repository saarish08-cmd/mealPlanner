package com.personalProject.meal_planner;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class NutritionApi {
    public static void main(String[] args) throws Exception{ //Covers both IOException and Interrupted Exception
        HttpClient client = HttpClient.newHttpClient(); //Sends the request over the internet like Jsoup
        HttpRequest request = HttpRequest.newBuilder() //Builds the request that the client sends like Jsoup.contruct()
                .uri(URI.create("https://api.nal.usda.gov/fdc/v1/foods/search?api_key=DEMO_KEY&query=banana"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
    }
}
