package visitor;

import constants.Attributes;
import constants.NodeNames;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.report.Report;
import table.BasicSymbolTable;
import utils.Logger;

import java.util.List;

public class PropertyVisitor extends Visitor {
    public PropertyVisitor(BasicSymbolTable symbolTable) {
        super(symbolTable);

        addVisit(NodeNames.objectProperty, this::visitMethod);
    }

    private Boolean visitMethod(JmmNode node, List<Report> reports) {
        System.out.println("object property");

        JmmNode object = node.getChildren().get(0);
        JmmNode property = node.getChildren().get(1);
        if (property.getKind().equals(NodeNames.length)) {
            if (!handleLength(object)) {
                //TODO: add to reports
                System.out.println("!!! Length not applied to array !!!");
            }
        }
        else if (property.getKind().equals(NodeNames.objectMethod))
            if (!handleMethod(node, property)) {
                //TODO: add to reports
                System.out.println("!!! Wrong method call !!!");
                System.out.println(node);
            }

        return true;
    }

    private Boolean handleLength(JmmNode object) {
        if (object.getKind().equals(NodeNames.identifier)) {
            Type type = getIdentifierType(object);
            return type.isArray();
        }
        Logger.log("[Check] Length not applied to identifier");
        return false;
    }

    //TODO: might need changes when method overload
    private Boolean handleMethod(JmmNode node, JmmNode method) {
        if (getMethodType(node) == null) return false;

        String methodName = method.getOptional(Attributes.name).orElse(null);
        if (methodName == null) return false;

        if (symbolTable.getReturnType(methodName) == null)
            return true;

        List<JmmNode> parameters = method.getChildren();
        List<Symbol> expectedParameters = symbolTable.getParameters(methodName);

        if (parameters.size() != expectedParameters.size())
            return false;

        for (int i = 0; i < parameters.size(); i++) {
            Type type = getNodeType(parameters.get(i));
            Type expectedType = expectedParameters.get(i).getType();
            if (!type.equals(expectedType)) return false;
        }

        return true;
    }
}
