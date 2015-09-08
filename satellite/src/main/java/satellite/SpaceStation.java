package satellite;

import android.util.Pair;

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
 * SpaceStation represents... a space station! :D
 *
 * It keeps track of satellites and provides connections for {@link MissionControlCenter}.
 */
public enum SpaceStation {

    INSTANCE;

    private HashMap<String, Pair<Subscription, Subject>> connections = new HashMap<>();

    /**
     * This is the core method that connects a satellite with {@link MissionControlCenter}.
     * The satellite gets created if it is not launched.
     *
     * @param key              a unique id of the connection that should survive configuration changes
     * @param subjectFactory   a factory for creating the subject that lies between the satellite and {@link MissionControlCenter}
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
                if (!connections.containsKey(key)) {
                    Subject<Notification<T>, Notification<T>> subject = subjectFactory.call();
                    connections.put(key,
                        new Pair<Subscription, Subject>(
                            satelliteFactory.call()
                                .materialize()
                                .subscribe(subject),
                            subject));
                }
                subscriber.add(connections.get(key).second.subscribe(subscriber));
            }
        });
    }

    public void recycle(String key) {
        if (connections.containsKey(key)) {
            connections.get(key).first.unsubscribe();
            connections.remove(key);
        }
    }

    public Set<String> keys() {
        return Collections.unmodifiableSet(connections.keySet());
    }
}
