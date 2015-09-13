package satellite.util;

import android.support.annotation.Nullable;

import rx.Notification;
import rx.exceptions.OnErrorNotImplementedException;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * This collection of functions makes it easy to split {@link rx.Notification} into
 * three (onNext, onError, onComplete) lambdas.
 */
public class RxNotification {

    /**
     * Returns an {@link Action1} that can be used to split {@link Notification} to appropriate
     * dematerialized onNext calls.
     *
     * @param onNext a method that will be called in case of onNext notification, or null.
     * @param <T>    a type of onNext value.
     * @return an {@link Action1} that can be used to split {@link Notification} to appropriate
     * onNext calls.
     */
    public static <T> Action1<Notification<T>> split(
        @Nullable Action1<T> onNext) {

        return split(onNext, null, null);
    }

    /**
     * Returns an {@link Action1} that can be used to split {@link Notification} to appropriate
     * dematerialized onNext and onError calls.
     *
     * @param onNext  a method that will be called in case of onNext notification, or null.
     * @param onError a method that will be called in case of onError notification, or null.
     * @param <T>     a type of onNext value.
     * @return an {@link Action1} that can be used to split {@link Notification} to appropriate
     * onNext, onError calls.
     */
    public static <T> Action1<Notification<T>> split(
        @Nullable Action1<T> onNext,
        @Nullable Action1<Throwable> onError) {

        return split(onNext, onError, null);
    }

    /**
     * Returns an {@link Action1} that can be used to split {@link Notification} to appropriate
     * dematerialized onNext, onError and onComplete calls.
     *
     * @param onNext      a method that will be called in case of onNext notification, or null.
     * @param onError     a method that will be called in case of onError notification, or null.
     * @param onCompleted a method that will be called in case of onCompleted notification, or null.
     * @param <T>         a type of onNext value.
     * @return an {@link Action1} that can be used to split {@link Notification} to appropriate
     * onNext, onError, onComplete calls.
     */
    public static <T> Action1<Notification<T>> split(
        @Nullable final Action1<T> onNext,
        @Nullable final Action1<Throwable> onError,
        @Nullable final Action0 onCompleted) {

        return new Action1<Notification<T>>() {
            @Override
            public void call(Notification<T> notification) {
                if (notification.isOnNext()) {
                    if (onNext != null)
                        onNext.call(notification.getValue());
                }
                else if (notification.isOnError()) {
                    if (onError != null)
                        onError.call(notification.getThrowable());
                    else
                        throw new OnErrorNotImplementedException(notification.getThrowable());
                }
                else if (notification.isOnCompleted()) {
                    if (onCompleted != null)
                        onCompleted.call();
                }
            }
        };
    }
}
