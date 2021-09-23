import org.hoi.classes.gfx.Flag;
import org.hoi.various.Bytes;
import org.hoi.various.Config;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ByteTest {
    public static void main (String... args) throws IOException, ClassNotFoundException {
        File DEF = new File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Hearts of Iron IV");

        Map<String, Flag> map = new HashMap<>() {{
            put("SPR", new Flag(DEF, "SPR"));
            put("GER", new Flag(DEF, "GER"));
        }};

        byte[] bytes = Bytes.ofMap(Flag::getBytes, map);
        System.out.println(Arrays.toString(bytes));
        System.out.println();

        Map<String, Flag> read = Bytes.toMap(Flag::getInstance, bytes);
        System.out.println();
    }
}
