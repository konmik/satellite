package satellite.io;

import android.os.Parcel;

import java.util.HashMap;
import java.util.Map;

public class OutputMap {

    private final Map<String, byte[]> map;

    public OutputMap() {
        this.map = new HashMap<>();
    }

    public OutputMap put(String key, Object value) {
        Parcel parcel = Parcel.obtain();
        parcel.writeValue(value);
        map.put(key, parcel.marshall());
        parcel.recycle();
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
