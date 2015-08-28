package satellite;

import android.os.Bundle;

import rx.Notification;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;
import satellite.connections.Connection;

/**
 * MissionControlCenter controls only one satellite.
 */
public class MissionControlCenter {

    public interface ConnectionFactory<T> extends Func2<String, Bundle, Connection<T>> {
    }

    private final String key;

    private PublishSubject<Bundle> launches = PublishSubject.create();
    private boolean restore;
    private Bundle statement;
    private Connection<?> connection;

    private static long id;

    public MissionControlCenter(Bundle state) {
        if (state == null)
            key = "id:" + ++id + " /time:" + System.nanoTime() + " /random:" + (int)(Math.random() * Long.MAX_VALUE);
        else {
            key = state.getString("key");
            restore = state.getBoolean("restore");
            statement = state.getBundle("statement");
        }
    }

    public Bundle saveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putString("key", key);
        bundle.putBoolean("restore", restore);
        bundle.putBundle("statement", statement);
        return bundle;
    }

    public <T> Observable<Notification<T>> connection(final ConnectionFactory<T> type) {
        return (restore ? launches.startWith(statement) : launches)
            .switchMap(new Func1<Bundle, Observable<Notification<T>>>() {
                @Override
                public Observable<Notification<T>> call(final Bundle bundle) {
                    if (connection != null)
                        Connection.recycle(key);
                    Connection<T> onSubscribe1 = type.call(key, statement);
                    connection = onSubscribe1;
                    return Observable.create(onSubscribe1);
                }
            })
            .doOnNext(new Action1<Notification<T>>() {
                @Override
                public void call(Notification<T> tNotification) {
                    if (tNotification.isOnCompleted())
                        dismiss();
                }
            })
            .filter(new Func1<Notification<T>, Boolean>() {
                @Override
                public Boolean call(Notification<T> notification) {
                    return !notification.isOnCompleted();
                }
            });
    }

    public void launch(Bundle statement) {
        dismiss();
        this.restore = true;
        this.statement = statement;
        launches.onNext(statement);
    }

    public void dismiss() {
        restore = false;
        statement = null;
        if (connection != null) {
            Connection.recycle(key);
            connection = null;
        }
    }
}
