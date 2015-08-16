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
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.Subject;

@SuppressWarnings("unchecked")
enum SpaceStation {

    INSTANCE;

    private HashMap<String, Subject> earthConnections = new HashMap<>();
    private HashMap<String, Subscription> satelliteConnections = new HashMap<>();
    private HashMap<String, Object> raw = new HashMap<>();

    public <T> T provide(String key, Func0<T> factory) {
        if (!raw.containsKey(key))
            raw.put(key, factory.call());
        return (T)raw.get(key);
    }

    public <T> T get(String key) {
        return (T)raw.get(key);
    }

    public <T> void put(String key, T value) {
        raw.put(key, value);
    }

    public void remove(String key) {
        raw.remove(key);
    }

    public <T> Observable<Notification<T>> connection(final String key, Func0<Subject> connectionFactory) {
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
                .materialize()
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        satelliteConnections.remove(key);
                    }
                })
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
        if (subscription != null)
            subscription.unsubscribe();
    }

    public void clear(Collection<String> keys) {
        for (String key : keys.toArray(new String[keys.size()])) {
            disconnectSatellite(key);
            earthConnections.remove(key);
        }
    }

    void print(Printer printer) {
        printer.println("satellite connection keys:");
        for (String key : satelliteConnections.keySet())
            printer.println(key);
        printer.println("earth connection keys:");
        for (String key : earthConnections.keySet())
            printer.println(key);
        printer.println("raw keys:");
        for (String key : raw.keySet())
            printer.println(key);
    }

    List<String> getEarthConnectionKeys() {
        return new ArrayList<>(earthConnections.keySet());
    }

    List<String> getSatelliteConnectionKeys() {
        return new ArrayList<>(satelliteConnections.keySet());
    }
}
