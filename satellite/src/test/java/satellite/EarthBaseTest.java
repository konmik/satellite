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
import satellite.io.InputMap;
import satellite.util.SubjectFactory;

import static java.util.Arrays.asList;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class EarthBaseTest {

    private static class EarthBaseLauncher implements MissionControlCenterTest.InstanceLauncher {

        final EarthBase earthBase;
        final int mccId;

        public EarthBaseLauncher(EarthBase earthBase, int mccId) {
            this.earthBase = earthBase;
            this.mccId = mccId;
        }

        @Override
        public Observable<Notification<Integer>> connection(SatelliteFactory<String, Integer> satelliteFactory) {
            return earthBase.connection(mccId, SubjectFactory.<Notification<Integer>>behaviorSubject(), satelliteFactory);
        }

        @Override
        public void launch(String statement) {
            earthBase.launch(mccId, statement);
        }

        @Override
        public void dismiss() {
            earthBase.dismiss(mccId);
        }
    }

    @After
    public void tearDown() throws Exception {
        new MissionControlCenterTest().tearDown();
    }

    @Test
    public void testConnectionImmediate() throws Exception {
        final EarthBase earthBase = new EarthBase();
        new MissionControlCenterTest().testConnectionImmediateStrategy(new EarthBaseLauncher(earthBase, 1));
        new MissionControlCenterTest().testConnectionImmediateStrategy(new EarthBaseLauncher(earthBase, 2));
    }

    @Test
    public void testConnectionDelayed() throws Exception {
        final EarthBase earthBase = new EarthBase();
        new MissionControlCenterTest().testConnectionDelayedStrategy(new EarthBaseLauncher(earthBase, 1));
        new MissionControlCenterTest().testConnectionDelayedStrategy(new EarthBaseLauncher(earthBase, 2));
    }

    @Test
    public void testDismiss() throws Exception {
        final EarthBase earthBase = new EarthBase();
        new MissionControlCenterTest().testDismissStrategy(new EarthBaseLauncher(earthBase, 1));
        new MissionControlCenterTest().testDismissStrategy(new EarthBaseLauncher(earthBase, 2));
        new MissionControlCenterTest().testDismissStrategy(new EarthBaseLauncher(earthBase, 1));
    }

    @Test
    public void testSaveRestoreRestart() throws Exception {
        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();
        TestObserver<Notification<Integer>> testObserver2 = new TestObserver<>();

        final EarthBase base = new EarthBase();
        Subscription subscription = base.connection(1, SubjectFactory.<Notification<Integer>>behaviorSubject(), MissionControlCenterTest.infiniteSatelliteFactory).subscribe(testObserver);
        Subscription subscription2 = base.connection(2, SubjectFactory.<Notification<Integer>>behaviorSubject(), MissionControlCenterTest.infiniteSatelliteFactory).subscribe(testObserver2);
        base.launch(1, "1");
        base.launch(2, "1");

        testObserver.assertReceivedOnNext(asList(Notification.createOnNext(1)));
        testObserver2.assertReceivedOnNext(asList(Notification.createOnNext(1)));

        InputMap state = base.instanceState();

        resetStation();

        TestObserver<Notification<Integer>> testObserver21 = new TestObserver<>();
        TestObserver<Notification<Integer>> testObserver22 = new TestObserver<>();
        final EarthBase base2 = new EarthBase(state);
        Subscription subscription22 = base2.connection(2, SubjectFactory.<Notification<Integer>>behaviorSubject(), MissionControlCenterTest.infiniteSatelliteFactory).subscribe(testObserver22);
        Subscription subscription21 = base2.connection(1, SubjectFactory.<Notification<Integer>>behaviorSubject(), MissionControlCenterTest.infiniteSatelliteFactory).subscribe(testObserver21);

        testObserver21.assertReceivedOnNext(asList(Notification.createOnNext(1)));
        testObserver22.assertReceivedOnNext(asList(Notification.createOnNext(1)));
    }

    @Test
    public void testSaveRestoreCompleted() throws Exception {
        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();
        TestObserver<Notification<Integer>> testObserver2 = new TestObserver<>();

        final EarthBase base = new EarthBase();
        Subscription subscription = base.connection(1, SubjectFactory.<Notification<Integer>>behaviorSubject(), MissionControlCenterTest.satelliteFactory).subscribe(testObserver);
        Subscription subscription2 = base.connection(2, SubjectFactory.<Notification<Integer>>behaviorSubject(), MissionControlCenterTest.satelliteFactory).subscribe(testObserver2);
        base.launch(1, "1");
        base.launch(2, "1");

        testObserver.assertReceivedOnNext(asList(Notification.createOnNext(1), Notification.<Integer>createOnCompleted()));
        testObserver2.assertReceivedOnNext(asList(Notification.createOnNext(1), Notification.<Integer>createOnCompleted()));

        InputMap state = base.instanceState();

        resetStation();

        TestObserver<Notification<Integer>> testObserver21 = new TestObserver<>();
        TestObserver<Notification<Integer>> testObserver22 = new TestObserver<>();
        final EarthBase base2 = new EarthBase(state);
        Subscription subscription22 = base2.connection(2, SubjectFactory.<Notification<Integer>>behaviorSubject(), MissionControlCenterTest.infiniteSatelliteFactory).subscribe(testObserver22);
        Subscription subscription21 = base2.connection(1, SubjectFactory.<Notification<Integer>>behaviorSubject(), MissionControlCenterTest.infiniteSatelliteFactory).subscribe(testObserver21);

        testObserver21.assertReceivedOnNext(Collections.<Notification<Integer>>emptyList());
        testObserver22.assertReceivedOnNext(Collections.<Notification<Integer>>emptyList());
    }

    private void resetStation() {
        for (String key : new HashSet<>(SpaceStation.INSTANCE.keys()))
            SpaceStation.INSTANCE.recycle(key);
    }
}