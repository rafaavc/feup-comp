import utils.Logger;

public class ArgsParser {
    final String filename;
    boolean optimizeO = false;
    int optimizeR = -1;

    public ArgsParser(String[] args) {
        filename = args[0];
        if (args.length < 2) return;

        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("-o")) optimizeO = true;
            else if (args[i].contains("-r")) {
                try {
                    optimizeR = Integer.parseInt(args[i].split("=")[1]);
                } catch(NumberFormatException e) {
                    Logger.err("The value of -r must be an integer.");
                }
            }
        }
    }

    public String getFilename() {
        return filename;
    }

    public boolean isOptimizeO() {
        return optimizeO;
    }

    public boolean isOptimizeR() {
        return optimizeR > 0;
    }

    public int getOptimizeR() {
        return optimizeR;
    }
}
