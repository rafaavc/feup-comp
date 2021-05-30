import java.util.ArrayList;
import java.util.List;

import optimization.ConstantPropagation;
import optimization.WhileOptimization;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import visitor.OllirVisitor;
import table.BasicSymbolTable;
import ollir.OllirBuilder;

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

public class OptimizationStage implements JmmOptimization {

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {

        JmmNode node = semanticsResult.getRootNode();

        // Convert the AST to a String containing the equivalent OLLIR code
        OllirVisitor visitor = new OllirVisitor((BasicSymbolTable) semanticsResult.getSymbolTable());
        visitor.visitNode(node);

        String ollirCode = visitor.getOllirBuilder().getCode();

        System.out.println("## Got the ollir code:\n\n" + ollirCode);

        // More reports from this stage
        List<Report> reports = new ArrayList<>();
        return new OllirResult(semanticsResult, ollirCode, reports);
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        // THIS IS JUST FOR CHECKPOINT 3
        return semanticsResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        // THIS IS JUST FOR CHECKPOINT 3
        return ollirResult;
    }

    public OllirResult optimizeO(JmmSemanticsResult semanticsResult, OllirResult ollirResult) {
        ollirResult = new ConstantPropagation().optimize(semanticsResult, ollirResult);
        ollirResult = new WhileOptimization().optimize(semanticsResult, ollirResult);
        return ollirResult;
    }

}

