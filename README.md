# Meal Planner

A Spring Boot app for logging meals and tracking nutrition. Still a work in progress.

Searches food items through the USDA FoodData Central API (calories, protein, carbs, fat pulled straight from their database instead of hand-entering nutrition info), and lets you log them against meals — breakfast/lunch/dinner/snack — by date. Backed by Spring Data JPA with an H2 database.

## Stack
- Java 25 / Spring Boot
- Spring Data JPA + H2
- Spring RestClient for the USDA API calls
- Lombok

## Running it

You'll need a free API key from [api.data.gov](https://api.data.gov/signup/) for USDA FoodData Central.

1. Copy `src/main/resources/application.properties.example` to `application.properties`
2. Drop your API key in
3. `./mvnw spring-boot:run`

## Status

Core food search + meal logging works. Still missing a frontend and daily/weekly nutrition summaries.
