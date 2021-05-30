package optimization;

import org.specs.comp.ollir.*;
import utils.Logger;

import java.util.List;
import java.util.function.Consumer;

import static org.specs.comp.ollir.OperationType.NOTB;

public class OllirVariableFinder {

    public static int getIndexOfInstructionWithLabel(Method method, String label) throws Exception {
        List<Instruction> instructions = method.getInstructions();
        for (int i = 0; i < instructions.size(); i++) {
            List<String> instructionLabels = method.getLabels(instructions.get(i));
            for (String l : instructionLabels) if (l.equals(label)) return i;
        }
        throw new Exception("Couldn't find instruction with label " + label);
    }

    public static String getIdentifier(Element element) {
        if (element.isLiteral()) return null;

        Operand operand = (Operand) element;

        if (element.getType().getTypeOfElement() == ElementType.THIS || operand.getName().equals("this")) return null;

        return operand.getName();
    }

    public static void findInstruction(Method method, Consumer<FinderAlert> consumer, Instruction i, int idx, boolean last) throws Exception {
        if (findInstruction(method, consumer, i) && !last)
            consumer.accept(new FinderAlert(idx + 1));
    }

    /**
     * Fills the sets of a given instruction
     * @param i The instruction
     * @return Whether we need to add the next instruction to the successors
     * @throws Exception miscellaneous
     */
    public static boolean findInstruction(Method method, Consumer<FinderAlert> consumer, Instruction i) throws Exception {
        switch (i.getInstType()) {
            case ASSIGN -> {
                AssignInstruction assignInstruction = (AssignInstruction) i;
                Operand destOperand = (Operand) assignInstruction.getDest();


                try {
                    // best way I found to do array assign
                    ArrayOperand arrayOperand = (ArrayOperand) destOperand;
                    consumer.accept(new FinderAlert(arrayOperand.getIndexOperands().get(0), FinderAlert.FinderAlertType.USE));

                    Logger.log("Array first index operand (" + destOperand.getName() + ") = " + arrayOperand.getIndexOperands().get(0));

                    consumer.accept(new FinderAlert(destOperand, FinderAlert.FinderAlertType.USE));

                } catch (Exception ignored) {
                    consumer.accept(new FinderAlert(destOperand, FinderAlert.FinderAlertType.DEF));
                }

                findInstruction(method, consumer, assignInstruction.getRhs());
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
                            consumer.accept(new FinderAlert(callInstruction.getListOfOperands().get(0), FinderAlert.FinderAlertType.USE));
                        }
                    }
                    case arraylength -> {
                        consumer.accept(new FinderAlert(firstCallOperand, FinderAlert.FinderAlertType.USE));
                    }
                    default -> {
                        if (invocationType != CallType.invokestatic)
                            consumer.accept(new FinderAlert(firstCallOperand, FinderAlert.FinderAlertType.USE));

                        for (Element el : callInstruction.getListOfOperands())
                            consumer.accept(new FinderAlert(el, FinderAlert.FinderAlertType.USE));
                    }
                }
            }
            case GOTO -> {
                GotoInstruction gotoInstruction = (GotoInstruction) i;
                consumer.accept(new FinderAlert(getIndexOfInstructionWithLabel(method, gotoInstruction.getLabel()))); // only has the jump successor
                return false; // so it doesn't add the next instruction as successor
            }
            case BRANCH -> {
                CondBranchInstruction condBranchInstruction = (CondBranchInstruction) i;

                Operation operation = condBranchInstruction.getCondOperation();
                consumer.accept(new FinderAlert(condBranchInstruction.getLeftOperand(), FinderAlert.FinderAlertType.USE));

                if (operation.getOpType() != NOTB) {
                    consumer.accept(new FinderAlert(condBranchInstruction.getRightOperand(), FinderAlert.FinderAlertType.USE));
                }
                String label = condBranchInstruction.getLabel();
                consumer.accept(new FinderAlert(getIndexOfInstructionWithLabel(method, label)));
            }
            case RETURN -> {
                ReturnInstruction returnInstruction = (ReturnInstruction) i;
                if (returnInstruction.hasReturnValue())
                    consumer.accept(new FinderAlert(returnInstruction.getOperand(), FinderAlert.FinderAlertType.USE));
            }
            case PUTFIELD -> {
                PutFieldInstruction putFieldInstruction = (PutFieldInstruction) i;
                Operand firstPutFieldOperand = (Operand) putFieldInstruction.getFirstOperand();
                Operand secondPutFieldOperand = (Operand) putFieldInstruction.getSecondOperand();

                consumer.accept(new FinderAlert(firstPutFieldOperand, FinderAlert.FinderAlertType.USE));
                consumer.accept(new FinderAlert(putFieldInstruction.getThirdOperand(), FinderAlert.FinderAlertType.USE));
            }
            case GETFIELD -> {
                GetFieldInstruction getFieldInstruction = (GetFieldInstruction) i;
                Operand firstGetFieldOperand = (Operand) getFieldInstruction.getFirstOperand();
                Operand secondGetFieldOperand = (Operand) getFieldInstruction.getSecondOperand();

                consumer.accept(new FinderAlert(firstGetFieldOperand, FinderAlert.FinderAlertType.USE));
            }
            case UNARYOPER -> {
                UnaryOpInstruction unaryOpInstruction = (UnaryOpInstruction) i;
                if (unaryOpInstruction.getUnaryOperation().getOpType() == NOTB) {
                    consumer.accept(new FinderAlert(unaryOpInstruction.getRightOperand(), FinderAlert.FinderAlertType.USE));
                } else {
                    Logger.err("!!! >> Unrecognized UnaryOpInstruction!!");
                }
            }
            case BINARYOPER -> {
                BinaryOpInstruction binaryOpInstruction = (BinaryOpInstruction) i;
                OperationType type = binaryOpInstruction.getUnaryOperation().getOpType();

                consumer.accept(new FinderAlert(binaryOpInstruction.getLeftOperand(), FinderAlert.FinderAlertType.USE));
                if (type != NOTB)
                    consumer.accept(new FinderAlert(binaryOpInstruction.getRightOperand(), FinderAlert.FinderAlertType.USE));
            }
            case NOPER -> {
                SingleOpInstruction singleOpInstruction = (SingleOpInstruction) i;
                Element el = singleOpInstruction.getSingleOperand();

                try // if it's array access
                {
                    ArrayOperand operand = (ArrayOperand) el;

                    consumer.accept(new FinderAlert(operand, FinderAlert.FinderAlertType.USE));
                    consumer.accept(new FinderAlert(operand.getIndexOperands().get(0), FinderAlert.FinderAlertType.USE));
                }
                catch (Exception ignore)
                {
                    consumer.accept(new FinderAlert(singleOpInstruction.getSingleOperand(), FinderAlert.FinderAlertType.USE));
                }

            }
        }
        return true;
    }
}
