package satellite.connections;

import android.os.Bundle;

import rx.Notification;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;
import satellite.SatelliteFactory;

public class SingleConnection<T> extends BaseConnection<T> {

    private final SatelliteFactory<T> factory;
    private final Bundle missionStatement;

    public SingleConnection(String key, SatelliteFactory<T> factory, Bundle missionStatement) {
        super(key);
        this.factory = factory;
        this.missionStatement = missionStatement;
    }

    @Override
    protected Observable<T> createSatellite() {
        return factory.call(missionStatement)
            .first();
    }

    @Override
    protected Subject<Notification<T>, Notification<T>> createSubject() {
        return BehaviorSubject.create();
    }
}
