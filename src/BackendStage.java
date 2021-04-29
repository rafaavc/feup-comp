import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.specs.comp.ollir.*;

import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;


/**
 * Copyright 2021 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

public class BackendStage implements JasminBackend {

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();

        try {

            // Example of what you can do with the OLLIR class
            ollirClass.checkMethodLabels(); // check the use of labels in the OLLIR loaded
            ollirClass.buildCFGs(); // build the CFG of each method
            ollirClass.outputCFGs(); // output to .dot files the CFGs, one per method
            ollirClass.buildVarTables(); // build the table of variables for each method
            ollirClass.show(); // print to console main information about the input OLLIR

            // Convert the OLLIR to a String containing the equivalent Jasmin code
            String jasminCode = getJasminCode(ollirClass); // Convert node ...

            // More reports from this stage
            List<Report> reports = new ArrayList<>();

            return new JasminResult(ollirResult, jasminCode, reports);

        } catch (OllirErrorException e) {
            return new JasminResult(ollirClass.getClassName(), null,
                    Collections.singletonList(Report.newError(Stage.GENERATION, -1, -1, "Exception during Jasmin generation", e)));
        }

    }

    private String getJasminCode(ClassUnit classUnit) {
        StringBuilder code = new StringBuilder();

        code.append(".class ").append(classUnit.getClassAccessModifier().toString()).append(" ").append(classUnit.getClassName());

        code.append(".super ").append(classUnit.getSuperClass());

        //constructor
        code.append(".method ").append(classUnit.getClassAccessModifier().toString()).append(" <init>()V");
        code.append("aload_0");
        code.append("invokenonvirtual java/lang/Object/<init>()V");
        code.append("return");
        code.append(".end method");

        for (Method m : classUnit.getMethods()) {
            String declaration = ".method " + m.getMethodAccessModifier().toString() + " ";

            if (m.isFinalMethod()) {
                declaration += "final ";
            }

            if (m.isStaticMethod()) {
                declaration += "static ";
            }

            declaration += m.getMethodName() + "(";

            for (Element e : m.getParams()) {
                declaration += getElementType(e.getType()) + ",";
            }
            if (m.getParams().size() > 0) {
                declaration = declaration.substring(0, declaration.length() -1);
            }

            declaration += ")" + getElementType(m.getReturnType());

            code.append(declaration);

            code.append(".limit locals 99");
            code.append(".limit stack 99");

            for (Instruction i : m.getInstructions()) {

            }
        }

        System.out.println(code);

        return code.toString();
    }

    private String getElementType(Type e) {
        ElementType eType = e.getTypeOfElement();

        return switch (eType) {
            case INT32 -> "I";
            case BOOLEAN, OBJECTREF, CLASS, THIS, STRING -> "x";
            case ARRAYREF -> "[";
            case VOID -> "V";
        };
    }

}
