package com.gnien.mealplanner.meal_planner.service;

import com.gnien.mealplanner.meal_planner.dto.FoodPayload;
import com.gnien.mealplanner.meal_planner.dto.LogEntryRequest;
import com.gnien.mealplanner.meal_planner.dto.MealResponse;
import com.gnien.mealplanner.meal_planner.dto.UpdateEntryRequest;
import com.gnien.mealplanner.meal_planner.model.Meal;
import com.gnien.mealplanner.meal_planner.repository.FoodRepository;
import com.gnien.mealplanner.meal_planner.repository.MealEntryRepository;
import com.gnien.mealplanner.meal_planner.repository.MealRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(MealService.class)
class MealServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 8, 30);

    @Autowired MealService mealService;
    @Autowired MealRepository mealRepository;
    @Autowired MealEntryRepository mealEntryRepository;
    @Autowired FoodRepository foodRepository;
    @Autowired TestEntityManager em;

    private Long entryId;

    @BeforeEach
    void logOneEntry() {
        MealResponse meal = mealService.logEntry(new LogEntryRequest(
            DAY, Meal.MealType.BREAKFAST,
            new FoodPayload(173904L, "Oatmeal, dry", 380.0, 13.0, 67.0, 7.0),
            1.0));
        entryId = meal.entries().get(0).id();
        flushAndClear();
    }

    @Test
    void changesServingSizeAndRescalesNutrition() {
        MealResponse meal = mealService.updateEntry(entryId, new UpdateEntryRequest(2.0, null, null));

        assertThat(meal.entries()).singleElement().satisfies(e -> {
            assertThat(e.servingSize()).isEqualTo(2.0);
            assertThat(e.nutrition().calories()).isEqualTo(760.0);
            assertThat(e.nutrition().protein()).isEqualTo(26.0);
        });
        assertThat(meal.subtotal().calories()).isEqualTo(760.0);
    }

    @Test
    void movesEntryToAnotherMealTypeCreatingThatMeal() {
        MealResponse meal = mealService.updateEntry(
            entryId, new UpdateEntryRequest(null, null, Meal.MealType.DINNER));
        assertThat(meal.mealType()).isEqualTo(Meal.MealType.DINNER);
        flushAndClear();

        List<Meal> meals = mealRepository.findByDate(DAY);
        assertThat(meals)
            .filteredOn(m -> m.getMealType() == Meal.MealType.DINNER)
            .singleElement()
            .satisfies(m -> assertThat(m.getEntries()).hasSize(1));
        assertThat(meals)
            .filteredOn(m -> m.getMealType() == Meal.MealType.BREAKFAST)
            .singleElement()
            .satisfies(m -> assertThat(m.getEntries()).isEmpty());
        assertThat(mealEntryRepository.findById(entryId)).isPresent();
    }

    @Test
    void moveReusesAnExistingTargetMeal() {
        mealService.logEntry(new LogEntryRequest(
            DAY, Meal.MealType.DINNER,
            new FoodPayload(1L, "Rice", 200.0, 4.0, 45.0, 0.5), 1.0));
        flushAndClear();

        mealService.updateEntry(entryId, new UpdateEntryRequest(null, null, Meal.MealType.DINNER));
        flushAndClear();

        assertThat(mealRepository.findByDate(DAY))
            .filteredOn(m -> m.getMealType() == Meal.MealType.DINNER)
            .singleElement()
            .satisfies(m -> assertThat(m.getEntries()).hasSize(2));
    }

    @Test
    void rejectsEmptyUpdate() {
        assertThatThrownBy(() ->
            mealService.updateEntry(entryId, new UpdateEntryRequest(null, null, null)))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("at least one field");
    }

    @Test
    void rejectsUnknownEntry() {
        assertThatThrownBy(() ->
            mealService.updateEntry(999_999L, new UpdateEntryRequest(1.5, null, null)))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("999999");
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
