package satellite.state;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import info.android15.satellite.BuildConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class TestSubBuilder {

    @Test
    public void adding_to_sub_adds_to_result_state_map() throws Exception {
        StateMap.Builder builder = StateMap.builder();
        builder.sub("sub_key").put("value_key", 1);
        assertEquals(1, builder.build().<StateMap>get("sub_key").get("value_key"));
    }

    @Test
    public void removing_sub_removes_from_result_state_map() throws Exception {
        StateMap.Builder builder = StateMap.builder();
        builder.sub("sub_key").put("value_key", 1);
        builder.remove("sub_key");
        assertNull(builder.build().get("sub_key"));
    }

    @Test
    public void requesting_the_same_builder_twice_returns_the_same_builder() throws Exception {
        StateMap.Builder builder = StateMap.builder();
        assertEquals(builder.sub("sub_key"), builder.sub("sub_key"));
    }

    @Test
    public void returns_unparceled_child_builder_as_well() throws Exception {
        StateMap.Builder builder = StateMap.builder();
        builder.sub("sub_key").put("value_key", 1);
        assertEquals(1, builder.build().toBuilder().sub("sub_key").build().get("value_key"));
    }

    @Test
    public void unparceled_child_saves_to_state() throws Exception {
        StateMap.Builder builder = StateMap.builder();
        builder.sub("sub_key").put("value_key", 1);

        StateMap.Builder map = builder.build().toBuilder();
        map.sub("sub_key").put("value_key", 2);

        StateMap sub = map.build().get("sub_key");
        assertEquals(2, sub.get("value_key"));
    }
}
