package satellite;

import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

/**
 * Channel delivery methods.
 */
public enum DeliveryMethod {

    /**
     * Only the first emitted value will be delivered.
     *
     * Observable will be immediately unsubscribed.
     */
    SINGLE {
        @Override
        void onNext(Restartable restartable) {
            restartable.dismiss();
        }
    },

    /**
     * Keeps the latest onNext value and emits it each time a new consumer is subscribed to the Restartable's channel.
     * If a new onNext value appears whilw there is a channel subscription, the value will be delivered immediately.
     */
    LATEST,

    /**
     * Keeps all onNext values and emits them each time a new subscriber gets subscribed to the Restartable's channel.
     * If a new onNext value appears while there is a channel subscription, the value will be delivered immediately.
     */
    REPLAY {
        @Override
        <T> Subject<T, T> createSubject() {
            return ReplaySubject.create();
        }
    },

    /**
     * If a new onNext value appears while there is a channel subscription, the value will be delivered immediately.
     */
    PUBLISH {
        @Override
        <T> Subject<T, T> createSubject() {
            return PublishSubject.create();
        }
    };

    <T> Subject<T, T> createSubject() {
        return BehaviorSubject.create();
    }

    void onNext(Restartable restartable) {
    }
}
