package visitor;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.specs.util.SpecsCheck;
import table.scopes.Scoped;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class PopulateTableVisitor extends AJmmVisitor<Scoped, Scoped> {
    private final BiFunction<Scoped, List<Scoped>, Scoped> reduce;

    public PopulateTableVisitor(BiFunction<Scoped, List<Scoped>, Scoped> reduce) {
        this.reduce = reduce;
    }

    public PopulateTableVisitor() {
        this((nodeResult, childrenResults) -> nodeResult);
        setDefaultVisit((jmmNode, scope) -> scope.add(jmmNode));
    }

    @Override
    public Scoped visit(JmmNode jmmNode, Scoped scope) {
        SpecsCheck.checkNotNull(jmmNode, () -> "Node should not be null");

        var visit = getVisit(jmmNode.getKind());

        // Preorder: 1st visit the node
        var newScope = visit.apply(jmmNode, scope);

        // Preorder: then, visit each children
        List<Scoped> childrenResults = new ArrayList<>();
        for (var child : jmmNode.getChildren()) {
            childrenResults.add(visit(child, newScope));
        }

        return reduce.apply(scope, childrenResults);
    }
}
