package satellite.connections;

import rx.Notification;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;
import rx.subjects.Subject;
import satellite.SatelliteFactory;
import satellite.io.InputMap;

public class Connection {

    public static <T> Observable.OnSubscribe<Notification<T>> factory(final String key, final Func0<Subject<Notification<T>, Notification<T>>> subjectFactory, final SatelliteFactory<T> satelliteFactory, final InputMap missionStatement) {
        return new Observable.OnSubscribe<Notification<T>>() {
            @Override
            public void call(Subscriber<? super Notification<T>> subscriber) {
                subscriber.add(
                    SpaceStation.INSTANCE
                        .provideSubject(key, new Func0<Subject<Notification<T>, Notification<T>>>() {
                            @Override
                            public Subject<Notification<T>, Notification<T>> call() {
                                Subject<Notification<T>, Notification<T>> subject = subjectFactory.call();
                                SpaceStation.INSTANCE.takeSubscription(key, satelliteFactory.call(missionStatement)
                                    .materialize()
                                    .subscribe(subject));
                                return subject;
                            }
                        })
                        .subscribe(subscriber));
            }
        };
    }

    public static void recycle(String key) {
        SpaceStation.INSTANCE.dropSubject(key);
        SpaceStation.INSTANCE.dropSubscription(key);
    }
}
