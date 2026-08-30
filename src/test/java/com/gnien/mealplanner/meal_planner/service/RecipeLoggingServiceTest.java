package com.gnien.mealplanner.meal_planner.service;

import com.gnien.mealplanner.meal_planner.dto.FoodPayload;
import com.gnien.mealplanner.meal_planner.dto.LogRecipeRequest;
import com.gnien.mealplanner.meal_planner.dto.MealResponse;
import com.gnien.mealplanner.meal_planner.dto.RecipeIngredientRequest;
import com.gnien.mealplanner.meal_planner.dto.RecipeRequest;
import com.gnien.mealplanner.meal_planner.model.Meal;
import com.gnien.mealplanner.meal_planner.repository.FoodRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({MealService.class, RecipeService.class, FoodCatalogService.class})
class RecipeLoggingServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 8, 30);

    @Autowired MealService mealService;
    @Autowired RecipeService recipeService;
    @Autowired FoodRepository foodRepository;

    private Long porridgeRecipe() {
        return recipeService.create(new RecipeRequest("Porridge", List.of(
            new RecipeIngredientRequest(new FoodPayload(1L, "Oats", 100.0, 10.0, 20.0, 5.0), 2.0),
            new RecipeIngredientRequest(new FoodPayload(2L, "Milk", 60.0, 3.0, 5.0, 3.0), 1.0)))).id();
    }

    @Test
    void logRecipeExpandsToOneScaledEntryPerIngredient() {
        Long recipeId = porridgeRecipe();

        MealResponse meal = mealService.logRecipe(
            new LogRecipeRequest(DAY, Meal.MealType.BREAKFAST, recipeId, 2.0));

        assertThat(meal.entries()).hasSize(2);
        assertThat(meal.entries()).extracting("servingSize")
            .containsExactlyInAnyOrder(4.0, 2.0);   // base 2.0 and 1.0, times factor 2
        // Oats 4*(100,10,..) + Milk 2*(60,3,..) = 400 + 120
        assertThat(meal.subtotal().calories()).isEqualTo(520.0);
    }

    @Test
    void logRecipeCountsEveryIngredientTowardsFrequentFoods() {
        mealService.logRecipe(new LogRecipeRequest(
            DAY, Meal.MealType.BREAKFAST, porridgeRecipe(), 1.0));

        assertThat(foodRepository.findByFdcId(1L)).get()
            .satisfies(f -> assertThat(f.getTimesLogged()).isEqualTo(1));
        assertThat(foodRepository.findByFdcId(2L)).get()
            .satisfies(f -> assertThat(f.getTimesLogged()).isEqualTo(1));
    }

    @Test
    void logRecipeRejectsUnknownRecipe() {
        assertThatThrownBy(() -> mealService.logRecipe(
            new LogRecipeRequest(DAY, Meal.MealType.BREAKFAST, 999L, 1.0)))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("999");
    }
}
