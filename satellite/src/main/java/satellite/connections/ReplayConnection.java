package satellite.connections;

import android.os.Bundle;

import rx.Notification;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func0;
import rx.subjects.ReplaySubject;
import satellite.MissionControlCenter;
import satellite.SatelliteFactory;

public class ReplayConnection<T> implements MissionControlCenter.SessionTypeOnSubscribe<T> {

    private static final MissionControlCenter.SessionFactory<Object> SESSION_FACTORY = new MissionControlCenter.SessionFactory<Object>() {
        @Override
        public MissionControlCenter.SessionTypeOnSubscribe<Object> call(String key, SatelliteFactory<Object> satelliteFactory, Bundle bundle) {
            return new ReplayConnection<>(key, satelliteFactory, bundle);
        }
    };

    public static <T> MissionControlCenter.SessionFactory<T> factory() {
        return (MissionControlCenter.SessionFactory<T>)SESSION_FACTORY;
    }

    private final String key;
    private final SatelliteFactory<T> factory;
    private final Bundle missionStatement;

    public ReplayConnection(String key, SatelliteFactory<T> factory, Bundle missionStatement) {
        this.key = key;
        this.factory = factory;
        this.missionStatement = missionStatement;
    }

    @Override
    public void call(Subscriber<? super Notification<T>> subscriber) {

        Observable<Notification<T>> subject = SpaceStation.INSTANCE
            .provide(key + "/subject", new Func0<Observable<Notification<T>>>() {
                @Override
                public Observable<Notification<T>> call() {

                    ReplaySubject<Notification<T>> subject = ReplaySubject.create();

                    SpaceStation.INSTANCE.put(key + "/subscription", factory.call(missionStatement)
                        .materialize()
                        .subscribe(subject));

                    return subject;
                }
            });

        subscriber.add(subject.subscribe(subscriber));
    }

    @Override
    public void recycle() {
        SpaceStation.INSTANCE.remove(key + "/subject");
        Subscription subscription = SpaceStation.INSTANCE.get(key + "/subscription");
        if (subscription != null) {
            subscription.unsubscribe();
            SpaceStation.INSTANCE.remove(key + "/subscription");
        }
    }
}
