package optimization;

import org.specs.comp.ollir.*;
import utils.Logger;

import java.util.*;

import static org.specs.comp.ollir.OperationType.NOTB;

public class Liveness {
    private final Method method;
    private final List<Set<String>> def = new ArrayList<>(),
        use = new ArrayList<>(),
        in = new ArrayList<>(),
        out = new ArrayList<>();
    private final List<Set<Integer>> successors = new ArrayList<>();
    private LivenessResult result;

    public Liveness(Method method) throws Exception {
        this.method = method;
        this.fillSets();
        this.result = calculate();
    }

    public LivenessResult getResult() {
        return result;
    }

    private LivenessResult calculate() throws Exception {



        return new LivenessResult(in, out);
    }

    private String getIdentifier(Element element) {
        if (element.isLiteral()) return null;

        Operand operand = (Operand) element;

        if (element.getType().getTypeOfElement() == ElementType.THIS || operand.getName().equals("this")) return null;

        return operand.getName();
    }

    public void addToInstructionUseSet(Element element, Set<String> instructionUse) {
        String name = getIdentifier(element);
        if (name != null) instructionUse.add(name);
    }

    public int getIndexOfInstructionWithLabel(String label) throws Exception {
        List<Instruction> instructions = method.getInstructions();
        for (int i = 0; i < instructions.size(); i++) {
            List<String> instructionLabels = method.getLabels(instructions.get(i));
            for (String l : instructionLabels) if (l.equals(label)) return i;
        }
        throw new Exception("Couldn't find instruction with label " + label);
    }

    public void fillSets() throws Exception {
        List<Instruction> instructions = method.getInstructions();
        for (int i = 0; i < instructions.size(); i++)
            fillSets(instructions.get(i), i, i == instructions.size() - 1);


    }

    public void fillSets(Instruction i, int idx, boolean last) throws Exception {
        Set<String> instructionDef = new HashSet<>(), instructionUse = new HashSet<>();
        Set<Integer> instructionSuccessors = new HashSet<>();

        if (fillSets(i, instructionDef, instructionUse, instructionSuccessors) && !last)
            instructionSuccessors.add(idx + 1);

        use.add(instructionUse);
        def.add(instructionDef);
        successors.add(instructionSuccessors);
        in.add(new HashSet<>());
        out.add(new HashSet<>());
    }

