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
 * immutable data immutable.
 */
public class InputMap implements Parcelable {

    public static final InputMap EMPTY = new InputMap();

    private final Map<String, byte[]> map;

    InputMap(Map<String, byte[]> map) {
        this.map = new HashMap<>(map);
    }

    public InputMap(Object... map) {
        if (map.length / 2 * 2 != map.length)
            throw new IllegalArgumentException("should provide <String> key - <?> value pairs");

        HashMap<String, byte[]> hashMap = new HashMap<>(map.length / 2);
        for (int i = 0; i < map.length; i += 2) {
            Parcel parcel = Parcel.obtain();
            parcel.writeValue(map[i + 1]);
            hashMap.put((String)map[i], parcel.marshall());
            parcel.recycle();
        }

        this.map = new HashMap<>(hashMap);
    }

    public Set<String> keys() {
        return Collections.unmodifiableSet(map.keySet());
    }

    public boolean contains(String key) {
        return map.containsKey(key);
    }

    public <T> T get(String key) {
        return (T)get(key, null);
    }

    public <T> T get(String key, T defaultValue) {
        if (!map.containsKey(key))
            return defaultValue;
        return unmarshall(map.get(key));
    }

    public OutputMap toOutput() {
        return new OutputMap(map);
    }

    private static <T> T unmarshall(byte[] array) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(array, 0, array.length);
        parcel.setDataPosition(0);
        Object value = parcel.readValue(CLASS_LOADER);
        parcel.recycle();
        return (T)value;
    }

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
}
