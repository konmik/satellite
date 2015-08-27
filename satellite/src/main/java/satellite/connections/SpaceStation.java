package satellite.connections;

import android.util.Printer;

import java.util.HashMap;

import rx.functions.Func0;
import satellite.MissionControlCenter;

/**
 * SpaceStation represents... a space station! :D
 *
 * It connects started satellites with a land base {@link MissionControlCenter}.
 */
public enum SpaceStation {

    INSTANCE;

    private HashMap<String, Object> raw = new HashMap<>();

    <T> T provide(String key, Func0<T> factory) {
        if (!raw.containsKey(key))
            raw.put(key, factory.call());
        return (T)raw.get(key);
    }

    <T> T get(String key) {
        return (T)raw.get(key);
    }

    <T> void put(String key, T value) {
        raw.put(key, value);
    }

    void remove(String key) {
        raw.remove(key);
    }

    public void print(Printer printer) {
        printer.println("raw keys:");
        for (String key : raw.keySet())
            printer.println(key);
    }
}
