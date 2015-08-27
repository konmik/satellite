package satellite.connections;

import android.util.Printer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import rx.Notification;
import rx.Observable;
import rx.functions.Func0;
import rx.subjects.Subject;
import satellite.MissionControlCenter;

/**
 * SpaceStation represents... a space station! :D
 *
 * It connects started satellites with a land base {@link MissionControlCenter}.
 */
public enum SpaceStation {

    INSTANCE;

    private HashMap<String, Subject> earthConnections = new HashMap<>();
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

    <T> Observable<Notification<T>> connection(final String key, Func0<Subject> connectionFactory) {
        if (earthConnections.get(key) == null) {
            earthConnections.put(key, connectionFactory.call());
        }
        return earthConnections.get(key);
    }

    void clear(Collection<String> keys) {
        for (String key : keys.toArray(new String[keys.size()])) {
            raw.remove(key);
            earthConnections.remove(key);
        }
    }

    public void print(Printer printer) {
        printer.println("earth connection keys:");
        for (String key : earthConnections.keySet())
            printer.println(key);
        printer.println("raw keys:");
        for (String key : raw.keySet())
            printer.println(key);
    }

    List<String> getEarthConnectionKeys() {
        return new ArrayList<>(earthConnections.keySet());
    }
}
