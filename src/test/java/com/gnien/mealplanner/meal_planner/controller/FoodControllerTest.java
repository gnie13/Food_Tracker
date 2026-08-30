package com.gnien.mealplanner.meal_planner.controller;

import com.gnien.mealplanner.meal_planner.dto.StoredFoodResponse;
import com.gnien.mealplanner.meal_planner.service.MealService;
import com.gnien.mealplanner.meal_planner.service.UsdaFoodService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    void frequentUsesDefaultLimitAndReturnsList() throws Exception {
        when(mealService.frequentFoods(10)).thenReturn(List.of(
            new StoredFoodResponse(1L, 42L, "Banana", 100, 5, 10, 2, 7, Instant.now(), false)));

        mvc.perform(get("/api/foods/frequent"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Banana"))
            .andExpect(jsonPath("$[0].timesLogged").value(7));

        verify(mealService).frequentFoods(10);
    }

    @Test
    void frequentPassesThroughAnExplicitLimit() throws Exception {
        when(mealService.frequentFoods(3)).thenReturn(List.of());

        mvc.perform(get("/api/foods/frequent").param("limit", "3"))
            .andExpect(status().isOk());

        verify(mealService).frequentFoods(eq(3));
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
        when(mealService.savedFoods()).thenReturn(List.of(
            new StoredFoodResponse(1L, 42L, "Almonds", 160, 6, 6, 14, 2, Instant.now(), true)));

        mvc.perform(get("/api/foods/saved"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Almonds"))
            .andExpect(jsonPath("$[0].saved").value(true));
    }

    @Test
    void putSaveDelegatesWithTrue() throws Exception {
        when(mealService.setSaved(5L, true)).thenReturn(
            new StoredFoodResponse(5L, 42L, "Almonds", 160, 6, 6, 14, 0, null, true));

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
