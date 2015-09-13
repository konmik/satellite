package satellite.state;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The typical state in Android applications is the state that is being kept inside of
 * Activity fields, prompting a programmer to reuse the state and get all the side-effects
 * of this act.
 *
 * {@link StateMap} represents a "map" storage that can be used to remove more state off the view logic.
 * It is immutable and Parcelable, thus it can be safely used without creation of
 * side effects.
 *
 * If you need to save data that is collected during activity, it is a good idea to create a
 * {@link satellite.state.StateMap.Builder} and use it to push data for the next activity instance.
 * {@link satellite.state.StateMap.Builder} is write-only, so no troubles with the state mutability can be created
 * this way.
 *
 * {@link StateMap} allows to keep the instance state out of activity code, isolating it by strictly allowing
 * to make write ({@link satellite.state.StateMap.Builder}) or read ({@link StateMap}) operations only.
 *
 * {@link StateMap} automatically marshalls/unmarshalls all data to avoid third-party modifications and to keep
 * immutable data immutable. Thus, it is not recommended to use {@link StateMap} on performance critical
 * application parts.
 */
public class StateMap implements Parcelable {

    private final Map<String, byte[]> map;

    public static StateMap empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Constructs StateMap using a sequence of key-value arguments.
     * Keys should be String, values should fit {@link Parcel#writeValue(Object)} argument
     * requirements.
     */
    public static StateMap sequence(Object... map) {
        if (map.length / 2 * 2 != map.length)
            throw new IllegalArgumentException("should provide <String> key - <?> value pairs");

        HashMap<String, byte[]> hashMap = new HashMap<>(map.length / 2);
        for (int i = 0; i < map.length; i += 2)
            hashMap.put((String)map[i], ParcelFn.marshall(map[i + 1]));

        return new StateMap(hashMap);
    }

    /**
     * Returns an immutable set of the keys contained in this {@link StateMap}.
     */
    public Set<String> keys() {
        return Collections.unmodifiableSet(map.keySet());
    }

    /**
     * Returns whether this {@link StateMap} contains the specified key.
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
     * Returns the output map which contains the current {@link StateMap} values.
     */
    public Builder toBuilder() {
        return new Builder(map);
    }

    /**
     * The builder that implements "output only" approach for handling state.
     * It is not recommended to use {@link #build()} just to check what is inside -
     * this will invalidate the whole purpose of using {@link StateMap}.
     */
    public static class Builder {

        private final Map<String, byte[]> map;

        public Builder() {
            this.map = new HashMap<>();
        }

        /**
         * Puts a value into the map.
         *
         * @param key   a value key.
         * @param value a value. It must satisfy {@link Parcel#writeValue(Object)}
         *              method requirements.
         * @return the same builder instance.
         */
        public Builder put(String key, Object value) {
            map.put(key, ParcelFn.marshall(value));
            return this;
        }

        /**
         * Removes a value from the map.
         *
         * @param key a value key.
         * @return the same builder instance.
         */
        public Builder remove(String key) {
            map.remove(key);
            return this;
        }

        /**
         * Builds the {@link StateMap} instance using collected key-value pairs.
         */
        public StateMap build() {
            return new StateMap(map);
        }

        private Builder(Map<String, byte[]> map) {
            this.map = new HashMap<>(map);
        }
    }

    StateMap(Map<String, byte[]> map) {
        this.map = new HashMap<>(map);
    }

    private static final StateMap EMPTY = new StateMap(Collections.<String, byte[]>emptyMap());

    private static final ClassLoader CLASS_LOADER = StateMap.class.getClassLoader();

    protected StateMap(Parcel in) {
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

    public static final Creator<StateMap> CREATOR = new Creator<StateMap>() {
        @Override
        public StateMap createFromParcel(Parcel in) {
            return new StateMap(in);
        }

        @Override
        public StateMap[] newArray(int size) {
            return new StateMap[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StateMap))
            return false;

        if (map == ((StateMap)o).map)
            return true;

        if (map.size() != ((StateMap)o).map.size())
            return false;

        for (Map.Entry<String, byte[]> entry : map.entrySet()) {
            String key = entry.getKey();
            byte[] value = entry.getValue();
            byte[] value2 = ((StateMap)o).map.get(key);
            if (value == null) {
                if (value2 != null || !((StateMap)o).map.containsKey(key))
                    return false;
            }
            else if (!Arrays.equals(value, value2))
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}
