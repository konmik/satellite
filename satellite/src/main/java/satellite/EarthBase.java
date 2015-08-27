package satellite;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

import rx.Notification;
import rx.Observable;

/**
 * EarthBase represents a set of {@link MissionControlCenter}.
 */
public class EarthBase {

    private final HashMap<Integer, MissionControlCenter> centers = new HashMap<>();

    public static class Builder {

        private final Bundle bundle;
        private final HashMap<Integer, MissionControlCenter.SessionType> types = new HashMap<>();

        public Builder(Bundle bundle) {
            this.bundle = bundle;
        }

        public Builder controlCenter(int id, MissionControlCenter.SessionType sessionType) {
            types.put(id, sessionType);
            return this;
        }

        public EarthBase build() {
            return new EarthBase(bundle, types);
        }
    }

    public <T> Observable<Notification<T>> connection(int id, SatelliteFactory<T> factory) {
        return centers.get(id).connection(factory);
    }

    public void launch(int id, Bundle missionStatement) {
        centers.get(id).launch(missionStatement);
    }

    public void dismiss(int id) {
        centers.get(id).dismiss();
    }

    public void dismissAll() {
        for (MissionControlCenter center : centers.values())
            center.dismiss();
    }

    public Bundle saveInstanceState() {
        Bundle bundle = new Bundle();
        for (Map.Entry<Integer, MissionControlCenter> entry : centers.entrySet())
            bundle.putBundle(Integer.toString(entry.getKey()), entry.getValue().saveInstanceState());
        return bundle;
    }

    private EarthBase(Bundle bundle, HashMap<Integer, MissionControlCenter.SessionType> types) {
        for (Map.Entry<Integer, MissionControlCenter.SessionType> entry : types.entrySet())
            centers.put(
                entry.getKey(),
                new MissionControlCenter(
                    entry.getValue(),
                    bundle == null ? null : bundle.getBundle(Integer.toString(entry.getKey()))));
    }
}
