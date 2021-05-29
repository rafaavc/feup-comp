package optimization;

import set.SetManipulator;
import utils.Logger;

import java.util.*;

public class LivenessResult {
    private final Map<String, LivenessRange> variables = new HashMap<>();
    private final int iterations;

    public int getIterations() {
        return iterations;
    }

    private void addVariable(int index, String variable) {
        if (!variables.containsKey(variable)) variables.put(variable, new LivenessRange(index));
    }

    public LivenessResult(int iterations, List<Set<String>> inInput, List<Set<String>> outInput) {
        this.iterations = iterations;

        List<Set<String>> in = SetManipulator.bidimensionalCopy(inInput);
        List<Set<String>> out = SetManipulator.bidimensionalCopy(outInput);

        Collections.reverse(in);
        Collections.reverse(out);

        // from now on in and out are in the correct instruction order

        for (int i = 0; i < in.size() && i < out.size(); i++) {
            Set<String> inSet = in.get(i);
            Set<String> outSet = out.get(i);

            // add new variables
            for (String var : inSet) addVariable(i, var);
            for (String var : outSet) addVariable(i, var);

            // end variables that are no longer live
            for (String variable : variables.keySet()) {
                if (!variables.get(variable).hasEnd() && !inSet.contains(variable)) {
                    Logger.err("Inset doesn't contain live variable in instruction " + i + "! (" + variable + ")");
                }
                if (!variables.get(variable).hasEnd() && !outSet.contains(variable)) {
                    variables.get(variable).setEnd(i);
                }
            }
        }
    }

    public LivenessRange getLivenessRange(String variable) {
        return variables.get(variable);
    }
}
