package satellite;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.HashSet;

import info.android15.satellite.BuildConfig;
import rx.Notification;
import rx.Observable;
import rx.Subscription;
import rx.observers.TestObserver;
import satellite.state.StateMap;
import satellite.util.SubjectFactory;

import static java.util.Arrays.asList;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class RestartableConnectionSetTest {

    private static class EarthBaseLauncher implements RestartableConnectionTest.InstanceLauncher {

        final RestartableConnectionSet restartableConnectionSet;
        final int mccId;

        public EarthBaseLauncher(RestartableConnectionSet restartableConnectionSet, int mccId) {
            this.restartableConnectionSet = restartableConnectionSet;
            this.mccId = mccId;
        }

        @Override
        public Observable<Notification<Integer>> connection(RestartableFactory<String, Integer> restartableFactory) {
            return restartableConnectionSet.connection(mccId, SubjectFactory.<Notification<Integer>>behaviorSubject(), restartableFactory);
        }

        @Override
        public void launch(String arg) {
            restartableConnectionSet.launch(mccId, arg);
        }

        @Override
        public void dismiss() {
            restartableConnectionSet.dismiss(mccId);
        }
    }

    @After
    public void tearDown() throws Exception {
        new RestartableConnectionTest().tearDown();
    }

    @Test
    public void testConnectionImmediate() throws Exception {
        final RestartableConnectionSet restartableConnectionSet = new RestartableConnectionSet();
        new RestartableConnectionTest().testConnectionImmediateStrategy(new EarthBaseLauncher(restartableConnectionSet, 1));
        new RestartableConnectionTest().testConnectionImmediateStrategy(new EarthBaseLauncher(restartableConnectionSet, 2));
    }

    @Test
    public void testConnectionDelayed() throws Exception {
        final RestartableConnectionSet restartableConnectionSet = new RestartableConnectionSet();
        new RestartableConnectionTest().testConnectionDelayedStrategy(new EarthBaseLauncher(restartableConnectionSet, 1));
        new RestartableConnectionTest().testConnectionDelayedStrategy(new EarthBaseLauncher(restartableConnectionSet, 2));
    }

    @Test
    public void testDismiss() throws Exception {
        final RestartableConnectionSet restartableConnectionSet = new RestartableConnectionSet();
        new RestartableConnectionTest().testDismissStrategy(new EarthBaseLauncher(restartableConnectionSet, 1));
        new RestartableConnectionTest().testDismissStrategy(new EarthBaseLauncher(restartableConnectionSet, 2));
        new RestartableConnectionTest().testDismissStrategy(new EarthBaseLauncher(restartableConnectionSet, 1));
        new RestartableConnectionTest().testDismissStrategy(new EarthBaseLauncher(restartableConnectionSet, 1));
    }

    @Test
    public void testSaveRestoreRestart() throws Exception {
        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();
        TestObserver<Notification<Integer>> testObserver2 = new TestObserver<>();

        final RestartableConnectionSet base = new RestartableConnectionSet();
        Subscription subscription = base.connection(1, SubjectFactory.<Notification<Integer>>behaviorSubject(), RestartableConnectionTest.INFINITE_RESTARTABLE_FACTORY).subscribe(testObserver);
        Subscription subscription2 = base.connection(2, SubjectFactory.<Notification<Integer>>behaviorSubject(), RestartableConnectionTest.INFINITE_RESTARTABLE_FACTORY).subscribe(testObserver2);
        base.launch(1, "1");
        base.launch(2, "1");

        testObserver.assertReceivedOnNext(asList(Notification.createOnNext(1)));
        testObserver2.assertReceivedOnNext(asList(Notification.createOnNext(1)));

        StateMap state = base.instanceState();

        resetStation();

        TestObserver<Notification<Integer>> testObserver21 = new TestObserver<>();
        TestObserver<Notification<Integer>> testObserver22 = new TestObserver<>();
        final RestartableConnectionSet base2 = new RestartableConnectionSet(state);
        Subscription subscription22 = base2.connection(2, SubjectFactory.<Notification<Integer>>behaviorSubject(), RestartableConnectionTest.INFINITE_RESTARTABLE_FACTORY).subscribe(testObserver22);
        Subscription subscription21 = base2.connection(1, SubjectFactory.<Notification<Integer>>behaviorSubject(), RestartableConnectionTest.INFINITE_RESTARTABLE_FACTORY).subscribe(testObserver21);

        testObserver21.assertReceivedOnNext(asList(Notification.createOnNext(1)));
        testObserver22.assertReceivedOnNext(asList(Notification.createOnNext(1)));
    }

    @Test
    public void testSaveRestoreCompleted() throws Exception {
        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();
        TestObserver<Notification<Integer>> testObserver2 = new TestObserver<>();

        final RestartableConnectionSet base = new RestartableConnectionSet();
        Subscription subscription = base.connection(1, SubjectFactory.<Notification<Integer>>behaviorSubject(), RestartableConnectionTest.RESTARTABLE_FACTORY).subscribe(testObserver);
        Subscription subscription2 = base.connection(2, SubjectFactory.<Notification<Integer>>behaviorSubject(), RestartableConnectionTest.RESTARTABLE_FACTORY).subscribe(testObserver2);
        base.launch(1, "1");
        base.launch(2, "1");

        testObserver.assertReceivedOnNext(asList(Notification.createOnNext(1), Notification.<Integer>createOnCompleted()));
        testObserver2.assertReceivedOnNext(asList(Notification.createOnNext(1), Notification.<Integer>createOnCompleted()));

        StateMap state = base.instanceState();

        resetStation();

        TestObserver<Notification<Integer>> testObserver21 = new TestObserver<>();
        TestObserver<Notification<Integer>> testObserver22 = new TestObserver<>();
        final RestartableConnectionSet base2 = new RestartableConnectionSet(state);
        Subscription subscription22 = base2.connection(2, SubjectFactory.<Notification<Integer>>behaviorSubject(), RestartableConnectionTest.INFINITE_RESTARTABLE_FACTORY).subscribe(testObserver22);
        Subscription subscription21 = base2.connection(1, SubjectFactory.<Notification<Integer>>behaviorSubject(), RestartableConnectionTest.INFINITE_RESTARTABLE_FACTORY).subscribe(testObserver21);

        testObserver21.assertReceivedOnNext(Collections.<Notification<Integer>>emptyList());
        testObserver22.assertReceivedOnNext(Collections.<Notification<Integer>>emptyList());
    }

    private void resetStation() {
        for (String key : new HashSet<>(ReconnectableMap.INSTANCE.keys()))
            ReconnectableMap.INSTANCE.dismiss(key);
    }
}