package satellite;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import info.android15.satellite.BuildConfig;
import rx.Notification;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.observers.TestObserver;
import rx.schedulers.TestScheduler;
import satellite.state.StateMap;
import satellite.util.SubjectFactory;

import static java.util.Arrays.asList;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MissionControlCenterTest {
    @After
    public void tearDown() throws Exception {
        resetStation();
    }

    public static final SatelliteFactory<String, Integer> satelliteFactory = new SatelliteFactory<String, Integer>() {
        @Override
        public Observable<Integer> call(String in) {
            return Observable.just(Integer.valueOf(in));
        }
    };

    public interface InstanceLauncher {
        Observable<Notification<Integer>> connection(SatelliteFactory<String, Integer> satelliteFactory);
        void launch(String statement);
        void dismiss();
    }

    private static class MccLauncher implements InstanceLauncher {

        private final MissionControlCenter<String, Integer> center = new MissionControlCenter<>();

        @Override
        public Observable<Notification<Integer>> connection(SatelliteFactory<String, Integer> satelliteFactory) {
            return center.connection(SubjectFactory.<Notification<Integer>>behaviorSubject(), satelliteFactory);
        }

        @Override
        public void launch(String statement) {
            center.launch(statement);
        }

        @Override
        public void dismiss() {
            center.dismiss();
        }
    }

    @Test
    public void testConnectionImmediate() throws Exception {
        testConnectionImmediateStrategy(new MccLauncher());
    }

    public void testConnectionImmediateStrategy(InstanceLauncher launcher) throws Exception {
        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();
        Subscription subscription = launcher.connection(satelliteFactory).subscribe(testObserver);
        launcher.launch("1");
        testObserver.assertReceivedOnNext(asList(Notification.createOnNext(1), Notification.<Integer>createOnCompleted()));
    }

    final TestScheduler testScheduler = new TestScheduler();
    SatelliteFactory<String, Integer> scheduledSatelliteFactory = new SatelliteFactory<String, Integer>() {
        @Override
        public Observable<Integer> call(String in) {
            return Observable.just(Integer.valueOf(in)).delay(5, TimeUnit.SECONDS, testScheduler);
        }
    };

    @Test
    public void testConnectionDelayed() throws Exception {
        testConnectionDelayedStrategy(new MccLauncher());
    }

    public void testConnectionDelayedStrategy(InstanceLauncher launcher) throws Exception {
        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();
        Subscription subscription = launcher.connection(scheduledSatelliteFactory).subscribe(testObserver);
        launcher.launch("1");
        testObserver.assertReceivedOnNext(Collections.<Notification<Integer>>emptyList());
        testScheduler.advanceTimeBy(6, TimeUnit.SECONDS);
        testObserver.assertReceivedOnNext(asList(Notification.createOnNext(1), Notification.<Integer>createOnCompleted()));
    }

    @Test
    public void testDismiss() throws Exception {
        testDismissStrategy(new MccLauncher());
    }

    public void testDismissStrategy(InstanceLauncher launcher) throws Exception {
        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();

        Subscription subscription = launcher.connection(scheduledSatelliteFactory).subscribe(testObserver);

        launcher.launch("1");
        testObserver.assertReceivedOnNext(Collections.<Notification<Integer>>emptyList());

        launcher.dismiss();

        testScheduler.advanceTimeBy(6, TimeUnit.SECONDS);
        testObserver.assertReceivedOnNext(Collections.<Notification<Integer>>emptyList());
    }

    public static final SatelliteFactory<String, Integer> infiniteSatelliteFactory = new SatelliteFactory<String, Integer>() {
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

    @Test
    public void testSaveRestoreRestart() throws Exception {
        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();

        MissionControlCenter<String, Integer> center = new MissionControlCenter<>();
        Subscription subscription = center.connection(SubjectFactory.<Notification<Integer>>behaviorSubject(), infiniteSatelliteFactory).subscribe(testObserver);
        center.launch("1");

        testObserver.assertReceivedOnNext(asList(Notification.createOnNext(1)));

        StateMap state = center.instanceState();

        resetStation();

        TestObserver<Notification<Integer>> testObserver2 = new TestObserver<>();
        MissionControlCenter<String, Integer> center2 = new MissionControlCenter<>(state);
        Subscription subscription2 = center2.connection(SubjectFactory.<Notification<Integer>>behaviorSubject(), infiniteSatelliteFactory).subscribe(testObserver2);

        testObserver2.assertReceivedOnNext(asList(Notification.createOnNext(1)));
    }

    @Test
    public void testSaveRestoreCompleted() throws Exception {
        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();

        MissionControlCenter<String, Integer> center = new MissionControlCenter<>();
        Subscription subscription = center.connection(SubjectFactory.<Notification<Integer>>behaviorSubject(), satelliteFactory).subscribe(testObserver);
        center.launch("1");

        testObserver.assertReceivedOnNext(asList(Notification.createOnNext(1), Notification.<Integer>createOnCompleted()));

        StateMap state = center.instanceState();

        resetStation();

        TestObserver<Notification<Integer>> testObserver2 = new TestObserver<>();
        MissionControlCenter<String, Integer> center2 = new MissionControlCenter<>(state);
        Subscription subscription2 = center2.connection(SubjectFactory.<Notification<Integer>>behaviorSubject(), satelliteFactory).subscribe(testObserver2);

        testObserver2.assertReceivedOnNext(Collections.<Notification<Integer>>emptyList());
    }

    private void resetStation() {
        for (String key : new HashSet<>(SpaceStation.INSTANCE.keys()))
            SpaceStation.INSTANCE.recycle(key);
    }
}