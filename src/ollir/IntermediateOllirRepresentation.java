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
}
