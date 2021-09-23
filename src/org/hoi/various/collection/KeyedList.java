package org.hoi.various.collection;

import java.util.*;

public abstract class KeyedList<K,V> extends AbstractMap<K,V> {
    final public List<V> list;

    public KeyedList(List<V> list) {
        this.list = list;
    }

    public KeyedList (V... array) {
        this.list = new ListedArray<>(array);
    }

    public abstract K getKey (int index);

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<Entry<K, V>>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                return new Iterator<Entry<K, V>>() {
                    int i = 0;

                    @Override
                    public boolean hasNext() {
                        return i < list.size();
                    }

                    @Override
                    public Entry<K, V> next() {
                        int j = i++;
                        return new Entry<K, V>() {
                            @Override
                            public K getKey() {
                                return KeyedList.this.getKey(j);
                            }

                            @Override
                            public V getValue() {
                                return KeyedList.this.list.get(j);
                            }

                            @Override
                            public V setValue(V value) {
                                return KeyedList.this.list.set(j, value);
                            }
                        };
                    }
                };
            }

            @Override
            public int size() {
                return list.size();
            }
        };
    }
}