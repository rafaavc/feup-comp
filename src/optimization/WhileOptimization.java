package optimization;

import constants.Ollir;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import static java.lang.Character.isDigit;

public class WhileOptimization {
    public OllirResult optimize(JmmSemanticsResult semanticsResult, OllirResult ollirResult) {
        String optimizedCode = findAndUpdateLoop(ollirResult.getOllirCode());
        System.out.println("## Got the ollir code after while loop optimization:\n");
        System.out.println(optimizedCode);
        return new OllirResult(semanticsResult, optimizedCode, semanticsResult.getReports());
    }

    private String findAndUpdateLoop(String ollirCode) {
        StringBuilder newOllirCode = new StringBuilder();
        StringBuilder ollirLoopCode = new StringBuilder();
        boolean isInLoop = false;
        int loopId;
        int loopIdSafe = -1;

        for (int i = 0; i < ollirCode.length(); i++) {
            loopId = isLoopStart(ollirCode, i);
            if (!isInLoop && loopId != -1) {
                loopIdSafe = loopId;
                isInLoop = true;
                ollirLoopCode.append("\t\t").append(ollirCode.charAt(i));
            } else if (isInLoop && isLoopEnd(ollirCode, i, loopIdSafe, ollirLoopCode)) {
                isInLoop = false;
                newOllirCode.append(updateLoop(ollirLoopCode.toString(), loopIdSafe));
                while (ollirCode.charAt(i) != '\n') i++;
            } else if (isInLoop) {
                ollirLoopCode.append(ollirCode.charAt(i));
            } else {
                newOllirCode.append(ollirCode.charAt(i));
            }
        }

        return newOllirCode.toString();
    }

    private String updateLoop(String ollirLoopCode, Integer loopId) {
        System.out.println("LOOP " + loopId);
        StringBuilder code = new StringBuilder();
        String[] instructions = ollirLoopCode.split("\n");
        String ifInstruction = getIfInstruction(instructions);
        String bodyInstruction = getBodyInstruction(instructions, loopId);

        code.append("\tgoto ").append(Ollir.loopEnd).append(loopId).append(";\n");
        code.append(bodyInstruction);
        code.append("\t\t").append(Ollir.loopEnd).append(loopId).append(":\n");
        code.append(ifInstruction);
        return code.toString();
    }

    private String getIfInstruction(String[] instructions) {
        StringBuilder ifInstruction = new StringBuilder();

        // 1st instruction is loop label; last is loop end label
        for (int i = 1; i < instructions.length - 1; i++) {
            ifInstruction.append(instructions[i]).append("\n");
            if (instructions[i].contains("if")) return ifInstruction.toString();
        }
        return ifInstruction.toString();
    }

    private String getBodyInstruction(String[] instructions, Integer loopId) {
        StringBuilder bodyInstruction = new StringBuilder();
        boolean add = false;
        String body = Ollir.loopBody + loopId + ":";
        String gotoLoop = "goto " + Ollir.loop + loopId + ";";
        String bodyHeader = "";

        // 1st instruction is loop label; last is loop end label
        for (int i = 1; i < instructions.length - 1; i++) {
            String instruction = instructions[i];
            if (instruction.contains(gotoLoop)) return bodyHeader + findAndUpdateLoop(bodyInstruction.toString());
            if (add) bodyInstruction.append(instruction).append("\n");
            else if (instruction.contains(body)) {
                add = true;
                bodyHeader = instruction + "\n";
            }
        }

        return bodyHeader + findAndUpdateLoop(bodyInstruction.toString());
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

    private boolean isLoopEnd(String ollirCode, int pos, int loopId, StringBuilder ollirLoopCode) {
        if (pos + Ollir.loopEnd.length() > ollirCode.length()) return false;

        for (char c : Ollir.loopEnd.toCharArray()) {
            if (c != ollirCode.charAt(pos++)) return false;
        }
        StringBuilder gotLoopId = new StringBuilder();
        while (isDigit(ollirCode.charAt(pos))) {
            gotLoopId.append(ollirCode.charAt(pos));
            pos++;
        }

        if (ollirCode.charAt(pos) == ':' && loopId == Integer.parseInt(gotLoopId.toString())) {
            ollirLoopCode.append(Ollir.loopEnd).append(loopId).append(":");
            return true;
        }
        return false;
    }
}
