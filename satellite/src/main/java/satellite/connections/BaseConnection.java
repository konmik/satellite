package satellite.connections;

import rx.Notification;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func0;
import rx.subjects.Subject;
import satellite.MissionControlCenter;

public abstract class BaseConnection<T> implements MissionControlCenter.Connection<T> {

    private static final String SATELLITE_SUBSCRIPTION_POSTFIX = " /satellite_subscription";
    private static final String SUBJECT_POSTFIX = " /subject";

    private final String key;

    public BaseConnection(String key) {
        this.key = key;
    }

    @Override
    public void call(Subscriber<? super Notification<T>> subscriber) {
        Observable<Notification<T>> subject = SpaceStation.INSTANCE
            .provide(key + SUBJECT_POSTFIX, new Func0<Observable<Notification<T>>>() {
                @Override
                public Observable<Notification<T>> call() {

                    Subject<Notification<T>, Notification<T>> subject = createSubject();

                    SpaceStation.INSTANCE.put(key + SATELLITE_SUBSCRIPTION_POSTFIX, createSatellite()
                        .materialize()
                        .subscribe(subject));

                    return subject;
                }
            });

        subscriber.add(subject.subscribe(subscriber));
    }

    @Override
    public void recycle() {
        SpaceStation.INSTANCE.remove(key + SUBJECT_POSTFIX);
        Subscription subscription = SpaceStation.INSTANCE.get(key + SATELLITE_SUBSCRIPTION_POSTFIX);
        if (subscription != null) {
            subscription.unsubscribe();
            SpaceStation.INSTANCE.remove(key + SATELLITE_SUBSCRIPTION_POSTFIX);
        }
    }

    protected abstract Observable<T> createSatellite();
    protected abstract Subject<Notification<T>, Notification<T>> createSubject();
}
