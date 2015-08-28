package satellite.connections;

import android.os.Bundle;

import rx.Notification;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func0;
import rx.subjects.Subject;
import satellite.SatelliteFactory;

public class Connection<T> implements Observable.OnSubscribe<Notification<T>> {

    private static final String SATELLITE_SUBSCRIPTION_POSTFIX = " /satellite_subscription";
    private static final String SUBJECT_POSTFIX = " /subject";

    public interface SubjectFactory<T> extends Func0<Subject<Notification<T>, Notification<T>>> {
    }

    private final String key;
    private final SatelliteFactory<T> factory;
    private final SubjectFactory<T> subjectFactory;
    private final Bundle missionStatement;

    public Connection(String key, SatelliteFactory<T> factory, SubjectFactory<T> subjectFactory, Bundle missionStatement) {
        this.key = key;
        this.factory = factory;
        this.subjectFactory = subjectFactory;
        this.missionStatement = missionStatement;
    }

    @Override
    public void call(Subscriber<? super Notification<T>> subscriber) {
        Observable<Notification<T>> subject = SpaceStation.INSTANCE
            .provide(key + SUBJECT_POSTFIX, new Func0<Observable<Notification<T>>>() {
                @Override
                public Observable<Notification<T>> call() {

                    Subject<Notification<T>, Notification<T>> subject = subjectFactory.call();

                    SpaceStation.INSTANCE.put(key + SATELLITE_SUBSCRIPTION_POSTFIX, factory.call(missionStatement)
                        .materialize()
                        .subscribe(subject));

                    return subject;
                }
            });

        subscriber.add(subject.subscribe(subscriber));
    }

    public static void recycle(String key) {
        SpaceStation.INSTANCE.remove(key + SUBJECT_POSTFIX);
        Subscription subscription = SpaceStation.INSTANCE.get(key + SATELLITE_SUBSCRIPTION_POSTFIX);
        if (subscription != null) {
            subscription.unsubscribe();
            SpaceStation.INSTANCE.remove(key + SATELLITE_SUBSCRIPTION_POSTFIX);
        }
    }
}
