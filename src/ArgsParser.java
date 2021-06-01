import utils.Logger;

public class ArgsParser {
    private final String filepath;
    private boolean optimizeO = false;
    private int optimizeR = -1;
    private int maxReports = 15;
    private final boolean isRun;
    private boolean toDir = false;
    private final String stdin;

    public String getUsage() {
        return "Usage:\n./comp2021-3d Main <jmmfilepath> [-e=<n1>] [-r=<n2>] [-o] [-d]\n\nWhere:\n\t-e shows n1 reports (n1 >= 0 and default = 15)\n\t-d saves the output files in the /compiled/<classname>/ folder\n\t-r assigns the variables to n2 registers (n2 > 0)\n\t-o activates constant propagation and whiles goto optimizations\n\nor\n\n./comp2021-3d Main run <jasminfilepath> [stdin]\n\nWhere:\n\tjasminfilepath is the path of the jasmin code file to run\n\tstdin is the input to be given to the program\n";
    }

    public ArgsParser(String[] args) throws Exception {
        if (args.length < 1) throw new Exception(getUsage());
        if (args[0].equals("run")) {
            if (args.length < 2) throw new Exception(getUsage());

            isRun = true;
            filepath = args[1];
            if (args.length >= 3)
                stdin = args[2];
            else stdin = null;
            return;
        }
        stdin = null;
        isRun = false;
        filepath = args[0];

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
            else if (args[i].contains("-e")) {
                try {
                    maxReports = Integer.parseInt(args[i].split("=")[1]);
                    if (maxReports < 0) throw new NumberFormatException();
                } catch(Exception e) {
                    throw new Exception(getUsage());
                }
            }
            else if (args[i].contains("-d")) toDir = true;
        }
    }

    public int getMaxReports() {
        return maxReports;
    }

    public boolean hasStdin() {
        return stdin != null;
    }

    public String getStdin() {
        return stdin;
    }

    public boolean isRun() {
        return isRun;
    }

    public boolean toDir() {
        return toDir;
    }

    public String getFilePath() {
        return filepath;
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
