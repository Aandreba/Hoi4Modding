package org.hoi.various;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class Objectx {
    public static <T extends Serializable> byte[] getBytes (T object) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    public static <T extends Serializable> Path saveTo (T object, File file) throws IOException {
        return Files.write(file.toPath(), getBytes(object));
    }

    public static <T extends Serializable> T getObject (InputStream is) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(is);
        T obj = (T) ois.readObject();
        ois.close();

        return obj;
    }

    public static <T extends Serializable> T getObject (byte... bytes) throws IOException, ClassNotFoundException {
        return getObject(new ByteArrayInputStream(bytes));
    }

    public static <T extends Serializable> T getObject (File file) throws IOException, ClassNotFoundException {
        return getObject(new FileInputStream(file));
    }
}
