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
public class RestartableConnectionGroupTest {

    private static class EarthBaseLauncher implements RestartableConnectionTest.InstanceLauncher {

        final RestartableConnectionGroup restartableConnectionGroup;
        final int mccId;

        public EarthBaseLauncher(RestartableConnectionGroup restartableConnectionGroup, int mccId) {
            this.restartableConnectionGroup = restartableConnectionGroup;
            this.mccId = mccId;
        }

        @Override
        public Observable<Notification<Integer>> connection(RestartableFactory<String, Integer> restartableFactory) {
            return restartableConnectionGroup.connection(mccId, SubjectFactory.<Notification<Integer>>behaviorSubject(), restartableFactory);
        }

        @Override
        public void launch(String statement) {
            restartableConnectionGroup.launch(mccId, statement);
        }

        @Override
        public void dismiss() {
            restartableConnectionGroup.dismiss(mccId);
        }
    }

    @After
    public void tearDown() throws Exception {
        new RestartableConnectionTest().tearDown();
    }

    @Test
    public void testConnectionImmediate() throws Exception {
        final RestartableConnectionGroup restartableConnectionGroup = new RestartableConnectionGroup();
        new RestartableConnectionTest().testConnectionImmediateStrategy(new EarthBaseLauncher(restartableConnectionGroup, 1));
        new RestartableConnectionTest().testConnectionImmediateStrategy(new EarthBaseLauncher(restartableConnectionGroup, 2));
    }

    @Test
    public void testConnectionDelayed() throws Exception {
        final RestartableConnectionGroup restartableConnectionGroup = new RestartableConnectionGroup();
        new RestartableConnectionTest().testConnectionDelayedStrategy(new EarthBaseLauncher(restartableConnectionGroup, 1));
        new RestartableConnectionTest().testConnectionDelayedStrategy(new EarthBaseLauncher(restartableConnectionGroup, 2));
    }

    @Test
    public void testDismiss() throws Exception {
        final RestartableConnectionGroup restartableConnectionGroup = new RestartableConnectionGroup();
        new RestartableConnectionTest().testDismissStrategy(new EarthBaseLauncher(restartableConnectionGroup, 1));
        new RestartableConnectionTest().testDismissStrategy(new EarthBaseLauncher(restartableConnectionGroup, 2));
        new RestartableConnectionTest().testDismissStrategy(new EarthBaseLauncher(restartableConnectionGroup, 1));
    }

    @Test
    public void testSaveRestoreRestart() throws Exception {
        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();
        TestObserver<Notification<Integer>> testObserver2 = new TestObserver<>();

        final RestartableConnectionGroup base = new RestartableConnectionGroup();
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
        final RestartableConnectionGroup base2 = new RestartableConnectionGroup(state);
        Subscription subscription22 = base2.connection(2, SubjectFactory.<Notification<Integer>>behaviorSubject(), RestartableConnectionTest.INFINITE_RESTARTABLE_FACTORY).subscribe(testObserver22);
        Subscription subscription21 = base2.connection(1, SubjectFactory.<Notification<Integer>>behaviorSubject(), RestartableConnectionTest.INFINITE_RESTARTABLE_FACTORY).subscribe(testObserver21);

        testObserver21.assertReceivedOnNext(asList(Notification.createOnNext(1)));
        testObserver22.assertReceivedOnNext(asList(Notification.createOnNext(1)));
    }

    @Test
    public void testSaveRestoreCompleted() throws Exception {
        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();
        TestObserver<Notification<Integer>> testObserver2 = new TestObserver<>();

        final RestartableConnectionGroup base = new RestartableConnectionGroup();
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
        final RestartableConnectionGroup base2 = new RestartableConnectionGroup(state);
        Subscription subscription22 = base2.connection(2, SubjectFactory.<Notification<Integer>>behaviorSubject(), RestartableConnectionTest.INFINITE_RESTARTABLE_FACTORY).subscribe(testObserver22);
        Subscription subscription21 = base2.connection(1, SubjectFactory.<Notification<Integer>>behaviorSubject(), RestartableConnectionTest.INFINITE_RESTARTABLE_FACTORY).subscribe(testObserver21);

        testObserver21.assertReceivedOnNext(Collections.<Notification<Integer>>emptyList());
        testObserver22.assertReceivedOnNext(Collections.<Notification<Integer>>emptyList());
    }

    private void resetStation() {
        for (String key : new HashSet<>(ReconnectableMap.INSTANCE.keys()))
            ReconnectableMap.INSTANCE.recycle(key);
    }
}