package satellite.connections;

import rx.Notification;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;
import satellite.SubjectFactory;

public class ReplaySubjectFactory<T> implements SubjectFactory<T> {
    @Override
    public Subject<Notification<T>, Notification<T>> call() {
        return ReplaySubject.create();
    }
}
