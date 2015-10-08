package satellite;

import rx.Notification;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import valuemap.ValueMap;

/**
 * Restartable controls only one restartable. A restartable is an
 * {@link Observable} which should be restarted after a process (not application) restart.
 *
 * Restartable saves restartable arguments and restarts the observable in
 * case of a process restart.
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
     */
    public Restartable(ValueMap.Builder out) {
        this.out = out;
        key = "id:" + ++id + " /time:" + System.nanoTime() + " /random:" + (int)(Math.random() * Long.MAX_VALUE);
        restore = false;
        arg = null;
        out.put("key", key);
    }

    /**
     * Creates a new Restartable form a given state that has been received
     * from previous instance out.
     */
    public Restartable(ValueMap in, ValueMap.Builder out) {
        this.out = out;
        key = in.get("key");
        restore = in.get("restore", false);
        arg = in.get("arg");
    }

    /**
     * Provides a connection to an observable through a given observable factory and a given intermediate subject factory.
     *
     * @param subjectFactory     a subject factory which creates a subject to
     *                           transmit observable emissions to views.
     * @param restartableFactory an observable factory which will be used to create an observable per launch.
     * @return an observable which emits {@link rx.Notification} of the restartable emissions.
     */
    public <T> Observable<Notification<T>> channel(
        final DeliveryMethod type,
        final RestartableFactoryNoArg<T> restartableFactory) {

        return channel(type, new Func1<Object, Observable<Notification<T>>>() {
            @Override
            public Observable<Notification<T>> call(Object ignored) {
                return ReconnectableMap.INSTANCE.channel(key, type, restartableFactory);
            }
        });
    }

    /**
     * Provides a connection to the given observable through a given observable factory and a given intermediate subject factory.
     *
     * This {@link #channel(SubjectFactory, RestartableFactoryNoArg)} variant is intended for observable factories that
     * require arguments.
     *
     * Note that to make use of arguments, both
     * {@code #connection(SubjectFactory, RestartableFactory)} and
     * {@link #launch(Object)} variants should be used.
     *
     * @param subjectFactory     a subject factory which creates a subject to
     *                           transmit observable emissions to views.
     * @param restartableFactory an observable factory which will be used to create an observable per launch.
     * @return an observable which emits {@link rx.Notification} of the restartable emissions.
     */
    public <A, T> Observable<Notification<T>> channel(
        final DeliveryMethod type,
        final RestartableFactory<A, T> restartableFactory) {

        return channel(type, new Func1<Object, Observable<Notification<T>>>() {
            @Override
            public Observable<Notification<T>> call(final Object arg) {
                return ReconnectableMap.INSTANCE.channel(key, type, new Func0<Observable<T>>() {
                    @Override
                    public Observable<T> call() {
                        return restartableFactory.call((A)arg);
                    }
                });
            }
        });
    }

    /**
     * Launches a restartable without providing arguments.
     * Dismisses the previous observable instance if it is not completed yet.
     */
    public void launch() {
        launch(null);
    }

    /**
     * Launches a restartable observable, providing an argument.
     * Dismisses the previous observable instance if it is not completed yet.
     *
     * Note that to make use of arguments, both
     * {@code #connection(SubjectFactory, RestartableFactory)} and
     * {@link #launch(Object)} variants should be used.
     *
     * @param arg an argument for the new observable.
     *            It must satisfy {@link android.os.Parcel#writeValue(Object)}
     *            method requirements.
     */
    public void launch(Object arg) {
        ReconnectableMap.INSTANCE.dismiss(key);
        out.put("restore", true);
        out.put("arg", arg);
        launches.onNext(arg);
    }

    /**
     * Unsubscribes and dismisses the observable.
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
                    type.onNext(key);
                }
            });
    }
}
