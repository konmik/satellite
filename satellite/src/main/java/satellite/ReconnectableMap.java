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
 * ReconnectableMap keeps track of reconnectable observables (reconnectable observable is an
 * {@link Observable} that emits materialized values into a {@link Subject}). The
 * subject is used to (re)connect to the observable.
 *
 * Reconnectable observables are used by {@link RestartableConnection}.
 */
public enum ReconnectableMap {

    INSTANCE;

    private HashMap<String, Object[]> connections = new HashMap<>();

    /**
     * This is the core method that connects an observable with {@link RestartableConnection}.
     * The observable gets created if it does not exist yet.
     *
     * @param key               a unique key of the connection.
     * @param subjectFactory    a factory for creating a subject that lies between the observable and {@link RestartableConnection}.
     * @param observableFactory an observable factory.
     * @param <T>               a type of observable`s onNext values
     * @return an observable that emits materialized notifications
     */
    public <T> Observable<Notification<T>> connection(
        final String key,
        final SubjectFactory<Notification<T>> subjectFactory,
        final Func0<Observable<T>> observableFactory) {

        return Observable.create(new Observable.OnSubscribe<Notification<T>>() {
            @Override
            public void call(Subscriber<? super Notification<T>> subscriber) {
                if (connections.containsKey(key))
                    subscriber.add(((Subject)connections.get(key)[1]).subscribe(subscriber));
                else {
                    Subject<Notification<T>, Notification<T>> subject = subjectFactory.call();
                    subscriber.add(subject.subscribe(subscriber));
                    connections.put(key, new Object[]{observableFactory.call().materialize().subscribe(subject), subject});
                }
            }
        });
    }

    /**
     * Unsubscribes a given observable and removes its subject.
     *
     * @param key a unique key of the connection.
     */
    public void dismiss(String key) {
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
