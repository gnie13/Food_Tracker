package com.gnien.mealplanner.meal_planner.service;

import com.gnien.mealplanner.meal_planner.dto.DailySummary;
import com.gnien.mealplanner.meal_planner.dto.LogEntryRequest;
import com.gnien.mealplanner.meal_planner.dto.LogRecipeRequest;
import com.gnien.mealplanner.meal_planner.dto.MealEntryResponse;
import com.gnien.mealplanner.meal_planner.dto.MealResponse;
import com.gnien.mealplanner.meal_planner.dto.StoredFoodResponse;
import com.gnien.mealplanner.meal_planner.dto.NutritionTotals;
import com.gnien.mealplanner.meal_planner.dto.RangeSummary;
import com.gnien.mealplanner.meal_planner.dto.UpdateEntryRequest;
import com.gnien.mealplanner.meal_planner.model.Food;
import com.gnien.mealplanner.meal_planner.model.Meal;
import com.gnien.mealplanner.meal_planner.model.MealEntry;
import com.gnien.mealplanner.meal_planner.model.Recipe;
import com.gnien.mealplanner.meal_planner.model.RecipeIngredient;
import com.gnien.mealplanner.meal_planner.repository.FoodRepository;
import com.gnien.mealplanner.meal_planner.repository.MealEntryRepository;
import com.gnien.mealplanner.meal_planner.repository.MealRepository;
import com.gnien.mealplanner.meal_planner.repository.RecipeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

@Service
public class MealService {

    /** Guard against an accidental multi-year range that would run a DB query per day. */
    private static final long MAX_RANGE_DAYS = 366;

    /** Hard ceiling on the "frequently added" list, whatever limit the caller asks for. */
    private static final int MAX_FREQUENT = 50;

    private final MealRepository mealRepository;
    private final MealEntryRepository mealEntryRepository;
    private final FoodRepository foodRepository;
    private final RecipeRepository recipeRepository;
    private final FoodCatalogService foodCatalog;

    public MealService(MealRepository mealRepository,
                       MealEntryRepository mealEntryRepository,
                       FoodRepository foodRepository,
                       RecipeRepository recipeRepository,
                       FoodCatalogService foodCatalog) {
        this.mealRepository = mealRepository;
        this.mealEntryRepository = mealEntryRepository;
        this.foodRepository = foodRepository;
        this.recipeRepository = recipeRepository;
        this.foodCatalog = foodCatalog;
    }

    /**
     * Log a food against the meal for a given date + meal type, creating the
     * meal (and persisting the food) on first use.
     */
    @Transactional
    public MealResponse logEntry(LogEntryRequest request) {
        Meal meal = mealRepository
            .findByDateAndMealType(request.date(), request.mealType())
            .orElseGet(() -> {
                Meal fresh = new Meal();
                fresh.setDate(request.date());
                fresh.setMealType(request.mealType());
                return mealRepository.save(fresh);
            });

        Food food = foodCatalog.resolveFood(request.food());
        foodCatalog.recordUsage(food);

        addEntry(meal, food, request.servingSize());
        return toResponse(meal);
    }

    /**
     * Log a whole recipe against a meal, one entry per ingredient with its
     * serving size scaled by the request's factor. The meal is created on first
     * use, and every ingredient counts towards the frequently-added list.
     */
    @Transactional
    public MealResponse logRecipe(LogRecipeRequest request) {
        Recipe recipe = recipeRepository.findById(request.recipeId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "No recipe with id " + request.recipeId()));

