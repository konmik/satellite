package satellite;

import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import rx.Notification;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.observers.TestObserver;
import rx.schedulers.TestScheduler;
import satellite.state.StateMap;
import satellite.util.SubjectFactory;

import static java.util.Arrays.asList;

public class BaseRestartableConnectionTest {

    public static final RestartableFactory<String, Integer> RESTARTABLE_FACTORY = new RestartableFactory<String, Integer>() {
        @Override
        public Observable<Integer> call(String in) {
            return Observable.just(Integer.valueOf(in));
        }
    };

    public static final RestartableFactory<String, Integer> INFINITE_RESTARTABLE_FACTORY = new RestartableFactory<String, Integer>() {
        @Override
        public Observable<Integer> call(final String in) {
            return Observable.create(new Observable.OnSubscribe<Integer>() {
                @Override
                public void call(Subscriber<? super Integer> subscriber) {
                    subscriber.onNext(Integer.valueOf(in));
                }
            });
        }
    };

    public interface InstanceLauncher {
        Observable<Notification<Integer>> connection(RestartableFactory<String, Integer> restartableFactory);
        void launch(String arg);
        void dismiss();
    }

    public static class RestartableConnectionLauncher implements InstanceLauncher {

        private final RestartableConnection connection = new RestartableConnection(new StateMap.Builder());

        @Override
        public Observable<Notification<Integer>> connection(RestartableFactory<String, Integer> restartableFactory) {
            return connection.connection(SubjectFactory.<Notification<Integer>>behaviorSubject(), restartableFactory);
        }

        @Override
        public void launch(String arg) {
            connection.launch(arg);
        }

        @Override
        public void dismiss() {
            connection.dismiss();
        }
    }

    public void testConnectionImmediateStrategy(BaseRestartableConnectionTest.InstanceLauncher launcher) throws Exception {
        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();
        Subscription subscription = launcher.connection(BaseRestartableConnectionTest.RESTARTABLE_FACTORY).subscribe(testObserver);
        launcher.launch("1");
        testObserver.assertReceivedOnNext(asList(Notification.createOnNext(1), Notification.<Integer>createOnCompleted()));
    }

    final TestScheduler testScheduler = new TestScheduler();
    RestartableFactory<String, Integer> scheduledRestartableFactory = new RestartableFactory<String, Integer>() {
        @Override
        public Observable<Integer> call(String in) {
            return Observable.just(Integer.valueOf(in)).delay(5, TimeUnit.SECONDS, testScheduler);
        }
    };

    public void testConnectionDelayedStrategy(BaseRestartableConnectionTest.InstanceLauncher launcher) throws Exception {
        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();
        Subscription subscription = launcher.connection(scheduledRestartableFactory).subscribe(testObserver);
        launcher.launch("1");
        testObserver.assertReceivedOnNext(Collections.<Notification<Integer>>emptyList());
        testScheduler.advanceTimeBy(6, TimeUnit.SECONDS);
        testObserver.assertReceivedOnNext(asList(Notification.createOnNext(1), Notification.<Integer>createOnCompleted()));
    }

    public void testDismissStrategy(BaseRestartableConnectionTest.InstanceLauncher launcher) throws Exception {
        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();

        Subscription subscription = launcher.connection(scheduledRestartableFactory).subscribe(testObserver);

        launcher.launch("1");
        testObserver.assertReceivedOnNext(Collections.<Notification<Integer>>emptyList());

        launcher.dismiss();

        testScheduler.advanceTimeBy(6, TimeUnit.SECONDS);
        testObserver.assertReceivedOnNext(Collections.<Notification<Integer>>emptyList());
    }

    public void resetStation() {
        for (String key : new HashSet<>(ReconnectableMap.INSTANCE.keys()))
            ReconnectableMap.INSTANCE.dismiss(key);
    }
}
