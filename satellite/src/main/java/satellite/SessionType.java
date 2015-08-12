package satellite;

import rx.Notification;
import rx.subjects.BehaviorSubject;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

public enum SessionType {

    SINGLE {
        @Override
        public <T> Subject<Notification<T>, Notification<T>> createSubject() {
            return BehaviorSubject.create();
        }
    },

    CACHE {
        @Override
        public <T> Subject<Notification<T>, Notification<T>> createSubject() {
            return BehaviorSubject.create();
        }
    },

    REPLAY {
        @Override
        public <T> Subject<Notification<T>, Notification<T>> createSubject() {
            return ReplaySubject.create();
        }
    };

    public abstract <T> Subject<Notification<T>, Notification<T>> createSubject();
}
