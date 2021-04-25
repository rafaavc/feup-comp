package visitor;

import constants.Attributes;
import constants.NodeNames;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import table.BasicSymbolTable;
import visitor.scopes.Scope;
import visitor.scopes.ScopeVisitor;

import java.util.List;

public class Visitor extends PreorderJmmVisitor<List<Report>, Boolean> {
    protected BasicSymbolTable symbolTable;
    private final ScopeVisitor scopeVisitor;

    public Visitor(BasicSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.scopeVisitor = new ScopeVisitor(symbolTable);
    }

    /**
     * Verifies if node given as parameter is present in symbol table and return its type.
     * @return Type of the identifier or null if not present in symbol table
     */
    protected Type getIdentifierType(JmmNode node) {
        Scope scope = scopeVisitor.visit(node);
        String nodeName = node.getOptional(Attributes.name).orElse(null);
        if (nodeName == null) return null;

        JmmNode methodScope = scope.getMethodScope();
        if (methodScope != null) {
            String methodName = methodScope.getOptional(Attributes.name).orElse(null);

            Symbol parameter = symbolTable.getParameter(methodName, nodeName);
            if (parameter != null) return parameter.getType();

            Symbol localVariable = symbolTable.getLocalVariable(methodName, nodeName);
            if (localVariable != null) return localVariable.getType();
        }

        JmmNode classScope = scope.getClassScope();
        if (classScope != null) {
            Symbol field = symbolTable.getField(nodeName);
            if (field != null) return field.getType();
        }

        return null;
    }

    protected Type isPrimitiveType(JmmNode node) {
        return switch (node.getKind()) {
            case NodeNames.integer -> new Type("int", false);
            case NodeNames.bool -> new Type("boolean", false);
            default -> null;
        };
    }

    protected Type isAllocType(JmmNode node) {
        if (!node.getKind().equals(NodeNames.newAlloc))
            return null;

        String nodeName = node.getOptional(Attributes.name).orElse("");
        if (nodeName.equals("int")) return new Type("int", true);
        else return new Type(nodeName, false);
    }
}
