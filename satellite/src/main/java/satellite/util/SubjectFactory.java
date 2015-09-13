package satellite.util;

import rx.functions.Func0;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

public abstract class SubjectFactory<T> implements Func0<Subject<T, T>> {

    public static <T> SubjectFactory<T> behaviorSubject() {
        return BEHAVIOR_SUBJECT_FACTORY;
    }

    public static <T> SubjectFactory<T> replaySubject() {
        return REPLAY_SUBJECT_FACTORY;
    }

    public static <T> SubjectFactory<T> publishSubject() {
        return PUBLISH_SUBJECT_FACTORY;
    }

    private static final SubjectFactory BEHAVIOR_SUBJECT_FACTORY = new SubjectFactory() {
        @Override
        public Object call() {
            return BehaviorSubject.create();
        }
    };

    private static final SubjectFactory REPLAY_SUBJECT_FACTORY = new SubjectFactory() {
        @Override
        public Object call() {
            return ReplaySubject.create();
        }
    };

    private static final SubjectFactory PUBLISH_SUBJECT_FACTORY = new SubjectFactory() {
        @Override
        public Object call() {
            return PublishSubject.create();
        }
    };
}
