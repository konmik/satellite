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

import static java.util.Arrays.asList;

public class BaseRestartableTest {

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
        Observable<Notification<Integer>> channel(RestartableFactory<String, Integer> restartableFactory);
        void launch(String arg);
        void dismiss();
    }

    public static class RestartableLauncher implements InstanceLauncher {

        private final Restartable restartable = new Restartable(new StateMap.Builder());

        @Override
        public Observable<Notification<Integer>> channel(RestartableFactory<String, Integer> restartableFactory) {
            return restartable.channel(ChannelType.LATEST, restartableFactory);
        }

        @Override
        public void launch(String arg) {
            restartable.launch(arg);
        }

        @Override
        public void dismiss() {
            restartable.dismiss();
        }
    }

    public void testConnectionImmediateStrategy(BaseRestartableTest.InstanceLauncher launcher) throws Exception {
        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();
        Subscription subscription = launcher.channel(BaseRestartableTest.RESTARTABLE_FACTORY).subscribe(testObserver);
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

    public void testConnectionDelayedStrategy(BaseRestartableTest.InstanceLauncher launcher) throws Exception {
        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();
        Subscription subscription = launcher.channel(scheduledRestartableFactory).subscribe(testObserver);
        launcher.launch("1");
        testObserver.assertReceivedOnNext(Collections.<Notification<Integer>>emptyList());
        testScheduler.advanceTimeBy(6, TimeUnit.SECONDS);
        testObserver.assertReceivedOnNext(asList(Notification.createOnNext(1), Notification.<Integer>createOnCompleted()));
    }

    public void testDismissStrategy(BaseRestartableTest.InstanceLauncher launcher) throws Exception {
        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();

        Subscription subscription = launcher.channel(scheduledRestartableFactory).subscribe(testObserver);

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
