package com.gnien.mealplanner.meal_planner.controller;

import com.gnien.mealplanner.meal_planner.dto.NutritionTotals;
import com.gnien.mealplanner.meal_planner.dto.RecipeRequest;
import com.gnien.mealplanner.meal_planner.dto.RecipeResponse;
import com.gnien.mealplanner.meal_planner.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

    private static final String VALID_BODY = """
        {"name":"Porridge","ingredients":[
          {"food":{"fdcId":1,"name":"Oats","calories":100,"protein":10,"carbs":20,"fat":5},"servingSize":2.0}
        ]}""";

    @Autowired MockMvc mvc;
    @MockitoBean RecipeService recipeService;

    @Test
    void createReturns201AndBody() throws Exception {
        when(recipeService.create(any(RecipeRequest.class))).thenReturn(
            new RecipeResponse(9L, "Porridge", List.of(), NutritionTotals.zero()));

        mvc.perform(post("/api/recipes").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(9));

        verify(recipeService).create(any(RecipeRequest.class));
    }

    @Test
    void createRejectsBlankName() throws Exception {
        mvc.perform(post("/api/recipes").contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY.replace("\"Porridge\"", "\"  \"")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createRejectsEmptyIngredients() throws Exception {
        mvc.perform(post("/api/recipes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Porridge\",\"ingredients\":[]}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getDelegates() throws Exception {
        when(recipeService.get(9L)).thenReturn(
            new RecipeResponse(9L, "Porridge", List.of(), NutritionTotals.zero()));

        mvc.perform(get("/api/recipes/9")).andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Porridge"));
    }

    @Test
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/api/recipes/9")).andExpect(status().isNoContent());
        verify(recipeService).delete(eq(9L));
    }
}
