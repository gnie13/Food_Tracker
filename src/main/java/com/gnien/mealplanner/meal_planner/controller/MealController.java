package com.gnien.mealplanner.meal_planner.controller;

import com.gnien.mealplanner.meal_planner.dto.LogEntryRequest;
import com.gnien.mealplanner.meal_planner.dto.MealResponse;
import com.gnien.mealplanner.meal_planner.dto.UpdateEntryRequest;
import com.gnien.mealplanner.meal_planner.service.MealService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/meals")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    /** Log a food against a meal. Returns the whole meal with updated totals. */
    @PostMapping("/entries")
    @ResponseStatus(HttpStatus.CREATED)
    public MealResponse logEntry(@Valid @RequestBody LogEntryRequest request) {
        return mealService.logEntry(request);
    }

    /** All meals logged on a date, e.g. {@code /api/meals?date=2026-08-29}. */
    @GetMapping
    public List<MealResponse> meals(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return mealService.mealsForDate(date);
    }

    /**
     * Partially update a logged entry — change its serving size and/or move it
     * to another date or meal type. Returns the meal the entry now belongs to.
     */
    @PatchMapping("/entries/{id}")
    public MealResponse updateEntry(
        @PathVariable Long id, @Valid @RequestBody UpdateEntryRequest request) {
        return mealService.updateEntry(id, request);
    }

    @DeleteMapping("/entries/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEntry(@PathVariable Long id) {
        mealService.deleteEntry(id);
    }
}
