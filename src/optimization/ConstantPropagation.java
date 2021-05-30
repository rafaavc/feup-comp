package optimization;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConstantPropagation {
    public OllirResult optimize(JmmSemanticsResult semanticsResult, OllirResult ollirResult) {
        findAndReplaceConstants(ollirResult);
        return ollirResult;
    }

    private List<String> findAndReplaceConstants(OllirResult ollirResult) {
        List<String> constants = new ArrayList<>();
        String ollirCode = ollirResult.getOllirCode();
        ClassUnit ollirClass = ollirResult.getOllirClass();

        // check first assignment to a constant
        // check if variable is used until all the next cases where it is assigned
        // remove assign instruction in method
        // replace all instances of variable in method

        String[] methods = ollirCode.split(".method");

        // first result of split is not a method
        List<String> newMethods = new ArrayList<>();
        for (int i = 1; i < methods.length; i++) {
            String processResult = "";
            String result;
            do {
                result = processResult;
                processResult = processMethod(methods[i], ollirClass.getMethods().get(i - 1));
            } while (!processResult.equals(""));
            if (!result.equals("")) newMethods.add(result);
            newMethods.add(methods[i]);
        }

        return constants;
    }

    private String processMethod(String methodCode, Method method) {
        List<String> constants = findConstants(method);
        if (constants.size() == 0) return "";

        return "";
    }

    private Operand getAssignLeftOperand(AssignInstruction assignInstruction) {
        return (Operand) assignInstruction.getDest();
    }

    private String getAssignRightOperand(AssignInstruction assignInstruction) {
        Instruction instruction = assignInstruction.getRhs();
        if (instruction.getInstType() != InstructionType.NOPER) return null;
        SingleOpInstruction singleOpInstruction = (SingleOpInstruction) instruction;
        Element element = singleOpInstruction.getSingleOperand();

        if (!element.isLiteral()) return null;
        return ((LiteralElement) element).getLiteral();
    }

    private List<String> findConstants(Method method) {
        HashMap<String, Descriptor> varTable = method.getVarTable();
        ArrayList<Instruction> listOfInstr = method.getInstructions();
        List<String> constants = new ArrayList<>();

        for (String varName : varTable.keySet()) {
            ElementType elementType = varTable.get(varName).getVarType().getTypeOfElement();
            if (elementType != ElementType.INT32 && elementType != ElementType.BOOLEAN) continue;
            boolean used = false;
            boolean checkConstant = true;
            String constValue = "";

            for (Instruction instruction : listOfInstr) {
                if (instruction.getInstType() == InstructionType.ASSIGN) {
                    AssignInstruction assignInstruction = (AssignInstruction) instruction;
                    String rightOperand = getAssignRightOperand(assignInstruction);
                    Operand leftOperand = getAssignLeftOperand(assignInstruction);

                    if (leftOperand.getName().equals(varName)) {
                        if (rightOperand == null) checkConstant = false;
                        else if (used) checkConstant = false;
                        else constValue = rightOperand;
                    } else if (utilizada) {
                        used = true;
                    }
                } else if (utilizada) {
                    used = true;
                }
            }

            if (checkConstant) constants.add(varName + "-" + constValue);
        }
        return constants;
    }

    private String removeConstants(String ollirCode, List<String> constants) {
        return "";
    }
}
