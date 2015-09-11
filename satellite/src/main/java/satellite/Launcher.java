package satellite;

import rx.Notification;
import rx.Observable;
import satellite.util.SubjectFactory;

/**
 * This is an abstraction for the easier implementation of base Android view classes that
 * are able to launch satellites.
 */
public interface Launcher {
    <A, T> Observable<Notification<T>> connection(int id, SubjectFactory<Notification<T>> subjectFactory, SatelliteFactory<A, T> satelliteFactory);
    <A> void launch(int id, A missionStatement);
    void dismiss(int id);
}
