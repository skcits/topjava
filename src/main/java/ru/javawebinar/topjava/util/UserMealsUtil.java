package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

import static ru.javawebinar.topjava.util.TimeUtil.isBetweenHalfOpen;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);

        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> excess = new HashMap<>();
        for (UserMeal entry : meals) {
            excess.merge(entry.getDateTime().toLocalDate(),
                    entry.getCalories(),
                    (a, b) -> Integer.sum(a, b));
        }
        List<UserMealWithExcess> filteredMeals = new ArrayList<>();
        meals.forEach(userMeal -> {
            if (isBetweenHalfOpen(userMeal.getDateTime().toLocalTime(), startTime, endTime)) {
                filteredMeals.add(new UserMealWithExcess(userMeal.getDateTime(), userMeal.getDescription(),
                        userMeal.getCalories(), (excess.get(userMeal.getDateTime().toLocalDate()) > caloriesPerDay)));
            }
        });
        return filteredMeals;
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> map = meals
                .stream()
                .collect(Collectors.toMap(
                        p -> p.getDateTime().toLocalDate(),
                        UserMeal::getCalories,
                        Integer::sum));

        return meals.stream()
                .filter(p -> isBetweenHalfOpen(p.getDateTime().toLocalTime(), startTime, endTime))
                .map(p -> new UserMealWithExcess(p.getDateTime(), p.getDescription(),
                        p.getCalories(), (map.get(p.getDateTime().toLocalDate()) > caloriesPerDay)))
                .collect(Collectors.toList());
    }
}