package satellite;

import android.util.SparseArray;

import rx.Notification;
import rx.Observable;
import satellite.io.InputMap;
import satellite.io.OutputMap;
import satellite.util.SubjectFactory;

/**
 * EarthBase represents a set of {@link MissionControlCenter}.
 * Each MissionControlCenter is indexed by its id.
 * EarthBase can be used to control a set of satellites.
 */
public class EarthBase implements Launcher {

    private final SparseArray<MissionControlCenter> centers = new SparseArray<>();

    /**
     * Creates a new EarthBase instance.
     */
    public EarthBase() {
    }

    /**
     * Creates an EarthBase instance from a given state. All instances of
     * {@link MissionControlCenter} will be restored as well.
     */
    public EarthBase(InputMap in) {
        for (String sId : in.keys())
            centers.put(Integer.valueOf(sId), new MissionControlCenter((InputMap)in.get(sId)));
    }

    /**
     * Provides a connection to the given satellite through a given subject.
     *
     * @param id               an id of MissionControlCenter which controls the satellite.
     * @param subjectFactory   a subject factory which creates a subject to
     *                         transmit satellite emissions to views.
     * @param satelliteFactory a satellite factory which will be used on launch.
     * @return an observable which emits {@link rx.Notification} of satellite emissions.
     */
    @Override
    public <A, T> Observable<Notification<T>> connection(int id, SubjectFactory<Notification<T>> subjectFactory, SatelliteFactory<A, T> satelliteFactory) {
        return this.<A, T>center(id).connection(subjectFactory, satelliteFactory);
    }

    /**
     * Launches a new satellite instance. Dismisses
     * the previous satellite instance if it is not completed yet.
     *
     * @param missionStatement a mission statement for the new satellite.
     */
    @Override
    public <A> void launch(int id, A missionStatement) {
        center(id).launch(missionStatement);
    }

    /**
     * Dismisses the current satellite on a given MissionControlCenter.
     *
     * @param id an id of MissionControlCenter which controls the satellite.
     */
    @Override
    public void dismiss(int id) {
        center(id).dismiss();
    }

    /**
     * Dismisses all satellites that are controlled by the EarthBase.
     */
    public void dismissAll() {
        for (int i = 0; i < centers.size(); i++)
            centers.valueAt(i).dismiss();
    }

    /**
     * Returns the instance state that can be used to create a restored instance of
     * EarthBase later, see {@link #EarthBase(InputMap)}.
     */
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