        Meal meal = mealRepository
            .findByDateAndMealType(request.date(), request.mealType())
            .orElseGet(() -> {
                Meal fresh = new Meal();
                fresh.setDate(request.date());
                fresh.setMealType(request.mealType());
                return mealRepository.save(fresh);
            });

        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            Food food = ingredient.getFood();
            foodCatalog.recordUsage(food);
            addEntry(meal, food, ingredient.getServingSize() * request.factor());
        }
        return toResponse(meal);
    }

    private void addEntry(Meal meal, Food food, double servingSize) {
        MealEntry entry = new MealEntry();
        entry.setMeal(meal);
        entry.setFood(food);
        entry.setServingSize(servingSize);
        meal.getEntries().add(entry);
        mealEntryRepository.save(entry);
    }

    /**
     * Foods the user logs most often, most-used first (ties broken by recency).
     * With a {@code mealType} the ranking and counts are scoped to that meal;
     * without one they cover every meal.
     */
    @Transactional(readOnly = true)
    public List<StoredFoodResponse> frequentFoods(Meal.MealType mealType, int limit) {
        int capped = Math.clamp(limit, 1, MAX_FREQUENT);
        if (mealType != null) {
            return mealEntryRepository.frequencyByMealType(mealType, PageRequest.of(0, capped)).stream()
                .map(row -> toStoredFoodResponse(
                    row.getFood(), (int) row.getTimesLogged(), atStartOfDay(row.getLastDate())))
                .toList();
        }
        return foodRepository
            .findByTimesLoggedGreaterThanOrderByTimesLoggedDescLastLoggedAtDesc(
                0, PageRequest.of(0, capped))
            .stream()
            .map(f -> toStoredFoodResponse(f, f.getTimesLogged(), f.getLastLoggedAt()))
            .toList();
    }

    private static Instant atStartOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    /** Foods the user has explicitly pinned, by name. */
    @Transactional(readOnly = true)
    public List<StoredFoodResponse> savedFoods() {
        return foodRepository.findBySavedTrueOrderByNameAsc().stream()
            .map(f -> toStoredFoodResponse(f, f.getTimesLogged(), f.getLastLoggedAt()))
            .toList();
    }

    /** Pin or unpin a stored food. */
    @Transactional
    public StoredFoodResponse setSaved(Long foodId, boolean saved) {
        Food food = foodRepository.findById(foodId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "No food with id " + foodId));
        food.setSaved(saved);
        Food stored = foodRepository.save(food);
        return toStoredFoodResponse(stored, stored.getTimesLogged(), stored.getLastLoggedAt());
    }

    private static StoredFoodResponse toStoredFoodResponse(Food f, int timesLogged, Instant lastLoggedAt) {
        return new StoredFoodResponse(
            f.getId(), f.getFdcId(), f.getName(),
            f.getCalories(), f.getProtein(), f.getCarbs(), f.getFat(),
            f.getServingGrams(), f.getServingText(),
            timesLogged, lastLoggedAt, f.isSaved());
    }

    @Transactional(readOnly = true)
    public List<MealResponse> mealsForDate(LocalDate date) {
        return mealRepository.findByDate(date).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public DailySummary summaryForDate(LocalDate date) {
        List<MealResponse> meals = mealsForDate(date);
        NutritionTotals totals = meals.stream()
            .map(MealResponse::subtotal)
            .reduce(NutritionTotals.zero(), NutritionTotals::plus)
            .rounded();
        return new DailySummary(date, totals, meals);
    }

    @Transactional(readOnly = true)
    public RangeSummary summaryForRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "endDate must not be before startDate");
        }
        long span = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (span > MAX_RANGE_DAYS) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "date range must not exceed " + MAX_RANGE_DAYS + " days");
        }
        List<DailySummary> days = Stream.iterate(startDate, d -> d.plusDays(1))
            .limit(span)
            .map(this::summaryForDate)
            .toList();
        NutritionTotals totals = days.stream()
            .map(DailySummary::totals)
            .reduce(NutritionTotals.zero(), NutritionTotals::plus)
            .rounded();
        return new RangeSummary(startDate, endDate, totals, days);
    }

    /**
     * Apply a partial update to a logged entry. A non-null {@code date} or
     * {@code mealType} moves the entry to the matching meal, creating that meal
     * on demand. The old meal is left in place even if it ends up empty — the
     * same way {@link #deleteEntry} leaves empty meals behind.
     */
    @Transactional
    public MealResponse updateEntry(Long entryId, UpdateEntryRequest request) {
        if (request.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "update request must set at least one field");
        }

        MealEntry entry = mealEntryRepository.findById(entryId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "No meal entry with id " + entryId));

        if (request.servingSize() != null) {
            entry.setServingSize(request.servingSize());
        }

        Meal current = entry.getMeal();
        LocalDate targetDate = request.date() != null ? request.date() : current.getDate();
        Meal.MealType targetType =
            request.mealType() != null ? request.mealType() : current.getMealType();

        if (targetDate.equals(current.getDate()) && targetType == current.getMealType()) {
            return toResponse(current);
        }

        Meal target = mealRepository.findByDateAndMealType(targetDate, targetType)
            .orElseGet(() -> {
                Meal fresh = new Meal();
                fresh.setDate(targetDate);
                fresh.setMealType(targetType);
                return mealRepository.save(fresh);
            });

        // Move via the owning side (MealEntry.meal) only. Removing the entry from
        // current.getEntries() would trip orphanRemoval and delete the row.
        entry.setMeal(target);
        target.getEntries().add(entry);
        mealEntryRepository.save(entry);

        return toResponse(target);
    }

    @Transactional
    public void deleteEntry(Long entryId) {
        MealEntry entry = mealEntryRepository.findById(entryId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "No meal entry with id " + entryId));
        // orphanRemoval on Meal.entries handles the delete once it's detached here
        entry.getMeal().getEntries().remove(entry);
        mealEntryRepository.delete(entry);
    }

    private MealResponse toResponse(Meal meal) {
        List<MealEntryResponse> entries = meal.getEntries().stream()
            .map(this::toEntryResponse)
            .toList();
        NutritionTotals subtotal = entries.stream()
            .map(MealEntryResponse::nutrition)
            .reduce(NutritionTotals.zero(), NutritionTotals::plus)
            .rounded();
        return new MealResponse(meal.getId(), meal.getDate(), meal.getMealType(), entries, subtotal);
    }

    private MealEntryResponse toEntryResponse(MealEntry entry) {
        Food food = entry.getFood();
        NutritionTotals nutrition = foodCatalog.macrosOf(food)
            .scale(entry.getServingSize())
            .rounded();
        return new MealEntryResponse(
            entry.getId(), food.getId(), food.getName(),
            entry.getServingSize(), gramsOf(entry.getServingSize()),
            food.getServingGrams(), food.getServingText(), nutrition);
    }

    /** A serving-size multiplier is "hundreds of grams" (macros are per 100 g). */
    static double gramsOf(double servingSize) {
        return Math.round(servingSize * 100.0 * 10.0) / 10.0;
    }
}
