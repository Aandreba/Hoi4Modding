package org.hoi.various;

import org.hoi.classes.history.Country;
import org.hoi.classes.history.State;
import org.hoi.classes.map.Province;
import org.hoi.various.collection.KeyedList;
import org.hoi.various.collection.KeyedValues;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;

public class Config implements Serializable {
    enum OperatingSystem {
        WINDOWS,
        MACOS,
        LINUX,
        SOLARIS,
        OTHER
    }

    final private static File CONFIG_FILE = new File("tmp");
    final public static OperatingSystem OS;

    private static File DEFAULT_DIR = new File(System.getProperty("user.home"));
    private static File HOI4_DIR;

    private static Province[] HOI4_PROVINCES;
    private static KeyedValues<Integer, Province> HOI4_PROVINCES_KEYED;

    private static State[] HOI4_STATES;
    private static KeyedValues<Integer, State> HOI4_STATES_KEYED;

    private static Country[] HOI4_COUNTRIES;
    private static KeyedValues<String, Country> HOI4_COUNTRIES_KEYED;

    static {
        // LOAD
        if (CONFIG_FILE.exists()) {
            try {
                loadConfig(CONFIG_FILE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            CONFIG_FILE.mkdir();
        }

        // OS DETECTION
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            OS = OperatingSystem.WINDOWS;
        } else if (os.contains("mac")) {
            OS = OperatingSystem.MACOS;
        } else if (os.contains("nix") | os.contains("nux") | os.contains("aix")) {
            OS = OperatingSystem.LINUX;
        } else if (os.contains("sunos")) {
            OS = OperatingSystem.SOLARIS;
        } else {
            OS = OperatingSystem.OTHER;
        }
    }

    public static File getDefaultDir () {
        return DEFAULT_DIR;
    }

    public static void setDefaultDir (File defaultDir) throws IOException {
        if (!defaultDir.exists()) {
            throw new FileNotFoundException();
        } else if (!defaultDir.isDirectory()) {
            throw new FileSystemException("Path provided isn't directory");
        }

        DEFAULT_DIR = defaultDir;
    }

    public static File getHoi4Dir () {
        return HOI4_DIR;
    }

    public static File getHoi4File (String path) {
        return new File(HOI4_DIR, path);
    }

    public static void loadHoi4Provinces () throws IOException {
        HOI4_PROVINCES = Province.getProvinces(HOI4_DIR).toArray(Province[]::new);
        HOI4_PROVINCES_KEYED = new KeyedValues<Integer, Province>(HOI4_PROVINCES) {
            @Override
            public Integer getKey(Province value) {
                return value.id;
            }
        };
    }

    public static void loadHoi4Countries () throws IOException {
        HOI4_COUNTRIES = Country.getCountries(HOI4_DIR, "common/country_tags/00_countries.txt").toArray(Country[]::new);
        HOI4_COUNTRIES_KEYED = new KeyedValues<String, Country>(HOI4_COUNTRIES) {
            @Override
            public String getKey(Country value) {
                return value.tag;
            }
        };
    }

    public static void loadHoi4States () throws IOException {
        HOI4_STATES = State.getStates(getHoi4File("history/states"), HOI4_PROVINCES_KEYED, HOI4_COUNTRIES_KEYED).toArray(State[]::new);
        HOI4_STATES_KEYED = new KeyedValues<Integer, State>(HOI4_STATES) {
            @Override
            public Integer getKey (State value) {
                return value.id;
            }
        };
    }

    public static void loadHoi4Data () throws IOException {
        loadHoi4Provinces();
        loadHoi4Countries();
        loadHoi4States();

        saveConfig(CONFIG_FILE);
    }

    public static void setHoi4Dir (File hoi4Dir) throws IOException {
        if (!hoi4Dir.exists()) {
            throw new FileNotFoundException();
        } else if (!hoi4Dir.isDirectory()) {
            throw new FileSystemException("Path provided isn't directory");
        }

        HOI4_DIR = hoi4Dir;
    }

    public static Province[] getHoi4Provinces() {
        return HOI4_PROVINCES.clone();
    }

    public static State[] getHoi4States() {
        return HOI4_STATES.clone();
    }

    public static Country[] getHoi4Countries() {
        return HOI4_COUNTRIES.clone();
    }

    public static KeyedValues<Integer, Province> getHoi4ProvincesKeyed() {
        return HOI4_PROVINCES_KEYED;
    }

    public static KeyedValues<Integer, State> getHoi4StatesKeyed() {
        return HOI4_STATES_KEYED;
    }

    public static KeyedValues<String, Country> getHoi4CountriesKeyed() {
        return HOI4_COUNTRIES_KEYED;
    }

    public static void saveConfig (File file) throws IOException {
        saveProvinces(file);
        saveCountries(file);
        saveStates(file);
    }

    public static void saveProvinces (File dir) throws IOException {
        byte[] provinces = Bytes.ofArray(HOI4_PROVINCES);
        Files.write(new File(dir, "prov.txt").toPath(), provinces);
    }

    public static void saveCountries (File dir) throws IOException {
        byte[] countries = Bytes.ofArray(HOI4_COUNTRIES);
        Files.write(new File(dir, "country.txt").toPath(), countries);
    }

    public static void saveStates (File dir) throws IOException {
        byte[] states = Bytes.ofArray(HOI4_STATES);
        Files.write(new File(dir, "states.txt").toPath(), states);
    }

    public static void loadConfig (File file) throws IOException, ClassNotFoundException {
        File provinces = new File(file, "prov.txt");
        HOI4_PROVINCES = Bytes.toArray(Province.class, Province::getInstance, Files.readAllBytes(provinces.toPath()));
        HOI4_PROVINCES_KEYED = new KeyedValues<Integer, Province>(HOI4_PROVINCES) {
            @Override
            public Integer getKey(Province value) {
                return value.id;
            }
        };

        File countries = new File(file, "country.txt");
        HOI4_COUNTRIES = Bytes.toArray(Country.class, Country::getInstance, Files.readAllBytes(countries.toPath()));
        HOI4_COUNTRIES_KEYED = new KeyedValues<String, Country>(HOI4_COUNTRIES) {
            @Override
            public String getKey(Country value) {
                return value.tag;
            }
        };

        File states = new File(file, "states.txt");
        HOI4_STATES = Bytes.toArray(State.class, x -> State.getInstance(HOI4_PROVINCES_KEYED, HOI4_COUNTRIES_KEYED, x), Files.readAllBytes(states.toPath()));
        HOI4_STATES_KEYED = new KeyedValues<Integer, State>(HOI4_STATES) {
            @Override
            public Integer getKey (State value) {
                return value.id;
            }
        };
    }

    private static class ConfigFile implements Serializable {
        private static final long serialVersionUID = 4614421651874L;

        private File DEFAULT_DIR;
        private File HOI4_DIR;
        private Province[] HOI4_PROVINCES;
        private State[] HOI4_STATES;
        private Country[] HOI4_COUNTRIES;

        public ConfigFile() {
            this.DEFAULT_DIR = Config.DEFAULT_DIR;
            this.HOI4_DIR = Config.HOI4_DIR;
            this.HOI4_PROVINCES = Config.HOI4_PROVINCES;
            this.HOI4_STATES = Config.HOI4_STATES;
            this.HOI4_COUNTRIES = Config.HOI4_COUNTRIES;
        }
    }
}
