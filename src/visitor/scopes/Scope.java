package visitor.scopes;

import constants.NodeNames;
import pt.up.fe.comp.jmm.JmmNode;

import java.util.HashMap;

public class Scope {
    private final HashMap<String, JmmNode> scopes = new HashMap<>();

    public void add(String nodeName, JmmNode node) {
        scopes.put(nodeName, node);
    }

    public JmmNode getMethodScope() {
        return scopes.get(NodeNames.method);
    }
}
