import java.util.*;

import utils.Logger;

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

    private static class LocalVariable {
        private int nextLocalVariable = 0;
        private Map<String, Integer> identifiers = new HashMap<>();

        public LocalVariable(ArrayList<Element> parameters) {
            for (Element parameter : parameters) {
                addCorrespondence(((Operand)parameter).getName(), getNextLocalVariable());
            }

        }

        public int getNextLocalVariable() {
            int tmp = nextLocalVariable;
            nextLocalVariable++;
            return tmp;
        }

        public void addCorrespondence(String identifier, int localVariable) {
            identifiers.put(identifier, localVariable);
        }

        public int getCorrespondence(String identifier) {
            return identifiers.get(identifier);
        }
    }

    private int nextLabel = 1;
    private ClassUnit classUnit = null;

    private String getNextLabel() {
        String tmp = "label" + nextLabel;
        nextLabel++;
        return tmp;
    }

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();
        classUnit = ollirClass;
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

    private String getJasminCode() {
        StringBuilder code = new StringBuilder();

        code.append(".class ").append(classUnit.getClassAccessModifier().toString()).append(" ");
        code.append(classUnit.getClassName()).append("\n");

        String superClass = classUnit.getSuperClass();
        if (superClass == null) superClass = "java/lang/Object";
        code.append(".super ").append(superClass).append("\n");

        for (Field field : classUnit.getFields()) {
            code.append(".field ")
                    .append(field.getFieldAccessModifier())
                    .append(" ")
                    .append(classUnit.getClassName())
                    .append(" ")
                    .append(field.getFieldName())
                    .append(" ")
                    .append(getElementType(field.getFieldType()))
                    .append("\n");
        }

        code.append(buildConstructor());

        for (Method m : classUnit.getMethods()) {
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

    private String buildConstructor() {
        return ".method " + classUnit.getClassAccessModifier().toString() + " <init>()V" + "\n" +
                "\taload_0" + "\n" +
                "\tinvokenonvirtual java/lang/Object/<init>()V" + "\n" +
                "\treturn" + "\n" +
                ".end method" + "\n";
    }

    private String buildMethodDeclaration(Method m) {
        StringBuilder declaration = new StringBuilder(".method " + m.getMethodAccessModifier().toString() + " ");

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

    private void buildInstruction(Instruction i, LocalVariable localVariable, StringBuilder sb) {
        i.show();
        switch (i.getInstType()) {
            case ASSIGN -> {
                AssignInstruction assignInstruction = (AssignInstruction) i;
                System.out.println("DEST = " + assignInstruction.getDest());
                System.out.println("RHS = " + assignInstruction.getRhs());
                System.out.println();
                int variable = localVariable.getNextLocalVariable();
                String identifierName = ((Operand) assignInstruction.getDest()).getName();
                localVariable.addCorrespondence(identifierName, variable);
                System.out.println("Building " + assignInstruction.getRhs());
                buildInstruction(assignInstruction.getRhs(), localVariable, sb);
                sb.append("\t").append(getElementTypePrefix(assignInstruction.getDest())).append("store ").append(variable).append("\n");
            }
            case CALL -> {
                CallInstruction callInstruction = (CallInstruction) i;
                System.out.println("What's on the operand list: ");
                System.out.println("FIRST ARG: " + callInstruction.getFirstArg());
                System.out.println("SECOND ARG: " + callInstruction.getSecondArg());
                for (Element el : callInstruction.getListOfOperands()) {
                    System.out.println("---");
                    System.out.println(el);
                    System.out.println("---");
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
                        .append(firstPutFieldOperand.getName().equals("this") ? classUnit.getClassName() : firstPutFieldOperand.getName())
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
                        .append(firstGetFieldOperand.getName().equals("this") ? classUnit.getClassName() : secondGetFieldOperand.getName())
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
            case BOOLEAN, CLASS, THIS, STRING -> "whhaaat";
            case ARRAYREF -> "[";
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
