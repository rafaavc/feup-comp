package ollir;

public class IntermediateOllirRepresentation {
    private final String before, current;

    public IntermediateOllirRepresentation(String current, String before) {
        this.current = current;
        this.before = before;
    }

    public String getCurrent() {
        return current;
    }

    public String getBefore() {
        return before;
    }

    @Override
    public String toString() {
        return "\n# BEFORE:\n" + before + "\n\n# CURRENT:\n" + current + "\n";
    }
}
