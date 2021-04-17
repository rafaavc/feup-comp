package visitor;

import constants.NodeNames;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import table.BasicSymbolTable;

import java.util.List;

public class OperatorsVisitor extends Visitor {

    public OperatorsVisitor(BasicSymbolTable symbolTable) {
        super(symbolTable);

        addVisit(NodeNames.add, this::visitArithmetic);
        addVisit(NodeNames.sub, this::visitArithmetic);
        addVisit(NodeNames.lessThan, this::visitArithmetic);

        addVisit(NodeNames.and, this::visitBinary);
        addVisit(NodeNames.not, this::visitBinary);
    }

    private Boolean visitArithmetic(JmmNode node, List<Report> reports) {
        //TODO: valid verifications accordingly to symbol table
        System.out.println("arithmetic");
        System.out.println(node.getKind());
        System.out.println(node.getChildren().get(0));
        System.out.println(node.getChildren().get(1));
        return true;
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
