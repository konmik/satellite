package satellite.util;

import org.junit.Test;
import org.mockito.Mockito;

import rx.Notification;
import rx.exceptions.OnErrorNotImplementedException;
import rx.functions.Action0;
import rx.functions.Action1;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class RxNotificationTest {

    @Test
    public void testSplit1() throws Exception {
        Action1<Integer> onNext = Mockito.mock(Action1.class);
        RxNotification.split(onNext).call(Notification.createOnNext(1));
        RxNotification.split(onNext).call(Notification.<Integer>createOnCompleted());
        verify(onNext, times(1)).call(1);
        verifyNoMoreInteractions(onNext);
    }

    @Test(expected = OnErrorNotImplementedException.class)
    public void testSplit1OnError() throws Exception {
        Action1<Integer> onNext = Mockito.mock(Action1.class);
        RxNotification.split(onNext).call(Notification.<Integer>createOnError(new RuntimeException()));
    }

    @Test
    public void testSplit2() throws Exception {
        Action1<Integer> onNext = Mockito.mock(Action1.class);
        Action1<Throwable> onError = Mockito.mock(Action1.class);

        Action1<Notification<Integer>> split = RxNotification.split(onNext, onError);

        split.call(Notification.createOnNext(1));
        RuntimeException exception = new RuntimeException();
        split.call(Notification.<Integer>createOnError(exception));
        split.call(Notification.<Integer>createOnCompleted());
        verify(onNext, times(1)).call(1);
        verify(onError, times(1)).call(exception);
        verifyNoMoreInteractions(onNext, onError);
    }

    @Test
    public void testSplit3() throws Exception {
        Action1<Integer> onNext = Mockito.mock(Action1.class);
        Action1<Throwable> onError = Mockito.mock(Action1.class);

        Action0 onCompleted = Mockito.mock(Action0.class);
        Action1<Notification<Integer>> split = RxNotification.split(onNext, onError, onCompleted);

        split.call(Notification.createOnNext(1));
        RuntimeException exception = new RuntimeException();
        split.call(Notification.<Integer>createOnError(exception));
        split.call(Notification.<Integer>createOnCompleted());
        verify(onNext, times(1)).call(1);
        verify(onError, times(1)).call(exception);
        verify(onCompleted, times(1)).call();
        verifyNoMoreInteractions(onNext, onError, onCompleted);
    }
}