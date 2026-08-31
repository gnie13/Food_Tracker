package com.gnien.mealplanner.meal_planner.controller;

import com.gnien.mealplanner.meal_planner.dto.StoredFoodResponse;
import com.gnien.mealplanner.meal_planner.model.Meal;
import com.gnien.mealplanner.meal_planner.service.MealService;
import com.gnien.mealplanner.meal_planner.service.UsdaFoodService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FoodController.class)
class FoodControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean UsdaFoodService usdaFoodService;
    @MockitoBean MealService mealService;

    private static StoredFoodResponse stored(String name, int timesLogged, boolean saved) {
        return new StoredFoodResponse(1L, 42L, name, 100, 5, 10, 2, 28.0, "1 oz",
            timesLogged, Instant.now(), saved);
    }

    @Test
    void frequentUsesDefaultLimitNoMealTypeAndReturnsList() throws Exception {
        when(mealService.frequentFoods(any(), anyInt()))
            .thenReturn(List.of(stored("Banana", 7, false)));

        mvc.perform(get("/api/foods/frequent"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Banana"))
            .andExpect(jsonPath("$[0].timesLogged").value(7));

        verify(mealService).frequentFoods(isNull(), eq(10));
    }

    @Test
    void frequentPassesThroughMealTypeAndLimit() throws Exception {
        when(mealService.frequentFoods(any(), anyInt())).thenReturn(List.of());

        mvc.perform(get("/api/foods/frequent").param("mealType", "BREAKFAST").param("limit", "3"))
            .andExpect(status().isOk());

        verify(mealService).frequentFoods(eq(Meal.MealType.BREAKFAST), eq(3));
    }

    @Test
    void frequentRejectsUnknownMealType() throws Exception {
        mvc.perform(get("/api/foods/frequent").param("mealType", "BRUNCH"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void frequentRejectsLimitBelowOne() throws Exception {
        mvc.perform(get("/api/foods/frequent").param("limit", "0"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void frequentRejectsLimitAboveFifty() throws Exception {
        mvc.perform(get("/api/foods/frequent").param("limit", "51"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void savedReturnsList() throws Exception {
        when(mealService.savedFoods()).thenReturn(List.of(stored("Almonds", 2, true)));

        mvc.perform(get("/api/foods/saved"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Almonds"))
            .andExpect(jsonPath("$[0].saved").value(true));
    }

    @Test
    void putSaveDelegatesWithTrue() throws Exception {
        when(mealService.setSaved(5L, true)).thenReturn(stored("Almonds", 0, true));

        mvc.perform(put("/api/foods/5/saved"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.saved").value(true));

        verify(mealService).setSaved(eq(5L), eq(true));
    }

    @Test
    void deleteUnsaveDelegatesWithFalseAndReturns204() throws Exception {
        mvc.perform(delete("/api/foods/5/saved"))
            .andExpect(status().isNoContent());

        verify(mealService).setSaved(eq(5L), eq(false));
    }
}
