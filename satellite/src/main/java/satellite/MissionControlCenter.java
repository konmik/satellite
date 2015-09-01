package satellite;

import rx.Notification;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;
import satellite.connections.Connection;
import satellite.io.InputMap;
import satellite.io.OutputMap;

/**
 * MissionControlCenter controls only one satellite.
 */
public class MissionControlCenter {

    public interface ConnectionFactory<T> extends Func2<String, InputMap, Connection<T>> {
    }

    private final String key;
    private final boolean restore;
    private final InputMap statement;
    private final OutputMap out;

    private final PublishSubject<InputMap> launches = PublishSubject.create();

    private static long id;

    public MissionControlCenter(InputMap inA) {
        InputMap in = inA != null ? inA :
            new InputMap("key", "id:" + ++id + " /time:" + System.nanoTime() + " /random:" + (int)(Math.random() * Long.MAX_VALUE));

        key = in.get("key");
        restore = in.get("restore", false);
        statement = in.get("statement");
        out = in.toOutput();
    }

    public InputMap saveInstanceState() {
        return out.toInput();
    }

    public <T> Observable<Notification<T>> connection(final ConnectionFactory<T> factory) {
        return (restore ? launches.startWith(statement) : launches)
            .switchMap(new Func1<InputMap, Observable<Notification<T>>>() {
                @Override
                public Observable<Notification<T>> call(InputMap input) {
                    return Observable.create(factory.call(key, input));
                }
            })
            .doOnNext(new Action1<Notification<T>>() {
                @Override
                public void call(Notification<T> tNotification) {
                    if (tNotification.isOnCompleted())
                        dismiss();
                }
            });
    }

    public void launch(InputMap statement) {
        Connection.recycle(key);
        out.put("restore", true);
        out.put("statement", statement);
        launches.onNext(statement);
    }

    public void dismiss() {
        out.remove("restore");
        out.remove("statement");
        Connection.recycle(key);
    }
}
