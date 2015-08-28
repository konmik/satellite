package satellite.connections;

import android.os.Bundle;

import rx.Notification;
import rx.Observable;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;
import satellite.MissionControlCenter;
import satellite.SatelliteFactory;

public class ReplayConnection<T> extends BaseConnection<T> {

    private final SatelliteFactory<T> factory;
    private final Bundle missionStatement;

    public ReplayConnection(String key, SatelliteFactory<T> factory, Bundle missionStatement) {
        super(key);
        this.factory = factory;
        this.missionStatement = missionStatement;
    }

    @Override
    protected Observable<T> createSatellite() {
        return factory.call(missionStatement);
    }

    @Override
    protected Subject<Notification<T>, Notification<T>> createSubject() {
        return ReplaySubject.create();
    }
}
