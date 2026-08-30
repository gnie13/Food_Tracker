package com.gnien.mealplanner.meal_planner.service;

import com.gnien.mealplanner.meal_planner.dto.FoodPayload;
import com.gnien.mealplanner.meal_planner.dto.StoredFoodResponse;
import com.gnien.mealplanner.meal_planner.dto.LogEntryRequest;
import com.gnien.mealplanner.meal_planner.model.Meal;
import com.gnien.mealplanner.meal_planner.repository.FoodRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({MealService.class, FoodCatalogService.class})
class FrequentFoodsServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 8, 30);

    @Autowired MealService mealService;
    @Autowired FoodRepository foodRepository;

    private void log(long fdcId, String name, int times) {
        for (int i = 0; i < times; i++) {
            mealService.logEntry(new LogEntryRequest(
                DAY, Meal.MealType.SNACK,
                new FoodPayload(fdcId, name, 100.0, 5.0, 10.0, 2.0), 1.0));
        }
    }

    @Test
    void loggingBumpsUsageCountersAndDedupesTheFood() {
        log(1L, "Banana", 3);

        assertThat(foodRepository.findByFdcId(1L)).get().satisfies(f -> {
            assertThat(f.getTimesLogged()).isEqualTo(3);
            assertThat(f.getLastLoggedAt()).isNotNull();
        });
        assertThat(foodRepository.count()).isEqualTo(1);
    }

    @Test
    void frequentFoodsRankedByCountThenRecency() throws InterruptedException {
        log(1L, "Rare", 1);
        Thread.sleep(2);
        log(2L, "AlsoRare", 1);   // same count, logged later
        log(3L, "Common", 5);

        List<StoredFoodResponse> frequent = mealService.frequentFoods(10);

        assertThat(frequent).extracting(StoredFoodResponse::name)
            .containsExactly("Common", "AlsoRare", "Rare");
        assertThat(frequent.get(0).timesLogged()).isEqualTo(5);
    }

    @Test
    void frequentFoodsRespectsLimitAndClampsOutOfRangeValues() {
        log(1L, "A", 4);
        log(2L, "B", 3);
        log(3L, "C", 2);

        assertThat(mealService.frequentFoods(2)).extracting(StoredFoodResponse::name)
            .containsExactly("A", "B");
        assertThat(mealService.frequentFoods(0)).hasSize(1);      // clamped up to 1
        assertThat(mealService.frequentFoods(999)).hasSize(3);    // clamped down to what exists
    }
}
