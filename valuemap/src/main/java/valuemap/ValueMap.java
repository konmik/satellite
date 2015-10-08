package valuemap;

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
 * {@link ValueMap} represents a "map" storage that can be used to remove more state off the view logic.
 * It is immutable and Parcelable, thus it can be safely used without side effects.
 *
 * If you need to save Activity instance state, it is a good idea to create a
 * {@link ValueMap.Builder} and use it to push data out for the next activity instance.
 * {@link ValueMap.Builder} is write-only, so no troubles with the state mutability can be created
 * this way.
 *
 * {@link ValueMap} allows to keep the instance state out of activity code, isolating it by strictly allowing
 * to make write ({@link ValueMap.Builder}) or read ({@link ValueMap}) operations only.
 *
 * {@link ValueMap} automatically marshalls/unmarshalls all data to avoid third-party modifications and to keep
 * immutable data completely immutable. Every time set/get is called a corresponding
 * {@link Parcel#marshall()}/{@link Parcel#unmarshall(byte[], int, int)} is called,
 * providing you with a fresh instance of the stored value.
 * Thus, it is not recommended to use {@link ValueMap} on performance critical application parts.
 */
public class ValueMap implements Parcelable {

    private final Map<String, byte[]> map;

    public static ValueMap empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Constructs ValueMap using a sequence of key-value arguments.
     * Keys should be String, values should fit {@link Parcel#writeValue(Object)} argument
     * requirements.
     */
    public static ValueMap map(Object... map) {
        if (map.length / 2 * 2 != map.length)
            throw new IllegalArgumentException("Arguments should be <String> key - <?> value pairs");

        HashMap<String, byte[]> hashMap = new HashMap<>(map.length / 2);
        for (int i = 0; i < map.length; i += 2)
            hashMap.put((String)map[i], ParcelFn.marshall(map[i + 1]));

        return new ValueMap(hashMap);
    }

    /**
     * Returns an immutable set of keys contained in this {@link ValueMap}.
     */
    public Set<String> keys() {
        return Collections.unmodifiableSet(map.keySet());
    }

    /**
     * Returns whether this {@link ValueMap} contains the specified key.
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
     * Returns the output map which contains the current {@link ValueMap} values.
     */
    public Builder toBuilder() {
        return new Builder(map);
    }

    /**
     * A builder that implements "output only" approach for handling state.
     * It is not recommended to use {@link #build()} just to check what is inside -
     * this will invalidate the whole purpose of using {@link ValueMap}.
     */
    public static class Builder {

        private final Map<String, byte[]> map = new HashMap<>();
        private final Map<String, Builder> sub = new HashMap<>();

        public Builder() {
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
            sub.remove(key);
            return this;
        }

        /**
         * Returns a child builder. The child builder will be marshalled into a {@link ValueMap}
         * instance with the key during the {@link #build()} call.
         *
         * @param key a child builder key.
         * @return a new builder instance or an existing one.
         */
        public Builder child(String key) {
            if (sub.containsKey(key)) {
                return sub.get(key);
            }
            else if (map.containsKey(key)) {
                Builder builder = ParcelFn.<ValueMap>unmarshall(map.get(key)).toBuilder();
                map.remove(key);
                sub.put(key, builder);
                return builder;
            }

            Builder builder = new Builder();
            sub.put(key, builder);
            return builder;
        }

        /**
         * Builds the {@link ValueMap} instance using collected key-value pairs.
         */
        public ValueMap build() {
            Map<String, byte[]> m = new HashMap<>(map);
            for (Map.Entry<String, Builder> entry : sub.entrySet())
                m.put(entry.getKey(), ParcelFn.marshall(entry.getValue().build()));
            return new ValueMap(m);
        }

        private Builder(Map<String, byte[]> map) {
            this.map.putAll(map);
        }
    }

    ValueMap(Map<String, byte[]> map) {
        this.map = new HashMap<>(map);
    }

    private static final ValueMap EMPTY = new ValueMap(Collections.<String, byte[]>emptyMap());

    private static final ClassLoader CLASS_LOADER = ValueMap.class.getClassLoader();

    protected ValueMap(Parcel in) {
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

    public static final Creator<ValueMap> CREATOR = new Creator<ValueMap>() {
        @Override
        public ValueMap createFromParcel(Parcel in) {
            return new ValueMap(in);
        }

        @Override
        public ValueMap[] newArray(int size) {
            return new ValueMap[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ValueMap))
            return false;

        Map<String, byte[]> otherMap = ((ValueMap)o).map;

        if (map.size() != otherMap.size())
            return false;

        for (Map.Entry<String, byte[]> entry : map.entrySet()) {
            if (!Arrays.equals(entry.getValue(), otherMap.get(entry.getKey())))
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}
