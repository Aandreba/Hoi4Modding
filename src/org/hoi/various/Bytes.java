package org.hoi.various;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class Bytes {
    public static byte[] ofInteger (int value) {
        return new byte[] { (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value };
    }

    public static int toInteger (byte... bytes) {
        return (bytes[3] & 0xFF) | ((bytes[2] & 0xFF) << 8) | ((bytes[1] & 0xFF) << 16) | ((bytes[0] & 0xFF) << 24);
    }

    public static byte[] ofFloat (float value) {
        return ofInteger(Float.floatToIntBits(value));
    }

    public static float toFloat (byte... bytes) {
        return Float.intBitsToFloat(toInteger(bytes));
    }

    public static byte[] ofImage (BufferedImage image) {
        if (image == null) {
            return new byte[0];
        }

        int width = image.getWidth();
        int height = image.getHeight();
        int pixels = width * height;
        int type = image.getType();

        ByteBuffer buffer = ByteBuffer.allocate(8 + width * height * 4);
        buffer.putInt(0, type);
        buffer.putInt(4, height);

        for (int i=0;i<pixels;i++) {
            int x = i / height;
            int y = i % height;
            buffer.putInt(8 + 4 * i, image.getRGB(x, y));
        }

        return buffer.array();
    }

    public static BufferedImage toImage (byte... bytes) {
        if (bytes.length == 0) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        int type = buffer.getInt(0);
        int height = buffer.getInt(4);
        int width = (bytes.length - 8) / (4 * height);
        int pixels = width * height;

        BufferedImage image = new BufferedImage(width, height, type);
        for (int i=0;i<pixels;i++) {
            int x = i / height;
            int y = i % height;
            image.setRGB(x, y, buffer.getInt(4 * i + 8));
        }

        return image;
    }

    public static byte[] ofIntArray (int... values) {
        ByteBuffer buffer = ByteBuffer.allocate(values.length * 4);
        for (int i=0;i<values.length;i++) {
            buffer.putInt(4 * i, values[i]);
        }

        return buffer.array();
    }

    public static <T> byte[] ofIntArray (Function<T, Integer> convert, T... values) {
        ByteBuffer buffer = ByteBuffer.allocate(values.length * 4);
        for (int i=0;i<values.length;i++) {
            buffer.putInt(4 * i, convert.apply(values[i]));
        }

        return buffer.array();
    }

    public static int[] toIntArray (byte... bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int[] values = new int[bytes.length / 4];

        for (int i=0;i<values.length;i++) {
            values[i] = buffer.getInt(4 * i);
        }

        return values;
    }

    public static <T> byte[] ofArray (Function<T, byte[]> convert, T... values) {
        byte[] bytes = new byte[0];

        for (int i=0;i<values.length;i++) {
            byte[] value = convert.apply(values[i]);
            byte[] newBytes = new byte[bytes.length + 4 + value.length];

            System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
            System.arraycopy(ofInteger(value.length), 0, newBytes, bytes.length, 4);
            System.arraycopy(value, 0, newBytes, bytes.length + 4, value.length);

            bytes = newBytes;
        }

        return bytes;
    }

    public static <T extends Serializable> byte[] ofArray (T... values) {
        return ofArray(Objectx::getBytes, values);
    }

    public static <T extends Storeable> byte[] ofArray (T... values) {
        return ofArray(Storeable::getBytes, values);
    }

    public static <T> T[] toArray (Class<T> type, Function<byte[], T> convert, byte... bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        T[] array = (T[]) Array.newInstance(type, 0);
        int pos = 0;

        while (pos < bytes.length) {
            int len = buffer.getInt(pos);
            pos += 4;

            byte[] valueBytes = Arrays.copyOfRange(bytes, pos, pos = pos + len);
            T value = convert.apply(valueBytes);

            T[] newArray = (T[]) Array.newInstance(type, array.length + 1);
            System.arraycopy(array, 0, newArray, 0, array.length);
            newArray[array.length] = value;

            array = newArray;
        }

        return array;
    }

    public static <T extends Serializable> T[] toArray (Class<T> type, byte... bytes) {
        return toArray(type, x -> {
            try {
                return (T) Objectx.getObject(x);
            } catch (Exception e) {
                return null;
            }
        }, bytes);
    }

    public static <T> byte[] ofList (Function<T, byte[]> convert, List<T> values) {
        byte[] bytes = new byte[0];

        for (int i=0;i<values.size();i++) {
            byte[] value = convert.apply(values.get(i));
            byte[] newBytes = new byte[bytes.length + 4 + value.length];

            System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
            System.arraycopy(ofInteger(value.length), 0, newBytes, bytes.length, 4);
            System.arraycopy(value, 0, newBytes, bytes.length, value.length);

            bytes = newBytes;
        }

        return bytes;
    }

    public static <T extends Serializable> byte[] ofSerializableList (List<T> values) {
        return ofList(Objectx::getBytes, values);
    }

    public static <T extends Storeable> byte[] ofStoreableList (List<T> values) {
        return ofList(Storeable::getBytes, values);
    }

    public static <T> ArrayList<T> toList (Function<byte[], T> convert, byte... bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        ArrayList<T> list = new ArrayList<>();
        int pos = 0;

        while (pos < bytes.length) {
            int len = buffer.getInt(pos);
            pos += 4;

            byte[] valueBytes = Arrays.copyOfRange(bytes, pos, pos = pos + len);
            list.add(convert.apply(valueBytes));
        }

        return list;
    }

    public static <T extends Serializable> ArrayList<T> toList (byte... bytes) {
        return toList(x -> {
            try {
                return (T) Objectx.getObject(bytes);
            } catch (Exception e) {
                return null;
            }
        });
    }

    public static <K,V> byte[] ofMap (Function<K, byte[]> convertKey, Function<V, byte[]> convertValue, Map<K,V> map) {
        byte[] bytes = new byte[0];

        for (Map.Entry<K,V> entry: map.entrySet()) {
            byte[] key = convertKey.apply(entry.getKey());
            byte[] value = convertValue.apply(entry.getValue());

            byte[] newBytes = new byte[8 + bytes.length + key.length + value.length];
            System.arraycopy(bytes, 0, newBytes, 0, bytes.length);

            int pos = bytes.length + 4;
            System.arraycopy(ofInteger(key.length), 0, newBytes, bytes.length, 4);
            System.arraycopy(key, 0, newBytes, pos, key.length);

            System.arraycopy(ofInteger(value.length), 0, newBytes, pos = pos + key.length, 4);
            System.arraycopy(value, 0, newBytes, pos + 4, value.length);

            bytes = newBytes;
        }

        return bytes;
    }

    public static <K,V> HashMap<K,V> toMap (Function<byte[], K> convertKey, Function<byte[], V> convertValue, byte... bytes) {
        HashMap<K,V> map = new HashMap<>();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        int pos = 0;
        while (pos < bytes.length) {
            int keyLength = buffer.getInt(pos);
            K key = convertKey.apply(Arrays.copyOfRange(bytes, pos += 4, pos += keyLength));

            if (key == null) {
                break;
            }

            int valueLength = buffer.getInt(pos);
            V value = convertValue.apply(Arrays.copyOfRange(bytes, pos += 4, pos += valueLength));

            map.put(key, value);
        }

        return map;
    }

    public static <K extends Serializable, V> byte[] ofMap (Function<V, byte[]> convertValue, Map<K,V> map) {
        return ofMap(Objectx::getBytes, convertValue, map);
    }

    public static <K extends Serializable, V> HashMap<K,V> toMap (Function<byte[], V> convertValue, byte... bytes) {
        return toMap(x -> {
            try {
                return (K) Objectx.getObject(x);
            } catch (Exception e) {
                return null;
            }
        }, convertValue, bytes);
    }

    public static byte[] ofMap (Map<? extends Serializable, BufferedImage> map) {
        return ofMap(Bytes::ofImage, map);
    }

    public static <K extends Serializable> HashMap<K, BufferedImage> toImageMap(byte... bytes) {
        return toMap(Bytes::toImage, bytes);
    }
}
