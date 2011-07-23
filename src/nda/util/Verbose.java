package nda.util;

/**
 * Implemented by components that can optionally output information.
 * 
 * @author Giuliano Vilela
 */
public interface Verbose {
    public void setVerbose(boolean verbose);
    public boolean getVerbose();
}
