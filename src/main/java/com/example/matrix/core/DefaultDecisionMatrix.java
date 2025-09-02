package com.example.matrix.core;

import com.example.matrix.DecisionMatrix;
import com.example.matrix.annotations.Factor;
import com.example.matrix.model.Interval;

import java.util.*;
import java.lang.reflect.Field;
import java.util.stream.Collectors;

final class DefaultDecisionMatrix<C, D> implements DecisionMatrix<C, D> {
    private final Class<C> caseType;
    private final Class<D> decisionType;
    private final Set<D> rows;

    DefaultDecisionMatrix(Class<C> caseType, Class<D> decisionType, Set<D> rows) {
        this.caseType = caseType;
        this.decisionType = decisionType;
        this.rows = rows;
    }

    @Override
    public List<D> getDecision(C byCase) {
        return rows.stream()
                .filter(row -> matches(byCase, row))
                .collect(Collectors.toList());
    }

    private boolean matches(C byCase, D decisionRow) {
        Map<String, Field> caseFactors = factors(caseType);
        Map<String, Field> decisionFactors = factors(decisionType);

        try {
            for (Map.Entry<String, Field> e : caseFactors.entrySet()) {
                String name = e.getKey();
                Field caseField = e.getValue();
                Field decisionField = decisionFactors.get(name);
                if (decisionField == null) return false;

                caseField.setAccessible(true);
                decisionField.setAccessible(true);

                Object caseVal = caseField.get(byCase);
                Object decisionVal = decisionField.get(decisionRow);

                if (!factorSatisfied(caseVal, decisionVal)) return false;
            }
            return true;
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Decision matching failed", ex);
        }
    }

    private static Map<String, Field> factors(Class<?> type) {
        Map<String, Field> map = new LinkedHashMap<>();
        for (Field f : type.getDeclaredFields()) {
            if (f.isAnnotationPresent(Factor.class)) {
                map.put(f.getName(), f);
            }
        }
        return map;
    }

    @SuppressWarnings({"rawtypes"})
    private static boolean factorSatisfied(Object caseVal, Object decisionVal) {
        if (decisionVal instanceof Interval<?> iv) {
            return intervalContains(iv, caseVal);
        }
        // equals для простых типов (String, enum, числа и т.п.)
        return Objects.equals(normalizeNumber(caseVal), normalizeNumber(decisionVal));
    }

    private static Object normalizeNumber(Object o) {
        if (o instanceof Number n) return n.doubleValue();
        return o;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean intervalContains(Interval<?> iv, Object caseVal) {
        if (caseVal == null) return false;

        Object from = iv.getFrom();
        Object to = iv.getTo();

        if (from instanceof Long && to instanceof Long) {
            long v = ((Number) caseVal).longValue();
            return v >= (Long) from && v <= (Long) to;
        }
        if (from instanceof Integer && to instanceof Integer) {
            int v = ((Number) caseVal).intValue();
            return v >= (Integer) from && v <= (Integer) to;
        }
        // generic fallback (на случай других Comparable)
        Comparable f = (Comparable) from;
        Comparable t = (Comparable) to;
        Comparable v = (Comparable) caseVal;
        return v.compareTo(f) >= 0 && v.compareTo(t) <= 0;
    }
}
