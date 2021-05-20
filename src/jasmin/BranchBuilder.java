package jasmin;

import constants.Ollir;
import org.specs.comp.ollir.Operation;

public class BranchBuilder {
    private int ifCount = 0;

    public String buildBranchInstruction(Operation operation) {
        String header = switch (operation.getOpType()) {
            case LTH -> "if_icmplt";
            case GTH -> "if_icmpgt";
            case EQ -> "if_icmpeq";
            case NEQ -> "if_icmpne";
            case LTE -> "if_icmple";
            case GTE -> "if_icmpge";
            case ANDB -> "iand\n\tifne";
            default -> "Operation in if condition not being taken into consideration!";
        };
        return "\t" + header + " " + Ollir.ifBody + ++ifCount + "\n";
    }
}
