package satellite;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;

import info.android15.satellite.BuildConfig;
import rx.Notification;
import rx.Observable;
import rx.Subscription;
import rx.observers.TestObserver;
import statemap.StateMap;

import static java.util.Arrays.asList;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class RestartableSetTest extends BaseRestartableTest {

    private static class RestartableConnectionSetLauncher implements BaseRestartableTest.InstanceLauncher {

        final RestartableSet restartableSet;
        final int restartableId;

        public RestartableConnectionSetLauncher(RestartableSet restartableSet, int restartableId) {
            this.restartableSet = restartableSet;
            this.restartableId = restartableId;
        }

        @Override
        public Observable<Notification<Integer>> channel(RestartableFactory<String, Integer> restartableFactory) {
            return restartableSet.restartable(restartableId, DeliveryMethod.LATEST, restartableFactory);
        }

        @Override
        public void launch(String arg) {
            restartableSet.launch(restartableId, arg);
        }

        @Override
        public void dismiss() {
            restartableSet.dismiss(restartableId);
        }
    }

    @After
    public void tearDown() throws Exception {
        new RestartableTest().tearDown();
    }

    @Test
    public void testConnectionImmediate() throws Exception {
        final RestartableSet restartableSet = new RestartableSet(new StateMap.Builder());
        testConnectionImmediateStrategy(new RestartableConnectionSetLauncher(restartableSet, 1));
        testConnectionImmediateStrategy(new RestartableConnectionSetLauncher(restartableSet, 2));
    }

    @Test
    public void testConnectionDelayed() throws Exception {
        final RestartableSet restartableSet = new RestartableSet(new StateMap.Builder());
        testConnectionDelayedStrategy(new RestartableConnectionSetLauncher(restartableSet, 1));
        testConnectionDelayedStrategy(new RestartableConnectionSetLauncher(restartableSet, 2));
    }

    @Test
    public void testDismiss() throws Exception {
        final RestartableSet restartableSet = new RestartableSet(new StateMap.Builder());
        testDismissStrategy(new RestartableConnectionSetLauncher(restartableSet, 1));
        testDismissStrategy(new RestartableConnectionSetLauncher(restartableSet, 2));
        testDismissStrategy(new RestartableConnectionSetLauncher(restartableSet, 1));
        testDismissStrategy(new RestartableConnectionSetLauncher(restartableSet, 1));
    }

    @Test
    public void testSaveRestoreRestart() throws Exception {
        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();
        TestObserver<Notification<Integer>> testObserver2 = new TestObserver<>();

        StateMap.Builder out = new StateMap.Builder();
        final RestartableSet base = new RestartableSet(out);
        Subscription subscription = base.restartable(1, DeliveryMethod.LATEST, INFINITE_RESTARTABLE_FACTORY).subscribe(testObserver);
        Subscription subscription2 = base.restartable(2, DeliveryMethod.LATEST, INFINITE_RESTARTABLE_FACTORY).subscribe(testObserver2);
        base.launch(1, "1");
        base.launch(2, "1");

        testObserver.assertReceivedOnNext(asList(Notification.createOnNext(1)));
        testObserver2.assertReceivedOnNext(asList(Notification.createOnNext(1)));

        resetStation();

        TestObserver<Notification<Integer>> testObserver21 = new TestObserver<>();
        TestObserver<Notification<Integer>> testObserver22 = new TestObserver<>();
        final RestartableSet base2 = new RestartableSet(out.build(), out);
        Subscription subscription22 = base2.restartable(2, DeliveryMethod.LATEST, INFINITE_RESTARTABLE_FACTORY).subscribe(testObserver22);
        Subscription subscription21 = base2.restartable(1, DeliveryMethod.LATEST, INFINITE_RESTARTABLE_FACTORY).subscribe(testObserver21);

        testObserver21.assertReceivedOnNext(asList(Notification.createOnNext(1)));
        testObserver22.assertReceivedOnNext(asList(Notification.createOnNext(1)));
    }

    @Test
    public void testSaveRestoreCompleted() throws Exception {
        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();
        TestObserver<Notification<Integer>> testObserver2 = new TestObserver<>();

        StateMap.Builder out = new StateMap.Builder();
        final RestartableSet base = new RestartableSet(out);
        Subscription subscription = base.restartable(1, DeliveryMethod.LATEST, RESTARTABLE_FACTORY).subscribe(testObserver);
        Subscription subscription2 = base.restartable(2, DeliveryMethod.LATEST, RESTARTABLE_FACTORY).subscribe(testObserver2);
        base.launch(1, "1");
        base.launch(2, "1");

        testObserver.assertReceivedOnNext(asList(Notification.createOnNext(1), Notification.<Integer>createOnCompleted()));
        testObserver2.assertReceivedOnNext(asList(Notification.createOnNext(1), Notification.<Integer>createOnCompleted()));

        resetStation();

        TestObserver<Notification<Integer>> testObserver21 = new TestObserver<>();
        TestObserver<Notification<Integer>> testObserver22 = new TestObserver<>();
        final RestartableSet base2 = new RestartableSet(out.build(), out);
        Subscription subscription22 = base2.restartable(2, DeliveryMethod.LATEST, INFINITE_RESTARTABLE_FACTORY).subscribe(testObserver22);
        Subscription subscription21 = base2.restartable(1, DeliveryMethod.LATEST, INFINITE_RESTARTABLE_FACTORY).subscribe(testObserver21);

        testObserver21.assertReceivedOnNext(Collections.<Notification<Integer>>emptyList());
        testObserver22.assertReceivedOnNext(Collections.<Notification<Integer>>emptyList());
    }
}