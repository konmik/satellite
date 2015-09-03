package satellite.subjects;

import rx.subjects.ReplaySubject;

public class ReplaySubjectFactory {

    private static final SubjectFactory INSTANCE = new SubjectFactory() {
        @Override
        public Object call() {
            return ReplaySubject.create();
        }
    };

    public static <T> SubjectFactory<T> instance() {
        return INSTANCE;
    }
}
