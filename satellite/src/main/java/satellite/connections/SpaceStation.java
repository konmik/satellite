package satellite.connections;

import android.util.Printer;

import java.util.HashMap;

import rx.Notification;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func0;
import rx.subjects.Subject;
import satellite.MissionControlCenter;

/**
 * SpaceStation represents... a space station! :D
 *
 * It keeps track of satellites and provides earth connections {@link MissionControlCenter}.
 */
public enum SpaceStation {

    INSTANCE;

    private HashMap<String, Subject> subjects = new HashMap<>();
    private HashMap<String, Subscription> subscriptions = new HashMap<>();

    /**
     * This is the core method that connects a satellite with {@link MissionControlCenter}.
     * The satellite gets created if it is not launched.
     *
     * @param key              a unique id of the connection that should survive configuration changes
     * @param subjectFactory   a factory for creating the subject that lies between the satellite and {@link MissionControlCenter}.
     * @param satelliteFactory a satellite factory
     * @param <T>              a type of satellite onNext values
     * @return an observable that emits satellite notifications
     */
    public <T> Observable<Notification<T>> provide(
        final String key,
        final Func0<Subject<Notification<T>, Notification<T>>> subjectFactory,
        final Func0<Observable<T>> satelliteFactory) {

        return Observable.create(new Observable.OnSubscribe<Notification<T>>() {
            @Override
            public void call(Subscriber<? super Notification<T>> subscriber) {
                if (!subjects.containsKey(key)) {
                    Subject<Notification<T>, Notification<T>> subject = subjectFactory.call();
                    subscriptions.put(key, satelliteFactory.call()
                        .materialize()
                        .subscribe(subject));
                    subjects.put(key, subject);
                }
                subscriber.add(subjects.get(key).subscribe(subscriber));
            }
        });
    }

    public void recycle(String key) {
        subjects.remove(key);
        if (subscriptions.containsKey(key)) {
            Subscription subscription = subscriptions.get(key);
            subscription.unsubscribe();
            subscriptions.remove(key);
        }
    }

    public void print(Printer printer) {
        printer.println("subjects:");
        for (String key : subjects.keySet())
            printer.println(key);
        printer.println("subscriptions:");
        for (String key : subscriptions.keySet())
            printer.println(key);
    }
}
