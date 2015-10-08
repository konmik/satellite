package valuemap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import info.android15.valuemap.BuildConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class TestSubBuilder {

    @Test
    public void adding_to_sub_adds_to_result_value_map() throws Exception {
        ValueMap.Builder builder = ValueMap.builder();
        builder.child("sub_key").put("value_key", 1);
        assertEquals(1, builder.build().<ValueMap>get("sub_key").get("value_key"));
    }

    @Test
    public void removing_sub_removes_from_result_value_map() throws Exception {
        ValueMap.Builder builder = ValueMap.builder();
        builder.child("sub_key").put("value_key", 1);
        builder.remove("sub_key");
        assertNull(builder.build().get("sub_key"));
    }

    @Test
    public void requesting_the_same_builder_twice_returns_the_same_builder() throws Exception {
        ValueMap.Builder builder = ValueMap.builder();
        assertEquals(builder.child("sub_key"), builder.child("sub_key"));
    }

    @Test
    public void returns_unparceled_child_builder_as_well() throws Exception {
        ValueMap.Builder builder = ValueMap.builder();
        builder.child("sub_key").put("value_key", 1);
        assertEquals(1, builder.build().toBuilder().child("sub_key").build().get("value_key"));
    }

    @Test
    public void unparceled_child_saves_to_value() throws Exception {
        ValueMap.Builder builder = ValueMap.builder();
        builder.child("sub_key").put("value_key", 1);

        ValueMap.Builder map = builder.build().toBuilder();
        map.child("sub_key").put("value_key", 2);

        ValueMap sub = map.build().get("sub_key");
        assertEquals(2, sub.get("value_key"));
    }
}
