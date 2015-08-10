package satellite.util;

import rx.Notification;
import rx.exceptions.OnErrorNotImplementedException;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * This collection of functions makes it easy to split {@link rx.Notification} into
 * three (onNext, onError, onComplete) lambdas.
 */
public class RxNotification {

    public static <T> Action1<Notification<T>> split(Action1<T> onNext) {
        return split(onNext, null, null);
    }

    public static <T> Action1<Notification<T>> split(Action1<T> onNext, Action1<Throwable> onError) {
        return split(onNext, onError, null);
    }

    public static <T> Action1<Notification<T>> split(final Action1<T> onNext, final Action1<Throwable> onError, final Action0 onCompleted) {
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
