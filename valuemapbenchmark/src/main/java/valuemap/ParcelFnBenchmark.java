package valuemap;

import android.os.Bundle;

import java.util.HashMap;

public class ParcelFnBenchmark {

    private static final String STRING = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
    public static final ValueMap MAP = ValueMap.map("1", 1, "2", STRING, "3", new Bundle());
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

    public static void testCombined() {
        MAP.get("1");
        MAP.get("2");
        MAP.get("3");
        MAP.get("4");
    }

    public static void testJavaMap() {
        JAVA_MAP.get("1");
        JAVA_MAP.get("2");
        JAVA_MAP.get("3");
        JAVA_MAP.get("4");
    }
}
