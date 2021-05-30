package optimization;

public class LivenessRange {
    private final int start;
    private int end = -1;

    public LivenessRange(int start) {
        this.start = start;
    }

    public LivenessRange(int start, int end) {
        this(start);
        this.end = end;
    }

    public void setEnd(int end) throws Exception {
        if (end < 0) throw new Exception("Trying to set end to a negative value");
        this.end = end;
    }

    public void removeEnd() {
        this.end = -1;
    }

    public boolean hasEnd() {
        return end != -1;
    }

    public int getEnd() throws Exception {
        if (end < 0) throw new Exception("Trying to access property end of LivenessRange without it being set!");
        return end;
    }

    public int getStart() {
        return start;
    }

    public String toString() {
        return start + ":" + end;
    }
}
