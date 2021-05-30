package optimization;

import org.specs.comp.ollir.*;
import set.SetManipulator;
import utils.Logger;

import java.util.*;

import static org.specs.comp.ollir.OperationType.NOTB;

public class Liveness {
    private final Method method;
    private final List<Set<String>> def, use, in = new ArrayList<>(), out = new ArrayList<>();
    private List<Set<Integer>> successors;
    private final LivenessResult result;

    public Liveness(Method method) throws Exception {
        this.def = new ArrayList<>();
        this.use = new ArrayList<>();
        this.successors = new ArrayList<>();

        this.method = method;
        this.fillSets();
        this.fillInOut();
        this.result = calculate();
    }

    public Liveness(List<Set<String>> def, List<Set<String>> use, List<Set<Integer>> successors) throws Exception {
        this.def = def;
        this.use = use;
        this.successors = successors;

        this.method = null;
        this.fillInOut();
        this.result = calculate();
    }

    public LivenessResult getResult() {
        return result;
    }

    private void fillInOut() {
        for (int i = 0; i < use.size(); i++) {
            in.add(new HashSet<>());
            out.add(new HashSet<>());
        }
    }

    private void reverse() {
        Collections.reverse(use);
        Collections.reverse(def);
        Collections.reverse(successors);

        List<Set<Integer>> succ = new ArrayList<>();
        for (Set<Integer> s : successors) {
            Set<Integer> newSet = new HashSet<>();
            for (int el : s) {
                newSet.add(def.size() - 1 - el); // invert indexes
            }
            succ.add(newSet);
        }

        successors = succ;
    }

    private LivenessResult calculate() throws Exception {
        this.reverse();
        int count = 0;
        while (true) {
            count++;

            List<Set<String>> in2 = SetManipulator.bidimensionalCopy(in);
            List<Set<String>> out2 = SetManipulator.bidimensionalCopy(out);

            for (int n = 0; n < use.size(); n++) {
                Set<String> newOut = new HashSet<>();
                for (int s : successors.get(n))
                    newOut = SetManipulator.union(in.get(s), newOut);
                out.set(n, newOut);
                in.set(n, SetManipulator.union(use.get(n), SetManipulator.difference(out.get(n), def.get(n))));
            }

            boolean end = true;
            for (int n = 0; n < use.size(); n++) {
                boolean end1 = SetManipulator.equal(in2, in);
                boolean end2 = SetManipulator.equal(out2, out);
                if (!end1 || !end2) end = false;
            }

            System.out.println("Iteration " + count);
            System.out.println(this.toString());

            if (end) break;
        }

        System.out.println("N iter = " + count);

        return new LivenessResult(count, in, out);
    }

    private String getIdentifier(Element element) {
        if (element.isLiteral()) return null;

        Operand operand = (Operand) element;

        if (element.getType().getTypeOfElement() == ElementType.THIS || operand.getName().equals("this")) return null;

        return operand.getName();
    }

    public void addToInstructionSet(Element element, Set<String> set) {
        String name = getIdentifier(element);
        if (name != null) set.add(name);
    }

    public void fillSets() throws Exception {
        List<Instruction> instructions = method.getInstructions();
        for (int i = 0; i < instructions.size(); i++)
            fillSets(instructions.get(i), i, i == instructions.size() - 1);
    }

    public void fillSets(Instruction i, int idx, boolean last) throws Exception {
        Set<String> instructionDef = new HashSet<>(), instructionUse = new HashSet<>();
        Set<Integer> instructionSuccessors = new HashSet<>();

        OllirVariableFinder.findInstruction(method, (FinderAlert alert) -> {
            switch(alert.getType()) {
                case USE -> addToInstructionSet(alert.getElement(), instructionUse);
                case DEF -> addToInstructionSet(alert.getElement(), instructionDef);
                case SUCCESSOR -> instructionSuccessors.add(alert.getValue());
            }
        }, i, idx, last);

        use.add(instructionUse);
        def.add(instructionDef);
        successors.add(instructionSuccessors);
    }

    public String getStringInDesiredSpace(String value, int space) {
        if (value.length() > space)
            return value.substring(0, space);


        if (value.length() < space) {
            char[] pad = new char[space - value.length()];
            Arrays.fill(pad, ' ');

            return value + String.valueOf(pad);
        }
        return value;
    }

    public <T> String getSetString(Set<T> set) {
        StringBuilder builder = new StringBuilder();
        builder.append("{ ");
        int count = 0;
        for (T el : set) {
            count++;
            builder.append(el);

            if (count < set.size()) builder.append(", ");
        }
        builder.append(" }");
        return builder.toString();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();


        builder.append(getStringInDesiredSpace("", 18)).append(" | ")
                .append(getStringInDesiredSpace("Use", 30)).append(" | ")
                .append(getStringInDesiredSpace("Def", 30)).append(" | ")
                .append(getStringInDesiredSpace("Successors", 30)).append(" | ")
                .append(getStringInDesiredSpace("In", 30)).append(" | ")
                .append(getStringInDesiredSpace("Out", 30))
                .append("\n");

        for (int i = 0; i < use.size(); i++) {
            builder.append(getStringInDesiredSpace("Instruction " + (use.size() - 1 - i), 18)).append(" | ")
                    .append(getStringInDesiredSpace(getSetString(use.get(i)), 30)).append(" | ")
                    .append(getStringInDesiredSpace(getSetString(def.get(i)), 30)).append(" | ")
                    .append(getStringInDesiredSpace(getSetString(successors.get(i)), 30)).append(" | ")
                    .append(getStringInDesiredSpace(getSetString(in.get(i)), 30)).append(" | ")
                    .append(getStringInDesiredSpace(getSetString(out.get(i)), 30))
                    .append("\n");
        }

        return builder.toString();
    }

}
