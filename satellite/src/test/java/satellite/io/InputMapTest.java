package satellite.io;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InputMapTest {
    @Test
    public void testEmpty() throws Exception {
        assertEquals(0, InputMap.empty().keys().size());
    }
}