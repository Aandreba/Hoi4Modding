package org.hoi.various;

import java.util.Collection;

public interface Condition<T> {
    boolean apply (T o);

    default Condition<T> and (Condition<T> other) {
        return x -> this.apply(x) && other.apply(x);
    }

    default Condition<T> or (Condition<T> other) {
        return x -> this.apply(x) || other.apply(x);
    }

    default T getIf (Collection<T> list) {
        for (T element: list) {
            if (this.apply(element)) {
                return element;
            }
        }

        return null;
    }

    static <E> E getIf (Collection<E> list, Condition<E> condition) {
        return condition.getIf(list);
    }
}
