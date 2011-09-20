package nda.util;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Least-Recently Used Cache.
 *
 * @author Giuliano Vilela
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    protected final int MAX_ENTRIES;


    public LRUCache(int max_entries) {
        super(max_entries+1, 1.0f, true);
        MAX_ENTRIES = max_entries;
    }


    public int getMaxEntries() {
        return MAX_ENTRIES;
    }


    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> entry) {
        return size() > MAX_ENTRIES;
    }


    private static final long serialVersionUID = 1L;
}
