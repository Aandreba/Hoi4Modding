package org.hoi.various.collection;

import java.util.*;

public class InvertedMap<K,V> extends AbstractMap<K,V> {
    final public Map<V,K> original;
    final private Set<Entry<V,K>> set;

    public InvertedMap(Map<V, K> original) {
        this.original = original;
        this.set = this.original.entrySet();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<Entry<K, V>>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                Iterator<Entry<V,K>> original = set.iterator();

                return new Iterator<Entry<K, V>>() {
                    @Override
                    public boolean hasNext() {
                        return original.hasNext();
                    }

                    @Override
                    public Entry<K, V> next() {
                        Entry<V, K> next = original.next();

                        return new Entry<K, V>() {
                            @Override
                            public K getKey() {
                                return next.getValue();
                            }

                            @Override
                            public V getValue() {
                                return next.getKey();
                            }

                            @Override
                            public V setValue (V newKey) {
                                K newValue = InvertedMap.this.original.get(newKey);
                                V oldKey = set.stream().map(Entry::getKey).filter(x -> x == newValue).findFirst().orElse(null);

                                InvertedMap.this.original.put(newKey, newValue);
                                InvertedMap.this.original.remove(oldKey);
                                return oldKey;
                            }
                        };
                    }
                };
            }

            @Override
            public int size() {
                return original.size();
            }
        };
    }
}
