package satellite;

import android.os.Bundle;
import android.util.SparseArray;

import rx.Notification;
import rx.Observable;

/**
 * EarthBase represents a set of {@link MissionControlCenter}.
 */
public class EarthBase {

    private final SparseArray<MissionControlCenter> centers = new SparseArray<>();

    public EarthBase(Bundle bundle, int... ids) {
        for (int id : ids)
            centers.put(id, new MissionControlCenter(bundle == null ? null : bundle.getBundle(Integer.toString(id))));
    }

    public <T> Observable<Notification<T>> connection(int id, SatelliteFactory<T> factory, MissionControlCenter.SessionType type) {
        return centers.get(id).connection(factory, type);
    }

    public void launch(int id, Bundle missionStatement) {
        centers.get(id).launch(missionStatement);
    }

    public void dismiss(int id) {
        centers.get(id).dismiss();
    }

    public void dismissAll() {
        for (int i = 0; i < centers.size(); i++)
            centers.valueAt(i).dismiss();
    }

    public Bundle saveInstanceState() {
        Bundle bundle = new Bundle();
        for (int i = 0; i < centers.size(); i++)
            bundle.putBundle(Integer.toString(centers.keyAt(i)), centers.valueAt(i).saveInstanceState());
        return bundle;
    }
}
