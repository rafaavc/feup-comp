package optimization;

import utils.Logger;

import java.util.*;

public class LivenessResult {
    private final Map<String, LivenessRange> variables = new HashMap<>();

    private void addVariable(int index, String variable) {
        if (!variables.containsKey(variable)) variables.put(variable, new LivenessRange(index));
    }

    public LivenessResult(List<Set<String>> in, List<Set<String>> out) {
        for (int i = 0; i < in.size() && i < out.size(); i++) {
            Set<String> inSet = in.get(i);
            Set<String> outSet = out.get(i);

            // add new variables
            for (String var : inSet) addVariable(i, var);
            for (String var : outSet) addVariable(i, var);

            // end variables that are no longer live
            for (String variable : variables.keySet()) {
                if (!variables.get(variable).hasEnd() && !inSet.contains(variable)) {
                    Logger.err("Inset doesn't contain live variable! (" + variable + ")");
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
