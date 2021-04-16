package visitor;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.specs.util.SpecsCheck;
import table.scopes.Scoped;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class Visitor extends AJmmVisitor<Data, Scoped> {
    private final BiFunction<Scoped, List<Scoped>, Scoped> reduce;

    public Visitor(BiFunction<Scoped, List<Scoped>, Scoped> reduce) {
        this.reduce = reduce;
    }

    public Visitor() {
        this((nodeResult, childrenResults) -> nodeResult);
        setDefaultVisit((jmmNode, data) -> data.getScope().add(jmmNode));
    }

    @Override
    public Scoped visit(JmmNode jmmNode, Data data) {
        SpecsCheck.checkNotNull(jmmNode, () -> "Node should not be null");

        var visit = getVisit(jmmNode.getKind());

        // Preorder: 1st visit the node
        var scope = visit.apply(jmmNode, data);
        var dataCopy = new Data(scope, data.getReports());

        // Preorder: then, visit each children
        List<Scoped> childrenResults = new ArrayList<>();
        for (var child : jmmNode.getChildren()) {
            childrenResults.add(visit(child, dataCopy));
        }

        return reduce.apply(scope, childrenResults);
    }
}
