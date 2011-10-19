package nda.data.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
    private final static String filepath1 = "data/test/behaviors/test_contacts.txt";
    private final static String filepath2 = "data/test/behaviors/ge5_contacts.txt";
    private final static String filepath3 = "data/test/behaviors/ge4_contacts.txt";

    private BehaviorHandlerI b1;
    private BehaviorHandlerI b2;
    private BehaviorHandlerI ge4_bh;


    @Before
    public void setUp() throws Exception {
        b1 = new TextBehaviorHandler(filepath1);
        b2 = new TextBehaviorHandler(filepath2);
        ge4_bh = new TextBehaviorHandler(filepath3);
    }


    /**
     * Test method for {@link nda.data.text.TextBehaviorHandler#getContactIntervals(java.lang.String)}.
     */
    @Test
    public void testGetIntervals() {
        assertEquals(1, b1.getContactIntervals("ball").size());
        assertEquals(3, b1.getContactIntervals("urchin").size());
        assertEquals(6, b2.getContactIntervals("ball").size());
        assertEquals(9, b2.getContactIntervals("brush").size());

        assertTrue(b1.getContactIntervals("ball").contains(Interval.make(5808, 5812)));
        assertTrue(b1.getContactIntervals("urchin").contains(Interval.make(5827, 5832)));
        assertTrue(b2.getContactIntervals("brush").contains(Interval.make(3642, 3648)));
        assertTrue(b2.getContactIntervals("food").contains(Interval.make(4712, 4754)));
        assertTrue(b2.getContactIntervals("ball").contains(Interval.make(3630, 3631)));
    }


    /**
     * Test method for {@link nda.data.text.TextBehaviorHandler#getEnclosingInterval(java.util.List)}.
     */
    @Test
    public void testGetExpositionInterval() {
        assertEquals(Interval.make(5808, 6293), b1.getExpositionInterval());
        assertEquals(Interval.make(3610, 4754), b2.getExpositionInterval());
    }


    /**
     * Test method for {@link nda.data.text.TextBehaviorHandler#getlabel(double)}.
     */
    @Test
    public void testGetLabel() {
        assertNull(b1.getLabel(0));
        assertNull(b1.getLabel(5807));
        assertNull(b1.getLabel(5812.5));
        assertNull(b1.getLabel(6294));
        assertEquals("ball", b1.getLabel(5808));
        assertEquals("ball", b1.getLabel(5809));
        assertEquals("food", b1.getLabel(5815));
        assertEquals("urchin", b1.getLabel(5828));
        assertEquals("brush", b1.getLabel(6293));
    }


    @Test
    public void testGE4Contacts() {
        for (String lb_1 : ge4_bh.getLabelSet()) {
            List<Interval> intervals_1 = ge4_bh.getContactIntervals(lb_1);

            for (String lb_2 : ge4_bh.getLabelSet()) {
                if (lb_2.equals(lb_1)) continue;

                List<Interval> intervals_2 = ge4_bh.getContactIntervals(lb_2);

                for (Interval i1 : intervals_1)
                    for (Interval i2 : intervals_2) {
                        Interval i = i1.intersection(i2);
                        assertTrue(i.isEmpty() || i.duration() == 0);
                    }
            }
        }
    }
}
