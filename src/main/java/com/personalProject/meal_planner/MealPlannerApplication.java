package com.personalProject.meal_planner;

import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.commons.math3.optim.*;

import java.awt.*;
import java.util.*;
import java.util.List;

@SpringBootApplication
public class MealPlannerApplication {

	public static void main(String[] args) {
        SpringApplication.run(MealPlannerApplication.class, args);
        //Small hardcoded menu for testing
        List<MealItem> menu = List.of(
                new MealItem("Grilled chicken breast", 165, 31),
                new MealItem("Jasmine Rice", 205, 4.3),
                new MealItem("Roasted vegetables", 80, 3),
                new MealItem("Power bowl", 420, 16),
                new MealItem("Greek yogurt parfait", 220, 15),
                new MealItem("Turkey chili", 310, 26));

        //Hardcoded person stats
        double weight = 70;   // kg
        double height = 178;  // cm
        int age = 19;
        String sex = "male";  // required input for the formula, same as any real fitness app asks
        String activity = "moderate";
        String goal = "maintain";

        double bmr = sex.equals("male")
                ? (10 * weight) + (6.25 * height) - (5 * age) + 5
                : (10 * weight) + (6.25 * height) - (5 * age) - 161;

        double activityMultiplier = switch (activity) {
            case "sedentary" -> 1.2;
            case "light" -> 1.375;
            case "moderate" -> 1.55;
            case "active" -> 1.725;
            default -> 1.55;
        };

        double baselineCalories = bmr * activityMultiplier;
        double targetCalories = goal.equals("bulk") ? baselineCalories + 500
                : goal.equals("cut") ? baselineCalories - 500
                : baselineCalories;

        double proteinPerKg = goal.equals("bulk") ? 1.8 : goal.equals("cut") ? 2.2 : 1.6;
        double targetProtein = weight * proteinPerKg;

        //Maximize total protein
        Collection<LinearConstraint> constraints = new ArrayList<>();
        double[] proteinCoefficients = menu.stream().mapToDouble(MealItem::getProtein).toArray();
        constraints.add(new LinearConstraint(proteinCoefficients, Relationship.GEQ, targetProtein));
        constraints.add(new LinearConstraint(proteinCoefficients, Relationship.LEQ, targetProtein * 1.15));

        //Calorie constraint
        double[] calorieCoefficients = menu.stream().mapToDouble(MealItem::getCalories).toArray();
        constraints.add(new LinearConstraint(calorieCoefficients, Relationship.LEQ, targetCalories * 1.05));
        constraints.add(new LinearConstraint(calorieCoefficients, Relationship.GEQ, targetCalories * 0.90));

        //Add a portion cap on items to assure calories are maintained
        for (int i = 0; i < menu.size(); i++) {
            double[] cap = new double[menu.size()];
            cap[i] = 1;
            constraints.add(new LinearConstraint(cap, Relationship.LEQ, 2.5));
        }

		SimplexSolver solver = new SimplexSolver();
		LinearObjectiveFunction objectiveC = new LinearObjectiveFunction(calorieCoefficients, 0);
		PointValuePair solution = solver.optimize(objectiveC, new LinearConstraintSet(constraints), GoalType.MINIMIZE);

		double totalProtein = 0;
		for (int i = 0; i < menu.size(); i++) {
			if (solution.getPoint()[i] > 0.01) {
				System.out.println(menu.get(i).getName() + ": " + solution.getPoint()[i]);
				totalProtein += proteinCoefficients[i] * solution.getPoint()[i];
			}
		}
		System.out.println("Total calories: " + solution.getValue());
		System.out.println("Total protein: " + totalProtein);
    }

}
