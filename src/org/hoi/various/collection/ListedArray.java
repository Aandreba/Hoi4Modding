package org.hoi.various.collection;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ListedArray<T> extends AbstractList<T> {
    final public T[] array;

    public ListedArray(T... array) {
        this.array = array;
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public T get (int index) {
        return array[index];
    }

    @Override
    public T set (int index, T element) {
        return array[index] = element;
    }

    @Override
    public T[] toArray() {
        return array.clone();
    }

    @Override
    public void sort(Comparator<? super T> c) {
        Arrays.sort(array, c);
    }

    @Override
    public Spliterator<T> spliterator() {
        return Arrays.spliterator(array);
    }

    @Override
    public String toString() {
        return Arrays.toString(array);
    }
}
