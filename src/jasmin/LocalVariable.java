package jasmin;

import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.Operand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LocalVariable {
    private int nextLocalVariable = 1;
    private final Map<String, Integer> identifiers = new HashMap<>();

    public LocalVariable(ArrayList<Element> parameters) {
        for (Element parameter : parameters) {
            addCorrespondence(((Operand)parameter).getName(), getNextLocalVariable());
        }
    }

    public int getNextLocalVariable() {
        int tmp = nextLocalVariable;
        nextLocalVariable++;
        return tmp;
    }

    public void addCorrespondence(String identifier, int localVariable) {
        identifiers.put(identifier, localVariable);
    }

    public int getCorrespondence(String identifier) {
        return identifiers.get(identifier);
    }
}
