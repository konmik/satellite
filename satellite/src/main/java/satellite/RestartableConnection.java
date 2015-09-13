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
 * RestartableConnection controls only one restartable.
 * It saves satellite arguments and restarts the satellite in
 * case of the process restart if the launched satellite is not completed.
 */
public class RestartableConnection<A, T> {

    private final String key;
    private final boolean restore;
    private final A statement;
    private final StateMap.Builder out;

    private final PublishSubject<A> launches = PublishSubject.create();

    private static long id;

    /**
     * Creates a new RestartableConnection.
     */
    public RestartableConnection() {
        key = "id:" + ++id + " /time:" + System.nanoTime() + " /random:" + (int)(Math.random() * Long.MAX_VALUE);
        restore = false;
        statement = (A)StateMap.empty();
        out = new StateMap.Builder().put("key", key);
    }

    /**
     * Creates a new RestartableConnection form a given state that has been received
     * from {@link #instanceState()}.
     */
    public RestartableConnection(StateMap in) {
        key = in.get("key");
        restore = in.get("restore", false);
        statement = (A)in.get("statement", StateMap.empty());
        out = in.toBuilder();
    }

    /**
     * Provides a connection to the given satellite through a given subject.
     *
     * @param subjectFactory     a subject factory which creates a subject to
     *                           transmit satellite emissions to views.
     * @param restartableFactory a satellite factory which will be used on launch.
     * @return an observable which emits {@link rx.Notification} of satellite emissions.
     */
    public Observable<Notification<T>> connection(
        final SubjectFactory<Notification<T>> subjectFactory,
        final RestartableFactory<A, T> restartableFactory) {

        return (restore ? launches.startWith(statement) : launches)
            .switchMap(new Func1<A, Observable<Notification<T>>>() {
                @Override
                public Observable<Notification<T>> call(final A statement) {
                    return ReconnectableMap.INSTANCE.connection(key, subjectFactory, new Func0<Observable<T>>() {
                        @Override
                        public Observable<T> call() {
                            return restartableFactory.call(statement);
                        }
                    });
                }
            })
            .doOnNext(new Action1<Notification<T>>() {
                @Override
                public void call(Notification<T> notification) {
                    if (notification.isOnCompleted() || notification.isOnError())
                        dismiss();
                }
            });
    }

    /**
     * Launches a new satellite instance. Dismisses
     * the previous satellite instance if it is not completed yet.
     *
     * @param missionStatement a mission statement for the new satellite.
     *                         It must satisfy {@link android.os.Parcel#writeValue(Object)}
     *                         method requirements.
     */
    public void launch(A missionStatement) {
        ReconnectableMap.INSTANCE.recycle(key);
        out.put("restore", true);
        out.put("statement", missionStatement);
        launches.onNext(missionStatement);
    }

    public void launch() {
        launch(null);
    }

    /**
     * Dismisses the current satellite.
     */
    public void dismiss() {
        ReconnectableMap.INSTANCE.recycle(key);
        out.remove("restore");
        out.remove("statement");
    }

    /**
     * Returns the instance state that can be used to create a new
     * RestartableConnection later.
     */
    public StateMap instanceState() {
        return out.build();
    }
}
