package org.hoi.various.collection;

import java.util.*;
import java.util.stream.Collectors;

public class SetMap<K,V> extends AbstractMap<K,V> {
    final public Set<Map.Entry<K,V>> set;

    public SetMap(Set<Entry<K, V>> set) {
        this.set = set;
    }

    public SetMap (Collection<Map.Entry<K,V>> collection) {
        this.set = new HashSet<>(collection);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return set;
    }
}
