# Meal Planner (Discontinued)

A Java / Spring Boot tool that was meant to build dining hall meal plans for University of Waterloo residence students, hitting a calorie and protein target using the real daily menu.

**Status: discontinued, October 2026.** The code is published as a record of the work and what I learned. **Please do not use it to make health or diet decisions.** The reasons are below.

## What it was meant to do

1. Scrape today's menu from the UW Food Services site.
2. Work out calories and protein for each dish.
3. Take a person's goal (bulk, maintain, cut) and stats, then use linear programming to pick portions of today's dishes that hit their targets.

## What works

- **Solver:** Apache Commons Math `SimplexSolver` solving the "diet problem", with Mifflin-St Jeor BMR, a 5-tier activity multiplier, and ISSN-based protein targets by goal. A bug where the solver returned negative portions was found and fixed.
- **Persistent profiles:** cookie-based, using JPA and H2.
- **Menu scraper:** Jsoup, grouping dishes by venue and category.
- **Nutrition pipeline:** for each dish, Gemini lists likely ingredients and gram weights. Each ingredient is then matched against USDA FoodData Central (Foundation and SR Legacy data, per 100 g) and summed.
- **Caching and API:** results are cached per dish, and a REST endpoint (`/api/menu`) serves the nutrition-tagged menu.

The solver was never connected to the real scraped menu. It still runs on a small hardcoded sample menu.

## Why it was discontinued

### 1. The health risk is real
The tool outputs specific calorie and protein numbers and tells someone how much to eat. If those numbers are wrong, the user acts on them. Leaving out medical conditions from scope was a start, but I cannot verify who is using the tool or why. Cutting targets, restrictive eating, and allergies are all places where confident but wrong numbers can do harm. Disclaimers do not fix this.

### 2. The nutrition numbers are estimates, not data
The dining halls do not publish recipes, portion sizes, or nutrition facts. Everything past the dish name is inferred:
- The ingredient list comes from an AI model guessing what a dish contains.
- The gram weights come from the same guess.
- The matches to USDA foods are an automated best guess.

Calling a number "calculated" hides that it comes from guesses stacked on guesses.

### 3. The estimates were unstable
Running the same dish repeatedly gave very different totals. Early test of "Cajun Shrimp Pasta" ranged from about 800 to 1,160 kcal across multiple runs. Prompt changes (separate ingredients, cooked weights, one cafeteria portion, lower temperature) narrowed this to roughly 10%, but the gap never closed. Caching hides the variance after the first run, but it also locks in whichever guess happened to come first.

### 4. Ingredient matching kept producing wrong results
- "bell peppers" matched a Taco Bell nachos entry, because of the shared word "bell".
- "cajun cream sauce" matched "Guava sauce, cooked".
- "pasta" sometimes matched dry pasta and sometimes cooked pasta, which is roughly a 2x calorie difference per 100 g.
- Brand and restaurant items turn up inside the "SR Legacy" data.

Each fix (brand filter, skipping seasonings, forcing cooked weights) handled one case. There is no automatic way to catch the next one.

### 5. No way to check the answers
There is no source of correct values for these dishes, and no feedback loop. A silently wrong number looks the same as a right one.

### 6. Unresolved real-world unknowns
- Breakfast does not appear on the daily menu page.
- The menu does not say which items are served at lunch or dinner.
- Portion size depends on the scoop, the server, and the student.

### 7. It broke its own design rule
The project's main idea was a deterministic solver where AI never decides a number that matters. In practice the numbers that matter most (what is in the dish and how much) ended up decided by AI. The solver was exact, but its inputs were not.

### 8. Fragile dependencies
- The scraper depends on UW's page structure and URLs. The URL format changed between my first build and the final test, and the scraper silently returned wrong data until I found it.
- The free tiers (Gemini, USDA demo key) have rate limits. Paid alternatives like Edamam charge ongoing per-recipe licensing fees, which does not suit a free student project.

## What I learned

- Parsing real HTML with Jsoup, and finding selectors by inspecting the live page.
- Calling REST APIs from Java and parsing JSON with Jackson. This includes the Jackson 3 move from `com.fasterxml` to `tools.jackson`.
- Breaking a problem into pieces (dish to ingredients to nutrition) and caching expensive results.
- Checking assumptions before building on them. Several of my bugs came from trusting something I had not verified.
- Knowing when to stop. A tool can be technically working and still not be safe to give people.

## What could make it viable

- Nutrition and portion data published by the dining service itself.
- A measured data source I could validate against.
- Showing ranges instead of single numbers, with plain labels for estimated values.
- Limiting it to menu browsing (what is served where) without health targets.

## Running it (for reference only)

Requires Java 25 and Maven. Set environment variables `GEMINI_API_KEY` and `USDA_API_KEY`. Keys are never committed to this repository.

The menu scraper, nutrition pipeline, and API can each be run on their own. Treat all output as unverified.
