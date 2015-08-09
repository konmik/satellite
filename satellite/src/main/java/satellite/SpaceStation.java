package satellite;

import java.util.Collection;
import java.util.HashMap;

import rx.Notification;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.Subject;
import satellite.util.LogTransformer;

@SuppressWarnings("unchecked")
enum SpaceStation {

    INSTANCE;

    private HashMap<String, Subject> earthConnectionSubject = new HashMap<>();
    private HashMap<String, Subscription> satelliteConnections = new HashMap<>();

    public <T> Observable<Notification<T>> connection(String key, SessionType session) {
        if (earthConnectionSubject.get(key) == null) {
            earthConnectionSubject.put(key, session.createSubject());
        }
        return earthConnectionSubject.get(key);
    }

    public boolean satelliteConnectionExist(String key) {
        Subscription subscription = satelliteConnections.get(key);
        return subscription != null && !subscription.isUnsubscribed();
    }

    public void connectWithSatellite(final String key, Observable satellite) {
        final Observer subject = earthConnectionSubject.get(key);
        satelliteConnections.put(key, satellite
            .compose(new LogTransformer("Satellite " + key + " -->"))
            .materialize()
            .filter(new Func1<Notification, Boolean>() {
                @Override
                public Boolean call(Notification o) {
                    return !o.isOnCompleted();
                }
            })
            .subscribe(new Action1() {
                @Override
                public void call(Object o) {
                    subject.onNext(o);
                }
            }));
    }

    public void disconnectFromSatellite(String key) {
        Subscription subscription = satelliteConnections.get(key);
        if (subscription != null) {
            subscription.unsubscribe();
            satelliteConnections.remove(key);
        }
    }

    public void clear(Collection<String> keys) {
        for (String key : keys.toArray(new String[keys.size()])) {
            disconnectFromSatellite(key);
            earthConnectionSubject.remove(key);
        }
    }
}
