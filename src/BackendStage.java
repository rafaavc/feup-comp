import java.util.*;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import utils.Logger;
import jasmin.LocalVariable;

import org.specs.comp.ollir.*;

import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;


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
    private SymbolTable symbolTable = null;

    private String getNextLabel() {
        String tmp = "label" + nextLabel;
        nextLabel++;
        return tmp;
    }

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();
        classUnit = ollirClass;
        symbolTable = ollirResult.getSymbolTable();
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
            .append(classUnit.getClassName())
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
        code.append(".super ").append(superClass).append("\n");

        for (Field field : classUnit.getFields()) addField(field, code);

        for (Method m : classUnit.getMethods()) {
            if (m.isConstructMethod()) {
                code.append(buildConstructor(superClass));
                continue;
            }
            code.append(buildMethodDeclaration(m)).append("\n");

            code.append("\t.limit locals 99\n");
            code.append("\t.limit stack 99\n");

            LocalVariable localVariable = new LocalVariable(m.getParams());
            List<Instruction> instructions = m.getInstructions();
            for (Instruction i : instructions) {
                StringBuilder sb = new StringBuilder();
                buildInstruction(i, localVariable, sb);
                code.append(sb);
            }

            if (instructions.get(instructions.size()-1).getInstType() != InstructionType.RETURN) {
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
                "\taload_0" + "\n" +
                "\tinvokenonvirtual " + superClass + "/<init>()V" + "\n" +
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

    private void loadElement(Element element, LocalVariable localVariable, StringBuilder sb) {Type elementType = element.getType();
        if (element.isLiteral()) {
            if (isPrimitive(elementType)) { //TODO: verify if is needed
                sb.append("\tldc ").append(((LiteralElement) element).getLiteral()).append("\n");
            } else {
                System.out.println("!!! >> Found element that is literal but is not primitive??");
            }
            return;
        }
        Operand operand = (Operand) element;
        System.out.println("Is operand, name = " + operand.getName());
        String typePrefix = getElementTypePrefix(element);
        sb.append("\t").append(typePrefix).append("load ").append(localVariable.getCorrespondence(operand.getName())).append("\n");

    }

    private String getClassNameWithImport(String className) {
        for (String imp : symbolTable.getImports()) {
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

    private void buildInstruction(Instruction i, LocalVariable localVariable, StringBuilder sb) {
        i.show();
        switch (i.getInstType()) {
            case ASSIGN -> {
                AssignInstruction assignInstruction = (AssignInstruction) i;
                int variable = localVariable.getNextLocalVariable();
                String identifierName = ((Operand) assignInstruction.getDest()).getName();
                localVariable.addCorrespondence(identifierName, variable);
                System.out.println("Building " + assignInstruction.getRhs());
                buildInstruction(assignInstruction.getRhs(), localVariable, sb);
                if (assignInstruction.getRhs().getInstType() == InstructionType.CALL && ((CallInstruction)assignInstruction.getRhs()).getInvocationType() == CallType.NEW) {
                    sb.append("\tdup\n");
                }
                else {
                    sb.append("\t").append(getElementTypePrefix(assignInstruction.getDest())).append("store ").append(variable).append("\n");
                }
            }
            case CALL -> {
                CallInstruction callInstruction = (CallInstruction) i;

                Operand firstCallOperand = (Operand) callInstruction.getFirstArg();
                LiteralElement secondCallOperand = (LiteralElement) callInstruction.getSecondArg();

                CallType invocationType = callInstruction.getInvocationType();

                switch(invocationType) {
                    case ldc, arraylength -> Logger.err("Received ldc or arraylength in invocation type (not supposed)");
                    case NEW -> sb.append("\tnew ").append(getClassNameRepresentation(firstCallOperand)).append("\n");
                    case invokespecial -> {
                        sb.append("\tinvokespecial ")
                                .append(getClassNameRepresentation(firstCallOperand))
                                .append("/").append(secondCallOperand.getLiteral().replace("\"", ""));

                        sb.append("(");
                        for (Element el : callInstruction.getListOfOperands()) sb.append(getElementType(el.getType()));
                        sb.append(")").append(getElementType(callInstruction.getReturnType())).append("\n");

                        sb.append("\tastore ").append(localVariable.getCorrespondence(firstCallOperand.getName())).append("\n");
                    }
                    default -> {
                        if (firstCallOperand.getType().getTypeOfElement() == ElementType.OBJECTREF)
                            loadElement(firstCallOperand, localVariable, sb);
                        else if (firstCallOperand.getType().getTypeOfElement() == ElementType.THIS)
                            sb.append("\taload 0\n");

                        for (Element el : callInstruction.getListOfOperands()) loadElement(el, localVariable, sb);

                        sb.append("\t").append(invocationType)
                                .append(" ")
                                .append(getClassNameRepresentation(firstCallOperand));

                        if (secondCallOperand != null)
                            sb.append("/").append(secondCallOperand.getLiteral().replace("\"", ""));

                        sb.append("(");
                        for (Element el : callInstruction.getListOfOperands()) sb.append(getElementType(el.getType()));
                        sb.append(")").append(getElementType(callInstruction.getReturnType())).append("\n");
                    }
                }
            }
            case GOTO, BRANCH -> Logger.err("Not for checkpoint 2");
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
                loadElement(putFieldInstruction.getThirdOperand(), localVariable, sb);
                Operand firstPutFieldOperand = (Operand) putFieldInstruction.getFirstOperand();
                Operand secondPutFieldOperand = (Operand) putFieldInstruction.getSecondOperand();
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
                if (unaryOpInstruction.getUnaryOperation().getOpType() == OperationType.NOTB) {
                    System.out.println("FOund NOTB!!!");
                    loadElement(unaryOpInstruction.getRightOperand(), localVariable, sb);
                    sb.append("\tldc 1\n");
                    sb.append("\tixor\n");
                } else {
                    Logger.err("!!! >> Unrecognized UnaryOpInstruction!!");
                }
            }
            case BINARYOPER -> {
                BinaryOpInstruction binaryOpInstruction = (BinaryOpInstruction) i;
                loadElement(binaryOpInstruction.getLeftOperand(), localVariable, sb);
                loadElement(binaryOpInstruction.getRightOperand(), localVariable, sb);
                String typePrefix = getElementTypePrefix(binaryOpInstruction.getLeftOperand());
                sb.append("\t");
                switch (binaryOpInstruction.getUnaryOperation().getOpType()) {
                    case ADD -> {
                        sb.append(typePrefix).append("add");
                    }
                    case MUL -> sb.append(typePrefix).append("mul");
                    case SUB -> sb.append(typePrefix).append("sub");
                    case DIV -> sb.append(typePrefix).append("div");
                    case ANDB -> sb.append(typePrefix).append("and"); // bitwise and works because it is between 0 and 1 (booleans)
                    case LTH -> {
                        String trueLabel = getNextLabel(), continueLabel = getNextLabel();
                        sb.append(typePrefix).append("sub\n")
                                .append("\tiflt ").append(trueLabel).append("\n") // 1st is lt 2nd if their subtraction is less than 0
                                .append("\tldc 0\n")
                                .append("\tgoto ").append(continueLabel).append("\n")
                                .append(trueLabel).append(":\n")
                                .append("\tldc 1\n")
                                .append(continueLabel).append(":");
                    }
                }
                sb.append("\n");
            }
            case NOPER -> {
                SingleOpInstruction singleOpInstruction = (SingleOpInstruction) i;
                System.out.println("TYPE: " + singleOpInstruction.getSingleOperand().getType());
                loadElement(singleOpInstruction.getSingleOperand(), localVariable, sb);
            }
        }

    }

    private String getElementType(Type e) {
        ElementType eType = e.getTypeOfElement();

        return switch (eType) {
            case INT32 -> "I";
            case BOOLEAN -> "I";
            case CLASS, THIS, STRING -> "whhaaat";
            case ARRAYREF -> "[Ljava/lang/String;";  // TODO
            case VOID -> "V";
            case OBJECTREF -> "L" + "TODO (this should be classname)" + ";"; // TODO
        };
    }

    private String getElementTypePrefix(Element element) {
        return switch(element.getType().getTypeOfElement()) {
            case INT32 -> "i";
            case BOOLEAN -> "i";
            case ARRAYREF -> "a";
            case OBJECTREF -> "a"; // ?
            default -> null;
        };
    }

    private boolean isPrimitive(Type elementType) {
        return elementType.getTypeOfElement().equals(ElementType.INT32) ||
                elementType.getTypeOfElement().equals(ElementType.BOOLEAN);
    }

}
