package org.hoi.classes.utils.history;

import java.util.ArrayList;
import java.util.Collections;

public class HistoryList extends ArrayList<Object> {
    protected HistoryList (Object... values) {
        super();
        Collections.addAll(this, values);
    }

    public <T> T getAs (int pos) {
        return (T) get(pos);
    }

    public <T> T getAs (Class<T> type, int pos) {
        return getAs(pos);
    }

    public String getString (int pos) {
        return get(pos).toString();
    }

    public Number getNumber (int pos) {
        return getAs(pos);
    }

    public int getInt (int pos) {
        return getNumber(pos).intValue();
    }

    public float getFloat (int pos) {
        return getNumber(pos).floatValue();
    }

    public boolean getBool (int pos) {
        return getAs(pos);
    }

    public HistoryReader getMap (int pos) {
        HistoryReader map = getAs(pos);
        return map == null ? new HistoryReader() : map;
    }

    public HistoryList getList (int pos) {
        Object obj = get(pos);
        if (obj == null) {
            return new HistoryList();
        } else if (obj instanceof HistoryList) {
            return (HistoryList) obj;
        }

        return new HistoryList(obj);
    }
}
