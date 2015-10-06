package statemap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashSet;

import info.android15.statemap.BuildConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class StateMapTest {

    @Test
    public void testEmpty() throws Exception {
        assertEquals(0, StateMap.empty().keys().size());
    }

    @Test
    public void testSequence() throws Exception {
        HashSet hashSet = getSetString123();
        StateMap map = getSequence123();
        assertTrue(hashSet.equals(map.keys()));
        assertEquals(1, map.get("1"));
        assertEquals(2, map.get("2"));
        assertEquals(3, map.get("3"));
    }

    @Test(expected = Exception.class)
    public void testSequenceWrongSequence() {
        StateMap.sequence("1", 1, "2");
    }

    @Test(expected = Exception.class)
    public void testSequenceWrongType() {
        StateMap.sequence(1, 1, "2");
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
        StateMap.Builder out = new StateMap.Builder();
        out.put("1", 1);
        out.put("2", 2);
        out.put("3", 3);
        assertTrue(out.build().equals(getSequence123()));
    }

    @Test
    public void testEquals() throws Exception {
        StateMap map1 = StateMap.sequence("1", 1, "2", 2);
        StateMap map2 = StateMap.sequence("1", 1, "2", 2);
        StateMap map3 = StateMap.sequence("1", 1, "3", 2);
        StateMap map4 = StateMap.sequence("1", 1);
        assertTrue(map1.equals(map2));
        assertFalse(map1.equals(map3));
        assertFalse(map1.equals(map4));
        assertTrue(StateMap.empty().equals(StateMap.empty()));
        assertTrue(StateMap.sequence(null, null).equals(StateMap.sequence(null, null)));
    }

    @Test
    public void testBuilder() throws Exception {
        assertNotNull(StateMap.builder());
    }

    private HashSet getSetString123() {
        HashSet hashSet = new HashSet();
        hashSet.add("1");
        hashSet.add("2");
        hashSet.add("3");
        return hashSet;
    }

    private StateMap getSequence123() {
        return StateMap.sequence("1", 1, "2", 2, "3", 3);
    }
}
