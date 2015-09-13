package satellite;

import android.util.SparseArray;

import rx.Notification;
import rx.Observable;
import satellite.state.StateMap;
import satellite.util.SubjectFactory;

/**
 * RestartableConnectionGroup represents a set of {@link RestartableConnection}.
 * Each RestartableConnection is indexed by its id.
 * RestartableConnectionGroup can be used to control a set of restartables.
 */
public class RestartableConnectionGroup implements Launcher {

    private final SparseArray<RestartableConnection> centers = new SparseArray<>();

    /**
     * Creates a new RestartableConnectionGroup instance.
     */
    public RestartableConnectionGroup() {
    }

    /**
     * Creates an RestartableConnectionGroup instance from a given state. All instances of
     * {@link RestartableConnection} will be restored as well.
     */
    public RestartableConnectionGroup(StateMap in) {
        for (String sId : in.keys())
            centers.put(Integer.valueOf(sId), new RestartableConnection((StateMap)in.get(sId)));
    }

    /**
     * Provides a connection to the given satellite through a given subject.
     *
     * @param id                 an id of RestartableConnection which controls the satellite.
     * @param subjectFactory     a subject factory which creates a subject to
     *                           transmit satellite emissions to views.
     * @param restartableFactory a satellite factory which will be used on launch.
     * @return an observable which emits {@link rx.Notification} of satellite emissions.
     */
    @Override
    public <A, T> Observable<Notification<T>> connection(int id, SubjectFactory<Notification<T>> subjectFactory, RestartableFactory<A, T> restartableFactory) {
        return this.<A, T>center(id).connection(subjectFactory, restartableFactory);
    }

    /**
     * Launches a new restartable instance. Dismisses
     * the previous restartable instance if it is not completed yet.
     *
     * @param missionStatement a mission statement for the new satellite.
     */
    @Override
    public <A> void launch(int id, A missionStatement) {
        center(id).launch(missionStatement);
    }

    /**
     * Dismisses the current satellite on a given RestartableConnection.
     *
     * @param id an id of RestartableConnection which controls the satellite.
     */
    @Override
    public void dismiss(int id) {
        center(id).dismiss();
    }

    /**
     * Dismisses all satellites that are controlled by the RestartableConnectionGroup.
     */
    public void dismissAll() {
        for (int i = 0; i < centers.size(); i++)
            centers.valueAt(i).dismiss();
    }

    /**
     * Returns the instance state that can be used to create a restored instance of
     * RestartableConnectionGroup later, see {@link #RestartableConnectionGroup(StateMap)}.
     */
    public StateMap instanceState() {
        StateMap.Builder out = new StateMap.Builder();
        for (int i = 0; i < centers.size(); i++)
            out.put(Integer.toString(centers.keyAt(i)), centers.valueAt(i).instanceState());
        return out.build();
    }

    private <A, T> RestartableConnection<A, T> center(int id) {
        if (centers.get(id) == null)
            centers.put(id, new RestartableConnection());
        return centers.get(id);
    }
}
