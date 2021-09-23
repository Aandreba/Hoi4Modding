package org.hoi.various;

import java.util.Arrays;

public class Arrayx {
    public static int[] toPrimitive (Integer... regular) {
        return Arrays.stream(regular).mapToInt(x -> x).toArray();
    }

    public static long[] toPrimitive (Long... regular) {
        return Arrays.stream(regular).mapToLong(x -> x).toArray();
    }

    public static double[] toPrimitive (Double... regular) {
        return Arrays.stream(regular).mapToDouble(x -> x).toArray();
    }
}
