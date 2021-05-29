public class ArgsParser {
    final String filename;
    boolean optimizeO = false;
    int optimizeR = -1;

    public ArgsParser(String[] args) {
        filename = args[0];
        if (args.length < 2) return;

        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("-o")) optimizeO = true;
            else if (args[i].contains("-r"))
                optimizeR = Integer.parseInt(args[i].split("=")[1]);
        }
    }

    public String getFilename() {
        return filename;
    }

    public boolean isOptimizeO() {
        return optimizeO;
    }

    public int getOptimizeR() {
        return optimizeR;
    }
}
