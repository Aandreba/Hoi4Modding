package org.hoi.various.collection.tuples;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.stream.Stream;

public abstract class Tuple extends AbstractCollection<Object> {
    public abstract Object get (int pos);

    @Override
    public Iterator<Object> iterator() {
        return new Iterator<Object>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < size();
            }

            @Override
            public Object next() {
                return get(i++);
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("(");
        for (Object obj: this) {
            builder.append(", ").append(obj);
        }

        return builder.substring(2)+')';
    }
}
