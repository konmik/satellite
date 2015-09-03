package satellite.connections;

import rx.Notification;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;
import satellite.SubjectFactory;

public class ReplaySubjectFactory<T> implements SubjectFactory<T> {

    private static final ReplaySubjectFactory INSTANCE = new ReplaySubjectFactory();

    public static <T> ReplaySubjectFactory<T> instance() {
        return INSTANCE;
    }

    @Override
    public Subject<Notification<T>, Notification<T>> call() {
        return ReplaySubject.create();
    }
}
