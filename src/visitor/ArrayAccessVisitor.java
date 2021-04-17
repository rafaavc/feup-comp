package visitor;

import constants.NodeNames;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import table.BasicSymbolTable;

import java.util.List;

public class ArrayAccessVisitor extends Visitor {

    public ArrayAccessVisitor(BasicSymbolTable symbolTable) {
        super(symbolTable);
        addVisit(NodeNames.arrayAccessResult, this::visitArrayAccess);
    }

    public Boolean visitArrayAccess(JmmNode node, List<Report> reports) {
        //TODO: valid verifications accordingly to symbol table
        System.out.println("array access");
        System.out.println(node.getKind());
        System.out.println(node.getChildren().get(0));
        System.out.println(node.getChildren().get(1));
        return true;
    }
}
