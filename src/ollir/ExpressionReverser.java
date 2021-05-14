package ollir;

import constants.NodeNames;
import pt.up.fe.comp.jmm.JmmNode;

public class ExpressionReverser {
    public IntermediateOllirRepresentation reverse(JmmNode expression, String representation, OllirBuilder ollirBuilder) {
        return switch (expression.getKind()) {
            case NodeNames.and -> reverseAnd(representation, ollirBuilder);
            case NodeNames.lessThan -> reverseLessThan(representation);
            case NodeNames.not -> reverseNot(representation);
            default -> new IntermediateOllirRepresentation(representation, "");
        };
    }

    private IntermediateOllirRepresentation reverseAnd(String representation, OllirBuilder ollirBuilder) {
        String aux = ollirBuilder.getNextAuxName() + ".bool";
        String before = aux + " :=.bool " + representation;
        return new IntermediateOllirRepresentation("!.bool " + aux, before);
    }

    private IntermediateOllirRepresentation reverseLessThan(String representation) {
        return new IntermediateOllirRepresentation(representation.replaceAll("<", ">="), "");
    }

    private IntermediateOllirRepresentation reverseNot(String representation) {
        return new IntermediateOllirRepresentation(representation.replaceFirst("!.bool ", ""), "");
    }
}
