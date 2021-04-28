package visitor;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import table.BasicSymbolTable;
import table.scopes.Scoped;
import utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class VisitorController {
    List<Report> semanticReports = new ArrayList<>();
    BasicSymbolTable table = new BasicSymbolTable();
    Scoped globalScope = table.getGlobalScope();
    JmmNode root;

    public VisitorController(JmmNode root) {
        this.root = root;
    }

    public void start() {
        //visit to fill in symbol table
        new PopulateTableVisitor().visit(root, globalScope);
        table.log();

        visit();
    }

    public List<Report> getReports() {
        return semanticReports;
    }

    public BasicSymbolTable getTable() {
        return table;
    }

    private void visit() {
        new ArithmeticOpVisitor(table).visit(root, semanticReports);
        new PropertyVisitor(table).visit(root, semanticReports);
        new AssignmentVisitor(table).visit(root, semanticReports);
        new ArrayAccessVisitor(table).visit(root, semanticReports);

        for (Report r : semanticReports) {
            Logger.log(r.toString());
        }
    }
}
