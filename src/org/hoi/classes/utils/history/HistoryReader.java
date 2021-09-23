package org.hoi.classes.utils.history;

import java.io.*;
import java.util.*;

public class HistoryReader extends AbstractMap<String, Object> {
    final public Set<Map.Entry<String, Object>> set;

    private HistoryReader(Set<Entry<String, Object>> set) {
        this.set = set;
    }

    protected HistoryReader(boolean writteable) {
        if (writteable) {
            this.set = new HashSet<>();
        } else {
            this.set = new AbstractSet<Entry<String, Object>>() {
                @Override
                public Iterator<Entry<String, Object>> iterator() {
                    return new Iterator<Entry<String, Object>>() {
                        @Override
                        public boolean hasNext() {
                            return false;
                        }

                        @Override
                        public Entry<String, Object> next() {
                            return null;
                        }
                    };
                }

                @Override
                public int size() {
                    return 0;
                }
            };
        }
    }

    public HistoryReader() {
        this(true);
    }

    public <T> T getAs (String key) {
        return (T) get(key);
    }

    public <T> T getAs (Class<T> type, String key) {
        return getAs(key);
    }

    public String getString (String key) {
        return get(key).toString();
    }

    public Number getNumber (String key) {
        return getAs(key);
    }

    public int getInt (String key) {
        return getNumber(key).intValue();
    }

    public float getFloat (String key) {
        return getNumber(key).floatValue();
    }

    public boolean getBool (String key) {
        return getAs(key);
    }

    public HistoryReader getMap (String key) {
        HistoryReader map = getAs(key);
        return map == null ? new HistoryReader(false) : map;
    }

    public HistoryList getList (String key) {
        Object obj = get(key);
        if (obj == null) {
            return new HistoryList();
        } else if (obj instanceof HistoryList) {
            return (HistoryList) obj;
        }

        return new HistoryList(obj);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return set;
    }

    public static HistoryReader parse (Reader reader) throws IOException {
        return mapReader(reader, null, null);
    }

    public static HistoryReader parse (File file) throws IOException {
        return parse(new FileReader(file));
    }

    public static HistoryReader parse (String string) throws IOException {
        return parse(new StringReader(string));
    }

    private static HistoryReader mapReader (Reader reader, String firstKey, Object firstValue) throws IOException {
        int result;
        HashMap<String, Object> map = new HashMap<>();

        if (firstKey != null && firstValue != null) {
            map.put(firstKey, firstValue);
        }

        StringBuilder name = new StringBuilder();
        boolean isComment = false;

        while ((result = reader.read()) != -1) {
            char character = (char) result;
            if (isComment && (character == '\r' || character == '\n')) {
                isComment = false;
            }

            if (isComment || character == '\r' || character == '\n' || character == ' ' || character == '\t') {
                continue;
            }

            if (character == '#') {
                isComment = true;
            } else if (character == '=') {
                Object value = valueReader(reader);
                addToMap(map, name.toString(), value);
                name = new StringBuilder();
            } else if (character == '}') {
                break;
            } else {
                name.append(character);
            }
        }

        return new HistoryReader(map.entrySet());
    }

    private static void addToMap (Map<String, Object> map, String key, Object value) {
        Object current = map.get(key);
        if (current == null) {
            map.put(key, value);
            return;
        }

        HistoryList list;
        if (current instanceof HistoryList) {
            list = (HistoryList) current;
        } else {
            list = new HistoryList();
            list.add(current);
        }

        if (value instanceof HistoryList) {
            list.addAll((HistoryList) value);
        } else {
            list.add(value);
        }

        map.replace(key, current, list);
    }

    private static Object listReader (Reader reader) throws IOException {
        int result;
        HistoryList list = new HistoryList();
        boolean isComment = false;

        while ((result = reader.read()) != -1) {
            char character = (char) result;
            if (isComment) {
                if (character == '\n') {
                    isComment = false;
                }

                continue;
            }

            if (character == '\r' || character == '\n' || character == ' ' || character == '\t') {
                continue;
            } else if (character == '}') {
                break;
            }

            if (character == '#') {
                isComment = true;
            } else if (character == '=') {
                return mapReader(reader, list.get(0).toString(), valueReader(reader));
            } else if (character == '{') {
                list.add(listReader(reader));
            } else if (character == '"') {
                Object string = stringReader(reader, "", true);
                if (string instanceof String) {
                    String[] parts = ((String) string).split("=");
                    if (parts.length > 1) {
                        return mapReader(reader, parts[0], parseString(parts[1]));
                    }
                }

                list.add(string);
            } else if (Character.isDigit(character)) {
                list.add(numberReader(reader, character));
            } else {
                Object string = stringReader(reader, new String(new char[]{character}), false);
                if (string instanceof String) {
                    String[] parts = ((String) string).split("=");
                    if (parts.length > 1) {
                        return mapReader(reader, parts[0], parseString(parts[1]));
                    }
                }

                list.add(string);
            }
        }

        return list.size() == 0 ? null : list;
    }

    private static Object valueReader (Reader reader) throws IOException {
        int result;

        while ((result = reader.read()) != -1) {
            char character = (char) result;
            if (character == '\r' || character == '\n' || character == ' ' || character == '\t') {
                continue;
            }

            if (character == '{') {
                return listReader(reader);
            } else if (character == '"') {
                return stringReader(reader, "", true);
            } else if (Character.isDigit(character)) {
                return numberReader(reader, character);
            } else {
                return stringReader(reader, new String(new char[]{character}), false);
            }
        }

        return null;
    }

    private static Object stringReader (Reader reader, String initialValue, boolean isString) throws IOException {
        int result = 0;
        StringBuilder builder = new StringBuilder(initialValue);

        while ((result = reader.read()) != -1) {
            char character = (char) result;
            if (character == '"' && isString) {
                break;
            }

            if (!isString && (character == '\r' || character == '\n' || character == ' ' || character == '\t')) {
                break;
            }

            builder.append(character);
        }

        if (isString) {
            return builder.toString();
        }

        String string = builder.toString();
        return string.equals("yes") ? Boolean.TRUE : (string.equals("no") ? Boolean.FALSE : string);
    }

    private static Number numberReader (Reader reader, char firstChar) throws IOException {
        int result;
        StringBuilder builder = new StringBuilder(new String(new char[]{firstChar}));
        boolean isDecimal = false;

        while ((result = reader.read()) != -1) {
            char character = (char) result;

            if (character == '.') {
                if (isDecimal) {
                    break;
                }
                isDecimal = true;
            } else if (!Character.isDigit(character)) {
                break;
            }

            builder.append(character);
        }

        return isDecimal ? Float.parseFloat(builder.toString()) : Integer.parseInt(builder.toString());
    }

    private static Object parseString (String string) {
        char first = string.charAt(0);
        if (first == '"') {
            return string.substring(1, string.length() - 1);
        } else if (Character.isDigit(first)) {
            return string.contains(".") ? Float.parseFloat(string) : Integer.parseInt(string);
        }

        return string;
    }
}
