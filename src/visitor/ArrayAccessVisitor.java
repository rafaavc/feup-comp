package visitor;

import constants.Attributes;
import constants.NodeNames;
import pt.up.fe.comp.jmm.JmmNode;
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
    }

    public Boolean visitArrayAccess(JmmNode node, List<Report> reports) {
        //TODO: valid verifications accordingly to symbol table
        System.out.println("array access " + node.getKind());
        System.out.println(node.getKind());

        JmmNode leftChild = node.getChildren().get(0);
        if (!leftChild.getKind().equals(NodeNames.identifier)) {
            String msg = "'" + leftChild.getKind() + "' can not be indexed. It is not an array.";
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get(Attributes.line)), Integer.parseInt(node.get(Attributes.column)), msg));
        } else {
            // check the type of the identifier
        }

        JmmNode rightChild = node.getChildren().get(1);
        JmmNode accessValue = rightChild.getChildren().get(0);
        if (!accessValue.getKind().equals(NodeNames.integer)) {
            String msg = "'" + accessValue.getKind() + "' can not be used to index an array. You must use an integer.";
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get(Attributes.line)), Integer.parseInt(node.get(Attributes.column)), msg));
        }


        return true;
    }
}
