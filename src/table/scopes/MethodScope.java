package table.scopes;

import constants.Attributes;
import constants.NodeNames;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import utils.JmmNodeInfo;
import utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class MethodScope implements Scoped {
    private Type returnType = null;
    private final String name;
    private final List<Symbol> parameters = new ArrayList<>();
    private final List<Symbol> localVariables = new ArrayList<>();

    public MethodScope(String name) {
        this.name = name;
    }

    public Scoped add(JmmNode node) {
        if (NodeNames.type.equals(node.getKind()))
        {
            if (node.getParent().getKind().equals(NodeNames.method))
            {
                String returnNodeName = node.get(Attributes.name);
                returnType = new Type(returnNodeName, JmmNodeInfo.isArray(node, returnNodeName));
                return this;
            }

            String symbolName = node.getParent().get(Attributes.name);
            Symbol symbol = new Symbol(new Type(node.get(Attributes.name), JmmNodeInfo.isArray(node, symbolName)), symbolName);

            if (node.getParent().getKind().equals(NodeNames.parameter)) parameters.add(symbol);
            else localVariables.add(symbol);
        }
        else Logger.err("Trying to add an unrecognized node type to the method scope. ('" + node.getKind() + "')");

        return this;
    }

    public void setReturnType(Type type) {
        this.returnType = type;
    }

    public Type getReturnType() {
        return returnType;
    }

    public String getName() {
        return name;
    }

    public List<Symbol> getParameters() {
        return parameters;
    }

    public List<Symbol> getLocalVariables() {
        return localVariables;
    }
}
