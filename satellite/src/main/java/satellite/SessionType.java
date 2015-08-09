package satellite;

import rx.Notification;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

public enum SessionType {

    FIRST {
        @Override
        public <T> Subject<Notification<T>, Notification<T>> createSubject() {
            return PublishSubject.create();
        }

        @Override
        public <T> Observable.Transformer<Notification<T>, Notification<T>> transformer() {
            return new Observable.Transformer<Notification<T>, Notification<T>>() {
                @Override
                public Observable<Notification<T>> call(Observable<Notification<T>> observable) {
                    return observable.first();
                }
            };
        }
    },

    CACHE {
        @Override
        public <T> Subject<Notification<T>, Notification<T>> createSubject() {
            return BehaviorSubject.create();
        }

        @Override
        public <T> Observable.Transformer<Notification<T>, Notification<T>> transformer() {
            return new Observable.Transformer<Notification<T>, Notification<T>>() {
                @Override
                public Observable<Notification<T>> call(Observable<Notification<T>> observable) {
                    return observable;
                }
            };
        }
    },

    REPLAY {
        @Override
        public <T> Subject<Notification<T>, Notification<T>> createSubject() {
            return ReplaySubject.create();
        }

        @Override
        public <T> Observable.Transformer<Notification<T>, Notification<T>> transformer() {
            return new Observable.Transformer<Notification<T>, Notification<T>>() {
                @Override
                public Observable<Notification<T>> call(Observable<Notification<T>> observable) {
                    return observable;
                }
            };
        }
    };

    public abstract <T> Subject<Notification<T>, Notification<T>> createSubject();
    public abstract <T> Observable.Transformer<Notification<T>, Notification<T>> transformer();
}
