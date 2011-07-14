package app;

import java.util.Set;

import nda.data.BehaviorHandlerI;
import nda.data.text.TextBehaviorHandler;


/**
 * This is a test application for the BehaviorHandlerI component and its
 * TextBehaviorHandler implementation. It shows how to read a text data file
 * containing an animal behavior specification and print it to the user.
 * 
 * @author Giuliano Vilela
 * @ingroup ExampleApps
 */
public class ShowBehaviorFileApp {
    public static void main(String[] args) throws Exception {
        // Path to the behavior data file
        String filepath = "data/test/behaviors/ge4_contacts.txt";

        // Load the data file into memory using a BehaviorHandlerI
        BehaviorHandlerI handler = new TextBehaviorHandler(filepath);

        // Access all the labels that were used in the file
        Set<String> labels = handler.getLabelSet();

        // Print the labels and its intervals
        for (String label : labels) {
            System.out.printf("Label \"%s\" is defined for intervals: %s\n",
                    label, handler.getIntervals(label));
        }
    }
}
