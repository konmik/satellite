package satellite.io;

import java.util.HashMap;
import java.util.Map;

public class OutputMap {

    private final Map<String, byte[]> map;

    public OutputMap() {
        this.map = new HashMap<>();
    }

    /**
     * Puts a value into the map.
     *
     * @param key   the value key.
     * @param value the value. It must satisfy {@link android.os.Parcel#writeValue(Object)}
     *              method requirements.
     * @return
     */
    public OutputMap put(String key, Object value) {
        map.put(key, ParcelFn.marshall(value));
        return this;
    }

    public OutputMap remove(String key) {
        map.remove(key);
        return this;
    }

    public InputMap toInput() {
        return new InputMap(map);
    }

    OutputMap(Map<String, byte[]> map) {
        this.map = new HashMap<>(map);
    }
}
