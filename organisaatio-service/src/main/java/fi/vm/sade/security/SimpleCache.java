package fi.vm.sade.security;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleCache {

    private SimpleCache() {
    }

    public static <KEY, VALUE> Map<KEY, VALUE> buildCache(final int MAX_CACHE_SIZE) {
        return Collections.synchronizedMap(new LinkedHashMap<KEY, VALUE>(MAX_CACHE_SIZE + 1, .75F, true) {
            // This method is called just after a new entry has been added
            public boolean removeEldestEntry(Map.Entry eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        });
    }

}
