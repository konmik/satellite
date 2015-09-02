package satellite;

import rx.Notification;
import rx.Observable;
import satellite.io.InputMap;

/**
 * This is the abstraction for the easier implementation of Android view classes that
 * are able to launch satellites.
 */
public interface Launcher {
    <T> Observable<Notification<T>> connection(int id, SubjectFactory<T> factory, SatelliteFactory<T> satelliteFactory);
    void launch(int id, InputMap missionStatement);
    void dismiss(int id);
}
