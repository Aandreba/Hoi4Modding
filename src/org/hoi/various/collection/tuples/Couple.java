package org.hoi.various.collection.tuples;

public class Couple<A,B> extends Tuple {
    public A first;
    public B second;

    public Couple (A first, B second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public Object get (int pos) {
        return switch (pos) {
            case 0 -> first;
            case 1 -> second;
            default -> null;
        };
    }
}
