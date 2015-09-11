package satellite;

import android.util.SparseArray;

import rx.Notification;
import rx.Observable;
import satellite.io.InputMap;
import satellite.io.OutputMap;
import satellite.util.SubjectFactory;

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
    public <A, T> Observable<Notification<T>> connection(int id, SubjectFactory<Notification<T>> subjectFactory, SatelliteFactory<A, T> satelliteFactory) {
        return this.<A, T>center(id).connection(subjectFactory, satelliteFactory);
    }

    @Override
    public <A> void launch(int id, A missionStatement) {
        center(id).launch(missionStatement);
    }

    @Override
    public void dismiss(int id) {
        center(id).dismiss();
    }

    public void dismissAll() {
        for (int i = 0; i < centers.size(); i++)
            centers.valueAt(i).dismiss();
    }

    public InputMap instanceState() {
        OutputMap out = new OutputMap();
        for (int i = 0; i < centers.size(); i++)
            out.put(Integer.toString(centers.keyAt(i)), centers.valueAt(i).instanceState());
        return out.toInput();
    }

    private <A, T> MissionControlCenter<A, T> center(int id) {
        if (centers.get(id) == null)
            centers.put(id, new MissionControlCenter());
        return centers.get(id);
    }
}
