package satellite;

import android.os.Parcelable;

import rx.Notification;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import satellite.io.InputMap;
import satellite.io.OutputMap;
import satellite.util.SubjectFactory;

/**
 * MissionControlCenter controls only one satellite.
 */
public class MissionControlCenter<A extends Parcelable, T> {

    private final String key;
    private final boolean restore;
    private final A statement;
    private final OutputMap out;

    private final PublishSubject<A> launches = PublishSubject.create();

    private static long id;

    public MissionControlCenter() {
        key = "id:" + ++id + " /time:" + System.nanoTime() + " /random:" + (int)(Math.random() * Long.MAX_VALUE);
        restore = false;
        statement = (A)InputMap.empty();
        out = new OutputMap()
            .put("key", key);
    }

    public MissionControlCenter(InputMap in) {
        key = in.get("key");
        restore = in.get("restore", false);
        statement = (A)in.get("statement", InputMap.empty());
        out = in.toOutput();
    }

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

    public void launch(A statement) {
        SpaceStation.INSTANCE.recycle(key);
        out.put("restore", true);
        out.put("statement", statement);
        launches.onNext(statement);
    }

    public void dismiss() {
        SpaceStation.INSTANCE.recycle(key);
        out.remove("restore");
        out.remove("statement");
    }

    public InputMap instanceState() {
        return out.toInput();
    }
}
