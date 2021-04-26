package table.scopes;

import constants.Attributes;
import constants.NodeNames;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Type;
import table.BasicSymbol;
import utils.JmmNodeInfo;

import java.util.ArrayList;
import java.util.List;

public class MethodScope implements Scoped {
    private Type returnType = null;
    private final String name;
    private final List<BasicSymbol> parameters = new ArrayList<>();
    private final List<BasicSymbol> localVariables = new ArrayList<>();

    public MethodScope(String name) {
        this.name = name;
    }

    public Scoped add(JmmNode node) {
        if (NodeNames.mainParameter.equals(node.getKind()))
        {
            parameters.add(new BasicSymbol(new Type("String", true), node.get(Attributes.name)));
        }
        else if (NodeNames.type.equals(node.getKind()))
        {
            if (node.getParent().getKind().equals(NodeNames.method))
            {
                String returnNodeName = node.get(Attributes.name);
                returnType = new Type(returnNodeName, JmmNodeInfo.isArray(node, returnNodeName));
                return this;
            }

            String symbolName = node.getParent().get(Attributes.name);
            BasicSymbol symbol = new BasicSymbol(new Type(node.get(Attributes.name), JmmNodeInfo.isArray(node, symbolName)), symbolName);

            if (node.getParent().getKind().equals(NodeNames.parameter)) parameters.add(symbol);
            else localVariables.add(symbol);
        }

        return this;
    }

    public Type getReturnType() {
        return returnType;
    }

    public String getName() {
        return name;
    }

    public List<BasicSymbol> getParameters() {
        return parameters;
    }

    public List<BasicSymbol> getLocalVariables() {
        return localVariables;
    }
}
