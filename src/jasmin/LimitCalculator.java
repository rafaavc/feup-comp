package jasmin;

import org.specs.comp.ollir.Descriptor;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.VarScope;

import java.util.HashMap;

public class LimitCalculator {
    public int limitLocals(Method m) {
        int locals = 0;
        HashMap<String, Descriptor> variables = m.getVarTable();

        for (String key : variables.keySet()) {
            if (variables.get(key).getScope() == VarScope.FIELD || key.equals("this")) continue;
            locals++;
        }

        return locals + 1;
    }

    public String limitStack(String jasminCode) {
        int maxCount = 0;
        int currentCount = 0;
        String[] instructions = jasminCode.split("\n");

        for (String instruction : instructions) {
            if (instruction.matches("^\t([ai]load|[ai]const|ldc|bipush|new|dup).*")) {
                currentCount++;
            } else {
                if (currentCount > maxCount) maxCount = currentCount;
                currentCount = 0;
            }
        }

        return jasminCode.replaceFirst("\\.limit stack 0", ".limit stack " + maxCount);
    }
}
