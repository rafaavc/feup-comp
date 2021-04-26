package visitor;

import constants.Attributes;
import constants.NodeNames;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
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
        BasicSymbol symbol = getIdentifierSymbol(node);
        if (symbol == null) return true;
        if (!symbol.isInit() && !isParameter(node)) {
            //TODO: add to reports
            System.out.println("!!! Not init !!!");
            System.out.println("Node: " + node);
            String name = node.getOptional(Attributes.name).orElse(null);
            System.out.println("Name: " + name);
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
            String methodName = methodScope.getOptional(Attributes.name).orElse(null);

            BasicSymbol parameter = symbolTable.getParameter(methodName, nodeName);
            return parameter != null;
        }
        return false;
    }
}
