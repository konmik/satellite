package satellite;

import rx.Notification;
import rx.Observable;
import satellite.util.SubjectFactory;

/**
 * This is an abstraction for the easier implementation of base Android view classes that
 * are able to launch satellites.
 */
public interface Launcher {
    /**
     * Creates a connection given connection id, subject factory and satellite factory.
     */
    <A, T> Observable<Notification<T>> connection(int id, SubjectFactory<Notification<T>> subjectFactory, RestartableFactory<A, T> restartableFactory);

    /**
     * Launches a satellite on a given connection id given a mission statement.
     */
    <A> void launch(int id, A missionStatement);

    /**
     * Dismisses a satellite on a given connection id.
     */
    void dismiss(int id);
}
