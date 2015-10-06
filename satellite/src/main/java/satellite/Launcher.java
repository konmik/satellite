package satellite;

import rx.Notification;
import rx.Observable;
import satellite.util.SubjectFactory;

/**
 * This is an abstraction for the easier implementation of classes that
 * are able to launch restartables.
 *
 * See {@link RestartableSet} for JavaDoc.
 */
public interface Launcher {
    <T> Observable<Notification<T>> restartable(int id, SubjectFactory<Notification<T>> subjectFactory, RestartableFactoryNoArg<T> restartableFactory);
    <A, T> Observable<Notification<T>> restartable(int id, SubjectFactory<Notification<T>> subjectFactory, RestartableFactory<A, T> restartableFactory);
    void launch(int id);
    void launch(int id, Object arg);
    void dismiss(int id);
}
