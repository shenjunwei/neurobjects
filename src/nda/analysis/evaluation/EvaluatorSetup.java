package nda.analysis.evaluation;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;

import weka.classifiers.Classifier;
import weka.core.Utils;

import nda.analysis.InvalidSetupFileException;
import nda.analysis.Setup;


/**
 * Thin wrapper for the evaluator configuration files.
 *
 * @author Giuliano Vilela
 */
public class EvaluatorSetup {
    private static final String WEKA_CLASSIFIERS_PACKAGE = "weka.classifiers";

    private static Yaml yaml = Setup.getYaml();

    private Map<String, Object> setup;
    private List<NamedClassifier> classifiers;


    public EvaluatorSetup(String filepath)
    throws InvalidSetupFileException, FileNotFoundException {
        this(new FileReader(filepath));
    }


    @SuppressWarnings("unchecked")
    public EvaluatorSetup(Reader reader) throws InvalidSetupFileException {
        try {
            setup = (Map<String, Object>) yaml.load(reader);
            parseClassifiers();
        } catch (ClassCastException e) {
            throw new InvalidSetupFileException(e);
        }
    }


    @Override
    public String toString() {
        return yaml.dump(setup);
    }


    public String getReportType() {
        return (String) setup.get("report");
    }


    public Object getReportParameter(String parameter) {
        return setup.get(parameter);
    }


    public List<NamedClassifier> getClassifiers() {
        return classifiers;
    }


    @SuppressWarnings("unchecked")
    protected void parseClassifiers() throws InvalidSetupFileException {
        classifiers = new ArrayList<NamedClassifier>();
        Map<String, Integer> seenNames = new HashMap<String, Integer>();

        List<Object> classList = (List<Object>) setup.get("classifiers");

        for (Object obj : classList) {
            Map<String, Object> classMap = (Map<String, Object>) obj;

            String classNameStr = (String) classMap.get("class");
            String className = getClassifierClassName(classNameStr);

            if (classNameStr == null)
                throw new InvalidSetupFileException("Invalid class: " + classNameStr);

            List<String[]> optionsList = parseOptions(classMap.get("options"));

            for (String[] options : optionsList) {
                String name;
                if (classMap.containsKey("id"))
                    name = (String) classMap.get("id");
                else
                    name = classNameStr;

                if (seenNames.containsKey(name)) {
                    int numUsed = seenNames.get(name);
                    seenNames.put(name, numUsed+1);
                    name = name + "#" + (numUsed+1);
                }
                else {
                    seenNames.put(name, 0);
                }

                try {
                    Classifier classifier = Classifier.forName(className, options);
                    classifiers.add(new NamedClassifier(name, classifier));
                } catch (Exception e) {
                    throw new InvalidSetupFileException(e);
                }
            }
        }
    }


    private static String getClassifierClassName(String classStr) {
        try {
            Class.forName(classStr);
            return classStr;
        } catch (ClassNotFoundException e) {
            if (!classStr.startsWith(WEKA_CLASSIFIERS_PACKAGE))
                return getClassifierClassName(WEKA_CLASSIFIERS_PACKAGE + '.' + classStr);
            else
                return null;
        }
    }


    @SuppressWarnings("unchecked")
    private static List<String[]> parseOptions(Object optionsObj)
    throws InvalidSetupFileException {
        List<String[]> allAssignments = new ArrayList<String[]>();

        if (optionsObj == null) {
            allAssignments.add(null);
        }
        else if (optionsObj instanceof String) {
            String optionsStr = (String) optionsObj;

            try {
                allAssignments.add(Utils.splitOptions(optionsStr));
            } catch (Exception e) {
                throw new InvalidSetupFileException(e);
            }
        }
        else {
            Map<String, Object> optionsMap = (Map<String, Object>) optionsObj;
            Set<String> optionNamesSet = optionsMap.keySet();
            List<String> optionNames = new ArrayList<String>(optionNamesSet);
            Map<String, Object> assignment = new HashMap<String, Object>();

            parseOptionsRec(optionsMap, optionNames, 0, assignment, allAssignments);
        }

        return allAssignments;
    }


    @SuppressWarnings("unchecked")
    private static void parseOptionsRec(
            Map<String, Object> optionsMap,
            List<String> optionNames,
            int current,
            Map<String, Object> currentAssignment,
            List<String[]> all)
    throws InvalidSetupFileException {

        if (current == optionNames.size()) {
            all.add(toWekaFormat(currentAssignment));
            return;
        }

        String name = optionNames.get(current);
        Object valueObj = optionsMap.get(name);

        // var_name : [value1, value2, ...]
        if (valueObj instanceof List<?>) {
            List<Object> values = (List<Object>) valueObj;

            for (Object value : values) {
                currentAssignment.put(name, value);
                parseOptionsRec(optionsMap, optionNames, current+1, currentAssignment, all);
            }
        }
        // var_name : from -> to @ step
        else if (valueObj instanceof String) {
            Scanner scanner = new Scanner((String) valueObj);

            double from = scanner.nextDouble();
            scanner.next();
            double to = scanner.nextDouble();
            scanner.next();
            double step = scanner.nextDouble();

            for (double value = from; value <= to; value += step) {
                currentAssignment.put(name, value);
                parseOptionsRec(optionsMap, optionNames, current+1, currentAssignment, all);
            }
        }
        // var_name :
        else if (valueObj == null) {
            currentAssignment.put(name, null);
            parseOptionsRec(optionsMap, optionNames, current+1, currentAssignment, all);
        }
        // var_name : value
        else {
            double value = getDouble(valueObj);
            currentAssignment.put(name, value);
            parseOptionsRec(optionsMap, optionNames, current+1, currentAssignment, all);
        }
    }


    private static String[] toWekaFormat(Map<String, Object> options) {
        int estimate = options.size() * 2;
        List<String> optionsList = new ArrayList<String>(estimate);

        for (String name : options.keySet()) {
            Object value = options.get(name);

            optionsList.add('-' + name);

            if (value == null)
                continue;

            if (value instanceof Double) {
                double valueD = (Double)value;
                int valueI = (int) Math.rint(valueD);

                if (Double.compare(valueD, valueI) == 0)
                    value = valueI;
            }

            optionsList.add(value.toString());
        }

        return optionsList.toArray(new String[0]);
    }


    private static double getDouble(Object num) {
        if (num instanceof Integer)
            return (Integer) num;
        else
            return (Double) num;
    }
}
