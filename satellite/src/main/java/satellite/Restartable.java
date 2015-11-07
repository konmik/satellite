package satellite;

import rx.Notification;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import valuemap.ValueMap;

/**
 * {@link Restartable} controls only one restartable. A restartable is an
 * {@link Observable} which should be restarted after a process (not application) restart.
 *
 * {@link Restartable} saves observable`s arguments and restarts the observable in
 * the case of a process restart.
 */
public class Restartable {

    private final String key;
    private final boolean restore;
    private final Object arg;
    private final ValueMap.Builder out;

    private final PublishSubject<Object> launches = PublishSubject.create();

    private static long id;

    /**
     * Creates a new Restartable.
     *
     * @param out an output that will be used to reconstruct the restartable later.
     */
    public Restartable(ValueMap.Builder out) {
        this.out = out;
        key = "id:" + ++id + " /time:" + System.nanoTime() + " /random:" + (int) (Math.random() * Long.MAX_VALUE);
        restore = false;
        arg = null;
        out.put("key", key);
    }

    /**
     * Creates a new Restartable form a given state that has been received
     * from the previous instance out argument.
     *
     * @param in  a value that has been constructed using the out argument of the previous Restartable`s instance.
     * @param out an output that will be used to reconstruct the restartable later.
     */
    public Restartable(ValueMap in, ValueMap.Builder out) {
        this.out = out;
        key = in.get("key");
        restore = in.get("restore", false);
        arg = in.get("arg");
    }

    /**
     * Provides a channel to an observable that can be created with a given observable factory.
     *
     * @param type                   a type of the channel.
     * @param observableFactoryNoArg an observable factory which will be used to create an observable per launch.
     * @return an observable which emits {@link rx.Notification} of onNext and onError observable emissions.
     */
    public <T> Observable<Notification<T>> channel(
        final DeliveryMethod type,
        final ObservableFactoryNoArg<T> observableFactoryNoArg) {

        return channel(type, new Func1<Object, Observable<Notification<T>>>() {
            @Override
            public Observable<Notification<T>> call(Object ignored) {
                return ReconnectableMap.INSTANCE.channel(key, type, observableFactoryNoArg);
            }
        });
    }

    /**
     * Provides a channel to an observable that can be created with a given observable factory.
     *
     * Note that to make use of arguments, both
     * {@link #channel(DeliveryMethod, ObservableFactory)} and
     * {@link #launch(Object)} variants should be used.
     *
     * @param type              a type of the channel.
     * @param observableFactory an observable factory which will be used to create an observable per launch.
     * @return an observable which emits {@link rx.Notification} of onNext and onError observable emissions.
     */
    public <A, T> Observable<Notification<T>> channel(
        final DeliveryMethod type,
        final ObservableFactory<A, T> observableFactory) {

        return channel(type, new Func1<Object, Observable<Notification<T>>>() {
            @Override
            public Observable<Notification<T>> call(final Object arg) {
                return ReconnectableMap.INSTANCE.channel(key, type, new Func0<Observable<T>>() {
                    @Override
                    public Observable<T> call() {
                        return observableFactory.call((A) arg);
                    }
                });
            }
        });
    }

    /**
     * Launches an observable without providing arguments.
     * Dismisses the previous observable instance if it is not completed yet.
     */
    public void launch() {
        launch(null);
    }

    /**
     * Launches an observable providing an argument.
     * Dismisses the previous observable instance if it is not completed yet.
     *
     * Note that to make use of arguments, both
     * {@link #channel(DeliveryMethod, ObservableFactory)} and
     * {@link #launch(Object)} variants should be used.
     *
     * @param arg an argument for the new observable.
     *            It must satisfy {@link android.os.Parcel#writeValue(Object)}
     *            method argument requirements.
     */
    public void launch(Object arg) {
        ReconnectableMap.INSTANCE.dismiss(key);
        out.put("restore", true);
        out.put("arg", arg);
        launches.onNext(arg);
    }

    /**
     * Unsubscribes and dismisses the current observable.
     */
    public void dismiss() {
        ReconnectableMap.INSTANCE.dismiss(key);
        out.remove("restore");
        out.remove("arg");
    }

    private <T> Observable<Notification<T>> channel(final DeliveryMethod type, Func1<Object, Observable<Notification<T>>> instantiate) {
        return (restore ? launches.startWith(arg) : launches)
            .switchMap(instantiate)
            .doOnNext(new Action1<Notification<T>>() {
                @Override
                public void call(Notification<T> notification) {
                    type.onNext(Restartable.this);
                }
            });
    }
}
