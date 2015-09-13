package satellite.io;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import info.android15.satellite.BuildConfig;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class BuilderTest {

    @Test
    public void testPut() throws Exception {
        StateMap.Builder out = new StateMap.Builder();
        out.put("1", 1);
        assertEquals(1, out.build().get("1"));
    }

    @Test
    public void testChangesOnDataDoNotChangeOutputMap() throws Exception {
        StateMap.Builder out = new StateMap.Builder();
        int[] ints = {1, 2, 3};
        out.put("2", ints);
        assertArrayEquals(new int[]{1, 2, 3}, (int[])out.build().get("2"));
        ints[1] = 1;
        assertArrayEquals(new int[]{1, 2, 3}, (int[])out.build().get("2"));
    }

    @Test
    public void testRemove() throws Exception {
        StateMap.Builder out = new StateMap.Builder();
        out.put("1", 1);
        out.remove("1");
        assertFalse(out.build().containsKey("1"));
    }

    @Test
    public void testToInput() throws Exception {
        StateMap.Builder out = new StateMap.Builder();
        out.put("1", 1);
        assertEquals(1, out.build().get("1"));
    }
}
