package visitor;

import constants.Attributes;
import constants.NodeNames;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import table.BasicSymbolTable;

import java.util.List;

public class AssignmentVisitor extends Visitor {

    public AssignmentVisitor(BasicSymbolTable symbolTable) {
        super(symbolTable);
        addVisit(NodeNames.assignment, this::visitAssignment);
    }

    public Boolean visitAssignment(JmmNode node, List<Report> reports) {
        System.out.println("assigment");
        System.out.println(node.getKind());
        System.out.println(node.getChildren().get(0));
        System.out.println(node.getChildren().get(0).getAttributes());
        getIdentifierType(node.getChildren().get(0));
        System.out.println(node.getChildren().get(1));
        System.out.println(node.getChildren().get(1).getAttributes());
        return true;
    }
}
