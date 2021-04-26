package visitor;

import constants.NodeNames;
import constants.Types;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.report.Report;
import table.BasicSymbolTable;

import java.util.List;

public class AssignmentVisitor extends Visitor {

    public AssignmentVisitor(BasicSymbolTable symbolTable) {
        super(symbolTable);
        addVisit(NodeNames.assignment, this::visitAssignment);
    }

    public Boolean visitAssignment(JmmNode node, List<Report> reports) {
        Type leftSideType = leftSideVerification(node);
        Type rightSideType = rightSideVerification(node);

        if (leftSideType == null || rightSideType == null ||
                (!leftSideType.equals(rightSideType)
                && !rightSideType.getName().equals(Types.expected))) {
            //TODO: add to reports
            System.out.println("!!!  WRONG ASSIGNMENT  !!!");
            System.out.println("left: " + node.getChildren().get(0));
            System.out.println("right[0]: " + node.getChildren().get(1).getChildren().get(0));
            System.out.println("right[1]: " + node.getChildren().get(1).getChildren().get(1));
        }
        return true;
    }

    private Type leftSideVerification(JmmNode node) {
        JmmNode leftChild = node.getChildren().get(0);
        return getBasicType(leftChild);
    }

    private Type rightSideVerification(JmmNode node) {
        JmmNode rightChild = node.getChildren().get(1);
        return getNodeType(rightChild);
    }
}
