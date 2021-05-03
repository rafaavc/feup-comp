package visitor;

import constants.NodeNames;
import constants.Types;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.report.Report;
import table.BasicSymbol;
import table.BasicSymbolTable;

import java.util.List;

public class AssignmentVisitor extends Visitor {

    public AssignmentVisitor(BasicSymbolTable symbolTable) {
        super(symbolTable);
        addVisit(NodeNames.assignment, this::visitAssignment);
    }

    public Boolean visitAssignment(JmmNode node, List<Report> reports) {
        BasicSymbol leftSideSymbol = leftSideVerification(node);
        Type leftSideType = leftSideSymbol.getType();
        Type rightSideType = rightSideVerification(node, reports);

        if (leftSideType == null || rightSideType == null || (!leftSideType.equals(rightSideType) && !rightSideType.getName().equals(Types.expected))) {
            reports.add(getReport(node, "Right hand side type does not match left hand side"));
        }

        leftSideSymbol.setInit(true);
        return true;
    }

    private BasicSymbol leftSideVerification(JmmNode node) {
        JmmNode leftChild = node.getChildren().get(0);
        return typeInterpreter.getAssignableSymbol(leftChild);
    }

    private Type rightSideVerification(JmmNode node, List<Report> reports) {
        JmmNode rightChild = node.getChildren().get(1);
        allIdentifiersInit(rightChild, reports);
        return typeInterpreter.getNodeType(rightChild);
    }

    private void allIdentifiersInit(JmmNode node, List<Report> reports) {
        new IdentifierVisitor(symbolTable).visit(node, reports);
    }
}
