package satellite.io;

import java.util.Arrays;
import java.util.Map;

class MarshallMapFn {
    static boolean equalsMap(Map<String, byte[]> map, Map<String, byte[]> other) {
        if (map == other)
            return true;

        if (map.size() != other.size())
            return false;

        for (Map.Entry<String, byte[]> entry : map.entrySet()) {
            Object key = entry.getKey();
            byte[] value = entry.getValue();
            byte[] value2 = other.get(key);
            if (value == null) {
                if (value2 != null || !other.containsKey(key))
                    return false;
            }
            else if (!Arrays.equals(value, value2))
                return false;
        }
        return true;
    }
}
