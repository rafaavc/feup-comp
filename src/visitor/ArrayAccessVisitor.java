package visitor;

import constants.Attributes;
import constants.NodeNames;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import table.BasicSymbolTable;

import java.util.List;

public class ArrayAccessVisitor extends Visitor {

    public ArrayAccessVisitor(BasicSymbolTable symbolTable) {
        super(symbolTable);
        addVisit(NodeNames.arrayAccessResult, this::visitArrayAccess);
        addVisit(NodeNames.newArraySize, this::visitNewArraySize);
    }

    public Report getReport(JmmNode node, String msg) {
        return new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get(Attributes.line)), Integer.parseInt(node.get(Attributes.column)), msg);
    }

    public Boolean visitNewArraySize(JmmNode node, List<Report> reports) {
        // TODO check if identifier has int type and expression results in int type
        if (!node.getChildren().get(0).getKind().equals(NodeNames.integer)) {
            reports.add(getReport(node, "'" + node.getChildren().get(0).getKind() + "' can not be used to index an array. You must use an integer."));
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

        // TODO check if identifier has int type and expression results in int type
        if (!accessValue.getKind().equals(NodeNames.integer)) {
            reports.add(getReport(node, "'" + accessValue.getKind() + "' can not be used to index an array. You must use an integer."));
        }

        return true;
    }
}
