package optimization;

import constants.Ollir;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import static java.lang.Character.isDigit;

public class WhileOptimization {
    public OllirResult optimize(OllirResult ollirResult) {
        System.out.println("WHILE OPTIMIZE");
        System.out.println(findAndUpdateLoop(ollirResult.getOllirCode()));
        return ollirResult;
    }

    private String findAndUpdateLoop(String ollirCode) {
        StringBuilder newOllirCode = new StringBuilder();
        StringBuilder ollirLoopCode = new StringBuilder();
        boolean isInLoop = false;
        int loopId;
        int loopIdSafe = -1;

        for (int i = 0; i < ollirCode.length(); i++) {
            loopId = isLoopStart(ollirCode, i);
            if (loopId != -1) {
                loopIdSafe = loopId;
                isInLoop = true;
                ollirLoopCode.append(ollirCode.charAt(i));
            } else if (isInLoop && isLoopEnd(ollirCode, i, loopIdSafe)) {
                isInLoop = false;
                newOllirCode.append(updateLoop(ollirLoopCode.toString()));
            } else if (isInLoop) {
                ollirLoopCode.append(ollirCode.charAt(i));
            } else {
                newOllirCode.append(ollirCode.charAt(i));
            }
        }

        return newOllirCode.toString();
    }

    private String updateLoop(String ollirLoopCode) {
        System.out.println("LOOP");
        System.out.println(ollirLoopCode);
        System.out.println("ENDLOOP");
        return "";
    }

    private int isLoopStart(String ollirCode, int pos) {
        if (pos + Ollir.loop.length() > ollirCode.length()) return -1;

        for (char c : Ollir.loop.toCharArray()) {
            if (c != ollirCode.charAt(pos++)) return -1;
        }
        StringBuilder gotLoopId = new StringBuilder();
        while (isDigit(ollirCode.charAt(pos))) {
            gotLoopId.append(ollirCode.charAt(pos));
            pos++;
        }

        if (ollirCode.charAt(pos) == ':')
            return Integer.parseInt(gotLoopId.toString());
        return -1;
    }

    private boolean isLoopEnd(String ollirCode, int pos, int loopId) {
        if (pos + Ollir.loopEnd.length() > ollirCode.length()) return false;

        for (char c : Ollir.loopEnd.toCharArray()) {
            if (c != ollirCode.charAt(pos++)) return false;
        }
        StringBuilder gotLoopId = new StringBuilder();
        while (isDigit(ollirCode.charAt(pos))) {
            gotLoopId.append(ollirCode.charAt(pos));
            pos++;
        }

        System.out.println("ID: " + loopId + " LOOP ID: " + gotLoopId);
        if (ollirCode.charAt(pos) == ':')
            return loopId == Integer.parseInt(gotLoopId.toString());
        return false;
    }
}
