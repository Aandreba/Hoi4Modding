package org.hoi.classes.history;

import org.hoi.classes.enums.Resource;
import org.hoi.classes.enums.buildings.ProvinceBuilding;
import org.hoi.classes.enums.buildings.StateBuilding;
import org.hoi.classes.map.Province;
import org.hoi.classes.utils.ImageResult;
import org.hoi.classes.utils.history.HistoryList;
import org.hoi.classes.utils.history.HistoryReader;
import org.hoi.various.Bytes;
import org.hoi.various.Storeable;
import org.hoi.various.collection.MapUtils;
import org.hoi.various.collection.SetMap;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class State implements Storeable {
    public int id;
    public String name;
    public int manpower;
    public Category category;
    public float buildingsMaxLevelFactor;
    public boolean impassable;
    public Province[] provinces;

    // HISTORY
    public Country owner;
    public Country[] coreOf, claimedBy;

    public Map<Resource, Float> resources;
    public Map<Province, Integer> victoryPoints;
    public Map<StateBuilding, Integer> stateBuildings;
    public Map<Province, Map<ProvinceBuilding, Integer>> provinceBuildings;

    public State(int id, String name, int manpower, Category category, float buildingsMaxLevelFactor, boolean impassable, Province[] provinces, Country owner, Country[] coreOf, Country[] claimedBy, Map<Resource, Float> resources, Map<Province, Integer> victoryPoints, Map<StateBuilding, Integer> stateBuildings, Map<Province, Map<ProvinceBuilding, Integer>> provinceBuildings) {
        this.id = id;
        this.name = name;
        this.manpower = manpower;
        this.category = category;
        this.buildingsMaxLevelFactor = buildingsMaxLevelFactor;
        this.impassable = impassable;
        this.provinces = provinces;
        this.owner = owner;
        this.coreOf = coreOf;
        this.claimedBy = claimedBy;
        this.resources = resources;
        this.victoryPoints = victoryPoints;
        this.stateBuildings = stateBuildings;
        this.provinceBuildings = provinceBuildings;
    }

    public State (File file, Map<Integer, Province> provinces, Map<String, Country> countries) throws IOException {
        HistoryReader contents = HistoryReader.parse(file);
        contents = contents.getMap("state");
        HistoryReader history = contents.getMap("history");

        this.id = contents.getInt("id");
        this.name = file.getName().split("-")[1].split(".txt")[0];
        this.manpower = contents.getInt("manpower");
        this.category = Category.valueOf(contents.getString("state_category").toUpperCase());

        try {
            this.buildingsMaxLevelFactor = contents.getFloat("buildings_max_level_factor");
        } catch (Exception e) {
            this.buildingsMaxLevelFactor = 1f;
        }

        try {
            this.impassable = contents.getBool("impassable");
        } catch (Exception e) {
            this.impassable = false;
        }

        this.provinces = contents.getList("provinces").stream().
                map(Object::toString).
                filter(x -> x.chars().allMatch(Character::isDigit)).
                mapToInt(Integer::parseInt).
                mapToObj(provinces::get).toArray(Province[]::new);

        this.owner = countries.get(history.getString("owner"));
        this.coreOf = history.getList("add_core_of").stream().map(countries::get).filter(Objects::nonNull).toArray(Country[]::new);
        this.claimedBy = contents.getList("add_claim_by").stream().map(countries::get).filter(Objects::nonNull).toArray(Country[]::new);

        this.resources = new SetMap<>(contents.getMap("resources").set.stream().map(x -> new Map.Entry<Resource,Float>(){
            @Override
            public Resource getKey() {
                return Resource.valueOf(x.getKey().toUpperCase());
            }

            @Override
            public Float getValue() {
                return ((Number) x.getValue()).floatValue();
            }

            @Override
            public Float setValue (Float value) {
                return (Float) x.setValue(value);
            }
        }).collect(Collectors.toSet()));

        HistoryList vp = history.getList("victory_points");
        this.victoryPoints = new HashMap<>();

        for (int i=0;i<vp.size();i+=2) {
            int j = i + 1;

            Province province = provinces.get(vp.getInt(i));
            int points = vp.getInt(j);
            victoryPoints.put(province, points);
        }

        HistoryReader buildings = history.getMap("buildings");
        this.stateBuildings = new HashMap<>();
        this.provinceBuildings = new HashMap<>();

        for (Map.Entry<String,Object> entry: buildings.entrySet()) {
            try {
                StateBuilding building = StateBuilding.valueOf(entry.getKey().toUpperCase());
                int level = ((Number) entry.getValue()).intValue();
                this.stateBuildings.put(building, level);
            } catch (Exception ignore) {
                int id = Integer.parseInt(entry.getKey());
                Province province = provinces.get(id);

                Map<String, Object> map = null;
                if (entry.getValue() instanceof HistoryReader) {
                    map = (HistoryReader) entry.getValue();
                } else if (entry.getValue() instanceof HistoryList) {
                    HistoryList list = (HistoryList) entry.getValue();
                    map = new HashMap<>();

                    for (Object obj: list) {
                        map.putAll((HistoryReader) obj);
                    }
                }

                Map<ProvinceBuilding, Integer> buildingsMap = new HashMap<>();
                for (Map.Entry<String, Object> entry2 : map.entrySet()) {
                    ProvinceBuilding building = ProvinceBuilding.valueOf(entry2.getKey().toUpperCase());
                    int level = ((Number) entry2.getValue()).intValue();
                    buildingsMap.put(building, level);
                }

                this.provinceBuildings.put(province, buildingsMap);
            }
        }
    }

    public boolean isCoreOf (Country country) {
        return Arrays.stream(coreOf).anyMatch(x -> x == country);
    }

    public boolean isClaimedBy (Country country) {
        return Arrays.stream(claimedBy).anyMatch(x -> x == country);
    }

    public Rectangle getBounds () {
        List<ImageResult> images = Arrays.stream(this.provinces).filter(x -> x.image != null && x.position != null).map(x -> new ImageResult(x.image, x.position)).collect(Collectors.toList());
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

        return new Rectangle(left, top, width, height);
    }

    public ImageResult getImage () {
        List<ImageResult> images = Arrays.stream(this.provinces).filter(x -> x.image != null && x.position != null).map(x -> new ImageResult(x.image, x.position)).collect(Collectors.toList());
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

    @Override
    public State clone() {
        return new State(id, name, manpower, category, buildingsMaxLevelFactor, impassable, provinces.clone(), owner, coreOf.clone(), claimedBy.clone(), MapUtils.clone(resources), MapUtils.clone(victoryPoints), MapUtils.clone(stateBuildings), MapUtils.clone(provinceBuildings));
    }

    @Override
    public String toString() {
        return "State {" +
                "id=" + id +
                '}';
    }

    // RESOURCES, VICTORY POINTS, BUILDINGS

    @Override
    public byte[] getBytes() {
        byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);
        byte[] provincesBytes = Bytes.ofIntArray(x -> x.id, this.provinces);
        byte[] coreBytes = Bytes.ofArray(x -> x.tag.getBytes(StandardCharsets.UTF_8), this.coreOf);
        byte[] claimBytes = Bytes.ofArray(x -> x.tag.getBytes(StandardCharsets.UTF_8), this.claimedBy);

        byte[] resourcesBytes = Bytes.ofMap(x -> Bytes.ofInteger(x.ordinal()), Bytes::ofFloat, this.resources);
        byte[] vpBytes = Bytes.ofMap(x -> Bytes.ofInteger(x.id), Bytes::ofInteger, this.victoryPoints);
        byte[] stateBuildBytes = Bytes.ofMap(x -> Bytes.ofInteger(x.ordinal()), Bytes::ofInteger, this.stateBuildings);
        byte[] provBuildBytes = Bytes.ofMap(x -> Bytes.ofInteger(x.id), x -> Bytes.ofMap(z -> Bytes.ofInteger(z.ordinal()), Bytes::ofInteger, x), this.provinceBuildings);

        ByteBuffer buffer = ByteBuffer.allocate(72 + nameBytes.length + provincesBytes.length + coreBytes.length + claimBytes.length + resourcesBytes.length + vpBytes.length + stateBuildBytes.length + provBuildBytes.length);
        int pos = 8 + nameBytes.length;

        buffer.putInt(0, this.id);
        buffer.putInt(4, nameBytes.length);
        buffer.put(8, nameBytes);

        buffer.putInt(pos, manpower);
        buffer.putInt(pos += 4, category.ordinal());
        buffer.putFloat(pos += 4, buildingsMaxLevelFactor);
        buffer.put(pos += 4, impassable ? (byte) 1 : 0);

        buffer.putInt(pos += 1, provincesBytes.length);
        buffer.put(pos += 4, provincesBytes);

        buffer.put(pos += provincesBytes.length, this.owner.tag.getBytes(StandardCharsets.UTF_8));
        buffer.putInt(pos += 3, coreBytes.length);
        buffer.put(pos += 4, coreBytes);
        buffer.putInt(pos += coreBytes.length, claimBytes.length);
        buffer.put(pos += 4, claimBytes);

        buffer.putInt(pos += claimBytes.length, resourcesBytes.length);
        buffer.put(pos += 4, resourcesBytes);
        buffer.putInt(pos += resourcesBytes.length, vpBytes.length);
        buffer.put(pos += 4, vpBytes);
        buffer.putInt(pos += vpBytes.length, stateBuildBytes.length);
        buffer.put(pos += 4, stateBuildBytes);
        buffer.putInt(pos += stateBuildBytes.length, provBuildBytes.length);
        buffer.put(pos + 4, provBuildBytes);

        return buffer.array();
    }

    public static State getInstance (Map<Integer, Province> provinceMap, Map<String, Country> countryMap, byte... bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int id = buffer.getInt(0);
        int pos;

        int nameLen = buffer.getInt(4);
        String name = new String(Arrays.copyOfRange(bytes, 8, pos = 8 + nameLen), StandardCharsets.UTF_8);

        int manpower = buffer.getInt(pos);
        Category category = Category.values()[buffer.getInt(pos += 4)];
        float buildingsMaxLevelFactor = buffer.getFloat(pos += 4);
        boolean impassable = buffer.get(pos += 4) == 1;

        int provLen = buffer.getInt(pos += 1);
        Province[] provinces = Arrays.stream(Bytes.toIntArray(Arrays.copyOfRange(bytes, pos += 4, pos += provLen))).mapToObj(provinceMap::get).toArray(Province[]::new);
        Country owner = countryMap.get(new String(Arrays.copyOfRange(bytes, pos, pos += 3), StandardCharsets.UTF_8));

        int coreLen = buffer.getInt(pos);
        Country[] coreOf = Bytes.toArray(Country.class, x -> countryMap.get(new String(x, StandardCharsets.UTF_8)), Arrays.copyOfRange(bytes, pos += 4, pos += coreLen));

        int claimLen = buffer.getInt(pos);
        Country[] claimedBy = Bytes.toArray(Country.class, x -> countryMap.get(new String(x, StandardCharsets.UTF_8)), Arrays.copyOfRange(bytes, pos += 4, pos += claimLen)); // TODO possible change from +4 to +3

        int resourceLen = buffer.getInt(pos);
        Map<Resource, Float> resources = Bytes.toMap(x -> Resource.values()[Bytes.toInteger(x)], Bytes::toFloat, Arrays.copyOfRange(bytes, pos += 4, pos += resourceLen));

        int vpLen = buffer.getInt(pos);
        Map<Province, Integer> vp = Bytes.toMap(x -> provinceMap.get(Bytes.toInteger(x)), Bytes::toInteger, Arrays.copyOfRange(bytes, pos += 4, pos += vpLen));

        int stateBuildLen = buffer.getInt(pos);
        Map<StateBuilding, Integer> stateBuilding = Bytes.toMap(x -> StateBuilding.values()[Bytes.toInteger(x)], Bytes::toInteger, Arrays.copyOfRange(bytes, pos += 4, pos += stateBuildLen));

        int provBuildLen = buffer.getInt(pos);
        Map<Province, Map<ProvinceBuilding, Integer>> provinceBuilding = Bytes.toMap(x -> provinceMap.get(Bytes.toInteger(x)), x -> Bytes.toMap(z -> ProvinceBuilding.values()[Bytes.toInteger(z)], Bytes::toInteger, x), Arrays.copyOfRange(bytes, pos += 4, pos += provBuildLen));

        return new State(id, name, manpower, category, buildingsMaxLevelFactor, impassable, provinces, owner, coreOf, claimedBy, resources, vp, stateBuilding, provinceBuilding);
    }

    public enum Category {
        WASTELAND(0),
        ENCLAVE(0),
        TINY_ISLAND(0),
        SMALL_ISLAND(1),
        PASTORAL(1),
        RURAL(2),
        TOWN(4),
        LARGE_TOWN(5),
        CITY(6),
        LARGE_CITY(8),
        METROPOLIS(10),
        MEGALOPOLIS(12);

        public int buildings;
        Category (int buildings) {
            this.buildings = buildings;
        }
    }

    public static ArrayList<State> getStates (File dir, Map<Integer, Province> provinces, Map<String, Country> countries) throws IOException {
        if (!dir.exists()) {
            throw new FileNotFoundException();
        } else if (!dir.isDirectory()) {
            throw new FileSystemException("File provided isn't a directory");
        }

        File[] files = dir.listFiles();
        assert files != null;

        ArrayList<State> list = new ArrayList<>();
        for (File file: files) {
            try {
                State state = new State(file, provinces, countries);
                list.add(state);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return list;
    }
}
