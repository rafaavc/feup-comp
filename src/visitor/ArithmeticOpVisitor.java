package visitor;

import constants.NodeNames;
import constants.Types;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.report.Report;
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

        Type leftType = typeInterpreter.getNodeType(leftNode);
        Type rightType = typeInterpreter.getNodeType(rightNode);
        Type expected = new Type(Types.integer, false);

        if (!isValidType(leftType, expected) || !isValidType(rightType, expected)) {
            reports.add(getReport(node, "Invalid arithmetic operation"));
            return false;
        }

        return true;
    }

    private Boolean isValidType(Type type, Type expected) {
        return type.equals(expected) || type.getName().equals(Types.expected);
    }
}