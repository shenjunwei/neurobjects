package nda.util;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;


/**
 * @author Giuliano Vilela
 */
public class LRUCacheTest {
    private LRUCache<Integer,Integer> cache;


    @Before
    public void setUp() throws Exception {
        cache = new LRUCache<Integer,Integer>(3);
    }


    @Test
    public void testNormalUse() {
        cache.put(1, 1);
        cache.put(2, 2);
        assertEquals(2, cache.size());
    }


    @Test
    public void testFixedSize() {
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        assertEquals(3, cache.size());

        cache.put(4, 4);
        assertFalse(cache.containsKey(1));
        assertEquals(3, cache.size());
        assertEquals((Integer) 2, cache.get(2));
        assertEquals((Integer) 3, cache.get(3));
        assertEquals((Integer) 4, cache.get(4));
    }


    @Test
    public void testLRUBehavior() {
        cache.put(1, 1); // 1
        cache.put(2, 2); // 1, 2
        cache.put(3, 3); // 1, 2, 3
        assertEquals(3, cache.size());

        cache.get(1);    // 2, 3, 1
        cache.put(4, 4); // 3, 1, 4
        assertEquals(3, cache.size());
        assertFalse(cache.containsKey(2));
        assertEquals((Integer) 4, cache.get(4));

        cache.put(5, 5); // 1, 4, 5
        cache.put(6, 6); // 4, 5, 6
        cache.get(5);    // 4, 6, 5
        cache.put(7, 7); // 6, 5, 7
        assertEquals(3, cache.size());
        assertEquals((Integer) 6, cache.get(6));
        assertEquals((Integer) 5, cache.get(5));
        assertEquals((Integer) 7, cache.get(7));
    }
}
