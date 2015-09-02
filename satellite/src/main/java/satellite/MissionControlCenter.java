package satellite;

import rx.Notification;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import satellite.connections.SpaceStation;
import satellite.io.InputMap;
import satellite.io.OutputMap;

/**
 * MissionControlCenter controls only one satellite.
 */
public class MissionControlCenter {

    private final String key;
    private final boolean restore;
    private final InputMap statement;
    private final OutputMap out;

    private final PublishSubject<InputMap> launches = PublishSubject.create();

    private static long id;

    public MissionControlCenter() {
        key = "id:" + ++id + " /time:" + System.nanoTime() + " /random:" + (int)(Math.random() * Long.MAX_VALUE);
        restore = false;
        statement = InputMap.EMPTY;
        out = new OutputMap()
            .put("key", key);
    }

    public MissionControlCenter(InputMap in) {
        key = in.get("key");
        restore = in.get("restore", false);
        statement = in.get("statement", InputMap.EMPTY);
        out = in.toOutput();
    }

    public <T> Observable<Notification<T>> connection(final SubjectFactory<T> subjectFactory, final SatelliteFactory<T> satelliteFactory) {
        return (restore ? launches.startWith(statement) : launches)
            .switchMap(new Func1<InputMap, Observable<Notification<T>>>() {
                @Override
                public Observable<Notification<T>> call(final InputMap statement) {
                    return SpaceStation.INSTANCE.provide(key, subjectFactory, new Func0<Observable<T>>() {
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
                    if (notification.isOnCompleted())
                        dismiss();
                }
            });
    }

    public void launch(InputMap statement) {
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

    public InputMap saveInstanceState() {
        return out.toInput();
    }
}
