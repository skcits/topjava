package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
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
        System.out.println("\n" + filteredByStreamsOne(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
        System.out.println("\n" + filteredByRecursion(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> caloriesBySomeDate = new HashMap<>();
        for (UserMeal entry : meals) {
            caloriesBySomeDate.merge(entry.getDate(),
                    entry.getCalories(),
                    (a, b) -> Integer.sum(a, b));
        }
        List<UserMealWithExcess> result = new ArrayList<>();
        meals.forEach(userMeal -> {
            if (isBetweenHalfOpen(userMeal.getTime(), startTime, endTime)) {
                result.add(newUserMealWithExcess(userMeal, caloriesBySomeDate, caloriesPerDay));
            }
        });
        return result;
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> caloriesBySomeDate = meals
                .stream()
                .collect(Collectors.toMap(
                        UserMeal::getDate,
                        UserMeal::getCalories,
                        Integer::sum));

        return meals.stream()
                .filter(p -> isBetweenHalfOpen(p.getTime(), startTime, endTime))
                .map(p -> newUserMealWithExcess(p, caloriesBySomeDate, caloriesPerDay))
                .collect(Collectors.toList());
    }

    public static List<UserMealWithExcess> filteredByStreamsOne(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Collector<Map.Entry<LocalDate, List<UserMeal>>, ?, List<UserMealWithExcess>> mealsCollector =
                Collector.of(
                        ArrayList::new,
                        (j, p) -> {
                            int caloriesBySomeDate = p.getValue().stream().mapToInt(UserMeal::getCalories).sum();
                            p.getValue().stream()
                                    .filter(t -> isBetweenHalfOpen(t.getTime(), startTime, endTime))
                                    .forEach(v -> j.add(new UserMealWithExcess(v.getDateTime(), v.getDescription(),
                                            v.getCalories(), (caloriesBySomeDate > caloriesPerDay))));
                        },
                        (l, r) -> {
                            l.addAll(r);
                            return l;
                        }
                );
        return meals.stream()
                .collect(groupingBy(UserMeal::getDate, toList()))
                .entrySet().stream()
                .collect(mealsCollector);
    }

    public static List<UserMealWithExcess> filteredByRecursion(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        List<UserMealWithExcess> result = new ArrayList<>();
        Map<LocalDate, Integer> caloriesBySomeDate = new HashMap<>();
        Iterator<UserMeal> iterator = meals.iterator();
        RecursionMealWithExcess(iterator, startTime, endTime, caloriesPerDay, caloriesBySomeDate, result);
        return result;
    }

    private static void RecursionMealWithExcess(Iterator<UserMeal> iterator, LocalTime startTime, LocalTime endTime, int caloriesPerDay,
                                                Map<LocalDate, Integer> caloriesBySomeDate, List<UserMealWithExcess> result) {
        if (iterator.hasNext()) {
            UserMeal mealRecord = iterator.next();
            caloriesBySomeDate.merge(mealRecord.getDate(), mealRecord.getCalories(), Integer::sum);
            RecursionMealWithExcess(iterator, startTime, endTime, caloriesPerDay, caloriesBySomeDate, result);
            if (isBetweenHalfOpen(mealRecord.getTime(), startTime, endTime)) {
                result.add(newUserMealWithExcess(mealRecord, caloriesBySomeDate, caloriesPerDay));
            }
        }
    }

    private static UserMealWithExcess newUserMealWithExcess(UserMeal meal, Map<LocalDate, Integer> caloriesBySomeDate, int caloriesPerDay) {
        return new UserMealWithExcess(meal.getDateTime(), meal.getDescription(),
                meal.getCalories(), (caloriesBySomeDate.get(meal.getDate()) > caloriesPerDay));
    }
}