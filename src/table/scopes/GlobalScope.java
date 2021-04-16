package table.scopes;

import constants.Attributes;
import constants.NodeNames;
import pt.up.fe.comp.jmm.JmmNode;
import utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class GlobalScope implements Scoped {
    private final List<String> imports = new ArrayList<>();
    private ClassScope classScope;

    public Scoped add(JmmNode node) {
        switch (node.getKind()) {
            case NodeNames.classDeclaration:
                String className = node.get(Attributes.name);
                String classExtends = node.getOptional(Attributes.extend).orElse(null);

                classScope = new ClassScope(className, classExtends);
                return classScope;

            case NodeNames.importNode:
                String importName = node.get(Attributes.name);
                imports.add(importName);
                break;

            default:
                Logger.err("Trying to add an unrecognized node type to the global scope. ('" + node.getKind() + "')");
                break;
        }
        return this;
    }

    public ClassScope getClassScope() {
        return classScope;
    }

    public List<String> getImports() {
        return imports;
    }
}
