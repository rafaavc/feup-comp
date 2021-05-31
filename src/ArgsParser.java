import utils.Logger;

public class ArgsParser {
    final String filename;
    boolean optimizeO = false;
    int optimizeR = -1;

    public String getUsage() {
        return "Usage:\n./comp2021-3d Main <testfilepath> [-r=<n>] [-o]\n\nWhere:\n\tn is a positive integer\n\t-r assigns the variables to n register\n\t-o activates constant propagation and whiles goto optimizations";
    }

    public ArgsParser(String[] args) throws Exception {
        if (args.length < 1) throw new Exception(getUsage());
        filename = args[0];

        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("-o")) optimizeO = true;
            else if (args[i].contains("-r")) {
                try {
                    optimizeR = Integer.parseInt(args[i].split("=")[1]);
                    if (optimizeR <= 0) throw new NumberFormatException();
                } catch(Exception e) {
                    throw new Exception(getUsage());
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
