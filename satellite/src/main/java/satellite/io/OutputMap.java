package satellite.io;

import java.util.HashMap;
import java.util.Map;

import satellite.util.ParcelFn;

public class OutputMap {

    private final Map<String, byte[]> map;

    public OutputMap() {
        this.map = new HashMap<>();
    }

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

    @Override
    public boolean equals(Object o) {
        return MarshallMapFn.equalsMap(map, ((OutputMap)o).map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}
