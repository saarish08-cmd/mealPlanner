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
        double weight = 70;
        String activity = "moderate";
        String goal = "maintain";
        double activityMultiplier = activity.equals("sedentary") ? 28 : activity.equals("active") ? 38 : 33;
        double baselineCalories = weight * activityMultiplier;
        double targetCalories = goal.equals("bulk") ? baselineCalories * 1.15 : goal.equals("cut") ? baselineCalories * 0.85 : baselineCalories;
        double targetProtein = weight * (goal.equals("maintain") ? 1.6 : 2.0);

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
