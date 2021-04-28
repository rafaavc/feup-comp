package visitor;

import constants.NodeNames;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import table.BasicSymbolTable;
import constants.Attributes;

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
        if (nodeKind.equals(NodeNames.and)) {
            return verifyBool(node.getChildren().get(0)) && verifyBool(node.getChildren().get(1));
        } else if (nodeKind.equals(NodeNames.not) || nodeKind.equals(NodeNames.condition)) {
            return verifyBool(node.getChildren().get(0));
        } else if (nodeKind.equals(NodeNames.lessThan)) {
            return verifyNum(node.getChildren().get(0)) && verifyNum(node.getChildren().get(1));
        }
        return false;
    }

    private Boolean verifyBool(JmmNode node) {
        String kind = node.getKind();

        if (kind.equals(NodeNames.identifier)) {
            kind = getIdentifierSymbol(node).getType().getName();
        } else if (kind.equals(NodeNames.objectProperty)) {
            kind = isObjectPropertyType(node).getName();
        }

        return kind.equals(NodeNames.bool) || kind.equals("boolean") || kind.equals(NodeNames.and) || kind.equals(NodeNames.not) || kind.equals(NodeNames.lessThan);
    }

    private Boolean verifyNum(JmmNode node) {
        String kind = node.getKind();

        if (kind.equals(NodeNames.identifier)) {
            kind = getIdentifierSymbol(node).getType().getName();
        } else if (kind.equals(NodeNames.objectProperty)) {
            kind = isObjectPropertyType(node).getName();
        }

        return kind.equals(NodeNames.integer) || kind.equals("int") || kind.equals(NodeNames.sum) || kind.equals(NodeNames.sub) || kind.equals(NodeNames.mul) || kind.equals(NodeNames.div) || kind.equals(NodeNames.arrayAccessResult);
    }
}
