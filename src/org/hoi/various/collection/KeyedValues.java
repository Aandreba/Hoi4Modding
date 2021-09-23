package org.hoi.various.collection;

import java.util.*;

public abstract class KeyedValues<K,V> extends AbstractMap<K,V> {
    final public List<V> list;

    public KeyedValues (List<V> list) {
        this.list = list;
    }

    public KeyedValues (V... array) {
        this.list = new ListedArray<>(array);
    }

    public abstract K getKey (V value);

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
                            V value = list.get(j);
                            K key = KeyedValues.this.getKey(value);

                            @Override
                            public K getKey() {
                                return key;
                            }

                            @Override
                            public V getValue() {
                                return value;
                            }

                            @Override
                            public V setValue(V value) {
                                this.value = value;
                                this.key = KeyedValues.this.getKey(value);
                                return KeyedValues.this.list.set(j, value);
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