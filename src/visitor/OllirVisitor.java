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
            case NodeNames.sum, NodeNames.mul, NodeNames.sub, NodeNames.div, NodeNames.lessThan, NodeNames.and, NodeNames.not -> {
                List<IntermediateOllirRepresentation> representations = new ArrayList<>();
                for (JmmNode child: node.getChildren()) representations.add(getOllirRepresentation(child, type, false));

                StringBuilder before = new StringBuilder();
                for (IntermediateOllirRepresentation rep : representations) before.append(rep.getBefore());

                String current;
                if (representations.size() == 1) current = ollirBuilder.operatorNameToSymbol(node.getKind()) + representations.get(0).getCurrent();
                else current = representations.get(0).getCurrent() + ollirBuilder.operatorNameToSymbol(node.getKind()) + representations.get(1).getCurrent();


                if (!assignment) {
                    String name = ollirBuilder.getNextAuxName();
                    before.append(ollirBuilder.getAssignmentCustom(new BasicSymbol(type, name), current));
                    current = name + ollirBuilder.typeToCode(type);
                }

                return new IntermediateOllirRepresentation(current, before.toString());
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
            case NodeNames.objectProperty -> {
                JmmNode identifier = node.getChildren().get(0);
                System.out.println("identifier: " + identifier);
                JmmNode property = node.getChildren().get(1);

                Type returnType = getNodeType(node);
                if (property.getKind().equals(NodeNames.objectMethod)) {
                    System.out.println("here MODAFOCA: " + node);
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

                        ollirBuilder.addMethodCall(identifierName, null, property, returnType, parametersRep, true);
                    } else {
                        BasicSymbol symbol = getIdentifierSymbol(identifier);
                        ollirBuilder.addMethodCall(symbol.getName(), symbol.getType(), property, returnType, parametersRep, false);
                    }

                } else if (property.getKind().equals(NodeNames.length)) {
                    //TODO: when considering arrays
                }
            }
        }

        for (JmmNode child : children) recursiveVisit(child);
    }
}
