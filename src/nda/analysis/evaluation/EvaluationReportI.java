package nda.analysis.evaluation;

import java.util.List;


/**
 * Represents a component capable of generating a report of a
 * evaluation operation.
 *
 * @author Giuliano Vilela
 */
public interface EvaluationReportI {
    public void makeReport(List<EvaluationResult> result) throws EvaluationException;
}
