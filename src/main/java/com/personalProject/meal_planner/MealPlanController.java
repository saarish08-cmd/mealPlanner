package com.personalProject.meal_planner;

import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.PointValuePair;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class MealPlanController {

    private final List<MealItem> menu = List.of(
            new MealItem("Grilled chicken breast", 165, 31, "serving"),
            new MealItem("Jasmine Rice", 205, 4.3, "cup"),
            new MealItem("Roasted vegetables", 80, 3, "cup"),
            new MealItem("Power bowl", 420, 16, "bowl"),
            new MealItem("Greek yogurt parfait", 220, 15, "cup"),
            new MealItem("Turkey chili", 310, 26, "bowl")
    );

    @PostMapping("/generate-plan")
    public Map<String, Object> generatePlan(@RequestBody MealPlanRequest request) {

        double bmr = request.getSex().equals("male")
                ? (10 * request.getWeight()) + (6.25 * request.getHeight()) - (5 * request.getAge()) + 5
                : (10 * request.getWeight()) + (6.25 * request.getHeight()) - (5 * request.getAge()) - 161;

        double activityMultiplier = switch (request.getActivity()) {
            case "sedentary" -> 1.2;
            case "light" -> 1.375;
            case "moderate" -> 1.55;
            case "active" -> 1.725;
            default -> 1.55;
        };

        double baselineCalories = bmr * activityMultiplier;
        double targetCalories = request.getGoal().equals("bulk") ? baselineCalories + 500
                : request.getGoal().equals("cut") ? baselineCalories - 500
                : baselineCalories;

        double proteinPerKg = request.getGoal().equals("bulk") ? 1.8 : request.getGoal().equals("cut") ? 2.2 : 1.6;
        double targetProtein = request.getWeight() * proteinPerKg;

        Collection<LinearConstraint> constraints = new ArrayList<>();
        double[] proteinCoefficients = menu.stream().mapToDouble(MealItem::getProtein).toArray();
        constraints.add(new LinearConstraint(proteinCoefficients, Relationship.GEQ, targetProtein));
        constraints.add(new LinearConstraint(proteinCoefficients, Relationship.LEQ, targetProtein * 1.15));

        double[] calorieCoefficients = menu.stream().mapToDouble(MealItem::getCalories).toArray();
        constraints.add(new LinearConstraint(calorieCoefficients, Relationship.LEQ, targetCalories * 1.05));
        constraints.add(new LinearConstraint(calorieCoefficients, Relationship.GEQ, targetCalories * 0.90));

        for (int i = 0; i < menu.size(); i++) {
            double[] cap = new double[menu.size()];
            cap[i] = 1;
            constraints.add(new LinearConstraint(cap, Relationship.LEQ, 2.5));
        }

        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction objective = new LinearObjectiveFunction(calorieCoefficients, 0);
        PointValuePair solution = solver.optimize(objective, new LinearConstraintSet(constraints), GoalType.MINIMIZE);

        List<Map<String, Object>> pickedItems = new ArrayList<>();
        double totalProtein = 0;
        for (int i = 0; i < menu.size(); i++) {
            if (solution.getPoint()[i] > 0.01) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", menu.get(i).getName());
                item.put("unit", menu.get(i).getUnit());
                item.put("portions", solution.getPoint()[i]);
                pickedItems.add(item);
                totalProtein += proteinCoefficients[i] * solution.getPoint()[i];
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("items", pickedItems);
        response.put("totalCalories", solution.getValue());
        response.put("totalProtein", totalProtein);
        return response;
    }
}