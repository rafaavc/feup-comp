package visitor;

import constants.Attributes;
import constants.NodeNames;
import constants.Types;
import ollir.IntermediateOllirRepresentation;
import ollir.OllirBuilder;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import table.BasicSymbolTable;
import table.BasicSymbol;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Type;
import utils.Logger;
import visitor.scopes.ScopeVisitor;

import java.util.ArrayList;
import java.util.List;

public class OllirVisitor extends Visitor {
    private final OllirBuilder ollirBuilder;

    public OllirVisitor(BasicSymbolTable table, OllirBuilder ollirBuilder) {
        super(table);
        this.ollirBuilder = ollirBuilder;
    }

    private IntermediateOllirRepresentation getOllirRepresentation(JmmNode node, Type type, boolean inline) {
        return switch (node.getKind()) {
            case NodeNames.sum, NodeNames.mul, NodeNames.sub, NodeNames.div, NodeNames.lessThan, NodeNames.and, NodeNames.not -> {
                if (node.getChildren().size() > 2 || node.getChildren().size() < 1)
                    Logger.err("> Invalid node in IntermediateOllirRepresentation!");

                List<IntermediateOllirRepresentation> representations = new ArrayList<>();
                for (JmmNode child : node.getChildren())
                    representations.add(getOllirRepresentation(child, type, child.getKind().equals(NodeNames.objectProperty)));

                StringBuilder before = new StringBuilder();
                for (IntermediateOllirRepresentation rep : representations) before.append(rep.getBefore());

                String current;
                if (representations.size() == 1)
                    current = ollirBuilder.operatorNameToSymbol(node.getKind()) + representations.get(0).getCurrent();
                else
                    current = representations.get(0).getCurrent() + ollirBuilder.operatorNameToSymbol(node.getKind()) + representations.get(1).getCurrent();


                if (!inline) {
                    String auxName = ollirBuilder.getNextAuxName();
                    before.append(ollirBuilder.getAssignmentCustom(new BasicSymbol(type, auxName), current));
                    current = auxName + ollirBuilder.typeToCode(type);
                }

                yield new IntermediateOllirRepresentation(current, before.toString());
            }

            case NodeNames.newAlloc -> {
                String name = node.get(Attributes.name);
                if (name.equals(Types.integer)) {
                    // TODO
                    Logger.err("> array instantiation is not for checkpoint 2");
                    yield null;
                }
                /*
                    A.myClass :=.myClass new(myClass).myClass;
                    invokespecial(A.myClass,"<init>").V;
                 */

                String auxName = ollirBuilder.getNextAuxName();
                String instantiation = ollirBuilder.getAssignmentCustom(new BasicSymbol(type, auxName), ollirBuilder.getClassInstantiation(name)) + ollirBuilder.getClassInitCall(auxName, name);
                String current, before;
                if (!inline) {
                    before = "";
                    current = instantiation;
                } else {
                    before = instantiation;
                    current = auxName + ollirBuilder.typeToCode(type);
                }

                yield new IntermediateOllirRepresentation(current, before);
            }

            case NodeNames.objectProperty -> handleObjectProperty(node, type, inline);

            default -> new IntermediateOllirRepresentation(ollirBuilder.getOperandOllirRepresentation(node, new ScopeVisitor(symbolTable).visit(node), getNodeType(node)), "");
        };
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
                if (isField(symbol))
                    ollirBuilder.addPutField(symbol, representation.getCurrent());
                else
                    ollirBuilder.add(ollirBuilder.getAssignmentCustom(symbol, representation.getCurrent()));

                return;
            }
            case NodeNames.objectProperty, NodeNames.newAlloc -> {
                Type returnType = getNodeType(node);
                IntermediateOllirRepresentation representation = getOllirRepresentation(node, returnType, false);

                ollirBuilder.add(representation.getBefore());
                ollirBuilder.add(representation.getCurrent());
            }

            case NodeNames.returnStatement -> {
                JmmNode returnIdentifier = node.getChildren().get(0);
                Type returnType = getNodeType(returnIdentifier);
                IntermediateOllirRepresentation representation = getOllirRepresentation(returnIdentifier, returnType, true);

                ollirBuilder.add(representation.getBefore());
                ollirBuilder.addReturn(representation.getCurrent(), returnType);

                return;
            }
        }

        for (JmmNode child : children) recursiveVisit(child);
    }

    private boolean isField(Symbol symbol) {
        List<Symbol> fields = symbolTable.getFields();

        for (Symbol field : fields) {
            if (field.equals(symbol)) return true;
        }
        return false;
    }

    private IntermediateOllirRepresentation handleObjectProperty(JmmNode node, Type type, boolean inline) {
        JmmNode identifier = node.getChildren().get(0);
        JmmNode property = node.getChildren().get(1);

        if (property.getKind().equals(NodeNames.objectMethod)) {
            return handleObjectMethod(node, type, inline);
        } else if (property.getKind().equals(NodeNames.length)) {
            //TODO: when considering arrays
        }

        return new IntermediateOllirRepresentation("\t\tTODO in handleObjectProperty\n", "\t\tTODO in handleObjectProperty\n");
    }

    private IntermediateOllirRepresentation handleObjectMethod(JmmNode node, Type type, boolean inline) {
        JmmNode identifier = node.getChildren().get(0);
        JmmNode property = node.getChildren().get(1);

        List<JmmNode> parameters = property.getChildren();
        List<String> parametersRep = new ArrayList<>();
        StringBuilder before = new StringBuilder();

        for (JmmNode parameter : parameters) {
            IntermediateOllirRepresentation representation = getOllirRepresentation(parameter, getNodeType(parameter), true);
            before.append(representation.getBefore());
            parametersRep.add(representation.getCurrent());
        }

        String methodCallOllir;

        if (isImportedClassInstance(identifier)) {
            String identifierName = identifier.getOptional(Attributes.name).orElse(null);
            if (identifierName == null)
                return new IntermediateOllirRepresentation("\t\tTODO in handleObjectMethod\n", "\t\tTODO in handleObjectMethod\n");

            methodCallOllir = ollirBuilder.getStaticMethodCall(identifierName, property, type, parametersRep);
        } else if (identifier.getKind().equals(NodeNames.thisName)) {
            methodCallOllir = ollirBuilder.getVirtualMethodCall("this", property, type, parametersRep);
        } else {
            BasicSymbol symbol = getIdentifierSymbol(identifier);
            methodCallOllir = ollirBuilder.getVirtualMethodCall(symbol.getName(), symbol.getType(), property, type, parametersRep);
        }

        if (inline) {
            String auxName = ollirBuilder.getNextAuxName();
            before.append(ollirBuilder.getAssignmentCustom(new BasicSymbol(type, auxName), methodCallOllir));
            methodCallOllir = auxName + ollirBuilder.typeToCode(type);
        } else methodCallOllir = "\t\t" + methodCallOllir + "\n";

        return new IntermediateOllirRepresentation(methodCallOllir, before.toString());
    }
}
