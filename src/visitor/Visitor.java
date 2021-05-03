package visitor;

import constants.Attributes;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import table.BasicSymbolTable;
import table.MethodIdBuilder;
import typeInterpreter.TypeInterpreter;
import visitor.scopes.ScopeVisitor;

import java.util.List;

public abstract class Visitor extends PreorderJmmVisitor<List<Report>, Boolean> {
    protected BasicSymbolTable symbolTable;
    protected final ScopeVisitor scopeVisitor;
    protected TypeInterpreter typeInterpreter;
    protected final MethodIdBuilder methodIdBuilder = new MethodIdBuilder();

    public Visitor(BasicSymbolTable symbolTable) {
        super((nodeResult, childrenResults) -> {
            if (nodeResult == null || childrenResults == null) //TODO: check why always null
                return false;
            if (!nodeResult) return false;
            for (boolean b : childrenResults)
                if (!b) return false;
            return true;
        });

        this.symbolTable = symbolTable;
        this.scopeVisitor = new ScopeVisitor(symbolTable);
        this.typeInterpreter = new TypeInterpreter(symbolTable, scopeVisitor);
    }

    public Report getReport(JmmNode node, String msg) {
        return new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get(Attributes.line)), Integer.parseInt(node.get(Attributes.column)), msg);
    }
}
