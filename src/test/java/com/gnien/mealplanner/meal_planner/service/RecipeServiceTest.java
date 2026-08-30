package com.gnien.mealplanner.meal_planner.service;

import com.gnien.mealplanner.meal_planner.dto.FoodPayload;
import com.gnien.mealplanner.meal_planner.dto.RecipeIngredientRequest;
import com.gnien.mealplanner.meal_planner.dto.RecipeRequest;
import com.gnien.mealplanner.meal_planner.dto.RecipeResponse;
import com.gnien.mealplanner.meal_planner.repository.FoodRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({RecipeService.class, FoodCatalogService.class})
class RecipeServiceTest {

    @Autowired RecipeService recipeService;
    @Autowired FoodRepository foodRepository;

    private static FoodPayload food(long fdcId, String name) {
        return new FoodPayload(fdcId, name, 100.0, 10.0, 20.0, 5.0);
    }

    private static RecipeRequest twoIngredientRecipe(String name) {
        return new RecipeRequest(name, List.of(
            new RecipeIngredientRequest(food(1L, "Oats"), 2.0),
            new RecipeIngredientRequest(food(2L, "Milk"), 0.5)));
    }

    @Test
    void createResolvesFoodsAndComputesBatchTotals() {
        RecipeResponse recipe = recipeService.create(twoIngredientRecipe("Porridge"));

        assertThat(recipe.id()).isNotNull();
        assertThat(recipe.ingredients()).hasSize(2);
        // 2.0 * (100,10,20,5) + 0.5 * (100,10,20,5) = 2.5 * (100,10,20,5)
        assertThat(recipe.totals().calories()).isEqualTo(250.0);
        assertThat(recipe.totals().protein()).isEqualTo(25.0);
        assertThat(foodRepository.count()).isEqualTo(2);
    }

    @Test
    void replaceSwapsIngredientsAndDropsTheOldRows() {
        Long id = recipeService.create(twoIngredientRecipe("Porridge")).id();

        RecipeResponse updated = recipeService.replace(id, new RecipeRequest("Porridge", List.of(
            new RecipeIngredientRequest(food(3L, "Water"), 1.0))));

        assertThat(updated.ingredients()).singleElement()
            .satisfies(i -> assertThat(i.foodName()).isEqualTo("Water"));
        assertThat(recipeService.get(id).ingredients()).hasSize(1);
    }

    @Test
    void listComesBackAlphabetical() {
        recipeService.create(twoIngredientRecipe("Ziti"));
        recipeService.create(twoIngredientRecipe("Anzac biscuits"));

        assertThat(recipeService.list()).extracting(RecipeResponse::name)
            .containsExactly("Anzac biscuits", "Ziti");
    }

    @Test
    void getAndDeleteRejectUnknownIds() {
        assertThatThrownBy(() -> recipeService.get(999L))
            .isInstanceOf(ResponseStatusException.class).hasMessageContaining("999");
        assertThatThrownBy(() -> recipeService.delete(999L))
            .isInstanceOf(ResponseStatusException.class).hasMessageContaining("999");
    }
}
