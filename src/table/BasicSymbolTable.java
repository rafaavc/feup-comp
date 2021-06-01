package table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import table.scopes.ClassScope;
import table.scopes.GlobalScope;
import table.scopes.MethodScope;
import utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BasicSymbolTable implements SymbolTable {
    private final GlobalScope global = new GlobalScope();

    public GlobalScope getGlobalScope() {
        return global;
    }

    @Override
    public List<String> getImports() {
        return global.getImports();
    }

    @Override
    public String getClassName() {
        ClassScope classScope = global.getClassScope();
        return classScope.getClassName();
    }

    @Override
    public String getSuper() {
        ClassScope classScope = global.getClassScope();
        return classScope.getSuperClassName();
    }

    @Override
    public List<Symbol> getFields() {
        ClassScope classScope = global.getClassScope();
        return new ArrayList<>(classScope.getFields());
    }

    public BasicSymbol getField(String fieldName) {
        ClassScope classScope = global.getClassScope();
        List<BasicSymbol> fields = classScope.getFields();
        for (BasicSymbol field : fields)
            if (fieldName.equals(field.getName())) return field;
        return null;
    }

    @Override
    public List<String> getMethods() {
        ClassScope classScope = global.getClassScope();
        return new ArrayList<>(classScope.getMethods().keySet());
    }

    @Override
    public Type getReturnType(String methodName) {
        ClassScope classScope = global.getClassScope();
        MethodScope methodScope = classScope.getMethod(methodName);
        if (methodScope == null) return null;
        return methodScope.getReturnType();
    }

    @Override
    public List<Symbol> getParameters(String methodName) {
        ClassScope classScope = global.getClassScope();
        MethodScope method = classScope.getMethod(methodName);
        return method == null ? new ArrayList<>() : new ArrayList<>(method.getParameters());
    }

    public BasicSymbol getParameter(String methodName, String parameterName) {
        ClassScope classScope = global.getClassScope();
        List<BasicSymbol> parameters = classScope.getMethod(methodName).getParameters();
        for (BasicSymbol parameter : parameters)
            if (parameterName.equals(parameter.getName())) return parameter;
        return null;
    }

    @Override
    public List<Symbol> getLocalVariables(String methodName) {
        ClassScope classScope = global.getClassScope();
        return new ArrayList<>(classScope.getMethod(methodName).getLocalVariables());
    }

    public BasicSymbol getLocalVariable(String methodName, String variableName) {
        ClassScope classScope = global.getClassScope();
        MethodScope method = classScope.getMethod(methodName);
        if (method == null) {
            Logger.err("Couldn't find method " + methodName);
            return null;
        }
        List<BasicSymbol> localVariables = method.getLocalVariables();
        for (BasicSymbol localVariable : localVariables)
            if (variableName.equals(localVariable.getName())) return localVariable;
        return null;
    }

    public void log() {
        Logger.log("# Imports");
        for (String imp : getImports()) Logger.log("-" + imp);
        Logger.log("");

        Logger.log("# Class " + getClassName() + (getSuper() != null ? " extends " + getSuper() : ""));
        Logger.log("");

        Logger.log("## Fields ");
        for (Symbol symb : getFields()) Logger.log("-" + symb);
        Logger.log("");

        Logger.log("## Methods");
        for (String method : getMethods()) {
            Logger.log("### New method");
            Logger.log("- " + method.split("-")[0] + ", returns " + getReturnType(method) + ", methodId = '" + method + "'");
            Logger.log("");
            Logger.log("#### Parameters");
            for (Symbol param : getParameters(method)) Logger.log("-" + param);
            Logger.log("");
            Logger.log("#### Local variables");
            for (Symbol param : getLocalVariables(method)) Logger.log("-" + param);
            Logger.log("");
        }
    }

    @Override
    public String print() {
        var builder = new StringBuilder();

        builder.append("Class: " + getClassName() + "\n");
        var superClass = getSuper() != null ? getSuper() : "java.lang.Object";
        builder.append("Super: " + superClass + "\n");
        builder.append("\nImports:");
        var imports = getImports();

        if (imports.isEmpty()) {
            builder.append(" <no imports>\n");
        } else {
            builder.append("\n");
            imports.forEach(fullImport -> builder.append(" - " + fullImport + "\n"));
        }

        var fields = getFields();
        builder.append("\nFields:");
        if (fields.isEmpty()) {
            builder.append(" <no fields>\n");
        } else {
            builder.append("\n");
            fields.forEach(field -> builder.append(" - " + field.print() + "\n"));
        }

        var methods = getMethods();
        builder.append("\nMethods: " + methods.size() + "\n");

        for (var method : methods) {
            var returnType = getReturnType(method);
            returnType = returnType == null ? new Type("void", false) : returnType;
            var params = getParameters(method);
            builder.append(" - " + returnType.print() + " " + method + "(");
            var paramsString = params.stream().map(param -> param != null ? param.print() : "<null param>")
                    .collect(Collectors.joining(", "));
            builder.append(paramsString + ")\n");
        }

        return builder.toString();
    }
}
