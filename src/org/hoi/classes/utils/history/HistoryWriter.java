package org.hoi.classes.utils.history;

import org.hoi.various.collection.tuples.Couple;

import java.util.ArrayList;
import java.util.Map;

public class HistoryWriter {
    final private ArrayList<Couple<String, String>> values;

    public HistoryWriter() {
        this.values = new ArrayList<>();
    }

    public void putString (String key, String value) {
        values.add(new Couple<>(key, '"'+value+'"'));
    }

    public void putTag (String key, String value) {
        values.add(new Couple<>(key, value));
    }

    public void addNumber (String key, Number value) {
        values.add(new Couple<>(key, value.toString()));
    }

    public void addBool (String key, boolean value) {
        values.add(new Couple<>(key, value ? "yes" : "no"));
    }

    public void addMap (String key, HistoryWriter map) {
        values.add(new Couple<>(key, map.toString()));
    }

    public void addMap (String key, )

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Couple<String, String> value: values) {
            builder.append(value.first).append(" = ").append(value.second).append('\n');
        }

        return builder.toString();
    }
}
