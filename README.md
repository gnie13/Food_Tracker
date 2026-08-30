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

## API

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/foods/search?query=` | Search USDA FoodData Central |
| GET | `/api/foods/frequent?limit=` | Foods logged most often (default 10, max 50), for quick re-adding |
| GET | `/api/foods/saved` | Foods you've pinned, alphabetised |
| PUT | `/api/foods/{id}/saved` | Pin a stored food (idempotent) |
| DELETE | `/api/foods/{id}/saved` | Unpin a stored food (idempotent) |
| POST | `/api/meals/entries` | Log a food against a meal (see body below) |
| GET | `/api/meals?date=YYYY-MM-DD` | Meals logged on a date, with subtotals |
| DELETE | `/api/meals/entries/{id}` | Remove a logged entry |
| GET | `/api/summary?date=YYYY-MM-DD` | Daily macro totals + per-meal breakdown |
| GET | `/api/summary/range?startDate=&endDate=` | Totals across a date range, one entry per day (weekly view) |

`POST /api/meals/entries` body — `food` is normally copied straight from a search result:

```json
{
  "date": "2026-08-29",
  "mealType": "BREAKFAST",
  "servingSize": 1.5,
  "food": { "fdcId": 173904, "name": "Oatmeal, dry", "calories": 380, "protein": 13, "carbs": 67, "fat": 7 }
}
```

`servingSize` is a multiplier on the food's stored macros (1.5 = one and a half of whatever USDA reported).

## Status

Food search, meal logging, and daily/weekly nutrition summaries all work. H2 is file-backed
(`./data/`), so logged meals survive restarts. Still missing a frontend.
