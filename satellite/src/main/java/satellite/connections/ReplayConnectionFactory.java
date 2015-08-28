package satellite.connections;

import rx.Notification;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;
import satellite.MissionControlCenter;
import satellite.SatelliteFactory;
import satellite.io.InputMap;

public class ReplayConnectionFactory<T> implements MissionControlCenter.ConnectionFactory<T> {

    private final SatelliteFactory<T> satelliteFactory;

    public ReplayConnectionFactory(SatelliteFactory<T> satelliteFactory) {
        this.satelliteFactory = satelliteFactory;
    }

    @Override
    public Connection<T> call(String key, InputMap missionStatement) {
        return new Connection<>(key, satelliteFactory, new Connection.SubjectFactory<T>() {
            @Override
            public Subject<Notification<T>, Notification<T>> call() {
                return ReplaySubject.create();
            }
        }, missionStatement);
    }
}
