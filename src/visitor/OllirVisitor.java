package visitor;

import constants.Attributes;
import constants.NodeNames;
import ollir.IntermediateOllirRepresentation;
import ollir.OllirBuilder;
import table.BasicSymbolTable;
import table.BasicSymbol;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Type;
import visitor.scopes.ScopeVisitor;

import java.util.ArrayList;
import java.util.List;

public class OllirVisitor extends Visitor {
    private final OllirBuilder ollirBuilder;

    public OllirVisitor(BasicSymbolTable table, OllirBuilder ollirBuilder) {
        super(table);
        this.ollirBuilder = ollirBuilder;
    }

    private IntermediateOllirRepresentation getOllirRepresentation(JmmNode node, Type type, boolean assignment) {
        switch (node.getKind()) {
            case NodeNames.sum, NodeNames.mul, NodeNames.sub, NodeNames.div, NodeNames.lessThan, NodeNames.and -> {
                IntermediateOllirRepresentation leftChild = getOllirRepresentation(node.getChildren().get(0), type, false);
                IntermediateOllirRepresentation rightChild = getOllirRepresentation(node.getChildren().get(1), type, false);

                String before = leftChild.getBefore() + rightChild.getBefore();
                String current = leftChild.getCurrent() + ollirBuilder.operatorNameToSymbol(node.getKind()) + rightChild.getCurrent();

                if (!assignment) {
                    String name = ollirBuilder.getNextAuxName();
                    before += ollirBuilder.getAssignmentCustom(new BasicSymbol(type, name), current);
                    current = name + ollirBuilder.typeToCode(type);
                }

                return new IntermediateOllirRepresentation(current, before);
            }
            case NodeNames.not -> {
                IntermediateOllirRepresentation child = getOllirRepresentation(node.getChildren().get(0), type, false);

                String before = child.getBefore();
                String current = ollirBuilder.operatorNameToSymbol(node.getKind()) + child.getCurrent();

                if (!assignment) {
                    String name = ollirBuilder.getNextAuxName();
                    before += ollirBuilder.getAssignmentCustom(new BasicSymbol(type, name), current);
                    current = name + ollirBuilder.typeToCode(type);
                }

                System.out.println("BEFORE: " + before);
                System.out.println("CURRENT: " + current);

                return new IntermediateOllirRepresentation(current, before);
            }
        }
        return new IntermediateOllirRepresentation(ollirBuilder.getOperandOllirRepresentation(node, new ScopeVisitor(symbolTable).visit(node), getNodeType(node)), "");
    }

    public void visitNode(JmmNode node) {
        ollirBuilder.addConstructor();
        recursiveVisit(node);
    }

    private void recursiveVisit(JmmNode node) {
        System.out.println("NODE: " + node);
        Type nodeType = getNodeType(node);
        String nodeKind = node.getKind();
        List<JmmNode> children = node.getChildren();

        switch (nodeKind) {
            case NodeNames.method, NodeNames.mainMethod -> ollirBuilder.addMethod(node);
            case NodeNames.assignment -> {
                BasicSymbol symbol = getIdentifierSymbol(node.getChildren().get(0));
                JmmNode rightSide = node.getChildren().get(1);

                IntermediateOllirRepresentation representation = getOllirRepresentation(rightSide, symbol.getType(), true);

                ollirBuilder.add(representation.getBefore());
                ollirBuilder.add(ollirBuilder.getAssignmentCustom(symbol, representation.getCurrent()));

                return;
            }
            case NodeNames.objectProperty ->
                handleObjectProperty(node);

            case NodeNames.returnStatement ->
                handleReturn(node);
        }

        for (JmmNode child : children) recursiveVisit(child);
    }

    private void handleObjectProperty(JmmNode node) {
        JmmNode identifier = node.getChildren().get(0);
        JmmNode property = node.getChildren().get(1);

        Type returnType = getNodeType(node);
        if (property.getKind().equals(NodeNames.objectMethod)) {
            List<JmmNode> parameters = property.getChildren();
            List<String> parametersRep = new ArrayList<>();

            for (JmmNode parameter : parameters) {
                IntermediateOllirRepresentation representation = getOllirRepresentation(parameter, getNodeType(parameter), false);
                ollirBuilder.add(representation.getBefore());
                parametersRep.add(representation.getCurrent());
            }

            if (isImportedClassInstance(identifier)) {
                String identifierName = identifier.getOptional(Attributes.name).orElse(null);
                if (identifierName == null) return;
                ollirBuilder.addStaticMethodCall(identifierName, property, returnType, parametersRep);
            } else {
                BasicSymbol symbol = getIdentifierSymbol(identifier);
                ollirBuilder.addVirtualMethodCall(symbol.getName(), symbol.getType(), property, returnType, parametersRep);
            }

        } else if (property.getKind().equals(NodeNames.length)) {
            //TODO: when considering arrays
        }
    }

    private void handleReturn(JmmNode node) {

    }
}
