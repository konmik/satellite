package satellite;

import android.util.SparseArray;

import rx.Notification;
import rx.Observable;
import satellite.io.InputMap;
import satellite.io.OutputMap;

/**
 * EarthBase represents a set of {@link MissionControlCenter}.
 */
public class EarthBase {

    private final SparseArray<MissionControlCenter> centers = new SparseArray<>();

    public EarthBase(InputMap in, int... ids) {
        for (int id : ids)
            centers.put(id, new MissionControlCenter(in == null ? null : (InputMap)in.get(Integer.toString(id))));
    }

    public <T> Observable<Notification<T>> connection(int id, MissionControlCenter.ConnectionFactory<T> type) {
        return centers.get(id).connection(type);
    }

    public void launch(int id, InputMap missionStatement) {
        centers.get(id).launch(missionStatement);
    }

    public void dismiss(int id) {
        centers.get(id).dismiss();
    }

    public void dismissAll() {
        for (int i = 0; i < centers.size(); i++)
            centers.valueAt(i).dismiss();
    }

    public OutputMap saveInstanceState() {
        OutputMap out = new OutputMap();
        for (int i = 0; i < centers.size(); i++)
            out.put(Integer.toString(centers.keyAt(i)), centers.valueAt(i).saveInstanceState().toInput());
        return out;
    }
}
