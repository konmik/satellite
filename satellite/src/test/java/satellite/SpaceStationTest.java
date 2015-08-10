package satellite;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rx.functions.Func0;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class SpaceStationTest {
    @Test
    public void testConnection() throws Exception {
        final PublishSubject subject1 = PublishSubject.create();
        assertEquals(subject1, SpaceStation.INSTANCE.connection("1", new Func0<Subject>() {
            @Override
            public Subject call() {
                return subject1;
            }
        }));
        assertEquals(subject1, SpaceStation.INSTANCE.connection("1", null));
        assertEquals(Collections.singletonList("1"), SpaceStation.INSTANCE.getEarthConnectionKeys());

        final PublishSubject subject2 = PublishSubject.create();
        assertEquals(subject2, SpaceStation.INSTANCE.connection("2", new Func0<Subject>() {
            @Override
            public Subject call() {
                return subject2;
            }
        }));
        List<String> keys = SpaceStation.INSTANCE.getEarthConnectionKeys();
        Collections.sort(keys);
        assertEquals(Arrays.asList("1", "2"), keys);

        SpaceStation.INSTANCE.clear(asList("1", "2"));

        assertEquals(new ArrayList<>(), SpaceStation.INSTANCE.getEarthConnectionKeys());
    }
}
