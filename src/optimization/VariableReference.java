package optimization;

public class VariableReference {
    private final String name;
    private final int parameterId;
    private LivenessRange livenessRange;

    public VariableReference(String name, int parameterId) {
        this.name = name;
        this.parameterId = parameterId;
    }

    public String getName() {
        return name;
    }

    public int getParamterId() {
        return parameterId;
    }

    public boolean isParameter() {
        return parameterId > 0;
    }

    public void setLivenessRange(LivenessRange range) {
        this.livenessRange = range;
    }

    public LivenessRange getLivenessRange() {
        return livenessRange;
    }
}
