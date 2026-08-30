package com.gnien.mealplanner.meal_planner.service;

import com.gnien.mealplanner.meal_planner.dto.NutritionTotals;
import com.gnien.mealplanner.meal_planner.dto.RecipeIngredientRequest;
import com.gnien.mealplanner.meal_planner.dto.RecipeIngredientResponse;
import com.gnien.mealplanner.meal_planner.dto.RecipeRequest;
import com.gnien.mealplanner.meal_planner.dto.RecipeResponse;
import com.gnien.mealplanner.meal_planner.model.Food;
import com.gnien.mealplanner.meal_planner.model.Recipe;
import com.gnien.mealplanner.meal_planner.model.RecipeIngredient;
import com.gnien.mealplanner.meal_planner.repository.RecipeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final FoodCatalogService foodCatalog;

    public RecipeService(RecipeRepository recipeRepository, FoodCatalogService foodCatalog) {
        this.recipeRepository = recipeRepository;
        this.foodCatalog = foodCatalog;
    }

    @Transactional
    public RecipeResponse create(RecipeRequest request) {
        Recipe recipe = new Recipe();
        recipe.setName(request.name());
        applyIngredients(recipe, request.ingredients());
        return toResponse(recipeRepository.save(recipe));
    }

    @Transactional(readOnly = true)
    public List<RecipeResponse> list() {
        return recipeRepository.findAllByOrderByNameAsc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public RecipeResponse get(Long id) {
        return toResponse(load(id));
    }

    @Transactional
    public RecipeResponse replace(Long id, RecipeRequest request) {
        Recipe recipe = load(id);
        recipe.setName(request.name());
        recipe.getIngredients().clear();   // orphanRemoval deletes the old rows
        applyIngredients(recipe, request.ingredients());
        return toResponse(recipeRepository.save(recipe));
    }

    @Transactional
    public void delete(Long id) {
        recipeRepository.delete(load(id));
    }

    private Recipe load(Long id) {
        return recipeRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "No recipe with id " + id));
    }

    private void applyIngredients(Recipe recipe, List<RecipeIngredientRequest> requested) {
        for (RecipeIngredientRequest req : requested) {
            Food food = foodCatalog.resolveFood(req.food());
            RecipeIngredient ingredient = new RecipeIngredient();
            ingredient.setRecipe(recipe);
            ingredient.setFood(food);
            ingredient.setServingSize(req.servingSize());
            recipe.getIngredients().add(ingredient);
        }
    }

    private RecipeResponse toResponse(Recipe recipe) {
        List<RecipeIngredientResponse> ingredients = recipe.getIngredients().stream()
            .map(this::toIngredientResponse)
            .toList();
        NutritionTotals totals = ingredients.stream()
            .map(RecipeIngredientResponse::nutrition)
            .reduce(NutritionTotals.zero(), NutritionTotals::plus)
            .rounded();
        return new RecipeResponse(recipe.getId(), recipe.getName(), ingredients, totals);
    }

    private RecipeIngredientResponse toIngredientResponse(RecipeIngredient ingredient) {
        Food food = ingredient.getFood();
        NutritionTotals nutrition = foodCatalog.macrosOf(food)
            .scale(ingredient.getServingSize())
            .rounded();
        return new RecipeIngredientResponse(
            ingredient.getId(), food.getId(), food.getName(),
            ingredient.getServingSize(), nutrition);
    }
}
