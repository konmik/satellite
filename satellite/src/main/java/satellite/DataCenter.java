package satellite;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

class DataCenter implements Parcelable {

    private static final ClassLoader CLASS_LOADER = DataCenter.class.getClassLoader();

    private final HashMap<Integer, String> keys;
    private final HashMap<Integer, SatelliteLaunch> launches;

    public DataCenter() {
        keys = new HashMap<>();
        launches = new HashMap<>();
    }

    public void registerLaunch(Integer id, SatelliteLaunch launch) {
        launches.put(id, launch);
    }

    public Collection<SatelliteLaunch> launches() {
        return launches.values();
    }

    public void deleteLaunch(Integer id) {
        launches.remove(id);
    }

    public List<String> keys() {
        return new ArrayList<>(keys.values());
    }

    public String provideKey(Integer id) {
        if (keys.containsKey(id))
            return keys.get(id);
        String key = "id:" + id + "/time:" + System.nanoTime() + "/random:" + (int)(Math.random() * Integer.MAX_VALUE);
        keys.put(id, key);
        return key;
    }

    public static final Creator<DataCenter> CREATOR = new Creator<DataCenter>() {
        @Override
        public DataCenter createFromParcel(Parcel in) {
            return new DataCenter(in);
        }

        @Override
        public DataCenter[] newArray(int size) {
            return new DataCenter[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @SuppressWarnings("unchecked")
    private DataCenter(Parcel in) {
        keys = in.readHashMap(CLASS_LOADER);
        launches = in.readHashMap(CLASS_LOADER);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeMap(keys);
        dest.writeMap(launches);
    }
}
