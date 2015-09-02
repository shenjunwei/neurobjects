# `PatternHandler` #

## Introduction ##

This document gives an overview of the `PatternHandler` component. This component provides
an useful abstraction of a set of spike rate _patterns_, as described in
CountMatrixComponent.


## Spike rate patterns relation ##

The `PatternHandler` component has a concept of the _spike rate patterns relation_ in the
sense that every `PatternHandler` is a thin layer for a relation of this kind. Each
pattern, probabily extracted from a `SpikeRateMatrixI`, is an instance of this relation.


## Objective ##

Having a standard abstraction for this kind of relation allows us to extract patterns from
a `SpikeRateMatrixI` and use various machine learning techniques to reason about its
properties. For instance, with a `PatternHandler` relation and a suitable classifier, one
can ask questions such as _"Considering a given set of time intervals and the observed
spike rate function, which ones correspond to a given stimulus?"_.


## Creating a `PatternHandler` ##

To create a `PatternHandler`, we need:

  * A `String` corresponding to the relation name
  * A `List<String>` of all the considered neuron names
  * The `windowWidth` of the patterns (see CountMatrixComponent for a better description)
  * A `Set<String>` of labels. I.e., the possible values for the class attribute of this relation.

Alternatively, one can use the `PatternHandler(String, SpikeRateMatrixI, Set<String>)`
constructor and use the neuron names and the window width of the `SpikeRateMatrixI`.


## Populating the relation ##

To add instances to the corresponding Weka relation the `PatternHandler.addPattern`
method is used. The given pattern, a `double[]`, should have one value for each non
class attribute (there are `W-1` attributes of this type). Alongside the pattern itself,
the user gives a `String` as the class value for the instance. Example:

```
// Setup the SpikeRateMatrixI before extracting patterns from it
Interval interval = matrixA.getInterval();
matrixA.setCurrentTime(interval.start());
matrixA.setWindowWidth(30);

// Add the patterns
for (double[] pattern : matrixA)
    relation.addPattern(pattern, "food"); 
```


## Integrating with the Weka software suite ##

The method `PatternHandler.toWekaFormat()` returns a String with the contents of a
ARFF file describing the given Weka relation. One can use this output to generate a
separate text file that can easily be opened and analysed using the Weka GUI.


## Complete example ##

```
package app;

import java.util.HashSet;
import java.util.Set;

import nda.data.CountMatrix;
import nda.data.Interval;
import nda.data.PatternHandler;
import nda.data.SpikeHandlerI;
import nda.data.SpikeRateMatrixI;
import nda.data.text.TextSpikeHandler;


/**
 * This is a test application for the PatternHandler component.
 * 
 * It shows how to open a set of files with spike train data, estimate the spike rate
 * function and create a Weka ARFF file containing patterns extracted from the spike rate
 * matrix.
 * 
 * @author Giuliano Vilela
 */
public class ExtractPatternsApp {
    private static final String spikeDir = "setup/spikes";

    public static void main(String[] args) throws Exception {
        // Create a SpikeHandlerI containing the desired neurons
        SpikeHandlerI spikeHandler = new TextSpikeHandler(spikeDir, "S1");

        /*
         * Setup the CountMatrix with the given parameters.
         * Each line of the matrix has 5 bins (columns)
         */
        SpikeRateMatrixI rateMatrix = new CountMatrix(spikeHandler, 5);

        /*
         * The spike rate matrix has the following layout:
         * 
         *           b0  b1  b2  b3  b4
         *   S1_03A  ..  ..  ..  ..  ..
         *   S1_07A  ..  ..  ..  ..  ..
         *   S1_08C  ..  ..  ..  ..  ..
         * 
         * We'll extract 3 patterns from it, beginning at the start of the spike interval
         * and having window width 3, so each pattern has 3*3 = 9 values.
         */
        Interval interval = rateMatrix.getInterval();
        rateMatrix.setCurrentTime(interval.start());
        rateMatrix.setWindowWidth(3);
        rateMatrix.setStep(1);

        // Each pattern will have one of the following labels
        Set<String> labels = new HashSet<String>();
        labels.add("A");
        labels.add("B");
        labels.add("C");

        // Create a PatterHandler object with the above configuration
        PatternHandler relation = new PatternHandler("S1_Patterns", rateMatrix, labels);

        // Insert the patterns
        int i = 0;
        for (double[] pattern : rateMatrix) {
            String label;

            if (i == 0)
                label = "A";
            else if (i == 1)
                label = "B";
            else
                label = "C";

            i++;
            relation.addPattern(pattern, label);
        }

        // Generate an ARFF representation of the PatternHandler and print it
        String weka_str = relation.toWekaFormat();
        System.out.println(weka_str);
    }
}
```