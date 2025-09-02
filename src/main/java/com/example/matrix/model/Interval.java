package com.example.matrix.model;

import lombok.Getter;

import java.util.Objects;

@Getter
public final class Interval<T extends Comparable<T>> {
    private final T from;
    private final T to;

    public Interval(T from, T to) {
        if (from == null || to == null || from.compareTo(to) > 0) {
            throw new IllegalArgumentException("Invalid interval: " + from + "-" + to);
        }
        this.from = from;
        this.to = to;
    }

    public boolean contains(T value) {
        return value.compareTo(from) >= 0 && value.compareTo(to) <= 0;
    }

    @Override public String toString() { return from + "-" + to; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Interval<?> that)) return false;
        return Objects.equals(from, that.from) && Objects.equals(to, that.to);
    }

    @Override public int hashCode() { return Objects.hash(from, to); }
}
