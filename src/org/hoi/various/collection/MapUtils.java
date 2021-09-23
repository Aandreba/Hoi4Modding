package org.hoi.various.collection;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapUtils {
    public static <K,V> Map<K,V> clone (Map<K,V> map) {
        return new SetMap<>(new ArrayList<>(map.entrySet()));
    }

    public static <K,V> Stream<Map.Entry<K,V>> stream (Map<K,V> map) {
        return map.entrySet().stream();
    }

    public static <K,V,T,Y> Stream<Map.Entry<T,Y>> map (Function<K, T> keyMap, Function<V, Y> valueMap, Stream<Map.Entry<K,V>> stream) {
        return stream.map(x -> new Map.Entry<T,Y>(){
            @Override
            public T getKey() {
                return keyMap.apply(x.getKey());
            }

            @Override
            public Y getValue() {
                return valueMap.apply(x.getValue());
            }

            @Override
            public Y setValue (Y value) {
                return null;
            }
        });
    }

    public static <K,V,T,Y> Map<T,Y> map (Function<K, T> keyMap, Function<V, Y> valueMap, Map<K,V> map) {
        return collect(map(keyMap, valueMap, stream(map)));
    }

    public static <K,V> Map<K,V> collect (Stream<Map.Entry<K,V>> stream) {
        return new SetMap<>(stream.collect(Collectors.toSet()));
    }
}
