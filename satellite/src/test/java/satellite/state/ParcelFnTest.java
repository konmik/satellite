package satellite.state;

import android.util.Pair;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import info.android15.satellite.BuildConfig;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ParcelFnTest {

    @Test
    public void testMarshall() throws Exception {
        assertEquals(1, ParcelFn.unmarshall(ParcelFn.marshall(1)));
        assertEquals("1", ParcelFn.unmarshall(ParcelFn.marshall("1")));
    }

    @Test(expected = Exception.class)
    public void testMarshallNotParcelable() throws Exception {
        ParcelFn.marshall(new Pair<>(1, 1));
    }

    @Test
    public void testClone() throws Exception {
        assertEquals(1, ParcelFn.clone(1));
    }
}