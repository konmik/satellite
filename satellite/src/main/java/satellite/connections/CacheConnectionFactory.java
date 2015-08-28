package satellite.connections;

import android.os.Bundle;

import rx.Notification;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;
import satellite.MissionControlCenter;
import satellite.SatelliteFactory;

public class CacheConnectionFactory<T> implements MissionControlCenter.ConnectionFactory<T> {

    private final SatelliteFactory<T> satelliteFactory;

    public CacheConnectionFactory(SatelliteFactory<T> satelliteFactory) {
        this.satelliteFactory = satelliteFactory;
    }

    @Override
    public Connection<T> call(String key, Bundle bundle) {
        return new Connection<>(key, satelliteFactory, new Connection.SubjectFactory<T>() {
            @Override
            public Subject<Notification<T>, Notification<T>> call() {
                return BehaviorSubject.create();
            }
        }, bundle);
    }
}
