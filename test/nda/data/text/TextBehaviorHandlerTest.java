package nda.data.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import nda.data.BehaviorHandlerI;
import nda.data.Interval;


/**
 * Tests for the TextBehaviorHandler class
 * 
 * @author Giuliano Vilela
 * @ingroup UnitTests
 */
public class TextBehaviorHandlerTest {
    private final static String filepath1 = "data/test/behaviors/ge4_contacts.txt";
    private final static String filepath2 = "data/test/behaviors/ge5_contacts.txt";

    private BehaviorHandlerI b1;
    private BehaviorHandlerI b2;


    @Before
    public void setUp() throws Exception {
        b1 = new TextBehaviorHandler(filepath1);
        b2 = new TextBehaviorHandler(filepath2);
    }


    /**
     * Test method for {@link nda.data.text.TextBehaviorHandler#getIntervals(java.lang.String)}.
     */
    @Test
    public void testGetIntervals() {
        assertEquals(1, b1.getIntervals("ball").size());
        assertEquals(3, b1.getIntervals("urchin").size());
        assertEquals(6, b2.getIntervals("ball").size());
        assertEquals(9, b2.getIntervals("brush").size());

        assertTrue(b1.getIntervals("ball").contains(Interval.make(5801, 5805)));
        assertTrue(b1.getIntervals("urchin").contains(Interval.make(5827, 5832)));
        assertTrue(b2.getIntervals("brush").contains(Interval.make(3642, 3648)));
        assertTrue(b2.getIntervals("food").contains(Interval.make(4712, 4754)));
        assertTrue(b2.getIntervals("ball").contains(Interval.make(3630, 3631)));
    }

    /**
     * Test method for {@link nda.data.text.TextBehaviorHandler#getEnclosingInterval(java.util.List)}.
     */
    @Test
    public void testGetEnclosingInterval() {
        List<String> l1 = Arrays.asList("ball", "urchin");
        List<String> l2 = Arrays.asList("brush", "food");

        assertEquals(Interval.make(5801, 5837), b1.getEnclosingInterval(l1));
        assertEquals(Interval.make(5811, 6293), b1.getEnclosingInterval(l2));
    }
}
