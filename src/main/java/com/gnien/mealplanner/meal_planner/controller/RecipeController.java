package com.gnien.mealplanner.meal_planner.controller;

import com.gnien.mealplanner.meal_planner.dto.RecipeRequest;
import com.gnien.mealplanner.meal_planner.dto.RecipeResponse;
import com.gnien.mealplanner.meal_planner.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Saved recipes — named bundles of foods the user logs in one go. */
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecipeResponse create(@Valid @RequestBody RecipeRequest request) {
        return recipeService.create(request);
    }

    @GetMapping
    public List<RecipeResponse> list() {
        return recipeService.list();
    }

    @GetMapping("/{id}")
    public RecipeResponse get(@PathVariable Long id) {
        return recipeService.get(id);
    }

    @PutMapping("/{id}")
    public RecipeResponse replace(@PathVariable Long id, @Valid @RequestBody RecipeRequest request) {
        return recipeService.replace(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        recipeService.delete(id);
    }
}
