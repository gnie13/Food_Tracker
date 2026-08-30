package com.gnien.mealplanner.meal_planner.repository;

import com.gnien.mealplanner.meal_planner.model.Food;
import com.gnien.mealplanner.meal_planner.model.Meal;
import com.gnien.mealplanner.meal_planner.model.MealEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface MealEntryRepository extends JpaRepository<MealEntry, Long> {

    /** How often each food has been logged against one meal type, most-logged first. */
    @Query("""
        select me.food as food, count(me) as timesLogged, max(m.date) as lastDate
        from MealEntry me
          join me.meal m
        where m.mealType = :mealType
        group by me.food
        order by count(me) desc, max(m.date) desc
        """)
    List<FoodFrequency> frequencyByMealType(Meal.MealType mealType, Pageable pageable);

    /** Projection row for {@link #frequencyByMealType}. */
    interface FoodFrequency {
        Food getFood();
        long getTimesLogged();
        LocalDate getLastDate();
    }
}
