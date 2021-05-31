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
import table.BasicSymbolTable;
import utils.Logger;
import visitor.OllirVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return new OllirResult(semanticsResult, ollirCode, reports);
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        // THIS IS JUST FOR CHECKPOINT 3
        return semanticsResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        try {
            int k = getMinimumPossible(ollirResult.getOllirClass().getMethods(), 0);
            Logger.log("Optimizin to the minimum possible k (" + k + ")");
            return optimize(ollirResult, k);
        } catch(Exception e) {
            ollirResult.getReports().add(new Report(ReportType.ERROR, Stage.OPTIMIZATION, -1, "Couldn't optimize! " + e.getMessage()));
            return ollirResult;
        }
    }

    public int getMinimumPossible(List<Method> methods, int k) throws Exception {
        int minK = k;
        boolean success = false;

        while (!success) {
            minK++;
            success = true;
            for (Method method : methods) {
                Liveness liveness = new Liveness(method);
                LivenessResult livenessResult = liveness.getResult();

                RegisterAllocator allocator = new RegisterAllocator(livenessResult.getVariables());

                if (!allocator.colorGraph(minK)) {
                    success = false;
                    break;
                }
            }
        }
        return minK;
    }

    public OllirResult optimize(OllirResult ollirResult, int k) {
        try {
            List<Method> methods = ollirResult.getOllirClass().getMethods();
            for (Method method : methods)
            {
                Liveness liveness = new Liveness(method);

                System.out.println(liveness);
                LivenessResult livenessResult = liveness.getResult();
                System.out.println(livenessResult);

                RegisterAllocator allocator = new RegisterAllocator(livenessResult.getVariables());

                if (allocator.colorGraph(k))
                {
                    System.out.println(allocator.getColoredGraph());
                    ollirResult.getReports().add(new Report(ReportType.LOG, Stage.OPTIMIZATION, -1, "Optimized register allocation to use " + k + " registers in method " + method.getMethodName() + "."));
                }
                else
                {
                    int minK = getMinimumPossible(methods, k);
                    ollirResult.getReports().add(new Report(ReportType.ERROR, Stage.OPTIMIZATION, -1, "Couldn't optimize register allocation to use " + k + " registers in method " + method.getMethodName() + ". The minimum possible is " + minK + "!"));
                    return ollirResult;
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

    public OllirResult optimizeO(JmmSemanticsResult semanticsResult, OllirResult ollirResult) {
        System.out.println("# OPTIMIZING...");
        ollirResult = new ConstantPropagation().optimize(semanticsResult, ollirResult);
        //ollirResult = new WhileOptimization().optimize(semanticsResult, ollirResult);

        return ollirResult;
    }

}

