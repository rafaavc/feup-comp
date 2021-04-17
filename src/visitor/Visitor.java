package visitor;

import constants.Attributes;
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
    private ScopeVisitor scopeVisitor;

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
        System.out.println("### METHOD NODE: " + methodScope);
        System.out.println("### NODE NAME: " + nodeName);
        if (methodScope != null) {
            String methodName = methodScope.getOptional(Attributes.name).orElse(null);
            System.out.println("#### METHOD NAME: " + methodName);

            Symbol parameter = symbolTable.getParameter(methodName, nodeName);
            System.out.println("#### PARAMETER: " + parameter);
            if (parameter != null) {
                System.out.println("#### FOUND IT: " + parameter.getType() + " " + parameter.getName());
                return parameter.getType();
            }

            Symbol localVariable = symbolTable.getLocalVariable(methodName, nodeName);
            System.out.println("#### LOCAL VAR: " + localVariable);
            if (localVariable != null) {
                System.out.println("#### FOUND IT: " + localVariable.getType() + " " + localVariable.getName());
                return localVariable.getType();
            }
        }

        JmmNode classScope = scope.getClassScope();
        if (classScope != null) {
            Symbol field = symbolTable.getField(nodeName);
            System.out.println("#### FIELD: " + field);
            if (field != null) {
                System.out.println("#### FOUND IT: " + field.getType() + " " + field.getName());
                return field.getType();
            }

        }

        return null;
    }
}
