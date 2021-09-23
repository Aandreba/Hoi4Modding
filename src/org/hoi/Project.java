package org.hoi;

import org.hoi.classes.history.Country;
import org.hoi.classes.map.Province;
import org.hoi.classes.history.State;
import org.hoi.classes.utils.ImageResult;
import org.hoi.classes.utils.history.HistoryReader;
import org.hoi.various.Config;
import org.hoi.various.Imagex;
import org.hoi.various.collection.KeyedValues;
import org.hoi.various.collection.MapUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Project {
    final public String name;
    final private ArrayList<Province> provinces;
    final private ArrayList<State> states;
    final private ArrayList<Country> countries;

    public Project (String name) {
        this.name = name;
        this.provinces = new ArrayList<>();
        this.states = new ArrayList<>();
        this.countries = new ArrayList<>();
    }

    public void setOwner (State state, Country country) {
        if (this.states.contains(state)) {
            state.owner = country;
        } else {
            state = state.clone();
            state.owner = country;
            this.states.add(state);
        }
    }

    public List<Province> getProvinces () {
        ArrayList<Province> provinces = new ArrayList<>();
        provinces.addAll(Config.getHoi4ProvincesKeyed().list);
        provinces.removeIf(x -> this.provinces.stream().anyMatch(k -> k.id == x.id));
        provinces.addAll(this.provinces);

        return provinces;
    }

    public KeyedValues<Integer, Province> getProvincesKeyed () {
        return new KeyedValues<Integer, Province>(getProvinces()) {
            @Override
            public Integer getKey(Province value) {
                return value.id;
            }
        };
    }

    public Province getProvince (int id) {
        return getProvincesKeyed().get(id);
    }

    public List<State> getStates () {
        ArrayList<State> states = new ArrayList<>();
        states.addAll(Config.getHoi4StatesKeyed().list);
        states.removeIf(x -> this.states.stream().anyMatch(k -> k.id == x.id));
        states.addAll(this.states);

        return states;
    }

    public KeyedValues<Integer, State> getStatesKeyed () {
        return new KeyedValues<Integer, State>(getStates()) {
            @Override
            public Integer getKey(State value) {
                return value.id;
            }
        };
    }

    public State getState (int id) {
        return getStatesKeyed().get(id);
    }

    public List<Country> getCountries () {
        ArrayList<Country> countries = new ArrayList<>();
        countries.addAll(Config.getHoi4CountriesKeyed().list);
        countries.removeIf(x -> this.countries.stream().anyMatch(k -> k.tag.equals(x.tag)));
        countries.addAll(this.countries);

        return countries;
    }

    public KeyedValues<String, Country> getCountriesKeyed () {
        return new KeyedValues<String, Country>(getCountries()) {
            @Override
            public String getKey (Country value) {
                return value.tag;
            }
        };
    }

    public Country getCountry (String tag) {
        return getCountriesKeyed().get(tag);
    }

    public BufferedImage getCountryMap() {
        class Result {
            final public ImageResult image;
            final public Country country;

            public Result(ImageResult image, Country country) {
                this.image = image;
                this.country = country;
            }
        }

        List<Country> countries = getCountries();
        List<Result> images = countries.stream().map(x -> new Result(x.getImage(), x)).collect(Collectors.toList());
        int left = Integer.MAX_VALUE, right = -1, top = Integer.MAX_VALUE, bottom = -1;

        for (Result result: images) {
            int __right = result.image.point.x + result.image.img.getWidth();
            int __bottom = result.image.point.y + result.image.img.getHeight();

            if (result.image.point.x < left) {
                left = result.image.point.x;
            }

            if (__right > right) {
                right = __right;
            }

            if (result.image.point.y < top) {
                top = result.image.point.y;
            }

            if (__bottom > bottom) {
                bottom = __bottom;
            }
        }

        int width = Math.max(1, right - left);
        int height = Math.max(1, bottom - top);

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = img.createGraphics();

        for (Result result: images) {
            BufferedImage clone = Imagex.clone(result.image.img);
            Imagex.tint(clone, result.country.color);

            graphics.drawImage(clone, result.image.point.x - left, result.image.point.y - top, null);
        }

        graphics.dispose();
        return img;
    }

    public BufferedImage getStateMap (Function<State, Color> colorFunction) {
        class Result {
            final public ImageResult image;
            final public State state;

            public Result(ImageResult image, State country) {
                this.image = image;
                this.state = country;
            }
        }

        List<State> states = getStates();
        List<Result> images = states.stream().map(x -> new Result(x.getImage(), x)).collect(Collectors.toList());
        int left = Integer.MAX_VALUE, right = -1, top = Integer.MAX_VALUE, bottom = -1;

        for (Result result: images) {
            int __right = result.image.point.x + result.image.img.getWidth();
            int __bottom = result.image.point.y + result.image.img.getHeight();

            if (result.image.point.x < left) {
                left = result.image.point.x;
            }

            if (__right > right) {
                right = __right;
            }

            if (result.image.point.y < top) {
                top = result.image.point.y;
            }

            if (__bottom > bottom) {
                bottom = __bottom;
            }
        }

        int width = Math.max(1, right - left);
        int height = Math.max(1, bottom - top);

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = img.createGraphics();

        for (Result result: images) {
            BufferedImage clone = Imagex.drawBorder(Imagex.clone(result.image.img), Color.BLACK);
            Imagex.tint(clone, colorFunction.apply(result.state));

            graphics.drawImage(clone, result.image.point.x - left, result.image.point.y - top, null);
        }

        graphics.dispose();
        return img;
    }

    public void export (File dir) throws IOException {
        dir = new File(dir, this.name);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new FileSystemException("Error generating directory");
        }

        File modFile = new File(dir.getParentFile(), name+".mod");
        File definition = new File(dir, "descriptor.mod");

        StringBuilder definitionStr = new StringBuilder("version=\"1\"\n");
        definitionStr.append("tags={\n\t\"Historical\"\n}\n");
        definitionStr.append("name=\"").append(this.name).append("\"\n");
        definitionStr.append("supported_version=\"1.10.8\"");

        Files.writeString(definition.toPath(), definitionStr.toString());
        Files.writeString(modFile.toPath(), definitionStr.append("\npath=\"").append(dir.getCanonicalPath().replace('\\', '/')).append('"').toString());
        exportStates(dir);
    }

    private void exportStates (File dir) throws IOException {
        File subdir = new File(dir, "history/states");
        if (!subdir.exists() && !subdir.mkdirs()) {
            throw new FileSystemException("Error generating directory");
        }

        for (State state: this.states) {
            HistoryReader map = new HistoryReader();
            HistoryReader history = new HistoryReader();

            map.put("id", state.id);
            map.putString("name", "STATE_"+state.id);
            map.put("manpower", state.manpower);
            map.put("state_category", state.category);
            map.put("buildings_max_level_factor", state.buildingsMaxLevelFactor);
            map.put("impassable", state.impassable);
            map.put("provinces", Arrays.stream(state.provinces).map(x -> x.id).collect(Collectors.toList()));
            map.put("resources", MapUtils.map(Enum::ordinal, x -> x, state.resources));

            history.put("owner", state.owner.tag);
            for (Country country: state.coreOf) {
                history.put();
            }
        }
    }
}
