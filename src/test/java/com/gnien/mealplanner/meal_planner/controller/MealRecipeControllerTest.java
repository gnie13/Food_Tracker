package com.gnien.mealplanner.meal_planner.controller;

import com.gnien.mealplanner.meal_planner.dto.LogRecipeRequest;
import com.gnien.mealplanner.meal_planner.dto.MealResponse;
import com.gnien.mealplanner.meal_planner.dto.NutritionTotals;
import com.gnien.mealplanner.meal_planner.model.Meal;
import com.gnien.mealplanner.meal_planner.service.MealService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MealController.class)
class MealRecipeControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean MealService mealService;

    @Test
    void fromRecipeDelegatesAndReturns201() throws Exception {
        when(mealService.logRecipe(any(LogRecipeRequest.class))).thenReturn(new MealResponse(
            1L, LocalDate.of(2026, 8, 30), Meal.MealType.BREAKFAST, List.of(), NutritionTotals.zero()));

        mvc.perform(post("/api/meals/entries/from-recipe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\":\"2026-08-30\",\"mealType\":\"BREAKFAST\",\"recipeId\":9,\"factor\":2.0}"))
            .andExpect(status().isCreated());

        verify(mealService).logRecipe(any(LogRecipeRequest.class));
    }

    @Test
    void fromRecipeRejectsNonPositiveFactor() throws Exception {
        mvc.perform(post("/api/meals/entries/from-recipe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\":\"2026-08-30\",\"mealType\":\"BREAKFAST\",\"recipeId\":9,\"factor\":0}"))
            .andExpect(status().isBadRequest());
    }
}
