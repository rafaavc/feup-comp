package visitor;

import constants.Attributes;
import constants.NodeNames;
import constants.Types;
import ollir.ExpressionReverser;
import ollir.IntermediateOllirRepresentation;
import ollir.OllirBuilder;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import table.BasicSymbolTable;
import table.BasicSymbol;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Type;
import utils.Logger;
import visitor.scopes.Scope;
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
                    current = representations.get(0).getCurrent() + ollirBuilder.operatorNameToSymbol(node.getKind())
                            + representations.get(1).getCurrent();


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

            default -> ollirBuilder.getOperandOllirRepresentation(node, new ScopeVisitor(symbolTable).visit(node), typeInterpreter.getNodeType(node));
        };
    }

    public void visitNode(JmmNode node) {
        String nodeKind = node.getKind();
        List<JmmNode> children = node.getChildren();

        switch (nodeKind) {
            case NodeNames.method, NodeNames.mainMethod -> ollirBuilder.addMethod(node);
            case NodeNames.assignment -> {
                BasicSymbol symbol = typeInterpreter.getIdentifierSymbol(node.getChildren().get(0));
                JmmNode rightSide = node.getChildren().get(1);

                IntermediateOllirRepresentation representation = getOllirRepresentation(rightSide, symbol.getType(), true);

                ollirBuilder.add(representation.getBefore());
                if (ollirBuilder.isField(symbol))
                    ollirBuilder.addPutField(symbol, representation.getCurrent());
                else
                    ollirBuilder.add(ollirBuilder.getAssignmentCustom(symbol, representation.getCurrent()));

                return;
            }
            case NodeNames.objectProperty, NodeNames.newAlloc -> {
                Type returnType = typeInterpreter.getNodeType(node);
                IntermediateOllirRepresentation representation = getOllirRepresentation(node, returnType, false);

                ollirBuilder.add(representation.getBefore());
                ollirBuilder.add(representation.getCurrent());

                return;
            }

            case NodeNames.returnStatement -> {
                JmmNode returnIdentifier = node.getChildren().get(0);
                Scope nodeScope = new ScopeVisitor(symbolTable).visit(returnIdentifier);
                Type returnType = symbolTable.getReturnType(methodIdBuilder.buildMethodId(nodeScope.getMethodScope()));

                IntermediateOllirRepresentation representation = getOllirRepresentation(returnIdentifier, returnType, false);

                ollirBuilder.add(representation.getBefore());
                ollirBuilder.addReturn(representation.getCurrent(), returnType);

                return;
            }

            case NodeNames.ifElse -> {
                JmmNode conditionExp = children.get(0).getChildren().get(0);
                Type returnType = typeInterpreter.getNodeType(conditionExp);
                System.out.println("Condition return type (should be boolean): " + returnType);

                JmmNode ifStatement = children.get(1);
                JmmNode elseStatement = children.get(2);

                IntermediateOllirRepresentation conditionExpRep = getOllirRepresentation(conditionExp, returnType, true);
                IntermediateOllirRepresentation reverseRep = new ExpressionReverser().reverse(conditionExp, conditionExpRep.getCurrent(), ollirBuilder);

                if (!conditionExpRep.getBefore().equals("")) ollirBuilder.add("\t\t" + conditionExpRep.getBefore() + "\n");
                if (!reverseRep.getBefore().equals("")) ollirBuilder.add("\t\t" + reverseRep.getBefore() + "\n");

                ollirBuilder.addIf(reverseRep.getCurrent());
                for (JmmNode ifChild : ifStatement.getChildren()) visitNode(ifChild);
                ollirBuilder.add("\t\tgoto endif\n");

		        ollirBuilder.add("\telse:\n");
                for (JmmNode elseChild : elseStatement.getChildren()) visitNode(elseChild);
                ollirBuilder.add("\tendif:\n");

                return;
            }
        }

        for (JmmNode child : children) visitNode(child);
    }

    private IntermediateOllirRepresentation handleObjectProperty(JmmNode node, Type type, boolean inline) {
        JmmNode property = node.getChildren().get(1);
        JmmNode parent = node.getParent();

        Type expectedType = switch (parent.getKind()) {
            case NodeNames.returnStatement -> {
                Scope nodeScope = new ScopeVisitor(symbolTable).visit(parent);
                yield symbolTable.getReturnType(methodIdBuilder.buildMethodId(nodeScope.getMethodScope()));
            }
            case NodeNames.assignment -> typeInterpreter.getNodeType(parent.getChildren().get(0));
            case NodeNames.lessThan, NodeNames.sum, NodeNames.sub, NodeNames.mul, NodeNames.div -> new Type(Types.integer, false);
            case NodeNames.and, NodeNames.not -> new Type(Types.bool, false);
            case NodeNames.objectMethod -> {
                List<JmmNode> children = parent.getChildren();
                System.out.println("## PARENT: " + parent);
                for (int i = 0; i < children.size(); i++) {
                    System.out.println("### CHILD: " + children.get(i));
                    if (children.get(i) == node) {
                        System.out.println("#### EQUALS!!!");
                        List<Symbol> parameters = symbolTable.getParameters(typeInterpreter.buildMethodCallId(parent));
                        System.out.println("##### PARAMETERS: " + parameters.size());
                        if (parameters.size() >= i + 1) {
                            Symbol parameter = parameters.get(i);
                            yield parameter != null ? parameter.getType() : null;
                        }
                        break;
                    }
                }
                Logger.err("Couldn't get the parameter correspondent to the Object property in OllirVisitor::handleObjectProperty");
                yield null;
            }
            default -> null;
        };

        if (property.getKind().equals(NodeNames.objectMethod)) {
            return handleObjectMethod(node, type, expectedType, inline);
        } else if (property.getKind().equals(NodeNames.length)) {
            //TODO: when considering arrays
        }

        return new IntermediateOllirRepresentation("\t\tTODO in handleObjectProperty\n", "\t\tTODO in handleObjectProperty\n");
    }

    public boolean isExpression(JmmNode node) {
        String kind = node.getKind();
        return switch (kind) {
            case NodeNames.and, NodeNames.lessThan, NodeNames.not, NodeNames.sum, NodeNames.sub, NodeNames.mul, NodeNames.div -> true;
            default -> false;
        };
    }

    private IntermediateOllirRepresentation handleObjectMethod(JmmNode node, Type type, Type expectedType, boolean inline) {
        JmmNode identifier = node.getChildren().get(0);
        JmmNode property = node.getChildren().get(1);

        List<JmmNode> parameters = property.getChildren();
        List<String> parametersRep = new ArrayList<>();
        StringBuilder before = new StringBuilder();

        List<Symbol> realParameters = symbolTable.getParameters(typeInterpreter.buildMethodCallId(property));

        for (int i = 0; i < parameters.size(); i++) {
            JmmNode parameter = parameters.get(i);
            Type nodeType = realParameters.size() >= i + 1 ? realParameters.get(i).getType() : typeInterpreter.getNodeType(parameter);

            IntermediateOllirRepresentation representation = getOllirRepresentation(parameter, nodeType, !isExpression(parameter));
            before.append(representation.getBefore());
            parametersRep.add(representation.getCurrent());
        }

        String methodCallOllir;
        if (typeInterpreter.isImportedClassInstance(identifier)) {
            String identifierName = identifier.getOptional(Attributes.name).orElse(null);
            if (identifierName == null)
                return new IntermediateOllirRepresentation("\t\tTODO in handleObjectMethod\n", "\t\tTODO in handleObjectMethod\n");

            methodCallOllir = ollirBuilder.getStaticMethodCall(identifierName, property, type, expectedType, parametersRep);
        } else if (identifier.getKind().equals(NodeNames.thisName)) {
            methodCallOllir = ollirBuilder.getVirtualMethodCall("this", property, type, expectedType, parametersRep);
        } else {
            //TODO: error when new Obj().func() (identifier is new)
            BasicSymbol symbol = typeInterpreter.getIdentifierSymbol(identifier);
            methodCallOllir = ollirBuilder.getVirtualMethodCall(symbol.getName(), symbol.getType(), property, type, expectedType, parametersRep);
        }

        if (inline) {
            String auxName = ollirBuilder.getNextAuxName();
            before.append(ollirBuilder.getAssignmentCustom(new BasicSymbol(type, auxName), methodCallOllir));
            methodCallOllir = auxName + ollirBuilder.typeToCode(type);
        } else methodCallOllir = "\t\t" + methodCallOllir + "\n";

        return new IntermediateOllirRepresentation(methodCallOllir, before.toString());
    }
}
