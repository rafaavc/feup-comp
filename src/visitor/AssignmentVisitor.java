package visitor;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.List;

//TODO: replace with constants
public class AssignmentVisitor extends PreorderJmmVisitor<List<Report>, Boolean> {

    public AssignmentVisitor() {
        addVisit("Assignment", this::visitAssignment);
    }

    public Boolean visitAssignment(JmmNode node, List<Report> reports) {
        //TODO: valid verifications accordingly to symbol table
        System.out.println("assigment");
        System.out.println(node.getKind());
        System.out.println(node.getChildren().get(0));
        System.out.println(node.getChildren().get(1));
        return true;
    }
}
