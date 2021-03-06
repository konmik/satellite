package valuemap;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static valuemap.ParcelFn.marshall;
import static valuemap.ParcelFn.unmarshall;

/**
 * {@link ValueMap} is like {@link android.os.Bundle} but it is immutable.
 *
 * {@link ValueMap} allows to keep the instance state out of activity code, isolating it by strictly allowing
 * to make write ({@link ValueMap.Builder}) or read ({@link ValueMap}) operations only.
 *
 * If you need to save Activity instance state, it is a good idea to create a
 * {@link ValueMap.Builder} and use it to push data out for the next activity instance.
 * {@link ValueMap.Builder} is write-only, so no troubles with the state mutability can be created
 * this way.
 *
 * {@link ValueMap} automatically marshalls/unmarshalls all data to avoid third-party modifications and to keep
 * immutable data completely immutable.
 *
 * Every time set/get is called a corresponding
 * {@link Parcel#marshall()}/{@link Parcel#unmarshall(byte[], int, int)} is called,
 * providing you with a fresh instance of the stored value.
 * Thus, it is not recommended to use {@link ValueMap} on performance critical application parts.
 *
 * Basic immutable Java types, BigInteger, BigDecimal and ValueMap are not automatically
 * marshalled/unmarshalled for performance reasons,
 * so you can use them without performance penalty.
 */
public class ValueMap implements Parcelable {

    private final Map<String, Object> map;

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

        Builder builder = new Builder();
        for (int i = 0; i < map.length; i += 2)
            builder.put((String)map[i], map[i + 1]);

        return builder.build();
    }

    /**
     * Returns an immutable set of keys contained in this {@link ValueMap}.
     */
    public Set<String> keys() {
        return map.keySet();
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
        if (map.containsKey(key)) {
            Object value = map.get(key);
            return (T)(value instanceof byte[] ? unmarshall((byte[])value) : value);
        }
        return defaultValue;
    }

    /**
     * Returns a {@link Builder} which contains the current {@link ValueMap} values.
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

        private final Map<String, Object> map;
        private final Map<String, Builder> children;

        public Builder() {
            this.map = new HashMap<>();
            this.children = new HashMap<>();
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
            map.put(key, isImmutable(value) ? value : marshall(value));
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
            children.remove(key);
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
            if (children.containsKey(key))
                return children.get(key);

            else if (map.containsKey(key)) {
                Builder builder = ParcelFn.<ValueMap>unmarshall((byte[])map.remove(key)).toBuilder();
                children.put(key, builder);
                return builder;
            }

            Builder builder = new Builder();
            children.put(key, builder);
            return builder;
        }

        /**
         * Builds the {@link ValueMap} instance using collected key-value pairs.
         */
        public ValueMap build() {
            Map<String, Object> map1 = new HashMap<>(this.map);
            for (Map.Entry<String, Builder> entry : children.entrySet())
                map1.put(entry.getKey(), marshall(entry.getValue().build()));
            return new ValueMap(map1);
        }

        private Builder(Map<String, Object> map) {
            this.map = new HashMap<>(map);
            this.children = new HashMap<>();
        }
    }

    private static boolean isImmutable(Object value) {
        return value == null ||
            value instanceof Integer ||
            value instanceof String ||
            value instanceof Boolean ||
            value instanceof ValueMap ||
            value instanceof Long ||
            value instanceof Double ||
            value instanceof Float ||
            value instanceof Byte ||
            value instanceof Character ||
            value instanceof Short ||
            value instanceof BigDecimal ||
            value instanceof BigInteger;
    }

    ValueMap(Map<String, Object> map) {
        this.map = Collections.unmodifiableMap(new HashMap<>(map));
    }

    private static final ValueMap EMPTY = new ValueMap(Collections.<String, Object>emptyMap());

    private static final ClassLoader CLASS_LOADER = ValueMap.class.getClassLoader();

    protected ValueMap(Parcel in) {
        this.map = Collections.unmodifiableMap(in.readHashMap(CLASS_LOADER));
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
    public boolean equals(Object other) {
        if (!(other instanceof ValueMap))
            return false;

        Map<String, Object> otherMap = ((ValueMap)other).map;

        if (map.size() != otherMap.size())
            return false;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            Object otherValue = otherMap.get(entry.getKey());
            if (value != otherValue && (value == null || !value.equals(otherValue)))
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}
