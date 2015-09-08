package satellite.io;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import info.android15.satellite.BuildConfig;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MarshallMapFnTest {
    @Test
    public void testEqualsMap() throws Exception {
        HashMap<String, byte[]> map1 = new HashMap<String, byte[]>() {{
            put("1", new byte[]{1});
            put("2", new byte[]{2});
        }};
        HashMap<String, byte[]> map2 = new HashMap<String, byte[]>() {{
            put("1", new byte[]{1});
            put("2", new byte[]{2});
        }};
        HashMap<String, byte[]> map3 = new HashMap<String, byte[]>() {{
            put("1", new byte[]{1});
            put("3", new byte[]{3});
        }};
        HashMap<String, byte[]> map4 = new HashMap<String, byte[]>() {{
            put("1", new byte[]{1});
        }};
        assertTrue(MarshallMapFn.equalsMap(map1, map2));
        assertFalse(MarshallMapFn.equalsMap(map1, map3));
        assertFalse(MarshallMapFn.equalsMap(map1, map4));
    }
}