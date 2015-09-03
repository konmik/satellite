package satellite;

import rx.Notification;
import rx.Observable;
import satellite.io.InputMap;
import satellite.subjects.SubjectFactory;

/**
 * This is an abstraction for the easier implementation of base Android view classes that
 * are able to launch satellites.
 */
public interface Launcher {
    <T> Observable<Notification<T>> connection(int id, SubjectFactory<T> subjectFactory, SatelliteFactory<T> satelliteFactory);
    void launch(int id, InputMap missionStatement);
    void dismiss(int id);
}
