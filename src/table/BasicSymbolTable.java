package table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import table.scopes.ClassScope;
import table.scopes.GlobalScope;
import utils.Logger;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<String> getMethods() {
        ClassScope classScope = global.getClassScope();
        return new ArrayList<>(classScope.getMethods().keySet());
    }

    @Override
    public Type getReturnType(String methodName) {
        ClassScope classScope = global.getClassScope();
        return classScope.getMethod(methodName).getReturnType();
    }

    @Override
    public List<Symbol> getParameters(String methodName) {
        ClassScope classScope = global.getClassScope();
        return classScope.getMethod(methodName).getParameters();
    }

    @Override
    public List<Symbol> getLocalVariables(String methodName) {
        ClassScope classScope = global.getClassScope();
        return classScope.getMethod(methodName).getLocalVariables();
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
            Logger.log("- " + method + ", returns " + getReturnType(method));
            Logger.log("");
            Logger.log("#### Parameters");
            for (Symbol param : getParameters(method)) Logger.log("-" + param);
            Logger.log("");
            Logger.log("#### Local variables");
            for (Symbol param : getLocalVariables(method)) Logger.log("-" + param);
            Logger.log("");
        }
    }
}
