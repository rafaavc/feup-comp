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

        if (leftSideType == null || rightSideType == null ||
                (!leftSideType.equals(rightSideType)
                && !rightSideType.getName().equals(Types.expected))) {
            //TODO: add to reports
            System.out.println("!!!  WRONG ASSIGNMENT  !!!");
            System.out.println("left: " + node.getChildren().get(0));
            System.out.println("right: " + node.getChildren().get(1));
        }

        leftSideSymbol.setInit(true);
        return true;
    }

    private BasicSymbol leftSideVerification(JmmNode node) {
        JmmNode leftChild = node.getChildren().get(0);
        return getAssignableSymbol(leftChild);
    }

    private Type rightSideVerification(JmmNode node, List<Report> reports) {
        JmmNode rightChild = node.getChildren().get(1);
        allIdentifiersInit(rightChild, reports);
        return getNodeType(rightChild);
    }

    private void allIdentifiersInit(JmmNode node, List<Report> reports) {
        new IdentifierVisitor(symbolTable).visit(node, reports);
    }
}
