package optimization;

import constants.Ollir;
import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConstantPropagation {
    public OllirResult optimize(JmmSemanticsResult semanticsResult, OllirResult ollirResult) {
        String newCode = String.join(".method", findAndReplaceConstants(ollirResult));

        System.out.println("## Got the ollir code after constant propagation:\n");
        System.out.println(newCode);

        return new OllirResult(semanticsResult, newCode, semanticsResult.getReports());
    }

    private String[] findAndReplaceConstants(OllirResult ollirResult) {
        String ollirCode = ollirResult.getOllirCode();
        ClassUnit ollirClass = ollirResult.getOllirClass();

        String[] methods = ollirCode.split(".method");

        for (int i = 1; i < methods.length; i++) {
            String processResult;
            String result = "";

            while (true) {
                processResult = processMethod(methods[i], ollirClass.getMethods().get(i));
                if (processResult.equals("")) break;

                methods[i] = processResult;
                String newCode = String.join(".method", methods);

                System.out.println("## NEW CODE\n");
                System.out.println(newCode);

                ollirResult = new OllirResult(newCode);
                ollirClass = ollirResult.getOllirClass();
                result = processResult;
            }
            if (!result.equals("")) methods[i] = result;
        }

        return methods;
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
        HashMap<String, Descriptor> varTable = method.getVarTable();
        ArrayList<Instruction> listOfInstr = method.getInstructions();
        List<String> constants = new ArrayList<>();
        HashMap<String, Element> locals = findLocalVars(method);

        if (shouldEnd(method)) return constants;

        for (String varName : locals.keySet()) {
            Element var = locals.get(varName);

            ElementType elementType = var.getType().getTypeOfElement();
            if (elementType != ElementType.INT32 && elementType != ElementType.BOOLEAN) continue;
            AtomicBoolean used = new AtomicBoolean(false);
            AtomicBoolean checkConstant = new AtomicBoolean(true);
            String constValue = "";

            for (Instruction instruction : listOfInstr) {
                try {
                    OllirVariableFinder.findInstruction((FinderAlert alert) -> {
                        String name = OllirVariableFinder.getIdentifier(alert.getElement());
                        if (name == null) return;
                        FinderAlert.FinderAlertType type = alert.getType();
                        if (type == FinderAlert.FinderAlertType.USE && name.equals(varName)) {
                            used.set(true);
                            if (alert.isArrayAccess()) checkConstant.set(false);
                        }
                    }, instruction);
                } catch (Exception ignored) {
                }

                if (instruction.getInstType() == InstructionType.ASSIGN) {
                    AssignInstruction assignInstruction = (AssignInstruction) instruction;
                    String rightOperand = getAssignRightOperand(assignInstruction);
                    Operand leftOperand = getAssignLeftOperand(assignInstruction);

                    if (leftOperand.getName().equals(varName)) {
                        if (rightOperand == null) checkConstant.set(false);
                        else if (used.get()) checkConstant.set(false);
                        else constValue = rightOperand;
                    }
                }
            }

            if (checkConstant.get()) {
                String type = (elementType == ElementType.INT32) ? ".i32" : ".bool";
                constants.add(varName + type + "-" + constValue + type);
            }
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
        List<String> instructions = new ArrayList<>(Arrays.asList(ollirCode.split("\n")));

        for (int i = 0; i < instructions.size(); i++) {
            for (String constant : constants) {
                String remove = constant.split("-")[0];
                String add = constant.split("-")[1];
                if (instructions.get(i).contains(remove + " :=")) {
                    instructions.remove(i--);
                }
                instructions.set(i, instructions.get(i).replace(remove, add));
            }
        }

        return String.join("\n", instructions);
    }

    private boolean shouldEnd(Method method) {
        ArrayList<Instruction> listOfInstr = method.getInstructions();
        for (Instruction instruction : listOfInstr) {
            if (instruction.getInstType() == InstructionType.BRANCH) {
                CondBranchInstruction condBranchInstruction = (CondBranchInstruction) instruction;
                String branchName = condBranchInstruction.getLabel();
                if (branchName.contains(Ollir.ifBody)) return true;
            }
        }
        return false;
    }
}
