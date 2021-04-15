package table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import table.scopes.ClassScope;
import table.scopes.GlobalScope;

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
}
