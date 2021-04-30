package visitor;

import constants.Attributes;
import constants.NodeNames;
import constants.Types;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import table.BasicSymbolTable;

import java.util.List;

public class ArithmeticOpVisitor extends Visitor {

    public ArithmeticOpVisitor(BasicSymbolTable symbolTable) {
        super(symbolTable);

        addVisit(NodeNames.sum, this::visitArithmetic);
        addVisit(NodeNames.sub, this::visitArithmetic);
    }

    private Boolean visitArithmetic(JmmNode node, List<Report> reports) {
        JmmNode leftNode = node.getChildren().get(0);
        JmmNode rightNode = node.getChildren().get(1);

        Type leftType = getNodeType(leftNode);
        Type rightType = getNodeType(rightNode);
        Type expected = new Type(Types.integer, false);

        if (!leftType.equals(expected) || !rightType.equals(expected)) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get(Attributes.line)), Integer.parseInt(node.get(Attributes.column)), "Invalid arithmetic operation"));
        }

        return true;
    }
}