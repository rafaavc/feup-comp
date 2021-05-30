import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import optimization.*;

import org.specs.comp.ollir.Instruction;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.Operand;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import utils.Logger;
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

    private final List<Report> reports = new ArrayList<>();

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {

        JmmNode node = semanticsResult.getRootNode();

        // Convert the AST to a String containing the equivalent OLLIR code
        OllirVisitor visitor = new OllirVisitor((BasicSymbolTable) semanticsResult.getSymbolTable());
        visitor.visitNode(node);

        String ollirCode = visitor.getOllirBuilder().getCode();

        System.out.println("## Got the ollir code:\n\n" + ollirCode);

        // More reports from this stage
        return optimize(new OllirResult(semanticsResult, ollirCode, reports));
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        // THIS IS JUST FOR CHECKPOINT 3
        return semanticsResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        try {
            List<Method> methods = ollirResult.getOllirClass().getMethods();
            for (Method method : methods)
            {
                Liveness liveness = new Liveness(method);

                System.out.println(liveness);
                LivenessResult livenessResult = liveness.getResult();
                System.out.println(livenessResult);

                RegisterAllocator allocator = new RegisterAllocator(livenessResult.getVariables());
                int k = 1;
                if (allocator.colorGraph(k))
                {
                    System.out.println(allocator.getColoredGraph());
                    ollirResult.getReports().add(new Report(ReportType.LOG, Stage.OPTIMIZATION, -1, "Optimized register allocation to use " + k + " registers in method " + method.getMethodName() + "."));
                }
                else
                {
                    // TODO error report and report minimum number of variables required
                    ollirResult.getReports().add(new Report(ReportType.WARNING, Stage.OPTIMIZATION, -1, "Couldn't optimize register allocation to use " + k + " registers in method " + method.getMethodName() + "."));
                    continue;
                }

                Map<String, Integer> graph = allocator.getColoredGraph();

                List<Instruction> instructions = method.getInstructions();
                for (int i = 0; i < instructions.size(); i++) {
                    OllirVariableFinder.findInstruction(method, (FinderAlert alert) -> {
                        if (alert.getType() == FinderAlert.FinderAlertType.USE || alert.getType() == FinderAlert.FinderAlertType.DEF) {
                            String name = OllirVariableFinder.getIdentifier(alert.getElement());
                            if (name != null && graph.containsKey(name)) {
                                Operand operand = (Operand) alert.getElement();
                                operand.setName("k" + graph.get(name));
                            }
                        }
                    }, instructions.get(i), i, i == instructions.size() - 1);
                }
            }
        } catch(Exception e) {
            Logger.err("Ollir optimization failed!");
            e.printStackTrace();
        }
        return ollirResult;
    }

}

