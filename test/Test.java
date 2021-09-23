import org.hoi.classes.history.State;
import org.hoi.various.Config;

import java.io.File;
import java.io.IOException;

public class Test {
    public static void main (String... args) throws IOException {
        Config.setHoi4Dir(new File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Hearts of Iron IV"));
        //Config.loadHoi4States();
        //Config.saveStates(new File("tmp"));

        State og = new State(Config.getHoi4File("history/states/1-France.txt"), Config.getHoi4ProvincesKeyed(), Config.getHoi4CountriesKeyed());
        var alpha = Config.getHoi4StatesKeyed();
        System.out.println();
    }
}
