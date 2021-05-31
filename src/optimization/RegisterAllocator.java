package optimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;

public class RegisterAllocator {
    Map<String, LivenessRange> variables;
    Set<String> variableNames;
    Map<String, List<String>> interferenceGraph = new HashMap<>();
    Map<String, Integer> coloredGraph = new HashMap<>();
    Stack<String> variableStack = new Stack<>();

    public RegisterAllocator(Map<String, LivenessRange> variables){
        this.variables = variables;
        this.variableNames = variables.keySet();
        try {
            buildInterferenceGraph();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, Integer> getColoredGraph() {
        return coloredGraph;
    }

    private void buildInterferenceGraph() throws Exception {
        for (String v : variableNames) {
            LivenessRange vRange = variables.get(v);
            List<String> interferences = new ArrayList<>();

            for (String aux : variableNames) {
                if (v.equals(aux)) continue;
                LivenessRange auxRange = variables.get(aux);

                if (vRange.getStart() <= auxRange.getStart() && vRange.getEnd() >= auxRange.getStart() || vRange.getStart() <= auxRange.getEnd() && vRange.getEnd() >= auxRange.getEnd() || vRange.getStart() >= auxRange.getStart() && vRange.getEnd() <= auxRange.getEnd()) {
                    interferences.add(aux);
                }
            }

            interferenceGraph.put(v, interferences);
        }
    }

    public boolean colorGraph(int k) {
        Map<String, List<String>> graph = new HashMap<>(interferenceGraph);
        boolean possible = simplify(k, graph);
        if (!possible) return false;

        System.out.println("The stack is " + variableStack);

        while(!variableStack.isEmpty()) {
            String v = variableStack.pop();
            int color = getColor(k, v);
            if (color < 0) return false;
            coloredGraph.put(v, color);
        }

        return true;
    }

    private boolean simplify(int k, Map<String, List<String>> graph) {
        Map<String, List<String>> newGraph;
        
        System.out.println(graph);

        if (graph.isEmpty()) return true;

        for (Entry<String, List<String>> entry : graph.entrySet()) {
            if (entry.getValue().size() < k) {
                newGraph = removeVariable(entry.getKey(), graph);
                variableStack.push(entry.getKey());
                return simplify(k, newGraph);
            }
        }

        return false;
    }

    private Map<String, List<String>> removeVariable(String v, Map<String, List<String>> graph) {
        List<String> toRemove = graph.get(v);

        graph.remove(v);

        for (String k : toRemove) {
            List<String> interferences = graph.get(k);
            if (interferences != null) interferences.remove(v);
            graph.replace(k, interferences);
        }

        return graph;
    }

    private int getColor(int k, String v) {
        List<String> interferences = interferenceGraph.get(v);

        for (int i = 0; i < k; i++) {
            boolean used = false;
            for (String aux : interferences) {
                if (!coloredGraph.containsKey(aux)) continue;
                if (coloredGraph.get(aux) == i) used = true;
            }
            if (used) continue;
            return i;
        }

        return -1;
    }
}
