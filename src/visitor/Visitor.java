package visitor;

import constants.Attributes;
import constants.NodeNames;
import constants.Types;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import table.BasicSymbol;
import table.BasicSymbolTable;
import visitor.scopes.Scope;
import visitor.scopes.ScopeVisitor;

import java.util.List;

public abstract class Visitor extends PreorderJmmVisitor<List<Report>, Boolean> {
    protected BasicSymbolTable symbolTable;
    protected final ScopeVisitor scopeVisitor;

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
    }

    protected Type getNodeType(JmmNode node) {
        Type primitive = isPrimitiveType(node);
        if (primitive != null) return primitive;

        Type alloc = isAllocType(node);
        if (alloc != null) return alloc;

        Type objectProperty = isObjectPropertyType(node);
        if (objectProperty != null) return objectProperty;

        Type expression = isExpressionType(node);
        if (expression != null) return expression;

        return getAssignableSymbol(node).getType();
    }

    protected BasicSymbol getAssignableSymbol(JmmNode node) {
        switch (node.getKind()) {
            case NodeNames.identifier -> {
                return getIdentifierSymbol(node);
            }
            case NodeNames.arrayAccessResult -> {
                JmmNode leftNode = node.getChildren().get(0);
                BasicSymbol symbol = getIdentifierSymbol(leftNode);

                String typeName = symbol.getType().getName();
                Type type = new Type(typeName, false);
                return new BasicSymbol(type, symbol.getName());
            }
        }
        return null;
    }

    /**
     * Verifies if node given as parameter is present in symbol table and return its type.
     * @return Type of the identifier or null if not present in symbol table
     */
    protected BasicSymbol getIdentifierSymbol(JmmNode node) {
        Scope scope = scopeVisitor.visit(node);
        String nodeName = node.getOptional(Attributes.name).orElse(null);
        if (nodeName == null) return null;

        JmmNode methodScope = scope.getMethodScope();
        if (methodScope != null) {
            String methodName = methodScope.getOptional(Attributes.name).orElse(null);

            BasicSymbol parameter = symbolTable.getParameter(methodName, nodeName);
            if (parameter != null) return parameter;

            BasicSymbol localVariable = symbolTable.getLocalVariable(methodName, nodeName);
            if (localVariable != null) return localVariable;
        }

        return symbolTable.getField(nodeName);
    }

    protected Type getMethodType(JmmNode node) {
        JmmNode identifier = node.getChildren().get(0);
        JmmNode objectMethod = node.getChildren().get(1);

        String identifierKind = identifier.getKind();
        String identifierName = identifier.getOptional(Attributes.name).orElse(null);

        String methodName = objectMethod.getOptional(Attributes.name).orElse(null);
        if (methodName == null) return null;

        if (identifierKind.equals(NodeNames.thisName) || isCurrentClassInstance(identifier)) {
            Type type = symbolTable.getReturnType(methodName);
            if (type != null) return type;

            if (symbolTable.getSuper() != null)
                return new Type(Types.expected, false);
        }
        else if (symbolTable.getImports().contains(identifierName) ||
                    isImportedClassInstance(identifier))
            return new Type(Types.expected, false);
        return null;
    }

    protected boolean isImportedClassInstance(JmmNode identifier) {
        BasicSymbol symbol = getIdentifierSymbol(identifier);
        if (symbol == null) return false;

        return symbolTable.getImports().contains(symbol.getType().getName());
    }

    protected boolean isCurrentClassInstance(JmmNode identifier) {
        BasicSymbol symbol = getIdentifierSymbol(identifier);
        if (symbol == null) return false;

        return symbol.getType().getName().equals(symbolTable.getClassName());
    }

    protected Type isPrimitiveType(JmmNode node) {
        return switch (node.getKind()) {
            case NodeNames.integer -> new Type(Types.integer, false);
            case NodeNames.bool -> new Type(Types.bool, false);
            default -> null;
        };
    }

    protected Type isAllocType(JmmNode node) {
        if (!node.getKind().equals(NodeNames.newAlloc))
            return null;

        String nodeName = node.getOptional(Attributes.name).orElse(null);
        if (nodeName == null) return null;

        if (nodeName.equals(Types.integer)) return new Type(Types.integer, true);
        else return new Type(nodeName, false);
    }

    protected Type isObjectPropertyType(JmmNode node) {
        if (!node.getKind().equals(NodeNames.objectProperty))
            return null;

        JmmNode property = node.getChildren().get(1);
        if (property.getKind().equals(NodeNames.length))
            return new Type(Types.integer, false);
        else if (property.getKind().equals(NodeNames.objectMethod))
            return getMethodType(node);
        return null;
    }

    protected Type isExpressionType(JmmNode node) {
        switch (node.getKind()) {
            case NodeNames.sum, NodeNames.sub, NodeNames.mul, NodeNames.div -> {
                return new Type(Types.integer, false);
            }
            case NodeNames.and, NodeNames.not, NodeNames.lessThan -> {
                return new Type(Types.bool, false);
            }
        }
        return null;
    }
}