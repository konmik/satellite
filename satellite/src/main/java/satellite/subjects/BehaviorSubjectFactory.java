package satellite.subjects;

import rx.subjects.BehaviorSubject;

public class BehaviorSubjectFactory {

    private static final SubjectFactory INSTANCE = new SubjectFactory() {
        @Override
        public Object call() {
            return BehaviorSubject.create();
        }
    };

    public static <T> SubjectFactory<T> instance() {
        return INSTANCE;
    }
}
