package table.scopes;

import pt.up.fe.comp.jmm.JmmNode;

public interface Scoped {

    /**
     * Adds a node to the scope (if applicable)
     * @param node the node to insert into the symbol table
     * @return returns the score of the node's children
     */
    Scoped add(JmmNode node);
}
