package org.hoi.various;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Finder {
    public static File find (File start, Condition<File> condition) {
        File[] files = start.listFiles();
        if (files == null) {
            return null;
        }

        for (File file: files) {
            if (file.isFile() && condition.apply(file)) {
                return file;
            }
        }

        for (File dir: files) {
            File find = find(dir, condition);
            if (find != null) {
                return find;
            }
        }

        return null;
    }

    public static List<File> findAll(File start, Condition<File> condition) {
        File[] files = start.listFiles();
        ArrayList<File> list = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && condition.apply(file)) {
                    list.add(file);
                } else if (file.isDirectory()) {
                    list.addAll(findAll(file, condition));
                }
            }
        }

        return list;
    }

    public static File find (Condition<File> condition) {
        File[] roots = File.listRoots();

        for (File root: roots) {
            File file = find(root, condition);
            if (file != null) {
                return file;
            }
        }

        return null;
    }

    public static List<File> findAll (Condition<File> condition) {
        File[] roots = File.listRoots();
        ArrayList<File> list = new ArrayList<>();

        for (File root: roots) {
            list.addAll(findAll(root, condition));
        }

        return list;
    }
}
