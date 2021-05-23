import java.awt.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;

import jasmin.BranchBuilder;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import utils.Logger;
import jasmin.LocalVariable;

import org.specs.comp.ollir.*;

import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import static org.specs.comp.ollir.OperationType.NOTB;


/**
 * Copyright 2021 SPeCS.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

public class BackendStage implements JasminBackend {
    private int nextLabel = 1;
    private ClassUnit classUnit = null;
    private final BranchBuilder branchBuilder = new BranchBuilder();

    private String getNextLabel() {
        String tmp = "label" + nextLabel;
        nextLabel++;
        return tmp;
    }

    private String getConst(int value) {
        if (value >= -1 && value <= 5) return "\ticonst_" + (value == -1 ? "m1" : value) + "\n";
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(value);

        if ((value >> 7) == 0) {
            Logger.log(value + " is less than 1 byte");
            return "\tbipush " + value + "\n";
        }
        return "\tldc " + value + "\n";
    }

    private String getLoad(String typePrefix, int variable) {
        assert variable >= 0;
        return "\t" + typePrefix + "load" + (variable <= 3 ? "_" : " ") + variable + "\n";
    }

    private String getLoadThis() {
        return getLoad("a", 0);
    }

    private String getStore(String typePrefix, int variable) {
        assert variable >= 0;
        return "\t" + typePrefix + "store" + (variable <= 3 ? "_" : " ") + variable + "\n";
    }

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();
        classUnit = ollirClass;
        SymbolTable symbolTable = ollirResult.getSymbolTable();
        try {

            // Example of what you can do with the OLLIR class
            ollirClass.checkMethodLabels(); // check the use of labels in the OLLIR loaded
            ollirClass.buildCFGs(); // build the CFG of each method
            ollirClass.outputCFGs(); // output to .dot files the CFGs, one per method
            ollirClass.buildVarTables(); // build the table of variables for each method
            ollirClass.show(); // print to console main information about the input OLLIR

            // Convert the OLLIR to a String containing the equivalent Jasmin code
            String jasminCode = getJasminCode();

            // More reports from this stage
            List<Report> reports = new ArrayList<>();

            return new JasminResult(ollirResult, jasminCode, reports);

        } catch (OllirErrorException e) {
            return new JasminResult(ollirClass.getClassName(), null,
                    Collections.singletonList(Report.newError(Stage.GENERATION, -1, -1, "Exception during Jasmin generation", e)));
        }

    }

    private void addField(Field field, StringBuilder sb) {
        sb.append(".field ")
                .append(getAccessType(field.getFieldAccessModifier()))
                .append(" ")
                .append(field.getFieldName())
                .append(" ")
                .append(getElementType(field.getFieldType()))
                .append("\n");
    }

    private String getJasminCode() {
        StringBuilder code = new StringBuilder();

        code.append(".class ").append(getAccessType(classUnit.getClassAccessModifier())).append(" ");
        code.append(classUnit.getClassName()).append("\n");

        String superClass = classUnit.getSuperClass();
        if (superClass == null) superClass = "java/lang/Object";
        code.append(".super ").append(getClassNameWithImport(superClass)).append("\n");

        for (Field field : classUnit.getFields()) addField(field, code);

        for (Method m : classUnit.getMethods()) {
            if (m.isConstructMethod()) {
                code.append(buildConstructor(getClassNameWithImport(superClass)));
                continue;
            }
            code.append(buildMethodDeclaration(m)).append("\n");

            code.append("\t.limit locals 105\n");
            code.append("\t.limit stack 105\n");

            LocalVariable localVariable = new LocalVariable(m.getParams());
            List<Instruction> instructions = m.getInstructions();
            for (Instruction i : instructions) {
                StringBuilder sb = new StringBuilder();
                for (String s : m.getLabels(i)) sb.append(s).append(":\n");
                buildInstruction(i, localVariable, sb, false);
                code.append(sb);
            }

            if (instructions.get(instructions.size() - 1).getInstType() != InstructionType.RETURN) {
                code.append("\treturn\n");
            }

            code.append(".end method\n");
        }

        System.out.println("Printing jasmin..");
        System.out.println(code);

        return code.toString();
    }

    private String getAccessType(AccessModifiers modifier) {
        if (modifier == AccessModifiers.DEFAULT) modifier = AccessModifiers.PUBLIC;
        return modifier.toString().toLowerCase();
    }

    private String buildConstructor(String superClass) {
        return ".method " + getAccessType(classUnit.getClassAccessModifier()) + " <init>()V" + "\n" +
                getLoadThis() +
                "\tinvokespecial " + superClass + "/<init>()V" + "\n" +
                "\treturn" + "\n" +
                ".end method" + "\n";
    }

    private String buildMethodDeclaration(Method m) {
        StringBuilder declaration = new StringBuilder(".method " + getAccessType(m.getMethodAccessModifier()) + " ");

        if (m.isStaticMethod()) declaration.append("static ");
        if (m.isFinalMethod()) declaration.append("final ");

        declaration.append(m.getMethodName()).append("(");

        for (Element e : m.getParams())
            declaration.append(getElementType(e.getType()));

        declaration.append(")").append(getElementType(m.getReturnType()));
        return declaration.toString();
    }

    private void loadElement(Element element, LocalVariable localVariable, StringBuilder sb) {
        Type elementType = element.getType();
        if (element.isLiteral()) {
            if (isPrimitive(elementType)) {
                // There are only int primitives
                assert element instanceof LiteralElement;
                LiteralElement literalElement = (LiteralElement) element;
                sb.append(getConst(Integer.parseInt(literalElement.getLiteral())));
            } else {
                System.out.println("!!! >> Found element that is literal but is not primitive??");
            }
            return;
        }

        if (element.getType().getTypeOfElement() == ElementType.THIS) {
            sb.append(getLoadThis());
            return;
        }
        Operand operand = (Operand) element;

        System.out.println("Loading element from local variables...");
        localVariable.log();
        System.out.println("Is operand, name = " + operand.getName());
        String typePrefix = getElementTypePrefix(element);
        Logger.log("Trying to find " + operand.getName());
        sb.append(getLoad(typePrefix, localVariable.getCorrespondence(operand.getName())));

    }

    private String getClassNameWithImport(String className) {
        for (String imp : classUnit.getImports()) {
            if (imp.endsWith(className)) {
                return imp.replace('.', '/');
            }
        }
        return className;
    }

    private String getClassNameRepresentation(Operand operand) {
        if (operand.getName().equals("this")) {
            return classUnit.getClassName();
        }
        if (operand.getType().getTypeOfElement() == ElementType.CLASS) {
            return getClassNameWithImport(operand.getName());
        }
        return getClassNameWithImport(((ClassType) operand.getType()).getName());
    }

    private void buildInstruction(Instruction i, LocalVariable localVariable, StringBuilder sb, boolean isRightSideOfAssignment) {
        i.show();

        switch (i.getInstType()) {
            case ASSIGN -> {
                AssignInstruction assignInstruction = (AssignInstruction) i;
                Operand destOperand = (Operand) assignInstruction.getDest();
                String identifierName = destOperand.getName();

                Integer variable = localVariable.getCorrespondence(identifierName);
                if (variable == null) {
                    variable = localVariable.getNextLocalVariable();
                    localVariable.addCorrespondence(identifierName, variable);
                }

                try {
                    // best way I found to do array assign
                    ArrayOperand arrayOperand = (ArrayOperand) destOperand;
                    try {
                        sb.append(getLoad("a", variable));
                        loadElement(arrayOperand.getIndexOperands().get(0), localVariable, sb);

                        buildInstruction(assignInstruction.getRhs(), localVariable, sb, true);

                        sb.append("\tiastore\n");
                    } catch (Exception e) {
                        Logger.err(e.getMessage() + "\n");
                        e.printStackTrace();
                    }
                } catch(Exception ignored) {
                    buildInstruction(assignInstruction.getRhs(), localVariable, sb, true);

                    if (assignInstruction.getRhs().getInstType() == InstructionType.CALL &&
                            ((CallInstruction) assignInstruction.getRhs()).getInvocationType() == CallType.NEW &&
                            ((CallInstruction) assignInstruction.getRhs()).getFirstArg().getType().getTypeOfElement() != ElementType.ARRAYREF) { // checking if it's not array instantiation
                        sb.append("\tdup\n");
                    } else {
                        sb.append(getStore(getElementTypePrefix(assignInstruction.getDest()), variable));
                    }
                }
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

    private String getElementType(Type type) {
        return switch (type.getTypeOfElement()) {
            case INT32, BOOLEAN -> "I";
            case THIS -> "whaaaat";
            case STRING -> "Ljava/lang/String;";
            case CLASS, OBJECTREF -> "L" + getClassNameWithImport(((ClassType) type).getName()) + ";";
            case ARRAYREF -> {
                ArrayType arrayType = (ArrayType) type;
                yield "[" + getElementType(new Type(arrayType.getTypeOfElements()));
            }
            case VOID -> "V";
        };
    }

    private String getElementTypePrefix(Element element) {
        return switch(element.getType().getTypeOfElement()) {
            case INT32, BOOLEAN -> "i";
            case ARRAYREF, OBJECTREF -> "a";
            default -> null;
        };
    }

    private boolean isPrimitive(Type elementType) {
        return elementType.getTypeOfElement().equals(ElementType.INT32) ||
                elementType.getTypeOfElement().equals(ElementType.BOOLEAN);
    }

}
