package satellite;

import android.util.SparseArray;

import rx.Notification;
import rx.Observable;
import satellite.io.InputMap;
import satellite.io.OutputMap;

/**
 * EarthBase represents a set of {@link MissionControlCenter}.
 */
public class EarthBase implements Launcher {

    private final SparseArray<MissionControlCenter> centers = new SparseArray<>();

    public EarthBase() {
    }

    public EarthBase(InputMap in) {
        for (String sId : in.keys())
            centers.put(Integer.valueOf(sId), new MissionControlCenter((InputMap)in.get(sId)));
    }

    @Override
    public <T> Observable<Notification<T>> connection(int id, MissionControlCenter.ConnectionFactory<T> type) {
        return getCenter(id).connection(type);
    }

    @Override
    public void launch(int id, InputMap missionStatement) {
        getCenter(id).launch(missionStatement);
    }

    @Override
    public void dismiss(int id) {
        getCenter(id).dismiss();
    }

    public void dismissAll() {
        for (int i = 0; i < centers.size(); i++)
            centers.valueAt(i).dismiss();
    }

    public InputMap saveInstanceState() {
        OutputMap out = new OutputMap();
        for (int i = 0; i < centers.size(); i++)
            out.put(Integer.toString(centers.keyAt(i)), centers.valueAt(i).saveInstanceState());
        return out.toInput();
    }

    private MissionControlCenter getCenter(int id) {
        if (centers.get(id) == null)
            centers.put(id, new MissionControlCenter());
        return centers.get(id);
    }
}