    /**
     * Fills the sets of a given instruction
     * @param i The instruction
     * @param instructionDef The def set
     * @param instructionUse The instruction use set
     * @param instructionSuccessors The instruction successors set
     * @return Whether we need to add the next instruction to the successors
     * @throws Exception miscellaneous
     */
    public boolean fillSets(Instruction i, Set<String> instructionDef, Set<String> instructionUse, Set<Integer> instructionSuccessors) throws Exception {
        switch (i.getInstType()) {
            case ASSIGN -> {
                AssignInstruction assignInstruction = (AssignInstruction) i;
                Operand destOperand = (Operand) assignInstruction.getDest();

                instructionDef.add(destOperand.getName());

                try {
                    // best way I found to do array assign
                    ArrayOperand arrayOperand = (ArrayOperand) destOperand;
//                    try {
                    addToInstructionUseSet(arrayOperand.getIndexOperands().get(0), instructionUse);

//                    } catch (Exception e) {
//                        Logger.err(e.getMessage() + "\n");
//                        e.printStackTrace();
//                    }
                } catch (Exception ignored) {}

                fillSets(assignInstruction.getRhs(), instructionDef, instructionUse, instructionSuccessors);
            }
            case CALL -> {
                CallInstruction callInstruction = (CallInstruction) i;

                Operand firstCallOperand = (Operand) callInstruction.getFirstArg();
                LiteralElement secondCallOperand = (LiteralElement) callInstruction.getSecondArg();

                CallType invocationType = callInstruction.getInvocationType();

                switch (invocationType) {
                    case ldc -> Logger.err("Received ldc in invocation type (not supposed)");
                    case NEW -> {
                        if (firstCallOperand.getType().getTypeOfElement() == ElementType.ARRAYREF) {
                            addToInstructionUseSet(callInstruction.getListOfOperands().get(0), instructionUse);
                        }
                    }
                    case arraylength -> {
                        addToInstructionUseSet(firstCallOperand, instructionUse);
                    }
                    default -> {
                        if (invocationType != CallType.invokestatic)
                            addToInstructionUseSet(firstCallOperand, instructionUse);

                        for (Element el : callInstruction.getListOfOperands())
                            addToInstructionUseSet(el, instructionUse);
                    }
                }
            }
            case GOTO -> {
                GotoInstruction gotoInstruction = (GotoInstruction) i;
                instructionSuccessors.add(getIndexOfInstructionWithLabel(gotoInstruction.getLabel())); // only has the jump successor
                return false; // so it doesn't add the next instruction as successor
            }
            case BRANCH -> {
                CondBranchInstruction condBranchInstruction = (CondBranchInstruction) i;

                Operation operation = condBranchInstruction.getCondOperation();
                addToInstructionUseSet(condBranchInstruction.getLeftOperand(), instructionUse);
                if (operation.getOpType() != NOTB) {
                    addToInstructionUseSet(condBranchInstruction.getRightOperand(), instructionUse);
                }
                String label = condBranchInstruction.getLabel();
                instructionSuccessors.add(getIndexOfInstructionWithLabel(label)); // the successor in case of jump
            }
            case RETURN -> {
                ReturnInstruction returnInstruction = (ReturnInstruction) i;
                if (returnInstruction.hasReturnValue())
                    addToInstructionUseSet(returnInstruction.getOperand(), instructionUse);
            }
            case PUTFIELD -> {
                PutFieldInstruction putFieldInstruction = (PutFieldInstruction) i;
                Operand firstPutFieldOperand = (Operand) putFieldInstruction.getFirstOperand();
                Operand secondPutFieldOperand = (Operand) putFieldInstruction.getSecondOperand();

                addToInstructionUseSet(firstPutFieldOperand, instructionUse);
                addToInstructionUseSet(putFieldInstruction.getThirdOperand(), instructionUse);
            }
            case GETFIELD -> {
                GetFieldInstruction getFieldInstruction = (GetFieldInstruction) i;
                Operand firstGetFieldOperand = (Operand) getFieldInstruction.getFirstOperand();
                Operand secondGetFieldOperand = (Operand) getFieldInstruction.getSecondOperand();

                addToInstructionUseSet(firstGetFieldOperand, instructionUse);
            }
            case UNARYOPER -> {
                UnaryOpInstruction unaryOpInstruction = (UnaryOpInstruction) i;
                if (unaryOpInstruction.getUnaryOperation().getOpType() == NOTB) {
                    addToInstructionUseSet(unaryOpInstruction.getRightOperand(), instructionUse);
                } else {
                    Logger.err("!!! >> Unrecognized UnaryOpInstruction!!");
                }
            }
            case BINARYOPER -> {
                BinaryOpInstruction binaryOpInstruction = (BinaryOpInstruction) i;
                OperationType type = binaryOpInstruction.getUnaryOperation().getOpType();

                addToInstructionUseSet(binaryOpInstruction.getLeftOperand(), instructionUse);
                if (type != NOTB)
                    addToInstructionUseSet(binaryOpInstruction.getRightOperand(), instructionUse);
            }
            case NOPER -> {
                SingleOpInstruction singleOpInstruction = (SingleOpInstruction) i;
                Element el = singleOpInstruction.getSingleOperand();

                try {  // if it's array access
                    ArrayOperand operand = (ArrayOperand) el;
//                    try {
                        addToInstructionUseSet(operand, instructionUse);
                        addToInstructionUseSet(operand.getIndexOperands().get(0), instructionUse);
//                    } catch (Exception e) {
//                        Logger.err(e.getMessage() + "\n");
//                        e.printStackTrace();
//                    }
                } catch (Exception ignore) {
                    addToInstructionUseSet(singleOpInstruction.getSingleOperand(), instructionUse);
                }

            }
        }
        return true;
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
                .append(getStringInDesiredSpace("Successors", 30)).append("\n");

        for (int i = 0; i < use.size(); i++) {
            builder.append(getStringInDesiredSpace("Instruction " + i, 18)).append(" | ")
                    .append(getStringInDesiredSpace(getSetString(use.get(i)), 30)).append(" | ")
                    .append(getStringInDesiredSpace(getSetString(def.get(i)), 30)).append(" | ")
                    .append(getStringInDesiredSpace(getSetString(successors.get(i)), 30)).append("\n");
        }

        return builder.toString();
    }

}
