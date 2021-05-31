package optimization;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConstantPropagation {
    public OllirResult optimize(JmmSemanticsResult semanticsResult, OllirResult ollirResult) {
        findAndReplaceConstants(ollirResult);
        return ollirResult;
    }

    private List<String> findAndReplaceConstants(OllirResult ollirResult) {
        List<String> constants = new ArrayList<>();
        String ollirCode = ollirResult.getOllirCode();
        ClassUnit ollirClass = ollirResult.getOllirClass();

        String[] methods = ollirCode.split(".method");

        // first result of split is not a method
        List<String> newMethods = new ArrayList<>();
        System.out.println("### OLLIR METHODS LENGTH: " + ollirClass.getMethods().size());
        for (int i = 1; i < methods.length; i++) {
            String processResult = "";
            String result;
            do {
                result = processResult;
                processResult = processMethod(methods[i], ollirClass.getMethods().get(i));
            } while (!processResult.equals(""));
            if (!result.equals("")) newMethods.add(result);
            newMethods.add(methods[i]);
        }

        return constants;
    }

    private String processMethod(String methodCode, Method method) {
        List<String> constants = findConstants(method);
        if (constants.size() == 0) return "";
        return removeConstants(methodCode, constants);
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
        System.out.println("## METHOD NAME: " + method.getMethodName());
        HashMap<String, Descriptor> varTable = method.getVarTable();
        ArrayList<Instruction> listOfInstr = method.getInstructions();
        List<String> constants = new ArrayList<>();
        HashMap<String, Element> locals = findLocalVars(method);

        System.out.println("## Before calling varTable: " + varTable.keySet().size());
        for (String varName : locals.keySet()) {
            Element var = locals.get(varName);
            System.out.println("## VAR NAME: " + varName);

            ElementType elementType = var.getType().getTypeOfElement();
            if (elementType != ElementType.INT32 && elementType != ElementType.BOOLEAN) continue;
            System.out.println("### Is bool or int");
            AtomicBoolean used = new AtomicBoolean(false);
            boolean checkConstant = true;
            String constValue = "";

            for (Instruction instruction : listOfInstr) {
                System.out.print("#### Instruction: ");
                instruction.show();

                try {
                    OllirVariableFinder.findInstruction((FinderAlert alert) -> {
                        String name = OllirVariableFinder.getIdentifier(alert.getElement());
                        if (name == null) return;
                        FinderAlert.FinderAlertType type = alert.getType();
                        if (type == FinderAlert.FinderAlertType.USE && name.equals(varName)) {
                            System.out.println("#### is being used");
                            used.set(true);
                        }
                    }, instruction);
                } catch (Exception ignored) {
                }

                if (instruction.getInstType() == InstructionType.ASSIGN) {
                    AssignInstruction assignInstruction = (AssignInstruction) instruction;
                    String rightOperand = getAssignRightOperand(assignInstruction);
                    Operand leftOperand = getAssignLeftOperand(assignInstruction);

                    if (leftOperand.getName().equals(varName)) {
                        System.out.println("#### is being assigned");
                        if (rightOperand == null) {
                            System.out.println("#### not assigned to a constant");
                            checkConstant = false;
                        }
                        else if (used.get()) {
                            System.out.println("#### was used before assign");
                            checkConstant = false;
                        }
                        else {
                            System.out.println("#### got right value");
                            constValue = rightOperand;
                        }
                    }
                }
            }

            if (checkConstant) constants.add(varName + "-" + constValue);
        }
        return constants;
    }

    private HashMap<String, Element> findLocalVars(Method method) {
        ArrayList<Instruction> listOfInstr = method.getInstructions();
        HashMap<String, Element> locals = new HashMap<>();

        for (Instruction instruction : listOfInstr) {
            try {
                OllirVariableFinder.findInstruction((FinderAlert alert) -> {
                    FinderAlert.FinderAlertType type = alert.getType();
                    String name = OllirVariableFinder.getIdentifier(alert.getElement());
                    if (type == FinderAlert.FinderAlertType.DEF && name != null) {
                        locals.put(name, alert.getElement());
                    }
                }, instruction);
            } catch (Exception ignored) {
            }
        }

        return locals;
    }

    private String removeConstants(String ollirCode, List<String> constants) {
        String[] instructions = ollirCode.split("\n");

        for (String instruction : instructions) {
            for (String constant : constants) {
            }
        }

        return "";
    }
}
