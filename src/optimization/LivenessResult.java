package optimization;

import set.SetManipulator;
import utils.Logger;

import java.util.*;

public class LivenessResult {
    private final Map<String, LivenessRange> variables = new HashMap<>();
    private final List<Set<String>> in, out;
    private final int iterations;

    public Map<String, LivenessRange> getVariables() {
        return variables;
    }

    public int getIterations() {
        return iterations;
    }

    public List<Set<String>> getIn() {
        return in;
    }

    public List<Set<String>> getOut() {
        return out;
    }

    private void addVariable(int index, String variable) {
        if (!variables.containsKey(variable)) variables.put(variable, new LivenessRange(index));
    }

    public LivenessResult(int iterations, List<Set<String>> inInput, List<Set<String>> outInput) throws Exception {
        this.iterations = iterations;

        List<Set<String>> in = SetManipulator.bidimensionalCopy(inInput);
        List<Set<String>> out = SetManipulator.bidimensionalCopy(outInput);

        this.in = in;
        this.out = out;

        Collections.reverse(in);
        Collections.reverse(out);

        // from now on in and out are in the correct instruction order

        for (int i = 0; i < in.size() && i < out.size(); i++) {
            Set<String> inSet = in.get(i);
            Set<String> outSet = out.get(i);

            // add new variables
//            for (String var : inSet) addVariable(i, var);
            for (String var : outSet) addVariable(i, var);

            // end variables that are no longer live
            for (String variable : variables.keySet()) {
                LivenessRange range = variables.get(variable);

                // if the variable doesn't have end, this instruction is after the instruction where the
                // variable appeared and the variable doesn't appearn in the outset, the variable is dead
                if (!range.hasEnd() && i > range.getStart() && !outSet.contains(variable))
                    range.setEnd(i - 1);

                // if the variable was given as dead and it appears in an out set then it lives again
                else if (range.hasEnd() && outSet.contains(variable))
                    range.removeEnd();

                // if the variable was given as dead and it appears in an in set without
                // appearing in the out then the time of it death is changed
                else if (range.hasEnd() && inSet.contains(variable))
                    range.setEnd((i - 1));
            }
        }
    }

    public LivenessRange getLivenessRange(String variable) {
        return variables.get(variable);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("### Liveness Result:\n");

        for (String variable : variables.keySet()) {
            LivenessRange range = variables.get(variable);
            builder.append(variable).append(" = ").append(range).append("\n");
        }

        builder.append("\n");

        return builder.toString();
    }
}
