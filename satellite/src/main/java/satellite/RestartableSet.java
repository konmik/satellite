package satellite;

import android.util.SparseArray;

import rx.Notification;
import rx.Observable;
import valuemap.ValueMap;

/**
 * RestartableSet represents a set of {@link Restartable}.
 * Each Restartable is indexed by its id.
 */
public class RestartableSet implements Launcher {

    private final SparseArray<Restartable> restartables = new SparseArray<>();
    private final ValueMap.Builder out;

    /**
     * Creates a new RestartableSet instance.
     */
    public RestartableSet(ValueMap.Builder out) {
        this.out = out;
    }

    /**
     * Creates an RestartableSet instance form a given state that has been received
     * from previous instance out.
     * All instances of {@link Restartable} will be restored as well.
     */
    public RestartableSet(ValueMap in, ValueMap.Builder out) {
        this.out = out;
        for (String sId : in.keys())
            restartables.put(Integer.valueOf(sId), new Restartable((ValueMap)in.get(sId), out.child(sId)));
    }

    /**
     * Provides a connection to an observable through a given observable factory and a given intermediate subject factory,
     * see {@link Restartable#channel(SubjectFactory, RestartableFactoryNoArg)}.
     *
     * @param id                 a {@link Restartable} id.
     * @param subjectFactory     a subject factory which creates a subject to
     *                           transmit observable emissions to views.
     * @param restartableFactory an observable factory which will be used to create an observable per launch.
     * @return an observable which emits {@link rx.Notification} of the observable`s emissions.
     */
    @Override
    public <T> Observable<Notification<T>> restartable(int id, DeliveryMethod type, RestartableFactoryNoArg<T> restartableFactory) {
        return this.<T>restartable(id).channel(type, restartableFactory);
    }

    /**
     * Provides a connection to the given observable through a given observable factory and a given intermediate subject factory,
     * see {@link Restartable#channel(SubjectFactory, RestartableFactory)}.
     *
     * This {@link #restartable(int, SubjectFactory, RestartableFactoryNoArg)} variant is intended for observable factories that
     * require arguments.
     *
     * Note that to make use of arguments, both
     * {@code #connection(SubjectFactory, RestartableFactory)} and
     * {@link #launch(int, Object)} variants should be used.
     *
     * @param subjectFactory     a subject factory which creates a subject to
     *                           transmit observable emissions to views.
     * @param restartableFactory an observable factory which will be used to create an observable per launch.
     * @return an observable which emits {@link rx.Notification} of the observable emissions.
     */
    @Override
    public <A, T> Observable<Notification<T>> restartable(int id, DeliveryMethod type, RestartableFactory<A, T> restartableFactory) {
        return this.<A, T>restartable(id).channel(type, restartableFactory);
    }

    /**
     * Launches a restartable without providing arguments.
     * Dismisses the previous observable instance if it is not completed yet.
     *
     * See {@link Restartable#launch()}.
     */
    @Override
    public void launch(int id) {
        this.restartable(id).launch();
    }

    /**
     * Launches a restartable observable, providing an argument.
     * Dismisses the previous observable instance if it is not completed yet.
     *
     * See {@link Restartable#launch(Object)}.
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
        this.restartable(id).launch(arg);
    }

    /**
     * Unsubscribes and dismisses a restartable observable.
     *
     * See {@link Restartable#dismiss()}.
     */
    @Override
    public void dismiss(int id) {
        if (restartables.get(id) != null)
            restartables.get(id).dismiss();
    }

    /**
     * Unsubscribes and dismisses all controlled restartable observables.
     *
     * See {@link Restartable#dismiss()}.
     */
    public void dismiss() {
        for (int i = 0; i < restartables.size(); i++)
            restartables.valueAt(i).dismiss();
    }

    private Restartable restartable(int id) {
        if (restartables.get(id) == null)
            restartables.put(id, new Restartable(out.child(Integer.toString(id))));
        return restartables.get(id);
    }
}
