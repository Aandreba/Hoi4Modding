package org.hoi.classes.utils;

import org.hoi.classes.history.State;
import org.hoi.various.Regex;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class Contents {
    final String contents;

    public Contents (String contents) {
        this.contents = contents;
    }

    public String getValue (String id) {
        String line = Regex.firstMatch(contents, id+"\\s*=\\s*.+");
        if (line == null) {
            return null;
        }

        return line.split("\\s*=\\s*")[1].replaceAll("\\s*#\\s*.+", "\n").trim();
    }

    public String[] getAllValues (String id) {
        List<String> lines = Regex.matchList(contents, id+"\\s*=\\s*.+");
        return lines.stream()
                .map(x -> x.split("\\s*=\\s*")[1].replaceAll("\\s*#\\s*.+", "\n").trim())
                .toArray(String[]::new);

    }

    public <T> T getObject (String id, Function<String, T> converter) {
        return converter.apply(getValue(id));
    }

    public String getString (String id) {
        return getObject(id, x -> x.substring(1, x.length() - 1));
    }

    public boolean getBool (String id) {
        return getObject(id, x -> x.equalsIgnoreCase("yes"));
    }

    public int getInt (String id) {
        return getObject(id, Integer::parseInt);
    }

    public float getFloat (String id) {
        return getObject(id, Float::parseFloat);
    }

    public State.Category getCategory (String id) {
        return getObject(id, x -> {
            x = x.replace("\"", "");
            return State.Category.valueOf(x.toUpperCase());
        });
    }

    // ARRAYS
    public String[] getArray (String id) {
        String value = Regex.firstMatch(contents, id+"\\s*=\\s*\\{\\n*\\s*.+\\n*\\s*\\}");
        value = value.split("\\s*=\\s*")[1].replaceAll("\\s*#\\s*.+", "\n").trim();

        return value.substring(1, value.length() - 1).trim().split("\\s+");
    }

    public int[] getIntArray (String id) {
        return Arrays.stream(getArray(id)).mapToInt(Integer::parseInt).toArray();
    }
}
