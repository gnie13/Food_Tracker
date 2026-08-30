package com.gnien.mealplanner.meal_planner.controller;

import com.gnien.mealplanner.meal_planner.dto.FrequentFoodResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
            new FrequentFoodResponse(1L, 42L, "Banana", 100, 5, 10, 2, 7, Instant.now())));

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
}
