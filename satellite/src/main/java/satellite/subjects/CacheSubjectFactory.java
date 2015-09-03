package satellite.subjects;

import rx.Notification;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

public class CacheSubjectFactory<T> implements SubjectFactory<T> {

    private static final CacheSubjectFactory INSTANCE = new CacheSubjectFactory();

    public static <T> CacheSubjectFactory<T> instance() {
        return INSTANCE;
    }

    @Override
    public Subject<Notification<T>, Notification<T>> call() {
        return BehaviorSubject.create();
    }
}
