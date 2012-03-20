package nda.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * @author Giuliano Vilela
 */
public class CollectionUtils {
    public static<T> T getElement(Collection<T> c) {
        if (c.size() != 1)
            throw new IllegalArgumentException("Collection has more than one element");

        return c.iterator().next();
    }


    public static<K,V> V getElement(Map<K, V> map) {
        return getElement(map.values());
    }


    @SuppressWarnings("unchecked")
    public static<T> List<T>[] splitOnHalf(List<T> list) {
        List<T> listA = list.subList(0, list.size()/2);
        List<T> listB = list.subList(list.size()/2, list.size());
        return (List<T>[]) new Object[] { listA, listB };
    }
}
