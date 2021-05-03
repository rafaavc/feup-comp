package visitor;

import constants.NodeNames;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import table.BasicSymbolTable;

import java.util.List;

public class BooleanOpVisitor extends Visitor {

    public BooleanOpVisitor(BasicSymbolTable symbolTable) {
        super(symbolTable);

        addVisit(NodeNames.and, this::visitBinary);
        addVisit(NodeNames.not, this::visitBinary);
        addVisit(NodeNames.lessThan, this::visitBinary);
        addVisit(NodeNames.condition, this::visitBinary);
    }

    private Boolean visitBinary(JmmNode node, List<Report> reports) {
        String nodeKind = node.getKind();
        Boolean result = false;
        if (nodeKind.equals(NodeNames.and)) {
            result = verifyBool(node.getChildren().get(0)) && verifyBool(node.getChildren().get(1));
            if (!result) reports.add(getReport(node, "Invalid and operation"));
        } else if (nodeKind.equals(NodeNames.not)) {
            result = verifyBool(node.getChildren().get(0));
            if (!result) reports.add(getReport(node, "Invalid not operation"));
        } else if (nodeKind.equals(NodeNames.lessThan)) {
            result = verifyNum(node.getChildren().get(0)) && verifyNum(node.getChildren().get(1));
            if (!result) reports.add(getReport(node, "Invalid less than operation"));
        } else if (nodeKind.equals(NodeNames.condition)) {
            result = verifyBool(node.getChildren().get(0));
            if (!result) reports.add(getReport(node, "Invalid condition"));
        }
        return result;
    }

    private Boolean verifyBool(JmmNode node) {
        String kind = node.getKind();

        if (kind.equals(NodeNames.identifier)) {
            kind = typeInterpreter.getIdentifierSymbol(node).getType().getName();
        } else if (kind.equals(NodeNames.objectProperty)) {
            kind = typeInterpreter.isObjectPropertyType(node).getName();
        }

        return kind.equals(NodeNames.bool) || kind.equals("boolean") || kind.equals(NodeNames.and) || kind.equals(NodeNames.not) || kind.equals(NodeNames.lessThan);
    }

    private Boolean verifyNum(JmmNode node) {
        String kind = node.getKind();

        if (kind.equals(NodeNames.identifier)) {
            kind = typeInterpreter.getIdentifierSymbol(node).getType().getName();
        } else if (kind.equals(NodeNames.objectProperty)) {
            kind = typeInterpreter.isObjectPropertyType(node).getName();
        }

        return kind.equals(NodeNames.integer) || kind.equals("int") || kind.equals(NodeNames.sum) || kind.equals(NodeNames.sub) || kind.equals(NodeNames.mul) || kind.equals(NodeNames.div) || kind.equals(NodeNames.arrayAccessResult);
    }
}
