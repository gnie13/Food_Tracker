package com.gnien.mealplanner.meal_planner.controller;

import com.gnien.mealplanner.meal_planner.dto.MealResponse;
import com.gnien.mealplanner.meal_planner.dto.NutritionTotals;
import com.gnien.mealplanner.meal_planner.dto.UpdateEntryRequest;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MealController.class)
class MealControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean MealService mealService;

    @Test
    void patchDelegatesAndReturnsMeal() throws Exception {
        MealResponse response = new MealResponse(
            7L, LocalDate.of(2026, 8, 30), Meal.MealType.DINNER,
            List.of(), NutritionTotals.zero());
        when(mealService.updateEntry(eq(3L), any(UpdateEntryRequest.class))).thenReturn(response);

        mvc.perform(patch("/api/meals/entries/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"servingSize\":2.0,\"mealType\":\"DINNER\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mealType").value("DINNER"));

        verify(mealService).updateEntry(eq(3L), any(UpdateEntryRequest.class));
    }

    @Test
    void patchRejectsNonPositiveServingSize() throws Exception {
        mvc.perform(patch("/api/meals/entries/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"servingSize\":-1.0}"))
            .andExpect(status().isBadRequest());
    }
}
