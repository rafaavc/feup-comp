package visitor;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.List;

//TODO: replace with constants
public class OperatorsVisitor extends PreorderJmmVisitor<List<Report>, Boolean> {

    public OperatorsVisitor() {
        addVisit("Add", this::visitArithmetic);
        addVisit("Sub", this::visitArithmetic);
        addVisit("LessThan", this::visitBinary);
        addVisit("And", this::visitBinary);
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
