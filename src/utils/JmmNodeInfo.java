package utils;

import pt.up.fe.comp.jmm.JmmNode;

public class JmmNodeInfo {
    public static boolean isArray(JmmNode node, String name) {
        String array = node.get("array");
        boolean val = array != null && array.equals("true");
        if (val) Logger.log("Found array variable: '" + name + "'");
        return val;
    }
}
