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
        Type leftSideType = leftSideVerification(node, reports);
        Type rightSideType = rightSideVerification(node, reports);
        
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

    private Type leftSideVerification(JmmNode node, List<Report> reports) {
        JmmNode leftChild = node.getChildren().get(0);
        return getChildType(leftChild, reports);
    }

    private Type rightSideVerification(JmmNode node, List<Report> reports) {
        JmmNode rightChild = node.getChildren().get(1);
        Type primitive = isPrimitiveType(rightChild);
        if (primitive != null) return primitive;

        Type alloc = isAllocType(rightChild);
        if (alloc != null) return alloc;

        Type objectProperty = isObjectPropertyType(rightChild);
        if (objectProperty != null) return objectProperty;

        Type expression = isExpressionType(rightChild);
        if (expression != null) return expression;

        return getChildType(rightChild, reports);
    }

    private Type getChildType(JmmNode child, List<Report> reports) {
        Type type = null;
        switch (child.getKind()) {
            case NodeNames.identifier -> type = getIdentifierType(child);
            case NodeNames.arrayAccessResult -> {
                Type tempType = getIdentifierType(child.getChildren().get(0));
                type = new Type(tempType.getName(), false);
            }
            default -> System.out.println("Node kind not covered yet: " + child);
        }
        return type;
    }
}
