package table;

import constants.Attributes;
import pt.up.fe.comp.jmm.JmmNode;
import utils.JmmNodeInfo;

import java.util.ArrayList;
import java.util.List;

public class MethodIdBuilder {
    // methodName-Type, (...)
    public String buildMethodId(JmmNode node) {
        List<JmmNode> children = new ArrayList<>(node.getChildren());
        String methodName = node.getOptional(Attributes.name).orElse("main");

        if (methodName.equals("main")) return buildMainMethodId(node);

        if (children.size() <= 2) return methodName;
        children.remove(0); // remove method return type child
        children.remove(children.size()-1); // remove method body child

        return methodName + "-" + buildParametersId(children);
    }

    private String buildParametersId(List<JmmNode> parameters) {
        List<String> parameterIds = new ArrayList<>();
        for (JmmNode parameter : parameters) {
            JmmNode parameterType = parameter.getChildren().get(0);
            boolean isArray = JmmNodeInfo.isArray(parameterType, null);

            String parameterId = "";
            parameterId += parameterType.get(Attributes.name);
            if (isArray) parameterId += "[]";

            parameterIds.add(parameterId);
        }
        return String.join(",", parameterIds);
    }

    private String buildMainMethodId(JmmNode node) {
        String parameterName = node.getChildren().get(0).get(Attributes.name);
        return "main-String[]:" + parameterName;
    }


}
