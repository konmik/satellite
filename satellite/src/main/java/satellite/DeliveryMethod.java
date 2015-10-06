package satellite;

import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

public enum DeliveryMethod {

    SINGLE {
        @Override
        void onNext(String key) {
            ReconnectableMap.INSTANCE.dismiss(key);
        }
    },

    LATEST,

    REPLAY {
        @Override
        <T> Subject<T, T> createSubject() {
            return ReplaySubject.create();
        }
    },

    PUBLISH {
        @Override
        <T> Subject<T, T> createSubject() {
            return PublishSubject.create();
        }
    };

    <T> Subject<T, T> createSubject() {
        return BehaviorSubject.create();
    }

    void onNext(String key) {
    }
}
