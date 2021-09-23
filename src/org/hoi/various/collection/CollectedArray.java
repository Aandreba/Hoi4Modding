package org.hoi.various.collection;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.Iterator;

public class CollectedArray<T> extends AbstractCollection<T> {
    final public T[] array;

    public CollectedArray(T... array) {
        this.array = array;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < array.length;
            }

            @Override
            public T next() {
                return array[i++];
            }
        };
    }

    @Override
    public int size() {
        return array.length;
    }
}
