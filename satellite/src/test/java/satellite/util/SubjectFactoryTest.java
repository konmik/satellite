package satellite.util;

import org.junit.Test;

import rx.subjects.BehaviorSubject;
import rx.subjects.ReplaySubject;

import static org.junit.Assert.assertTrue;

public class SubjectFactoryTest {

    @Test
    public void testBehaviorSubject() throws Exception {
        assertTrue(SubjectFactory.behaviorSubject().call() instanceof BehaviorSubject);
    }

    @Test
    public void testReplaySubject() throws Exception {
        assertTrue(SubjectFactory.replaySubject().call() instanceof ReplaySubject);
    }
}