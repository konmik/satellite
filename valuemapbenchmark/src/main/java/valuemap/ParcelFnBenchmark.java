package valuemap;

import android.os.Bundle;

import java.math.BigInteger;
import java.util.HashMap;

public class ParcelFnBenchmark {

    private static final String STRING = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
    private static final ValueMap MAP_COMBINED = ValueMap.map("1", 1, "2", STRING, "3", new Bundle());
    private static final ValueMap MAP_IMMUTABLE = ValueMap.map("1", 1, "2", STRING, "3", new BigInteger("12"));
    private static final Integer INTEGER = 1;
    private static final HashMap<String, Object> JAVA_MAP = new HashMap<String, Object>() {{
        put("1", INTEGER);
        put("2", STRING);
        put("3", new Bundle());
    }};

    public static void testString() {
        ParcelFn.unmarshall(ParcelFn.marshall(STRING));
    }

    public static void testInteger() {
        ParcelFn.unmarshall(ParcelFn.marshall(INTEGER));
    }

    public static void testNoop() {
        int i = 1;
    }

    public static void testCombinedMap() {
        MAP_COMBINED.get("1");
        MAP_COMBINED.get("2");
        MAP_COMBINED.get("3");
        MAP_COMBINED.get("4");
    }

    public static void testImmutableMap() {
        MAP_IMMUTABLE.get("1");
        MAP_IMMUTABLE.get("2");
        MAP_IMMUTABLE.get("3");
        MAP_IMMUTABLE.get("4");
    }

    public static void testJavaMap() {
        JAVA_MAP.get("1");
        JAVA_MAP.get("2");
        JAVA_MAP.get("3");
        JAVA_MAP.get("4");
    }
}
