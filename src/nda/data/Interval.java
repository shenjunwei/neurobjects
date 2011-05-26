package nda.data;

/**
 * Represents a time interval (window). 
 * 
 * @author giulianoxt
 */
public class Interval {
    protected double start;
    protected double end;
    
    public final static Interval EMPTY = new Interval(
            Double.NaN, Double.NaN
    );
    public final static Interval INF = new Interval(
            Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY
    );
    
    public static Interval make(double start, double end) {
        Interval interval = new Interval(start, end);
        
        if (!interval.isValid()) {
            throw new IllegalArgumentException("Interval must have a valid range [a,b]");
        }
        else {
            return interval;
        }
    }
    
    public Interval(double start, double end) {
        this.start = start;
        this.end = end;
    }
    
    @Override
    public String toString() {
        return String.format("[%.3f, %.3f]", start, end);
    }
    
    public double start() {
        return start;
    }
    
    public double end() {
        return end;
    }
    
    public double duration() {
        return end - start;
    }
    
    public boolean contains(double t) {
        return start <= t && t <= end;
    }
    
    public boolean contains(Interval interval) {
        assert interval.isValid();
        return start <= interval.start() && interval.end() <= end;
    }
    
    public boolean isValid() {
        return start <= end;
    }
    
    public boolean isEmpty() {
        return !isValid();
    }
    
    public Interval intersection(Interval interval) {
        assert interval.isValid();
        
        double a = Math.max(start, interval.start());
        double b = Math.min(end, interval.end()); 
        return new Interval(a, b);
    }
}
