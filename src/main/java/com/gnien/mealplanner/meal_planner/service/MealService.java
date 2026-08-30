package com.gnien.mealplanner.meal_planner.service;

import com.gnien.mealplanner.meal_planner.dto.DailySummary;
import com.gnien.mealplanner.meal_planner.dto.FoodPayload;
import com.gnien.mealplanner.meal_planner.dto.LogEntryRequest;
import com.gnien.mealplanner.meal_planner.dto.MealEntryResponse;
import com.gnien.mealplanner.meal_planner.dto.MealResponse;
import com.gnien.mealplanner.meal_planner.dto.NutritionTotals;
import com.gnien.mealplanner.meal_planner.dto.RangeSummary;
import com.gnien.mealplanner.meal_planner.dto.UpdateEntryRequest;
import com.gnien.mealplanner.meal_planner.model.Food;
import com.gnien.mealplanner.meal_planner.model.Meal;
import com.gnien.mealplanner.meal_planner.model.MealEntry;
import com.gnien.mealplanner.meal_planner.repository.FoodRepository;
import com.gnien.mealplanner.meal_planner.repository.MealEntryRepository;
import com.gnien.mealplanner.meal_planner.repository.MealRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

@Service
public class MealService {

    /** Guard against an accidental multi-year range that would run a DB query per day. */
    private static final long MAX_RANGE_DAYS = 366;

    private final MealRepository mealRepository;
    private final MealEntryRepository mealEntryRepository;
    private final FoodRepository foodRepository;

    public MealService(MealRepository mealRepository,
                       MealEntryRepository mealEntryRepository,
                       FoodRepository foodRepository) {
        this.mealRepository = mealRepository;
        this.mealEntryRepository = mealEntryRepository;
        this.foodRepository = foodRepository;
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

        Food food = resolveFood(request.food());

        MealEntry entry = new MealEntry();
        entry.setMeal(meal);
        entry.setFood(food);
        entry.setServingSize(request.servingSize());
        meal.getEntries().add(entry);
        mealEntryRepository.save(entry);

        return toResponse(meal);
    }

    /** Reuse an already-stored food by its USDA id, otherwise save the payload. */
    private Food resolveFood(FoodPayload payload) {
        return foodRepository.findByFdcId(payload.fdcId())
            .orElseGet(() -> {
                Food food = new Food();
                food.setFdcId(payload.fdcId());
                food.setName(payload.name());
                food.setCalories(payload.calories());
                food.setProtein(payload.protein());
                food.setCarbs(payload.carbs());
                food.setFat(payload.fat());
                return foodRepository.save(food);
            });
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
        NutritionTotals nutrition = new NutritionTotals(
            food.getCalories(), food.getProtein(), food.getCarbs(), food.getFat()
        ).scale(entry.getServingSize()).rounded();
        return new MealEntryResponse(
            entry.getId(), food.getId(), food.getName(), entry.getServingSize(), nutrition);
    }
}
