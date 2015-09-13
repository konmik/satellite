package satellite;

import rx.Notification;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import satellite.state.StateMap;
import satellite.util.SubjectFactory;

/**
 * RestartableConnection controls only one restartable. A restartable is an
 * {@link Observable} which should be restarted after a process (not application) restart.
 *
 * RestartableConnection saves restartable arguments and restarts the observable in
 * case of a process restart.
 */
public class RestartableConnection {

    private final String key;
    private final boolean restore;
    private final Object arg;
    private final StateMap.Builder out;

    private final PublishSubject<Object> launches = PublishSubject.create();

    private static long id;

    /**
     * Creates a new RestartableConnection.
     */
    public RestartableConnection() {
        key = "id:" + ++id + " /time:" + System.nanoTime() + " /random:" + (int)(Math.random() * Long.MAX_VALUE);
        restore = false;
        arg = StateMap.empty();
        out = StateMap.builder().put("key", key);
    }

    /**
     * Creates a new RestartableConnection form a given state that has been received
     * from {@link #instanceState()}.
     */
    public RestartableConnection(StateMap in) {
        key = in.get("key");
        restore = in.get("restore", false);
        arg = in.get("arg", StateMap.empty());
        out = in.toBuilder();
    }

    /**
     * Provides a connection to an observable through a given observable factory and a given subject factory.
     *
     * @param subjectFactory     a subject factory which creates a subject to
     *                           transmit observable emissions to views.
     * @param restartableFactory an observable factory which will be used to create an observable per launch.
     * @return an observable which emits {@link rx.Notification} of the restartable emissions.
     */
    public <T> Observable<Notification<T>> connection(
        final SubjectFactory<Notification<T>> subjectFactory,
        final RestartableFactoryNoArg<T> restartableFactory) {

        return connection(new Func1<Object, Observable<Notification<T>>>() {
            @Override
            public Observable<Notification<T>> call(Object ignored) {
                return ReconnectableMap.INSTANCE.connection(key, subjectFactory, restartableFactory);
            }
        });
    }

    /**
     * Provides a connection to the given observable through a given observable factory and a given subject factory.
     *
     * This {@link #connection(SubjectFactory, RestartableFactoryNoArg)} variant is intended for observable factories that
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
    public <A, T> Observable<Notification<T>> connection(
        final SubjectFactory<Notification<T>> subjectFactory,
        final RestartableFactory<A, T> restartableFactory) {

        return connection(new Func1<Object, Observable<Notification<T>>>() {
            @Override
            public Observable<Notification<T>> call(final Object arg) {
                return ReconnectableMap.INSTANCE.connection(key, subjectFactory, new Func0<Observable<T>>() {
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

    /**
     * Returns the instance state that can be used to create a new
     * RestartableConnection later, see {@link #RestartableConnection(StateMap)}.
     */
    public StateMap instanceState() {
        return out.build();
    }

    private <T> Observable<Notification<T>> connection(Func1<Object, Observable<Notification<T>>> instantiate) {
        return (restore ? launches.startWith(arg) : launches)
            .switchMap(instantiate)
            .doOnNext(new Action1<Notification<T>>() {
                @Override
                public void call(Notification<T> notification) {
                    if (notification.isOnCompleted() || notification.isOnError())
                        dismiss();
                }
            });
    }
}
