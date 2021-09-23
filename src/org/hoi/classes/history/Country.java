package org.hoi.classes.history;

import org.hoi.classes.enums.Ideology;
import org.hoi.classes.gfx.Flag;
import org.hoi.classes.utils.Contents;
import org.hoi.classes.utils.ImageResult;
import org.hoi.various.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Country implements Storeable {
    public String tag;
    public String name;
    public Color color;

    public int researchSlots;
    public float stability, warSupport;

    public Ideology rulingParty;
    public HashMap<Ideology, Integer> popularities;
    public Flag flag;

    public Country (String tag, String name, Color color, int researchSlots, float stability, float warSupport, Ideology rulingParty, HashMap<Ideology, Integer> popularities, Flag flag) {
        this.tag = tag;
        this.name = name;
        this.color = color;
        this.researchSlots = researchSlots;
        this.stability = stability;
        this.warSupport = warSupport;
        this.rulingParty = rulingParty;
        this.popularities = popularities;
        this.flag = flag;
    }

    @Override
    public byte[] getBytes() {
        byte[] tagBytes = this.tag.getBytes(StandardCharsets.UTF_8);
        byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);
        byte[] popularityBytes = Bytes.ofMap(x -> Bytes.ofInteger(x.ordinal()), Bytes::ofInteger, popularities);
        byte[] flagBytes = flag.getBytes();

        ByteBuffer buffer = ByteBuffer.allocate(32 + tagBytes.length + nameBytes.length + popularityBytes.length + flagBytes.length);
        int pos = 3;

        buffer.put(0, tagBytes);
        buffer.putInt(pos, nameBytes.length);
        buffer.put(pos += 4, nameBytes);
        pos += nameBytes.length;

        buffer.putInt(pos, this.color.getRGB());
        buffer.putInt(pos += 4, this.researchSlots);
        buffer.putFloat(pos += 4, this.stability);
        buffer.putFloat(pos += 4, this.warSupport);
        buffer.putInt(pos += 4, this.rulingParty.ordinal());

        buffer.putInt(pos += 4, popularityBytes.length);
        buffer.put(pos += 4, popularityBytes);
        buffer.put(pos + popularityBytes.length, flagBytes);

        return buffer.array();
    }

    public static Country getInstance (byte... bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int pos = 3;

        String tag = new String(Arrays.copyOf(bytes, 3), StandardCharsets.UTF_8);
        int nameLen = buffer.getInt(pos);
        String name = new String(Arrays.copyOfRange(bytes, pos += 4, pos += nameLen), StandardCharsets.UTF_8);

        Color color = new Color(buffer.getInt(pos), true);
        int researchSlots = buffer.getInt(pos += 4);
        float stability = buffer.getFloat(pos += 4);
        float warSupport = buffer.getFloat(pos += 4);
        Ideology rulingParty = Ideology.values()[buffer.getInt(pos += 4)];

        int mapLen = buffer.getInt(pos += 4);
        HashMap<Ideology, Integer> popularities = Bytes.toMap(x -> Ideology.values()[Bytes.toInteger(x)], Bytes::toInteger, Arrays.copyOfRange(bytes, pos += 4, pos += mapLen));
        Flag flag = Flag.getInstance(Arrays.copyOfRange(bytes, pos, bytes.length));

        return new Country(tag, name, color, researchSlots, stability, warSupport, rulingParty, popularities, flag);
    }

    public BufferedImage getFlag () {
        BufferedImage flag = this.flag.getOfIdeology(this.rulingParty);
        if (flag == null) {
            flag = this.flag.getDefault();
        }

        return flag;
    }

    public ImageResult getImage () {
        List<ImageResult> images = Arrays.stream(Config.getHoi4States()).filter(x -> x.owner == this).map(State::getImage).collect(Collectors.toList());
        int left = Integer.MAX_VALUE, right = -1, top = Integer.MAX_VALUE, bottom = -1;

        for (ImageResult result: images) {
            int __right = result.point.x + result.img.getWidth();
            int __bottom = result.point.y + result.img.getHeight();

            if (result.point.x < left) {
                left = result.point.x;
            }

            if (__right > right) {
                right = __right;
            }

            if (result.point.y < top) {
                top = result.point.y;
            }

            if (__bottom > bottom) {
                bottom = __bottom;
            }
        }

        int width = Math.max(1, right - left);
        int height = Math.max(1, bottom - top);

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = img.createGraphics();

        for (ImageResult result: images) {
            graphics.drawImage(result.img, result.point.x - left, result.point.y - top, null);
        }

        graphics.dispose();
        return new ImageResult(img, new Point(left, top));
    }

    public static ArrayList<Country> getCountries (File basePath, String countryTagsPath) throws IOException {
        File countryTag = new File(basePath, countryTagsPath);
        if (!countryTag.exists()) {
            throw new FileNotFoundException();
        } else if (!countryTag.isFile()) {
            throw new FileSystemException("File provided isn't a file");
        }

        String contents = Files.readString(countryTag.toPath());
        String[] rows = contents.split("\\n");

        ArrayList<Country> list = new ArrayList<>();
        for (String row: rows) {
            String tag = row.substring(0, 3);
            String name;
            Color color;

            int researchSlots;
            float stability, warSupport;
            Ideology rulingParty;
            HashMap<Ideology, Integer> popularities = new HashMap<>();

            // COUNTRY FILE
            String commonPath = Regex.firstMatch(row, "\".+\"");
            File common = new File(basePath, "common/"+commonPath.substring(1, commonPath.length() - 1));
            name = common.getName().split("\\.")[0];

            String commonContents = Files.readString(common.toPath());
            String colorString = Regex.firstMatch(commonContents, "color\\s*=\\s*(rgb)*\\s*\\{\\n*\\s*.+\\n*\\s*\\}");
            colorString = colorString.split("\\s*=\\s*")[1].replaceAll("\\s*#\\s*.+", "\n").trim();

            String[] colorArray = colorString.substring(colorString.indexOf('{')+1, colorString.length() - 1).trim().split("\\s+");
            color = new Color(
                    Integer.parseInt(colorArray[0]),
                    Integer.parseInt(colorArray[1]),
                    Integer.parseInt(colorArray[2])
            );

            // HISTORY
            File historyFile = Arrays.stream(new File(basePath, "history/countries").listFiles()).filter(x -> x.getName().startsWith(tag)).findFirst().get();
            Contents historyContents = new Contents(Files.readString(historyFile.toPath()));

            try {
                researchSlots = historyContents.getInt("set_research_slots");
            } catch (Exception e) {
                researchSlots = 2;
            }

            try {
                stability = historyContents.getFloat("set_stability");
            } catch (Exception e) {
                stability = 0.5f;
            }

            try {
                warSupport = historyContents.getFloat("set_war_support");
            } catch (Exception e) {
                warSupport = 0.5f;
            }

            try {
                rulingParty = Ideology.valueOf(historyContents.getValue("ruling_party").toUpperCase());
            } catch (Exception e) {
                rulingParty = Ideology.NEUTRALITY;
            }

            for (Ideology ideology: Ideology.values()) {
                try {
                    popularities.put(ideology, historyContents.getInt(ideology.name().toLowerCase()));
                } catch (Exception e) {
                    popularities.put(ideology, 0);
                }
            }

            // FLAG
            list.add(new Country(tag, name, color, researchSlots, stability, warSupport, rulingParty, popularities, new Flag(basePath, tag)));
        }

        return list;
    }

    @Override
    public String toString() {
        return tag + " - " + name;
    }
}
