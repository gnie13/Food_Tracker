package com.gnien.mealplanner.meal_planner.controller;

import com.gnien.mealplanner.meal_planner.dto.DailySummary;
import com.gnien.mealplanner.meal_planner.dto.RangeSummary;
import com.gnien.mealplanner.meal_planner.service.MealService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/summary")
public class SummaryController {

    private final MealService mealService;

    public SummaryController(MealService mealService) {
        this.mealService = mealService;
    }

    /** Daily macro totals + per-meal breakdown, e.g. {@code /api/summary?date=2026-08-29}. */
    @GetMapping
    public DailySummary daily(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return mealService.summaryForDate(date);
    }

    /**
     * Totals across an inclusive date range, one day per entry — use it for a
     * weekly view, e.g. {@code /api/summary/range?startDate=2026-08-24&endDate=2026-08-30}.
     */
    @GetMapping("/range")
    public RangeSummary range(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return mealService.summaryForRange(startDate, endDate);
    }
}
