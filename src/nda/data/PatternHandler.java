package nda.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;


/**
 * Defines a set of neuronal responses patterns
 *
 * @author Nivaldo Vasconcelos
 * @author Giuliano Vilela
 */
public class PatternHandler {
    private Instances relation;


    public PatternHandler(PatternHandler handler) {
        Instances other_rel = handler.getRelation();
        relation = new Instances(other_rel, other_rel.numInstances());
    }


    public PatternHandler(String name, SpikeRateMatrixI rateMatrix, Set<String> labels) {
        this(name, rateMatrix.getNeuronNames(), rateMatrix.getWindowWidth(), labels);
    }


    public PatternHandler (String name, List<String> neuronNames,
            int windowWidth, Set<String> labels) {

        if (neuronNames.isEmpty() || labels.isEmpty() || windowWidth < 1)
            throw new IllegalArgumentException();

        FastVector attributes = new FastVector();
        for (String neuron : neuronNames) {
            for (int i = 0; i < windowWidth; ++i)
                attributes.addElement(new Attribute(neuron + "_" + i));
        }

        FastVector class_values = new FastVector();
        for (String label : labels) {
            class_values.addElement(label);
        }

        attributes.addElement(new Attribute("label", class_values));

        int capacity = neuronNames.size() * windowWidth;
        relation = new Instances(name, attributes, capacity);
        relation.setClass(relation.attribute("label"));
    }


    @Override
    public String toString() {
        return relation.toSummaryString();
    }


    /**
     * Builds a weka header based on current set of patterns
     * 
     * Based on the current neuron names returns a string in which is a Weka
     * File header, where each neuron contributes with N bins, where N is equal
     * to (size of pattern)/(number of neurons). N should be a integer.
     * 
     * Please see http://www.cs.waikato.ac.nz/~ml/weka/arff.html for more
     * details about the Attribute-Relation File Format (ARFF)
     */
    public String toWekaFormat() {
        return relation.toString();
    }


    /**
     * Adds the given pattern in the current set of patterns.
     * 
     * @param pat
     *            given pattern to be added in the current set of patterns
     */
    public void addPattern(double[] pattern, String label) {
        if (pattern.length != relation.numAttributes()-1) {
            throw new IllegalArgumentException("pattern has an invalid number of values");
        }

        Instance instance = new Instance(relation.numAttributes());
        instance.setDataset(relation);

        for (int i = 0; i < pattern.length; ++i)
            instance.setValue(i, pattern[i]);

        instance.setClassValue(label);
        relation.add(instance);
    }


    public int getDimension() {
        return relation.numAttributes()-1;
    }


    /** Inserts a set of raw pattern in the current set of patterns.
     * 
     * Given a list of raw patterns (double 1Dvectors) inserts them in current set of patterns using the additional information: labels, initial time, and time step
     * 
     * @param ps list of raw patterns to be inserted in the current set of patterns;
     * @param labels label to be used in the inserted patterns;
     * @param time initial time to be used in the inserted patterns;
     * @param timeStep time step to be used in the inserted patterns from first time.
     */
    public void addPatterns(List<double[]> patterns, String label) {
        for (double[] pattern : patterns) {
            addPattern(pattern, label);
        }
    }


    /**
     * Given a label, returns the correspondent set of patterns stored
     * If there is no patterns associated with that label returns \c null.
     * 
     * @param label label to be used to returns the patterns
     * @return A list of patterns that are associated to label
     */
    public List<double[]> getPatterns(String label) {
        Attribute class_attr = relation.classAttribute();
        List<double[]> patterns = new ArrayList<double[]>();

        @SuppressWarnings("rawtypes")
        Enumeration e = relation.enumerateInstances();

        while (e.hasMoreElements()) {
            Instance instance = (Instance)e.nextElement();

            if (instance.stringValue(class_attr).equals(label)) {
                double[] values = instance.toDoubleArray();
                patterns.add(Arrays.copyOfRange(values, 0, values.length-1));
            }
        }

        return patterns;
    }


    public Set<String> getLabelSet() {
        Set<String> labels = new HashSet<String>();
        Attribute label_attr = relation.classAttribute();

        for (int i = 0; i < label_attr.numValues(); ++i) {
            labels.add(label_attr.value(i));
        }

        return labels;
    }


    /** Returns the total number of stored patterns
     * 
     * This value is result of the summation of all pattern for each label.
     * 
     * @return total number of stored patterns
     */
    public int size() {
        return relation.numInstances();
    }


    public Instances getRelation() {
        return relation;
    }
}
