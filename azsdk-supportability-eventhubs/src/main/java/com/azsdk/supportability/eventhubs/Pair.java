package com.azsdk.supportability.eventhubs;

public class Pair<T1, T2> {
    private final T1 first;
    private final T2 second;

    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public static <T1, T2> Pair<T1, T2> create(T1 r, T2 s) {
        return new Pair<>(r, s);
    }

    public T1 getFirst() {
        return this.first;
    }

    public T2 getSecond() {
        return this.second;
    }

    public String toString() {
        return "Tuple2 [value1=" + this.first + ", value2=" + this.second + "]";
    }
}
