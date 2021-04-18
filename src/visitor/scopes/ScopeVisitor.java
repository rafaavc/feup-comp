package visitor.scopes;

import constants.NodeNames;
import pt.up.fe.comp.jmm.JmmNode;
import table.BasicSymbolTable;

public class ScopeVisitor {
    protected BasicSymbolTable symbolTable;

    public ScopeVisitor(BasicSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public Scope visit(JmmNode node) {
        Scope scope = new Scope();

        JmmNode current = node.getParent();
        while (current != null) {
            switch(current.getKind()) {
                case NodeNames.classDeclaration:
                    scope.add(NodeNames.classDeclaration, current);
                    break;
                case NodeNames.mainMethod:
                case NodeNames.method:
                    scope.add(NodeNames.method, current);
                    break;
            }
            current = current.getParent();
        }

        return scope;
    }
}
