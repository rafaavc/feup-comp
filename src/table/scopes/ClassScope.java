package table.scopes;

import constants.Attributes;
import constants.NodeNames;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Type;
import table.BasicSymbol;
import utils.Logger;

import java.util.*;

import static utils.JmmNodeInfo.isArray;

public class ClassScope implements Scoped {
    private final String className, superClassName;
    private final List<BasicSymbol> fields = new ArrayList<>();
    private final Map<String, MethodScope> methods = new HashMap<>();

    public ClassScope(String className, String superClassName) {
        this.className = className;
        this.superClassName = superClassName;
    }

    public Scoped add(JmmNode node) {

        switch(node.getKind()) {
            case NodeNames.method:
            case NodeNames.mainMethod:
                String methodName = node.getOptional(Attributes.name).orElse("main");
                MethodScope methodScope = new MethodScope(methodName);
                methods.put(methodName, methodScope);
                return methodScope;

            case NodeNames.type:
                JmmNode parent = node.getParent();
                String variableName = parent.get(Attributes.name);

                BasicSymbol symbol = new BasicSymbol(new Type(node.get(Attributes.name), isArray(node, variableName)), variableName);
                fields.add(symbol);
                break;

            default:
                Logger.err("Trying to add an unrecognized node type to the class scope. ('" + node.getKind() + "')");
                break;
        }

        return this;
    }

    public String getClassName() {
        return className;
    }

    public String getSuperClassName() {
        return superClassName;
    }

    public List<BasicSymbol> getFields() {
        return fields;
    }

    public Map<String, MethodScope> getMethods() {
        return methods;
    }

    public MethodScope getMethod(String name) {
        return methods.get(name);
    }
}
