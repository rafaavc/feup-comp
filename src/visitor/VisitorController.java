package visitor;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import table.BasicSymbolTable;
import table.scopes.Scoped;

import java.util.ArrayList;
import java.util.List;

public class VisitorController {
    BasicSymbolTable table = new BasicSymbolTable();
    Scoped globalScope = table.getGlobalScope();
    JmmNode root;

    public VisitorController(JmmNode root) {
        this.root = root;
    }

    public void start() {
        //visit to fill in symbol table
        new PopulateTableVisitor().visit(root, globalScope);
        //table.log();
        List<Report> semanticReports = new ArrayList<>();

        typeVerification(semanticReports);
        methodVerification(semanticReports);

        semanticReports.forEach(System.out::println);
    }

    private void typeVerification(List<Report> semanticReports) {
        //new AssignmentVisitor(table).visit(root, semanticReports);
        //new OperatorsVisitor().visit(root, semanticReports);
        new ArrayAccessVisitor(table).visit(root, semanticReports);
    }

    private void methodVerification(List<Report> semanticReports) {
        //TODO
    }
}
