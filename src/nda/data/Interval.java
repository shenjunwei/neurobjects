package nda.data;

import java.util.Comparator;


/**
 * Represents a time interval (window).
 *
 * An Interval is a closed range <tt>[a,b]</tt>, represented by two double variables,
 * that model a time window in a determined experiment.
 *
 * @author Giuliano Vilela
 */
public class Interval {
    /** Start time of the interval (\c a) */
    protected double start;
    /** End time of the interval (\c b) */
    protected double end;

    /**
     * Pre-defined empty Interval. It represents an invalid range.
     *
     * Note that, for every \c T: @code Interval.EMPTY.contains(T) == false @endcode
     */
    public final static Interval EMPTY = new Interval(
            Double.NaN, Double.NaN
    );

    /**
     * Pre-defined infinite Interval. It represents the entire history of an experiment.
     *
     * Note that, for every \c T @code Interval.INF.contains(T) == true @endcode
     */
    public final static Interval INF = new Interval(
            Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY
    );


    public static class ElementComparator implements Comparator<Interval> {
        private int element;

        public ElementComparator() {
            this(0);
        }

        public ElementComparator(int el) {
            element = el;
        }

        @Override
        public int compare(Interval a, Interval b) {
            if (element == 0)
                return Double.compare(a.start, b.start);
            else
                return Double.compare(a.end, b.end);
        }
    }



    /**
     * Create an Interval with the given boundaries.
     *
     * Creates an Interval representing the range <tt>[start, end]</tt>. It's a safe
     * constructor with additional checks on the parameters.
     *
     * @param start Start time of the interval.
     * @param end End time of the interval.
     *
     * @return A valid Interval representing the range <tt>[start, end]</tt>.
     * @throws IllegalArgumentException If <tt>[start, end]</tt> isn't a valid time range.
     */
    public static Interval make(double start, double end) {
        Interval interval = new Interval(start, end);

        if (!interval.isValid())
            throw new IllegalArgumentException("Interval must have a valid range [a,b]");
        else
            return interval;
    }


    /**
     * Create an Interval with the given boundaries.
     *
     * Creates an Interval representing the range <tt>[start, end]</tt>. Its an unsafe
     * constructor with no checks on the parameters: the returned Interval may be invalid.
     *
     * @param start Start time of the interval.
     * @param end End time of the interval.
     */
    public Interval(double start, double end) {
        this.start = start;
        this.end = end;
    }


    /**
     * String representation of the interval in the form "[a, b]".
     */
    @Override
    public String toString() {
        return String.format("[%.3f, %.3f]", start, end);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Interval) {
            Interval interval = (Interval)obj;
            return Double.compare(start, interval.start()) == 0 &&
            Double.compare(end, interval.end()) == 0;
        } else {
            return false;
        }
    }


    /**
     * Start time of this interval.
     *
     * @return If the interval is valid, return the smallest \c T such that
     * <tt>this.contains(T)</tt>. If the interval is invalid, the return value is
     * undefined.
     */
    public double start() {
        return start;
    }


    /**
     * End time of this interval.
     *
     * @return If the interval is valid, return the greatest \c T such that
     * <tt>this.contains(T)</tt>. If the interval is invalid, the return value is
     * undefined.
     */
    public double end() {
        return end;
    }


    /**
     * Duration of this interval.
     *
     * @return Return the duration of the interval, defined as <tt>end-start</tt>. If the
     * interval is invalid, the return value is undefined.
     */
    public double duration() {
        return end - start;
    }


    /**
     * Check if the given time measurement belongs in the range defined by this interval.
     *
     * @param t Time parameter
     * @return True if t is in the range <tt>[start, end]</tt>, false otherwise. If the
     * interval is invalid, the return value is undefined.
     */
    public boolean contains(double t) {
        return start <= t && t <= end;
    }


    /**
     * Check if the given Interval is a subset of this interval.
     *
     * @param interval A time interval <tt>[c, d]</tt>.
     * @return True if <tt>[c, d]</tt> is a subset of <tt>[a, b]</tt>, false otherwise.
     * If any of the intervals is invalid, the return value is undefined.
     */
    public boolean contains(Interval interval) {
        assert interval.isValid();
        return start <= interval.start() && interval.end() <= end;
    }


    /**
     * Determine if this is a valid interval.
     *
     * A valid interval represents a true numeric range <tt>[a, b]</tt>. An invalid
     * interval is a range created with wrong values (i.e., <tt>a > b</tt>) or an empty
     * range.
     *
     * @return True if this is a valid interval, false otherwise.
     */
    public boolean isValid() {
        return start <= end;
    }


    /**
     * Determine if this is an empty interval.
     *
     * @return True if, and only if, <tt>[start, end]</tt> is the empty set.
     * False otherwise.
     */
    public boolean isEmpty() {
        return !isValid();
    }


    /**
     * Calculate the intersection between a given interval and this interval.
     *
     * The intersection between intervals is defined as the greatest (in terms of
     * duration) interval <tt>I</tt> such that <tt>this.contains(I) &&
     * interval.contains(I)</tt>.
     *
     * @param interval A valid time interval.
     * @return The intersection of <tt>[start, end]</tt> and
     * <tt>[interval.start(), interval.end()]</tt>.
     */
    public Interval intersection(Interval interval) {
        assert interval.isValid();

        double a = Math.max(start, interval.start());
        double b = Math.min(end, interval.end());
        return new Interval(a, b);
    }


    /**
     * Create a new interval such that the resulting interval is the smallest interval
     * that contains both this interval and a given interval.
     * 
     * @param interval The given interval to be enclosed
     * @return The resulting enclosing interval
     */
    public Interval enclose(Interval interval) {
        return Interval.make(Math.min(start, interval.start),
                Math.max(end, interval.end));
    }


    /**
     * Create a new interval such that it is displaced by <tt>offset</tt> seconds
     * in relation to the original interval.
     */
    public Interval shift(double offset) {
        return Interval.make(start+offset, end+offset);
    }
}
