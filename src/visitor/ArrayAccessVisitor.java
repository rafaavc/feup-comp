package visitor;

import constants.Attributes;
import constants.NodeNames;
import constants.Types;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import table.BasicSymbolTable;

import java.util.List;

public class ArrayAccessVisitor extends Visitor {

    public ArrayAccessVisitor(BasicSymbolTable symbolTable) {
        super(symbolTable);
        addVisit(NodeNames.arrayAccessResult, this::visitArrayAccess);
        addVisit(NodeNames.newArraySize, this::visitNewArraySize);
    }

    public Boolean visitNewArraySize(JmmNode node, List<Report> reports) {
        String childType = getNodeType(node.getChildren().get(0)).getName();
        if (!childType.equals(Types.integer)) {
            reports.add(getReport(node, "Type '" + childType + "' can not be used to index an array. You must use an integer."));
        }
        return true;
    }

    public Boolean visitArrayAccess(JmmNode node, List<Report> reports) {
        JmmNode leftChild = node.getChildren().get(0);
        if (!leftChild.getKind().equals(NodeNames.identifier)) {
            reports.add(getReport(node, "'" + leftChild.getKind() + "' can not be indexed. It is not an array."));
        } else {
            Symbol symbol = getIdentifierSymbol(leftChild);
            System.out.println("Found symbol of " + symbol.getName() + ": " + symbol.toString());
            if (!symbol.getType().isArray()) {
                reports.add(getReport(node, "Identifier '" + leftChild.get(Attributes.name) + "' can not be indexed. It is not an array."));
            }
        }

        JmmNode rightChild = node.getChildren().get(1);
        JmmNode accessValue = rightChild.getChildren().get(0);

        String accessValueType = getNodeType(accessValue).getName();
        if (!accessValueType.equals(Types.integer)) {
            reports.add(getReport(node, "Type '" + accessValueType + "' can not be used to index an array. You must use an integer."));
        }

        return true;
    }
}
