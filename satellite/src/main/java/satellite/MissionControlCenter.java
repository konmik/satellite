package satellite;

import rx.Notification;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import satellite.io.StateMap;
import satellite.util.SubjectFactory;

/**
 * MissionControlCenter controls only one satellite.
 * It saves satellite arguments and restarts the satellite in
 * case of the process restart if the launched satellite is not completed.
 */
public class MissionControlCenter<A, T> {

    private final String key;
    private final boolean restore;
    private final A statement;
    private final StateMap.Builder out;

    private final PublishSubject<A> launches = PublishSubject.create();

    private static long id;

    /**
     * Creates a new MissionControlCenter.
     */
    public MissionControlCenter() {
        key = "id:" + ++id + " /time:" + System.nanoTime() + " /random:" + (int)(Math.random() * Long.MAX_VALUE);
        restore = false;
        statement = (A)StateMap.empty();
        out = new StateMap.Builder().put("key", key);
    }

    /**
     * Creates a new MissionControlCenter form a given state that has been received
     * from {@link #instanceState()}.
     */
    public MissionControlCenter(StateMap in) {
        key = in.get("key");
        restore = in.get("restore", false);
        statement = (A)in.get("statement", StateMap.empty());
        out = in.toBuilder();
    }

    /**
     * Provides a connection to the given satellite through a given subject.
     *
     * @param subjectFactory   a subject factory which creates a subject to
     *                         transmit satellite emissions to views.
     * @param satelliteFactory a satellite factory which will be used on launch.
     * @return an observable which emits {@link rx.Notification} of satellite emissions.
     */
    public Observable<Notification<T>> connection(
        final SubjectFactory<Notification<T>> subjectFactory,
        final SatelliteFactory<A, T> satelliteFactory) {

        return (restore ? launches.startWith(statement) : launches)
            .switchMap(new Func1<A, Observable<Notification<T>>>() {
                @Override
                public Observable<Notification<T>> call(final A statement) {
                    return SpaceStation.INSTANCE.connection(key, subjectFactory, new Func0<Observable<T>>() {
                        @Override
                        public Observable<T> call() {
                            return satelliteFactory.call(statement);
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
        SpaceStation.INSTANCE.recycle(key);
        out.put("restore", true);
        out.put("statement", missionStatement);
        launches.onNext(missionStatement);
    }

    /**
     * Dismisses the current satellite.
     */
    public void dismiss() {
        SpaceStation.INSTANCE.recycle(key);
        out.remove("restore");
        out.remove("statement");
    }

    /**
     * Returns the instance state that can be used to create a new
     * MissionControlCenter later.
     */
    public StateMap instanceState() {
        return out.build();
    }
}
