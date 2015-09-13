package satellite.io;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The typical state in Android applications is the state that is being kept inside of
 * Activity fields, prompting a programmer to reuse the state and get all the side-effects
 * of this act.
 *
 * Input/OutputMap represent a "map" storage that can be used to remove more state off the view logic.
 *
 * InputMap is immutable and Parcelable, thus it can be safely used.
 *
 * OutputMap is write-only, so no troubles with the state mutability can be created.
 *
 * Input/OutputMap allow to keep the instance state out of activity code, isolating it by strictly allowing to make write
 * or read operations only.
 *
 * Input/OutputMap automatically marshalls/unmarshalls all data to avoid third-party modifications and to keep
 * immutable data immutable. Thus, it is not recommended to use Input/OutputMap on performance critical
 * application parts.
 */
public class InputMap implements Parcelable {

    private final Map<String, byte[]> map;

    public static InputMap empty() {
        return EMPTY;
    }

    /**
     * Constructs InputMap using a sequence of key-value arguments.
     * Keys should be String, values should fit {@link Parcel#writeValue(Object)} argument
     * requirements.
     */
    public static InputMap sequence(Object... map) {
        if (map.length / 2 * 2 != map.length)
            throw new IllegalArgumentException("should provide <String> key - <?> value pairs");

        HashMap<String, byte[]> hashMap = new HashMap<>(map.length / 2);
        for (int i = 0; i < map.length; i += 2)
            hashMap.put((String)map[i], ParcelFn.marshall(map[i + 1]));

        return new InputMap(hashMap);
    }

    /**
     * Returns an immutable set of the keys contained in this {@link InputMap}.
     */
    public Set<String> keys() {
        return Collections.unmodifiableSet(map.keySet());
    }

    /**
     * Returns whether this {@link InputMap} contains the specified key.
     */
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    /**
     * Returns the value of the mapping with the specified key, or null.
     */
    public <T> T get(String key) {
        return (T)get(key, null);
    }

    /**
     * Returns the value of the mapping with the specified key, or the given default value.
     */
    public <T> T get(String key, T defaultValue) {
        if (!map.containsKey(key))
            return defaultValue;
        return ParcelFn.unmarshall(map.get(key));
    }

    /**
     * Returns the output map which contains the current {@link InputMap} values.
     */
    public OutputMap toOutput() {
        return new OutputMap(map);
    }

    InputMap(Map<String, byte[]> map) {
        this.map = new HashMap<>(map);
    }

    private static final InputMap EMPTY = new InputMap(Collections.<String, byte[]>emptyMap());

    private static final ClassLoader CLASS_LOADER = InputMap.class.getClassLoader();

    protected InputMap(Parcel in) {
        map = in.readHashMap(CLASS_LOADER);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeMap(map);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<InputMap> CREATOR = new Creator<InputMap>() {
        @Override
        public InputMap createFromParcel(Parcel in) {
            return new InputMap(in);
        }

        @Override
        public InputMap[] newArray(int size) {
            return new InputMap[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        return o instanceof InputMap && MarshallMapFn.equalsMap(map, ((InputMap)o).map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}
