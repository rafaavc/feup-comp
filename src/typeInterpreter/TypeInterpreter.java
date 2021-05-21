package typeInterpreter;

import constants.Attributes;
import constants.NodeNames;
import constants.Types;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import table.BasicSymbol;
import table.BasicSymbolTable;
import table.MethodIdBuilder;
import utils.Logger;
import visitor.scopes.Scope;
import visitor.scopes.ScopeVisitor;

import java.util.ArrayList;
import java.util.List;

public class TypeInterpreter {
    private final BasicSymbolTable symbolTable;
    protected final ScopeVisitor scopeVisitor;
    protected final MethodIdBuilder methodIdBuilder = new MethodIdBuilder();

    public TypeInterpreter(BasicSymbolTable symbolTable, ScopeVisitor scopeVisitor) {
        this.symbolTable = symbolTable;
        this.scopeVisitor = scopeVisitor;
    }

    public Type getNodeType(JmmNode node) {
        Type primitive = isPrimitiveType(node);
        if (primitive != null) return primitive;

        Type alloc = isAllocType(node);
        if (alloc != null) return alloc;

        Type objectProperty = isObjectPropertyType(node);
        if (objectProperty != null) return objectProperty;

        Type expression = isExpressionType(node);
        if (expression != null) return expression;

        BasicSymbol symbol = getAssignableSymbol(node);

        if (symbol == null) return null;
        return symbol.getType();
    }

    public BasicSymbol getAssignableSymbol(JmmNode node) {
        Logger.log("Received " + node.toString() + " kind = " + node.getKind());
        // TODO
        switch (node.getKind()) {
            case NodeNames.identifier -> {
                return getIdentifierSymbol(node);
            }
            case NodeNames.arrayAccessResult -> {
                JmmNode leftNode = node.getChildren().get(0);
                Logger.log("The left node is " + leftNode.toString());
                Symbol idSymbol = getIdentifierSymbol(leftNode);
                Logger.log("Returned symbol " + idSymbol);
                String typeName = idSymbol.getType().getName();
                Type newType = new Type(typeName, false);
                return new BasicSymbol(newType, leftNode.get(Attributes.name) + "[" + "]");
            }
        }
        return null;
    }

    /**
     * Verifies if node given as parameter is present in symbol table and return its type.
     *
     * @return Type of the identifier or null if not present in symbol table
     */
    public BasicSymbol getIdentifierSymbol(JmmNode node) {
        Scope scope = scopeVisitor.visit(node);
        String nodeName = node.getOptional(Attributes.name).orElse(null);
        Logger.log("Node name = " + nodeName);
        if (nodeName == null) return null;

        JmmNode methodScope = scope.getMethodScope();
        if (methodScope != null) {
            Logger.log("Looking in method scope...");
            String methodId = methodIdBuilder.buildMethodId(methodScope);

            Logger.log("Method id = '" + methodId + "', ");

            BasicSymbol parameter = symbolTable.getParameter(methodId, nodeName);
            if (parameter != null) return parameter;

            BasicSymbol localVariable = symbolTable.getLocalVariable(methodId, nodeName);
            if (localVariable != null) return localVariable;
        }

        return symbolTable.getField(nodeName);
    }

    public Type getMethodType(JmmNode node) {
        JmmNode identifier = node.getChildren().get(0);
        JmmNode objectMethod = node.getChildren().get(1);

        String methodName = objectMethod.getOptional(Attributes.name).orElse(null);
        if (methodName == null) return null;

        if (isCurrentClassInstance(identifier)) {
            Type type = symbolTable.getReturnType(buildMethodCallId(objectMethod));
            if (type != null) return type;

            if (symbolTable.getSuper() != null) {
                if (!validateStaticMethodParameters(objectMethod)) return null;
                return new Type(Types.expected, false);
            }

        } else if (isImportedClassInstance(identifier)) {
            if (!validateStaticMethodParameters(objectMethod)) return null;
            return new Type(Types.expected, false);
        }
        return null;
    }

    private boolean validateStaticMethodParameters(JmmNode node) {
        List<JmmNode> params = node.getChildren();
        for (JmmNode param : params) {
            if (getNodeType(param) == null) return false;
        }
        return true;
    }

    public boolean isImportedClassInstance(JmmNode identifier) {
        String identifierName = identifier.getOptional(Attributes.name).orElse(null);
        if (identifierName == null) return false;

        return symbolTable.getImports().contains(identifierName);
    }

    public boolean isCurrentClassInstance(JmmNode identifier) {
        if (identifier.getKind().equals(NodeNames.thisName)) return true;

        if (identifier.getKind().equals(NodeNames.identifier)) {
            BasicSymbol symbol = getIdentifierSymbol(identifier);
            if (symbol == null) return false;

            return symbol.getType().getName().equals(symbolTable.getClassName());
        }

        Type type = isAllocType(identifier);
        if (type == null) return false;
        return type.getName().equals(symbolTable.getClassName());
    }

    public Type isPrimitiveType(JmmNode node) {
        return switch (node.getKind()) {
            case NodeNames.integer -> new Type(Types.integer, false);
            case NodeNames.bool -> new Type(Types.bool, false);
            default -> null;
        };
    }

    public Type isAllocType(JmmNode node) {
        if (!node.getKind().equals(NodeNames.newAlloc))
            return null;

        String nodeName = node.getOptional(Attributes.name).orElse(null);
        if (nodeName == null) return null;

        if (nodeName.equals(Types.integer)) return new Type(Types.integer, true);
        else return new Type(nodeName, false);
    }

    public Type isObjectPropertyType(JmmNode node) {
        if (!node.getKind().equals(NodeNames.objectProperty))
            return null;

        JmmNode property = node.getChildren().get(1);
        if (property.getKind().equals(NodeNames.length))
            return new Type(Types.integer, false);
        else if (property.getKind().equals(NodeNames.objectMethod))
            return getMethodType(node);
        return null;
    }

    public Type isExpressionType(JmmNode node) {
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

    public String buildMethodCallId(JmmNode node) {
        String methodName = node.getOptional(Attributes.name).orElse("main");
        String params = buildParametersCallId(node.getChildren());
        return params.equals("") ? methodName : methodName + "-" + params;
    }

    private String buildParametersCallId(List<JmmNode> parameters) {
        List<String> parameterIds = new ArrayList<>();
        for (JmmNode parameter : parameters) {
            Type type = getNodeType(parameter);
            if (type == null) return "";

            if (type.isArray()) parameterIds.add(type.getName() + "[]");
            else parameterIds.add(type.getName());
        }
        return String.join(",", parameterIds);
    }
}
