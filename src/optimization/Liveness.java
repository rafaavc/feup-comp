package optimization;

import jasmin.LocalVariable;
import org.specs.comp.ollir.*;
import utils.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Liveness {
    private final Method method;
    private final List<Set<String>> def = new ArrayList<>(),
        use = new ArrayList<>(),
        successors = new ArrayList<>(),
        in = new ArrayList<>(),
        out = new ArrayList<>();

    public Liveness(Method method) {
        this.method = method;
    }


    public LivenessResult get() {
        List<Instruction> instructions = method.getInstructions();
        for (Instruction instruction : instructions) {
            fillSets(instruction);
        }

        return new LivenessResult(in, out);
    }

    private String getIdentifier(Element element) {
        if (element.isLiteral()) return null;

        Operand operand = (Operand) element;

        if (element.getType().getTypeOfElement() == ElementType.THIS || operand.getName().equals("this")) return null;

        return operand.getName();
    }

    public void fillSets(Instruction i) {
        Set<String> instructionDef = new HashSet<>(), instructionUse = new HashSet<>(), instructionSuccessors = new HashSet<>();

        fillSets(i, instructionDef, instructionUse, instructionSuccessors);

        use.add(instructionUse);
        def.add(instructionDef);
        successors.add(instructionSuccessors);
    }

    public void fillSets(Instruction i, Set<String> instructionDef, Set<String> instructionUse, Set<String> instructionSuccessors) {
        switch (i.getInstType()) {
            case ASSIGN -> {
                AssignInstruction assignInstruction = (AssignInstruction) i;
                Operand destOperand = (Operand) assignInstruction.getDest();

                instructionDef.add(destOperand.getName());

                try {
                    // best way I found to do array assign
                    ArrayOperand arrayOperand = (ArrayOperand) destOperand;
//                    try {
                        String name = getIdentifier(arrayOperand.getIndexOperands().get(0));
                        if (name != null) instructionUse.add(name);

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
                    case ldc -> Logger.err("Received ldc or arraylength in invocation type (not supposed)");
                    case NEW -> {
                        if (firstCallOperand.getType().getTypeOfElement() == ElementType.ARRAYREF) {
                            loadElement(callInstruction.getListOfOperands().get(0), localVariable, sb);
                            sb.append("\tnewarray int\n");
                        } else {
                            sb.append("\tnew ").append(getClassNameRepresentation(firstCallOperand)).append("\n");
                        }
                    }
                    case invokespecial -> {
                        sb.append("\tinvokespecial ")
                                .append(getClassNameRepresentation(firstCallOperand))
                                .append("/").append(secondCallOperand.getLiteral().replace("\"", ""));

                        sb.append("(");
                        for (Element el : callInstruction.getListOfOperands()) sb.append(getElementType(el.getType()));
                        sb.append(")").append(getElementType(callInstruction.getReturnType())).append("\n");

                        sb.append(getStore("a", localVariable.getCorrespondence(firstCallOperand.getName())));
                    }
                    case arraylength -> {
                        loadElement(firstCallOperand, localVariable, sb);
                        sb.append("\tarraylength\n");
                    }
                    default -> {
                        if (firstCallOperand.getType().getTypeOfElement() == ElementType.OBJECTREF)
                            loadElement(firstCallOperand, localVariable, sb);
                        else if (firstCallOperand.getType().getTypeOfElement() == ElementType.THIS)
                            sb.append(getLoadThis());

                        for (Element el : callInstruction.getListOfOperands()) loadElement(el, localVariable, sb);

                        sb.append("\t").append(invocationType)
                                .append(" ")
                                .append(getClassNameRepresentation(firstCallOperand));

                        if (secondCallOperand != null)
                            sb.append("/").append(secondCallOperand.getLiteral().replace("\"", ""));

                        sb.append("(");
                        for (Element el : callInstruction.getListOfOperands()) sb.append(getElementType(el.getType()));
                        sb.append(")").append(getElementType(callInstruction.getReturnType())).append("\n");

                        if (callInstruction.getReturnType().getTypeOfElement() != ElementType.VOID
                                && !isRightSideOfAssignment) {
                            sb.append("\tpop\n");  // pops the return value off the stack
                        }
                    }
                }
            }
            case GOTO -> {
                GotoInstruction gotoInstruction = (GotoInstruction) i;
                sb.append("\tgoto ").append(gotoInstruction.getLabel()).append("\n");
            }
            case BRANCH -> {
                CondBranchInstruction condBranchInstruction = (CondBranchInstruction) i;

                Operation operation = condBranchInstruction.getCondOperation();
                loadElement(condBranchInstruction.getLeftOperand(), localVariable, sb);
                if (operation.getOpType() != NOTB) {
                    loadElement(condBranchInstruction.getRightOperand(), localVariable, sb);
                }
                sb.append(branchBuilder.buildBranchInstruction(operation, condBranchInstruction.getLabel()));
            }
            case RETURN -> {
                ReturnInstruction returnInstruction = (ReturnInstruction) i;
                if (returnInstruction.hasReturnValue()) {
                    loadElement(returnInstruction.getOperand(), localVariable, sb);
                    String typePrefix = getElementTypePrefix(returnInstruction.getOperand());
                    sb.append("\t").append(typePrefix);
                } else sb.append("\t");
                sb.append("return\n");
            }
            case PUTFIELD -> {
                PutFieldInstruction putFieldInstruction = (PutFieldInstruction) i;
                Operand firstPutFieldOperand = (Operand) putFieldInstruction.getFirstOperand();
                Operand secondPutFieldOperand = (Operand) putFieldInstruction.getSecondOperand();

                loadElement(firstPutFieldOperand, localVariable, sb);
                loadElement(putFieldInstruction.getThirdOperand(), localVariable, sb);

                sb.append("\tputfield ")
                        .append(getClassNameRepresentation(firstPutFieldOperand))
                        .append("/")
                        .append(secondPutFieldOperand.getName())
                        .append(" ")
                        .append(getElementType(putFieldInstruction.getSecondOperand().getType()))
                        .append("\n");
            }
            case GETFIELD -> {
                GetFieldInstruction getFieldInstruction = (GetFieldInstruction) i;
                Operand firstGetFieldOperand = (Operand) getFieldInstruction.getFirstOperand();
                Operand secondGetFieldOperand = (Operand) getFieldInstruction.getSecondOperand();

                loadElement(firstGetFieldOperand, localVariable, sb);

                sb.append("\tgetfield ")
                        .append(getClassNameRepresentation(firstGetFieldOperand))
                        .append("/")
                        .append(secondGetFieldOperand.getName())
                        .append(" ")
                        .append(getElementType(getFieldInstruction.getSecondOperand().getType()))
                        .append("\n");
            }
            case UNARYOPER -> {
                UnaryOpInstruction unaryOpInstruction = (UnaryOpInstruction) i;
                if (unaryOpInstruction.getUnaryOperation().getOpType() == NOTB) {
                    loadElement(unaryOpInstruction.getRightOperand(), localVariable, sb);
                    sb.append(getConst(1));
                    sb.append("\tixor\n");
                } else {
                    Logger.err("!!! >> Unrecognized UnaryOpInstruction!!");
                }
            }
            case BINARYOPER -> {
                BinaryOpInstruction binaryOpInstruction = (BinaryOpInstruction) i;
                OperationType type = binaryOpInstruction.getUnaryOperation().getOpType();

                loadElement(binaryOpInstruction.getLeftOperand(), localVariable, sb);
                if (type != NOTB) {
                    loadElement(binaryOpInstruction.getRightOperand(), localVariable, sb);
                    sb.append("\t");
                }

                String typePrefix = getElementTypePrefix(binaryOpInstruction.getLeftOperand());
                switch (type) {
                    case ADD -> sb.append(typePrefix).append("add");
                    case MUL -> sb.append(typePrefix).append("mul");
                    case SUB -> sb.append(typePrefix).append("sub");
                    case DIV -> sb.append(typePrefix).append("div");
                    case ANDB -> sb.append(typePrefix).append("and"); // bitwise and works because it is between 0 and 1 (booleans)
                    case LTH -> {
                        String trueLabel = getNextLabel(), continueLabel = getNextLabel();
                        sb.append(typePrefix).append("sub\n")
                                .append("\tiflt ").append(trueLabel).append("\n") // 1st is lt 2nd if their subtraction is less than 0
                                .append(getConst(0))
                                .append("\tgoto ").append(continueLabel).append("\n")
                                .append(trueLabel).append(":\n")
                                .append(getConst(1))
                                .append(continueLabel).append(":");
                    }
                    case NOTB -> {
                        sb.append("\tldc 1\n");
                        sb.append("\tixor");
                    }
                }
                sb.append("\n");
            }
            case NOPER -> {
                SingleOpInstruction singleOpInstruction = (SingleOpInstruction) i;
                Element el = singleOpInstruction.getSingleOperand();

                try {  // if it's array access
                    ArrayOperand operand = (ArrayOperand) el;
                    try {
                        operand.setType(new Type(ElementType.ARRAYREF));
                        loadElement(operand, localVariable, sb);
                        loadElement(operand.getIndexOperands().get(0), localVariable, sb);
                        sb.append("\tiaload\n");
                    } catch (Exception e) {
                        Logger.err(e.getMessage() + "\n");
                        e.printStackTrace();
                    }
                } catch (Exception ignore) {
                    loadElement(singleOpInstruction.getSingleOperand(), localVariable, sb);
                }

            }
        }
    }
}
