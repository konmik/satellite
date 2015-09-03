package satellite.subjects;

import rx.Notification;
import rx.functions.Func0;
import rx.subjects.Subject;

public interface SubjectFactory<T> extends Func0<Subject<Notification<T>, Notification<T>>> {
}
