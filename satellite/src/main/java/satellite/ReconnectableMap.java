package satellite;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import rx.Notification;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func0;
import rx.subjects.Subject;
import satellite.util.SubjectFactory;

/**
 * ReconnectableMap keeps track of restartable observables and provides observables for {@link RestartableConnection}.
 */
public enum ReconnectableMap {

    INSTANCE;

    private HashMap<String, Object[]> connections = new HashMap<>();

    /**
     * This is the core method that connects a satellite with {@link RestartableConnection}.
     * The satellite gets created if it is not launched yet.
     *
     * @param key              a unique key of the connection that should survive configuration changes
     * @param subjectFactory   a factory for creating the subject that lies between the satellite and {@link RestartableConnection}
     * @param satelliteFactory a satellite factory
     * @param <T>              a type of satellite's onNext values
     * @return an observable that emits satellite notifications
     */
    public <T> Observable<Notification<T>> connection(
        final String key,
        final SubjectFactory<Notification<T>> subjectFactory,
        final Func0<Observable<T>> satelliteFactory) {

        return Observable.create(new Observable.OnSubscribe<Notification<T>>() {
            @Override
            public void call(Subscriber<? super Notification<T>> subscriber) {
                if (connections.containsKey(key))
                    subscriber.add(((Subject)connections.get(key)[1]).subscribe(subscriber));
                else {
                    Subject<Notification<T>, Notification<T>> subject = subjectFactory.call();
                    subscriber.add(subject.subscribe(subscriber));
                    connections.put(key, new Object[]{satelliteFactory.call().materialize().subscribe(subject), subject});
                }
            }
        });
    }

    /**
     * Unsubscribes a given satellite from connection and removes the connection subject.
     *
     * @param key a unique key of the connection.
     */
    public void recycle(String key) {
        if (connections.containsKey(key)) {
            ((Subscription)connections.get(key)[0]).unsubscribe();
            connections.remove(key);
        }
    }

    /**
     * Return a current list of connection keys.
     */
    public Set<String> keys() {
        return Collections.unmodifiableSet(connections.keySet());
    }
}
