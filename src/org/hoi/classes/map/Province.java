package org.hoi.classes.map;

import org.hoi.Project;
import org.hoi.classes.history.State;
import org.hoi.classes.utils.ImageResult;
import org.hoi.various.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class Province implements Storeable {
    final public int id;
    final public Color color;
    final public Type type;
    final public boolean coastal;
    final public Terrain terrain;
    final public Point position;
    final public BufferedImage image;

    public Province (int id, Color color, Type type, boolean coastal, Terrain terrain, BufferedImage image, Point position) {
        this.id = id;
        this.color = color;
        this.type = type;
        this.coastal = coastal;
        this.terrain = terrain;
        this.image = image;
        this.position = position;
    }

    public State getState (Project project) {
        return project.getStates().stream().filter(x -> Arrays.stream(x.provinces).anyMatch(k -> k == this)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return "Province{" +
                "id=" + id +
                '}';
    }

    @Override
    public byte[] getBytes() {
        byte[] imageBytes = Bytes.ofImage(this.image);
        ByteBuffer buffer = ByteBuffer.allocate(25 + imageBytes.length);

        buffer.putInt(0, this.id);
        buffer.putInt(4, this.color.getRGB());
        buffer.putInt(8, this.type.ordinal());
        buffer.put(12, this.coastal ? (byte) 1 : 0);
        buffer.putInt(13, this.terrain.ordinal());

        if (this.position == null) {
            buffer.putLong(17, -1);
        } else {
            buffer.putInt(17, this.position.x);
            buffer.putInt(21, this.position.y);
            buffer.put(25, imageBytes);
        }

        return buffer.array();
    }

    public static Province getInstance (byte... bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        int id = buffer.getInt(0);
        Color color = new Color(buffer.getInt(4), true);
        Type type = Type.values()[buffer.getInt(8)];
        boolean coastal = buffer.get(12) == 1;
        Terrain terrain = Terrain.values()[buffer.getInt(13)];
        Point position;
        BufferedImage image;

        if (buffer.getLong(17) == -1) {
            position = null;
            image = null;
        } else {
            position = new Point(buffer.getInt(17), buffer.getInt(21));
            image = Bytes.toImage(Arrays.copyOfRange(bytes, 25, bytes.length));
        }

        return new Province(id, color, type, coastal, terrain, image, position);
    }

    final private static double ln64 = Math.log(64);
    public static ArrayList<Province> getProvinces (File dir) throws IOException {
        // PROVINCE SHAPE
        double scale = Math.min(1, Math.max(0, Math.log(Threading.MAX_THREADS) / ln64));

        BufferedImage read = ImageIO.read(new File(dir, "map/provinces.bmp"));
        BufferedImage img = Imagex.resizeTo(read, (int) (read.getWidth() * scale), (int) (read.getHeight() * scale));
        HashMap<Color, ArrayList<Point>> pixels = new HashMap<>();

        // GENERATE ARRAYS
        for (int i=0;i<img.getWidth();i++) {
            for (int j=0;j<img.getHeight();j++) {
                Color color = Imagex.getPixel(img, i, j);
                ArrayList<Point> list = pixels.computeIfAbsent(color, x -> new ArrayList<>());
                list.add(new Point(i, j));
            }

            System.out.println(i * 100 / img.getWidth());
        }

        HashMap<Color, ImageResult> images = new HashMap<>();
        int q = 0;

        for (Map.Entry<Color, ArrayList<Point>> entry: pixels.entrySet()) {
            images.put(entry.getKey(), generateFrom(entry.getValue()));
            System.out.println(q++ * 100 / pixels.size());
        }

        // PROVINCE INFO
        ArrayList<Province> provinces = new ArrayList<>();
        String definitionContent = Files.readString(new File(dir, "map/definition.csv").toPath());
        String[] rows = definitionContent.split(System.lineSeparator());

        for (String row: rows) {
            String[] cols = row.split(";");

            int id = Integer.parseInt(cols[0]);
            Color color = new Color(Integer.parseInt(cols[1]), Integer.parseInt(cols[2]), Integer.parseInt(cols[3]));
            Type type = Type.valueOf(cols[4].toUpperCase());
            boolean coastal = Boolean.parseBoolean(cols[5]);
            Terrain terrain = Terrain.valueOf(cols[6].toUpperCase());

            ImageResult result = images.get(color);
            if (result == null) {
                provinces.add(new Province(id, color, type, coastal, terrain, null, null));
            } else {
                provinces.add(new Province(id, color, type, coastal, terrain, result.img, result.point));
            }
        }

        return provinces;
    }

    private static ImageResult generateFrom (ArrayList<Point> pixels) {
        int left = Integer.MAX_VALUE, right = -1, top = Integer.MAX_VALUE, bottom = -1;

        for (Point point: pixels) {
            if (point == null) {
                continue;
            }

            if (point.x < left) {
                left = point.x;
            }

            if (point.x > right) {
                right = point.x;
            }

            if (point.y < top) {
                top = point.y;
            }

            if (point.y > bottom) {
                bottom = point.y;
            }
        }

        int width = Math.max(1, right - left + 1);
        int height = Math.max(1, bottom - top + 1);

        int finalLeft = left;
        int finalTop = top;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Threading threading = new Threading(pixels.size()) {
            @Override
            public void epoch(int pos) {
                Point pixel = pixels.get(pos);
                int i = pixel.x - finalLeft;
                int j = pixel.y - finalTop;

                img.setRGB(i, j, 0xFFFFFFFF);
            }
        };

        threading.run();
        return new ImageResult(img, new Point(left, top));
    }

    public enum Type {
        LAND,
        SEA,
        LAKE
    }

    public enum Terrain {
        DESERT,
        FOREST,
        HILLS,
        JUNGLE,
        MARSH,
        MOUNTAIN,
        PLAINS,
        URBAN,
        LAKES,
        OCEAN,
        UNKNOWN
    }
}
