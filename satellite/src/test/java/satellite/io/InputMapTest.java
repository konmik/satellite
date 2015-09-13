package satellite.io;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashSet;

import info.android15.satellite.BuildConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class InputMapTest {

    @Test
    public void testEmpty() throws Exception {
        assertEquals(0, InputMap.empty().keys().size());
    }

    @Test
    public void testSequence() throws Exception {
        HashSet hashSet = getSetString123();
        InputMap map = getSequence123();
        assertTrue(hashSet.equals(map.keys()));
        assertEquals(1, map.get("1"));
        assertEquals(2, map.get("2"));
        assertEquals(3, map.get("3"));
    }

    @Test(expected = Exception.class)
    public void testSequenceWrongSequence() {
        InputMap.sequence("1", 1, "2");
    }

    @Test(expected = Exception.class)
    public void testSequenceWrongType() {
        InputMap.sequence(1, 1, "2");
    }

    @Test
    public void testKeys() throws Exception {
        assertEquals(getSetString123(), getSequence123().keys());
    }

    @Test
    public void testContains() throws Exception {
        assertTrue(getSequence123().containsKey("1"));
        assertFalse(getSequence123().containsKey("-1"));
    }

    @Test
    public void testGet() throws Exception {
        assertEquals(1, getSequence123().get("1"));
        assertEquals(null, getSequence123().get("-1"));
    }

    @Test
    public void testGetOr() throws Exception {
        assertEquals(1, (int)getSequence123().get("1", 2));
        assertEquals(2, (int)getSequence123().get("-1", 2));
    }

    @Test
    public void testToOutput() throws Exception {
        OutputMap out = new OutputMap();
        out.put("1", 1);
        out.put("2", 2);
        out.put("3", 3);
        assertTrue(out.toInput().equals(getSequence123()));
    }

    private HashSet getSetString123() {
        HashSet hashSet = new HashSet();
        hashSet.add("1");
        hashSet.add("2");
        hashSet.add("3");
        return hashSet;
    }

    private InputMap getSequence123() {
        return InputMap.sequence("1", 1, "2", 2, "3", 3);
    }
}
