package satellite.connections;

import rx.Notification;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;
import satellite.SubjectFactory;

public class CacheSubjectFactory<T> implements SubjectFactory<T> {
    @Override
    public Subject<Notification<T>, Notification<T>> call() {
        return BehaviorSubject.create();
    }
}
