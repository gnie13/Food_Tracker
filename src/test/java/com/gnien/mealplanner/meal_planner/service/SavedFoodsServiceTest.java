package com.gnien.mealplanner.meal_planner.service;

import com.gnien.mealplanner.meal_planner.dto.FoodPayload;
import com.gnien.mealplanner.meal_planner.dto.LogEntryRequest;
import com.gnien.mealplanner.meal_planner.dto.StoredFoodResponse;
import com.gnien.mealplanner.meal_planner.model.Meal;
import com.gnien.mealplanner.meal_planner.repository.FoodRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({MealService.class, FoodCatalogService.class})
class SavedFoodsServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 8, 30);

    @Autowired MealService mealService;
    @Autowired FoodRepository foodRepository;

    private long logAndGetFoodId(long fdcId, String name) {
        mealService.logEntry(new LogEntryRequest(
            DAY, Meal.MealType.SNACK,
            new FoodPayload(fdcId, name, 100.0, 5.0, 10.0, 2.0), 1.0));
        return foodRepository.findByFdcId(fdcId).orElseThrow().getId();
    }

    @Test
    void savingThenUnsavingAFoodMovesItInAndOutOfTheList() {
        long id = logAndGetFoodId(1L, "Greek yogurt");

        StoredFoodResponse saved = mealService.setSaved(id, true);
        assertThat(saved.saved()).isTrue();
        assertThat(mealService.savedFoods()).extracting(StoredFoodResponse::foodId).containsExactly(id);

        mealService.setSaved(id, false);
        assertThat(mealService.savedFoods()).isEmpty();
    }

    @Test
    void savedFoodsComeBackAlphabetical() {
        mealService.setSaved(logAndGetFoodId(1L, "Walnuts"), true);
        mealService.setSaved(logAndGetFoodId(2L, "Almonds"), true);

        assertThat(mealService.savedFoods()).extracting(StoredFoodResponse::name)
            .containsExactly("Almonds", "Walnuts");
    }

    @Test
    void savingIsIdempotent() {
        long id = logAndGetFoodId(1L, "Oats");
        mealService.setSaved(id, true);
        mealService.setSaved(id, true);

        assertThat(mealService.savedFoods()).hasSize(1);
    }

    @Test
    void setSavedRejectsUnknownFood() {
        assertThatThrownBy(() -> mealService.setSaved(999_999L, true))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("999999");
    }
}
