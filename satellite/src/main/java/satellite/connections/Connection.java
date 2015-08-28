package satellite.connections;

import rx.Notification;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;
import rx.subjects.Subject;
import satellite.SatelliteFactory;
import satellite.io.InputMap;

public class Connection<T> implements Observable.OnSubscribe<Notification<T>> {

    public interface SubjectFactory<T> extends Func0<Subject<Notification<T>, Notification<T>>> {
    }

    private final String key;
    private final SatelliteFactory<T> satelliteFactory;
    private final SubjectFactory<T> subjectFactory;
    private final InputMap missionStatement;

    public Connection(String key, SatelliteFactory<T> satelliteFactory, SubjectFactory<T> subjectFactory, InputMap missionStatement) {
        this.key = key;
        this.satelliteFactory = satelliteFactory;
        this.subjectFactory = subjectFactory;
        this.missionStatement = missionStatement;
    }

    @Override
    public void call(Subscriber<? super Notification<T>> subscriber) {
        Observable<Notification<T>> subject = SpaceStation.INSTANCE
            .provideSubject(key, new Func0<Subject<Notification<T>, Notification<T>>>() {
                @Override
                public Subject<Notification<T>, Notification<T>> call() {

                    Subject<Notification<T>, Notification<T>> subject = subjectFactory.call();

                    SpaceStation.INSTANCE.takeSubscription(key, satelliteFactory.call(missionStatement)
                        .materialize()
                        .subscribe(subject));

                    return subject;
                }
            });

        subscriber.add(subject.subscribe(subscriber));
    }

    public static void recycle(String key) {
        SpaceStation.INSTANCE.dropSubject(key);
        SpaceStation.INSTANCE.dropSubscription(key);
    }
}
