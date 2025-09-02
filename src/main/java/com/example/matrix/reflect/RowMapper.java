package com.example.matrix.reflect;

import com.example.matrix.model.Interval;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.lang.reflect.Field;
import java.util.List;

public final class RowMapper {

    private RowMapper() { }

    /**
     * Преобразует массив токенов (уже сплитнутых по ';') в экземпляр целевого класса.
     * Поля берутся в порядке объявления (факторы + решение).
     */
    public static <T> T map(String[] tokens, Class<T> type) {
        try {
            T instance = type.getDeclaredConstructor().newInstance();

            List<Field> fields = Arrays.stream(type.getDeclaredFields()).toList();
            int idx = 0;

            for (Field f : fields) {
                // Берём только помеченные аннотациями или пропускаем непомеченные
                // (на случай служебных полей/названий)
                if (!f.isAnnotationPresent(com.example.matrix.annotations.Factor.class)
                        && !f.isAnnotationPresent(com.example.matrix.annotations.Decision.class)) {
                    continue;
                }
                if (idx >= tokens.length) {
                    throw new IllegalArgumentException("Not enough tokens for " + type.getSimpleName());
                }

                String raw = tokens[idx++].trim();
                f.setAccessible(true);
                f.set(instance, convert(raw, f));
            }

            return instance;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new IllegalStateException("Row mapping failed for " + type.getName(), e);
        }
    }

    private static Object convert(String raw, Field f) {
        Class<?> t = f.getType();

        if (t == String.class) return raw;
        if (t == Double.class || t == double.class) return Double.valueOf(raw);
        if (t == Integer.class || t == int.class) return Integer.valueOf(raw);
        if (t == Long.class || t == long.class) return Long.valueOf(raw);

        if (Interval.class.isAssignableFrom(t)) {
            Type g = f.getGenericType();
            if (g instanceof ParameterizedType p) {
                Type arg = p.getActualTypeArguments()[0];
                if (arg == Long.class) {
                    String[] bounds = splitInterval(raw);
                    return new Interval<>(Long.valueOf(bounds[0]), Long.valueOf(bounds[1]));
                } else if (arg == Integer.class) {
                    String[] bounds = splitInterval(raw);
                    return new Interval<>(Integer.valueOf(bounds[0]), Integer.valueOf(bounds[1]));
                }
            }
            throw new IllegalArgumentException("Unsupported interval type for field: " + f.getName());
        }

        throw new IllegalArgumentException("No converter for type: " + t.getName());
    }

    private static String[] splitInterval(String raw) {
        String[] parts = raw.split("-", 2);
        if (parts.length != 2) throw new IllegalArgumentException("Bad interval: " + raw);
        return new String[]{parts[0].trim(), parts[1].trim()};
    }
}
