package satellite;

import android.util.SparseArray;

import rx.Notification;
import rx.Observable;
import satellite.state.StateMap;
import satellite.util.SubjectFactory;

/**
 * RestartableConnectionSet represents a set of {@link RestartableConnection}.
 * Each RestartableConnection is indexed by its id.
 */
public class RestartableConnectionSet implements Launcher {

    private final SparseArray<RestartableConnection> connections = new SparseArray<>();

    /**
     * Creates a new RestartableConnectionSet instance.
     */
    public RestartableConnectionSet() {
    }

    /**
     * Creates an RestartableConnectionSet instance form a given state that has been received
     * from {@link #instanceState()}.
     * All instances of {@link RestartableConnection} will be restored as well.
     */
    public RestartableConnectionSet(StateMap in) {
        for (String sId : in.keys())
            connections.put(Integer.valueOf(sId), new RestartableConnection((StateMap)in.get(sId)));
    }

    /**
     * Provides a connection to an observable through a given observable factory and a given subject factory,
     * see {@link RestartableConnection#connection(SubjectFactory, RestartableFactoryNoArg)}.
     *
     * @param id                 a {@link RestartableConnection} id.
     * @param subjectFactory     a subject factory which creates a subject to
     *                           transmit observable emissions to views.
     * @param restartableFactory an observable factory which will be used to create a satellite per launch.
     * @return an observable which emits {@link rx.Notification} of satellite emissions.
     */
    @Override
    public <T> Observable<Notification<T>> connection(int id, SubjectFactory<Notification<T>> subjectFactory, RestartableFactoryNoArg<T> restartableFactory) {
        return this.<T>connection(id).connection(subjectFactory, restartableFactory);
    }

    /**
     * Provides a connection to the given observable through a given observable factory and a given subject factory,
     * see {@link RestartableConnection#connection(SubjectFactory, RestartableFactory)}.
     *
     * This {@link #connection(int, SubjectFactory, RestartableFactoryNoArg)} variant is intended for observable factories that
     * require arguments.
     *
     * Note that to make use of arguments, both
     * {@code #connection(SubjectFactory, RestartableFactory)} and
     * {@link #launch(int, Object)} variants should be used.
     *
     * @param subjectFactory     a subject factory which creates a subject to
     *                           transmit observable emissions to views.
     * @param restartableFactory an observable factory which will be used to create a satellite per launch.
     * @return an observable which emits {@link rx.Notification} of satellite emissions.
     */
    @Override
    public <A, T> Observable<Notification<T>> connection(int id, SubjectFactory<Notification<T>> subjectFactory, RestartableFactory<A, T> restartableFactory) {
        return this.<A, T>connection(id).connection(subjectFactory, restartableFactory);
    }

    /**
     * Launches a restartable without providing arguments.
     * Dismisses the previous observable instance if it is not completed yet.
     *
     * See {@link RestartableConnection#launch()}.
     */
    @Override
    public void launch(int id) {
        this.connection(id).launch();
    }

    /**
     * Launches a restartable observable, providing an argument.
     * Dismisses the previous observable instance if it is not completed yet.
     *
     * See {@link RestartableConnection#launch(Object)}.
     *
     * Note that to make use of arguments, both
     * {@code #connection(SubjectFactory, RestartableFactory)} and
     * {@link #launch(int, Object)} variants should be used.
     *
     * @param arg an argument for the new observable.
     *            It must satisfy {@link android.os.Parcel#writeValue(Object)}
     *            method requirements.
     */
    @Override
    public void launch(int id, Object arg) {
        this.connection(id).launch(arg);
    }

    /**
     * Unsubscribes and dismisses a restartable observable.
     *
     * See {@link RestartableConnection#dismiss()}.
     */
    @Override
    public void dismiss(int id) {
        if (connections.get(id) != null)
            connections.get(id).dismiss();
    }

    /**
     * Unsubscribes and dismisses all controlled restartable observables.
     *
     * See {@link RestartableConnection#dismiss()}.
     */
    public void unsubscribe() {
        for (int i = 0; i < connections.size(); i++)
            connections.valueAt(i).dismiss();
    }

    /**
     * Returns the instance state that can be used to create a restored instance of
     * RestartableConnectionSet later.
     *
     * See {@link #RestartableConnectionSet(StateMap)}, {@link RestartableConnection#instanceState()}.
     */
    public StateMap instanceState() {
        StateMap.Builder out = new StateMap.Builder();
        for (int i = 0; i < connections.size(); i++)
            out.put(Integer.toString(connections.keyAt(i)), connections.valueAt(i).instanceState());
        return out.build();
    }

    private RestartableConnection connection(int id) {
        if (connections.get(id) == null)
            connections.put(id, new RestartableConnection());
        return connections.get(id);
    }
}
