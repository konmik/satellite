package satellite;

import android.util.SparseArray;

import rx.Notification;
import rx.Observable;
import valuemap.ValueMap;

/**
 * {@link RestartableSet} represents a set of {@link Restartable}.
 * Each {@link Restartable} is indexed by an id.
 */
public class RestartableSet implements Launcher {

    private final SparseArray<Restartable> restartables = new SparseArray<>();
    private final ValueMap.Builder out;

    /**
     * Creates a new RestartableSet instance.
     *
     * @param out an output that will be used to reconstruct the RestartableSet later.
     */
    public RestartableSet(ValueMap.Builder out) {
        this.out = out;
    }

    /**
     * Creates a RestartableSet instance form a given state that has been received
     * from the previous instance`s out argument.
     * All instances of {@link Restartable} will be restored as well.
     *
     * @param in  a value that has been constructed using the out argument of the previous RestartableSet`s instance.
     * @param out an output that will be used to reconstruct the RestartableSet later.
     */
    public RestartableSet(ValueMap in, ValueMap.Builder out) {
        this.out = out;
        for (String sId : in.keys())
            restartables.put(Integer.valueOf(sId), new Restartable((ValueMap)in.get(sId), out.child(sId)));
    }

    /**
     * Provides a channel to an observable that can be created with a given observable factory.
     *
     * @param id                     a {@link Restartable} id.
     * @param type                   a type of the channel.
     * @param observableFactoryNoArg an observable factory which will be used to create an observable per launch.
     * @return an observable which emits {@link rx.Notification} of onNext and onError observable emissions.
     */
    @Override
    public <T> Observable<Notification<T>> channel(int id, DeliveryMethod type, ObservableFactoryNoArg<T> observableFactoryNoArg) {
        return this.<T>restartable(id).channel(type, observableFactoryNoArg);
    }

    /**
     * Provides a channel to an observable that can be created with a given observable factory.
     *
     * This {@link #channel(int, DeliveryMethod, ObservableFactoryNoArg)} variant is intended for observable factories that
     * require arguments.
     *
     * Note that to make use of arguments, both
     * {@link #channel(int, DeliveryMethod, ObservableFactory)} and
     * {@link #launch(int, Object)} variants should be used.
     *
     * @param id                a {@link Restartable} id.
     * @param type              a type of the channel.
     * @param observableFactory an observable factory which will be used to create an observable per launch.
     * @return an observable which emits {@link rx.Notification} of onNext and onError observable emissions.
     */
    @Override
    public <A, T> Observable<Notification<T>> channel(int id, DeliveryMethod type, ObservableFactory<A, T> observableFactory) {
        return this.<A, T>restartable(id).channel(type, observableFactory);
    }

    /**
     * Launches an observable without providing arguments.
     * Dismisses the previous observable instance if it is not completed yet.
     *
     * @param id a {@link Restartable} id.
     */
    @Override
    public void launch(int id) {
        restartable(id).launch();
    }

    /**
     * Launches an observable providing an argument.
     * Dismisses the previous observable instance if it is not completed yet.
     *
     * Note that to make use of arguments, both
     * {@link #channel(int, DeliveryMethod, ObservableFactory)} and
     * {@link #launch(int, Object)} variants should be used.
     *
     * @param id  a {@link Restartable} id.
     * @param arg an argument for the new observable.
     *            It must satisfy {@link android.os.Parcel#writeValue(Object)}
     *            method argument requirements.
     */
    @Override
    public void launch(int id, Object arg) {
        restartable(id).launch(arg);
    }

    /**
     * Unsubscribes and dismisses the current observable of a given {@link Restartable}.
     *
     * @param id a {@link Restartable} id.
     */
    @Override
    public void dismiss(int id) {
        if (restartables.get(id) != null)
            restartables.get(id).dismiss();
    }

    /**
     * Unsubscribes and dismisses all controlled observables.
     */
    public void dismiss() {
        for (int i = 0; i < restartables.size(); i++)
            dismiss(restartables.keyAt(i));
    }

    private Restartable restartable(int id) {
        if (restartables.get(id) == null)
            restartables.put(id, new Restartable(out.child(Integer.toString(id))));
        return restartables.get(id);
    }
}
