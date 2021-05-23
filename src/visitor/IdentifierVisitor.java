package visitor;

import constants.Attributes;
import constants.NodeNames;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import table.BasicSymbol;
import table.BasicSymbolTable;
import visitor.scopes.Scope;

import java.util.List;

public class IdentifierVisitor extends Visitor {
    public IdentifierVisitor(BasicSymbolTable symbolTable) {
        super(symbolTable);

        addVisit(NodeNames.identifier, this::verifyInit);
    }

    private Boolean verifyInit(JmmNode node, List<Report> reports) {
        BasicSymbol symbol = typeInterpreter.getIdentifierSymbol(node);
        if (symbol == null && !typeInterpreter.isImportedClassInstance(node)) {
            reports.add(getReport(node, "Variable " + node.getOptional(Attributes.name).orElse(null) + " does not exist"));
            return false;
        }

        if (symbol == null) return true;
        if (!symbol.isInit() && !isParameter(node)) {
            reports.add(getReport(ReportType.WARNING, node, "Variable not initialized"));
            return false;
        }
        return true;
    }

    private Boolean isParameter(JmmNode node) {
        Scope scope = scopeVisitor.visit(node);
        String nodeName = node.getOptional(Attributes.name).orElse(null);
        if (nodeName == null) return false;

        JmmNode methodScope = scope.getMethodScope();
        if (methodScope != null) {
            String methodId = methodIdBuilder.buildMethodId(methodScope);

            BasicSymbol parameter = symbolTable.getParameter(methodId, nodeName);
            return parameter != null;
        }
        return false;
    }
}
