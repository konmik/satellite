package satellite;

import android.os.Bundle;

import rx.Notification;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import satellite.connections.CacheResultConnectionOnSubscribe;
import satellite.connections.ReplayResultConnectionOnSubscribe;
import satellite.connections.SingleResultConnectionOnSubscribe;

/**
 * MissionControlCenter controls only one satellite.
 */
public class MissionControlCenter {

    public interface SessionTypeOnSubscribe<T> extends Observable.OnSubscribe<Notification<T>> {
        void recycle();
    }

    public enum SessionType {
        SINGLE {
            @Override
            protected <T> SessionTypeOnSubscribe<T> createOnSubscribe(String key, SatelliteFactory<T> factory, Bundle missionStatement) {
                return new SingleResultConnectionOnSubscribe<>(key, factory, missionStatement);
            }
        },
        CACHE {
            @Override
            protected <T> SessionTypeOnSubscribe<T> createOnSubscribe(String key, SatelliteFactory<T> factory, Bundle missionStatement) {
                return new CacheResultConnectionOnSubscribe<>(key, factory, missionStatement);
            }
        },
        REPLAY {
            @Override
            protected <T> SessionTypeOnSubscribe<T> createOnSubscribe(String key, SatelliteFactory<T> factory, Bundle missionStatement) {
                return new ReplayResultConnectionOnSubscribe<>(key, factory, missionStatement);
            }
        };

        protected abstract <T> SessionTypeOnSubscribe<T> createOnSubscribe(String key, SatelliteFactory<T> factory, Bundle missionStatement);
    }

    private final SessionType type;
    private final String key;

    private PublishSubject<Bundle> launches = PublishSubject.create();
    private boolean restore;
    private Bundle statement;
    private SessionTypeOnSubscribe<?> onSubscribe;

    private static long id;

    public MissionControlCenter(SessionType type, Bundle state) {
        this.type = type;
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

    public <T> Observable<Notification<T>> connection(final SatelliteFactory<T> factory) {
        return (restore ? launches.startWith(statement) : launches)
            .switchMap(new Func1<Bundle, Observable<Notification<T>>>() {
                @Override
                public Observable<Notification<T>> call(final Bundle bundle) {
                    if (onSubscribe != null)
                        onSubscribe.recycle();
                    SessionTypeOnSubscribe<T> onSubscribe1 = type.createOnSubscribe(key, factory, statement);
                    onSubscribe = onSubscribe1;
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
        if (onSubscribe != null) {
            onSubscribe.recycle();
            onSubscribe = null;
        }
    }
}
