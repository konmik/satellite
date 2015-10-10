package satellite;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import rx.Notification;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.observers.Subscribers;
import rx.subjects.Subject;

/**
 * ReconnectableMap keeps track of reconnectable observables (reconnectable observable is an
 * {@link Observable} that emits materialized values into a {@link Subject}). The
 * subject is used to (re)connect to the observable.
 *
 * Reconnectable observables are used by {@link Restartable}.
 */
public enum ReconnectableMap {

    INSTANCE;

    private HashMap<String, Subject> subjects = new HashMap<>();
    private HashMap<String, Subscription> subscriptions = new HashMap<>();

    /**
     * This is the core method that connects an observable with {@link Restartable}.
     * If the channel does not exist yet it will be created.
     *
     * @param key               a unique key of the connection.
     * @param method            a delivery method which will be used for the channel.
     * @param observableFactory an observable factory.
     * @param <T>               a type of observable`s onNext values
     * @return an observable that emits materialized notifications
     */
    public <T> Observable<Notification<T>> channel(
        final String key,
        final DeliveryMethod method,
        final Func0<Observable<T>> observableFactory) {

        return Observable.create(new Observable.OnSubscribe<Notification<T>>() {
            @Override
            public void call(final Subscriber<? super Notification<T>> subscriber) {
                if (subjects.containsKey(key))
                    subjects.get(key).subscribe(subscriber);
                else {
                    final Subject<Notification<T>, Notification<T>> subject = method.createSubject();
                    subjects.put(key, subject);
                    subject.subscribe(subscriber);

                    final Subscriber<Notification<T>> subjectSubscriber = Subscribers.create(new Action1<Notification<T>>() {
                        @Override
                        public void call(Notification<T> notification) {
                            subject.onNext(notification);
                        }
                    });
                    subscriptions.put(key, subjectSubscriber);

                    observableFactory.call()
                        .materialize()
                        .doOnNext(new Action1<Notification<T>>() {
                            @Override
                            public void call(Notification<T> notification) {
                                if (notification.isOnCompleted() || notification.isOnError())
                                    removeSubscription(key);
                            }
                        })
                        .filter(new Func1<Notification<T>, Boolean>() {
                            @Override
                            public Boolean call(Notification<T> notification) {
                                return !notification.isOnCompleted();
                            }
                        })
                        .subscribe(subjectSubscriber);
                }
            }
        });
    }

    /**
     * Dismisses a channel.
     *
     * @param key a unique key of the channel.
     */
    public void dismiss(String key) {
        removeSubscription(key);
        subjects.remove(key);
    }

    /**
     * Return the current list of channel keys.
     */
    public Set<String> keys() {
        return Collections.unmodifiableSet(subscriptions.keySet());
    }

    private void removeSubscription(String key) {
        Subscription subscription = subscriptions.get(key);
        if (subscription != null) {
            subscription.unsubscribe();
            subscriptions.remove(key);
        }
    }
}
