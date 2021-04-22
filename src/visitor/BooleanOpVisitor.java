package visitor;

import constants.NodeNames;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import table.BasicSymbolTable;

import java.util.List;

public class BooleanOpVisitor extends Visitor {

    public BooleanOpVisitor(BasicSymbolTable symbolTable) {
        super(symbolTable);

        addVisit(NodeNames.and, this::visitBinary);
        addVisit(NodeNames.not, this::visitBinary);
    }

    private Boolean visitBinary(JmmNode node, List<Report> reports) {
        //TODO: valid verifications accordingly to symbol table
        System.out.println("binary");
        System.out.println(node.getKind());
        System.out.println(node.getChildren().get(0));
        System.out.println(node.getChildren().get(1));
        return true;
    }
}
