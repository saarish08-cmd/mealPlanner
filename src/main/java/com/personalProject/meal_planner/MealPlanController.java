package com.personalProject.meal_planner;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.PointValuePair;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import jakarta.servlet.http.*;

@RestController
public class MealPlanController {

    //Hardcoded menu items
    private final List<MealItem> menu = List.of(
            new MealItem("Grilled chicken breast", 165, 31, "serving"),
            new MealItem("Jasmine Rice", 205, 4.3, "cup"),
            new MealItem("Roasted vegetables", 80, 3, "cup"),
            new MealItem("Power bowl", 420, 16, "bowl"),
            new MealItem("Greek yogurt parfait", 220, 15, "cup"),
            new MealItem("Turkey chili", 310, 26, "bowl")
    );
    private final ProfileRepository profileRepository;

    public MealPlanController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @PostMapping("/generate-plan")
    public Map<String, Object> generatePlan(@RequestBody MealPlanRequest request, HttpServletResponse httpResponse, HttpServletRequest httpRequest) {
        //Calculate BMR using Mifflin-St Jeor equation
        double bmr = request.getSex().equals("male")
                ? (10 * request.getWeight()) + (6.25 * request.getHeight()) - (5 * request.getAge()) + 5
                : (10 * request.getWeight()) + (6.25 * request.getHeight()) - (5 * request.getAge()) - 161;

        //Apply physical activity multipliers to find total daily energy expenditure
        double activityMultiplier = switch (request.getActivity()) {
            case "sedentary" -> 1.2;
            case "light" -> 1.375;
            case "moderate" -> 1.55;
            case "active" -> 1.725;
            default -> 1.55;
        };

        //Determine final calorie target by adding/subtracting 500kcal
        double baselineCalories = bmr * activityMultiplier;
        double targetCalories = request.getGoal().equals("bulk") ? baselineCalories + 500
                : request.getGoal().equals("cut") ? baselineCalories - 500
                : baselineCalories;

        //Calculate protein target based on goal
        double proteinPerKg = request.getGoal().equals("bulk") ? 1.8 : request.getGoal().equals("cut") ? 2.2 : 1.6;
        double targetProtein = request.getWeight() * proteinPerKg;

        Collection<LinearConstraint> constraints = new ArrayList<>();

        //Constraint: The total protein should land between 100% and 115% of the target
        double[] proteinCoefficients = menu.stream().mapToDouble(MealItem::getProtein).toArray();
        constraints.add(new LinearConstraint(proteinCoefficients, Relationship.GEQ, targetProtein));
        constraints.add(new LinearConstraint(proteinCoefficients, Relationship.LEQ, targetProtein * 2.0));

        //Constraint: The total calories should land between +/- 2% of the target
        double[] calorieCoefficients = menu.stream().mapToDouble(MealItem::getCalories).toArray();
        constraints.add(new LinearConstraint(calorieCoefficients, Relationship.LEQ, targetCalories * 1.02));
        constraints.add(new LinearConstraint(calorieCoefficients, Relationship.GEQ, targetCalories * 0.98));

        // Constraint: Cap each item to a maximum of 2.5 portions to ensure variety
        for (int i = 0; i < menu.size(); i++) {
            double[] cap = new double[menu.size()];
            cap[i] = 1;
            constraints.add(new LinearConstraint(cap, Relationship.LEQ, 2.5));
        }

        //Minimize total portion volume
        double[] portionCoefficients = new double[menu.size()];
        Arrays.fill(portionCoefficients, 1.0);

        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction objective = new LinearObjectiveFunction(portionCoefficients, 0);
        PointValuePair solution = solver.optimize(objective, new LinearConstraintSet(constraints), GoalType.MINIMIZE, new NonNegativeConstraint(true));

        //Filter out unused items and print used items onto the frontend
        List<Map<String, Object>> pickedItems = new ArrayList<>();
        double totalProtein = 0;
        double totalCalories = 0;
        for (int i = 0; i < menu.size(); i++) {
            double portion = solution.getPoint()[i];
            if (portion > 0.01) {
                double roundedPortion = Math.round(portion*100.0)/100.0;
                Map<String, Object> item = new HashMap<>();
                item.put("name", menu.get(i).getName());
                item.put("unit", menu.get(i).getUnit());
                item.put("portions", roundedPortion);
                pickedItems.add(item);
                totalProtein += proteinCoefficients[i] * portion;
                totalCalories += calorieCoefficients[i] * portion;
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("items", pickedItems);
        response.put("totalCalories", Math.round(totalCalories * 10.0) / 10.0);
        response.put("totalProtein", Math.round(totalProtein * 10.0) / 10.0);

        //Cookie and Database logic
        String cookieId = null;
        //Read Cookie:
        if(httpRequest.getCookies() != null){ //The request might not have any cookies, so check for null
            for(Cookie cookie : httpRequest.getCookies()){//Loop through all the cookies sent by the browser
                if(cookie.getName().equals("profileId"))//When smth named "profileId" is found, save its value
                    cookieId = cookie.getValue();
            }
        }

        //Generate a cookie for new user:
        if(cookieId != null){
            cookieId = UUID.randomUUID().toString();//Generates a random string
            Cookie newCookie = new Cookie("profileId", cookieId);
            newCookie.setMaxAge(60*60*24*365); //Cookie expires in 1 year
            newCookie.setPath("/"); //Makes the cookie valid across the website
            httpResponse.addCookie(newCookie); //Attaches the cookie to user's browser
        }

        // Fetch existing profile or create a new one:
        List<Profile> existingProfiles = profileRepository.findByCookieId(cookieId); //Check the database for is the cookie already exists
        Profile profile = existingProfiles.isEmpty() ? new Profile() : existingProfiles.get(0); //If not, create a new profile with that cookie

        // Add current stats to profile
        profile.setCookieId(cookieId);
        profile.setWeight(request.getWeight());
        profile.setHeight(request.getHeight());
        profile.setAge(request.getAge());
        profile.setSex(request.getSex());
        profile.setActivity(request.getActivity());
        profile.setGoal(request.getGoal());

        // Save to database
        profileRepository.save(profile);

        return response;
    }

    @GetMapping("/profile")
    public Profile loadProfile(HttpServletRequest httpRequest){
        //Check for cookies sent by the browser
        String cookieId = null;
        if(httpRequest.getCookies() != null){
            for(Cookie cookie : httpRequest.getCookies()){
                if(cookie.getName().equals("profileId"))
                    cookieId = cookie.getValue();
            }
        }

        //If a cookie exists, ask the database for their saved stats
        if(cookieId != null){
            List<Profile> existingProfiles = profileRepository.findByCookieId(cookieId);
            //If the data is found, send it to frontend
            if(!existingProfiles.isEmpty()){
                return existingProfiles.get(0);
            }
        }

        //If no cookie is found or database is empty, return null
        return null;
    }
}