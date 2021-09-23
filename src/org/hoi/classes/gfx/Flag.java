package org.hoi.classes.gfx;

import org.hoi.classes.enums.Ideology;
import org.hoi.classes.utils.TGAReader;
import org.hoi.various.Bytes;
import org.hoi.various.Storeable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

public class Flag implements Storeable {
    private BufferedImage defaultFlag;
    private HashMap<Ideology, BufferedImage> byIdeology;

    public Flag (BufferedImage defaultFlag, HashMap<Ideology, BufferedImage> byIdeology) {
        this.defaultFlag = defaultFlag;
        this.byIdeology = byIdeology;
    }

    public Flag (File basePath, String tag) {
        this.byIdeology = new HashMap<>();
        File folder = new File(basePath, "gfx/flags");

        try {
            defaultFlag = TGAReader.read(new File(folder, tag + ".tga").toPath());
        } catch (Exception e) {
            defaultFlag = null;
        }

        for (Ideology ideology: Ideology.values()) {
            BufferedImage img = null;
            try {
                img = TGAReader.read(new File(folder, tag+"_"+ideology.name().toLowerCase()+".tga").toPath());
            } catch (Exception ignore) {}

            byIdeology.put(ideology, img);
        }
    }

    public BufferedImage getDefault () {
        return defaultFlag;
    }

    public BufferedImage getOfIdeology (Ideology ideology) {
        return byIdeology.get(ideology);
    }

    @Override
    public byte[] getBytes () {
        byte[] defaultFlag = Bytes.ofImage(this.defaultFlag);
        byte[] map = Bytes.ofMap(this.byIdeology);

        byte[] bytes = new byte[4 + defaultFlag.length + map.length];
        System.arraycopy(Bytes.ofInteger(defaultFlag.length), 0, bytes, 0, 4);
        System.arraycopy(defaultFlag, 0, bytes, 4, defaultFlag.length);
        System.arraycopy(map, 0, bytes, 4 + defaultFlag.length, map.length);

        return bytes;
    }

    public static Flag getInstance (byte... bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int defaultLen = buffer.getInt(0);

        int alpha = 4 + defaultLen;
        BufferedImage defaultFlag = Bytes.toImage(Arrays.copyOfRange(bytes, 4, alpha));
        HashMap<Ideology, BufferedImage> byIdeology = Bytes.toImageMap(Arrays.copyOfRange(bytes, alpha, bytes.length));

        return new Flag(defaultFlag, byIdeology);
    }
}
