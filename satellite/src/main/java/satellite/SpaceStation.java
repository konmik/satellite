package satellite;

import android.util.Printer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import rx.Notification;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.Subject;
import satellite.util.LogTransformer;

@SuppressWarnings("unchecked")
enum SpaceStation {

    INSTANCE;

    private HashMap<String, Subject> earthConnections = new HashMap<>();
    private HashMap<String, Subscription> satelliteConnections = new HashMap<>();

    public <T> Observable<Notification<T>> connection(String key, Func0<Subject> connectionFactory) {
        if (earthConnections.get(key) == null) {
            earthConnections.put(key, connectionFactory.call());
        }
        return earthConnections.get(key);
    }

    public void connectSatellite(final String key, Func0<Observable> satelliteFactory) {
        Subscription subscription = satelliteConnections.get(key);
        if (subscription == null || subscription.isUnsubscribed()) {
            final Observer subject = earthConnections.get(key);
            satelliteConnections.put(key, satelliteFactory.call()
                .compose(new LogTransformer("Satellite " + key + " -->"))
                .materialize()
                .filter(new Func1<Notification, Boolean>() {
                    @Override
                    public Boolean call(Notification notification) {
                        return !notification.isOnCompleted();
                    }
                })
                .subscribe(new Action1() {
                    @Override
                    public void call(Object o) {
                        subject.onNext(o);
                    }
                }));
        }
    }

    public void disconnectSatellite(String key) {
        Subscription subscription = satelliteConnections.get(key);
        if (subscription != null) {
            subscription.unsubscribe();
            satelliteConnections.remove(key);
        }
    }

    public void clear(Collection<String> keys) {
        for (String key : keys.toArray(new String[keys.size()])) {
            disconnectSatellite(key);
            earthConnections.remove(key);
        }
    }

    public void print(Printer printer) {
        printer.println("satellite connection keys:");
        for (String key : satelliteConnections.keySet())
            printer.println(key);
        printer.println("earth connection keys:");
        for (String key : earthConnections.keySet())
            printer.println(key);
    }

    List<String> getEarthConnectionKeys() {
        return new ArrayList<>(earthConnections.keySet());
    }

    List<String> getSatelliteConnectionKeys() {
        return new ArrayList<>(satelliteConnections.keySet());
    }
}
