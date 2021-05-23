package visitor;

import constants.Attributes;
import constants.NodeNames;
import constants.Types;
import ollir.IntermediateOllirRepresentation;
import ollir.OllirBuilder;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import table.BasicSymbol;
import table.BasicSymbolTable;
import utils.Logger;
import visitor.scopes.Scope;
import visitor.scopes.ScopeVisitor;

import java.util.ArrayList;
import java.util.List;

public class OllirVisitor extends Visitor {
    private final OllirBuilder ollirBuilder;

    public OllirVisitor(BasicSymbolTable table) {
        super(table);
        this.ollirBuilder = new OllirBuilder(this, table);
    }

    public IntermediateOllirRepresentation getOllirRepresentation(JmmNode node, Type type, boolean inline) {
        return getOllirRepresentation(node, type, inline, false);
    }

    public IntermediateOllirRepresentation getOllirRepresentation(JmmNode node, Type type, boolean inline, boolean isReturn) {
        return switch (node.getKind()) {
            case NodeNames.sum, NodeNames.mul, NodeNames.sub, NodeNames.div, NodeNames.lessThan, NodeNames.and, NodeNames.not -> {
                if (node.getChildren().size() > 2 || node.getChildren().size() < 1)
                    Logger.err("> Invalid node in IntermediateOllirRepresentation!");

                List<IntermediateOllirRepresentation> representations = new ArrayList<>();

                Type expectedType = switch(node.getKind()) {
                    case NodeNames.sum, NodeNames.mul, NodeNames.sub, NodeNames.div, NodeNames.lessThan -> new Type(Types.integer, false);
                    case NodeNames.and, NodeNames.not -> new Type(Types.bool, false);
                    default -> null;
                };

                for (JmmNode child : node.getChildren())
                    representations.add(getOllirRepresentation(child, expectedType, child.getKind().equals(NodeNames.objectProperty)));

                StringBuilder before = new StringBuilder();
                for (IntermediateOllirRepresentation rep : representations) before.append(rep.getBefore());

                String current;
                if (representations.size() == 1)
                    current = representations.get(0).getCurrent() + ollirBuilder.operatorNameToSymbol(node.getKind())
                            + representations.get(0).getCurrent();
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
                    JmmNode size = node.getChildren().get(0).getChildren().get(0);
                    IntermediateOllirRepresentation length = getOllirRepresentation(size, typeInterpreter.getNodeType(size), true);

                    IntermediateOllirRepresentation assignmentCustom = ollirBuilder.getAssignmentCustom(new Type(Types.integer, true), ollirBuilder.getArrayInstantiation(length.getCurrent()));
                    String before = length.getBefore() + assignmentCustom.getBefore();

                    yield new IntermediateOllirRepresentation(assignmentCustom.getCurrent(), before);
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

            case NodeNames.objectProperty -> handleObjectProperty(node, type, inline, isReturn);

            default -> ollirBuilder.getOperandOllirRepresentation(node, new ScopeVisitor(symbolTable).visit(node), typeInterpreter.getNodeType(node), inline);
        };
    }

    public OllirBuilder getOllirBuilder() {
        return ollirBuilder;
    }

    public void visitNode(JmmNode node) {
        String nodeKind = node.getKind();
        List<JmmNode> children = node.getChildren();

        switch (nodeKind) {
            case NodeNames.method, NodeNames.mainMethod -> ollirBuilder.addMethod(node);
            case NodeNames.assignment -> {
                JmmNode leftSideNode = node.getChildren().get(0);
                JmmNode rightSideNode = node.getChildren().get(1);

                BasicSymbol symbol = null;
                IntermediateOllirRepresentation leftSideRepresentation = null;
                Scope leftSideNodeScope = new ScopeVisitor(symbolTable).visit(leftSideNode);
                if (leftSideNode.getKind().equals(NodeNames.arrayAccessResult)) {
                    leftSideRepresentation = ollirBuilder.getOperandOllirRepresentation(leftSideNode, leftSideNodeScope, null);
                } else {
                    symbol = typeInterpreter.getIdentifierSymbol(leftSideNode);
                }

                Type returnType = symbol == null ? new Type(Types.integer, false) : symbol.getType();

                if (symbol != null && ollirBuilder.isField(symbol) && !ollirBuilder.isInLocalScope(leftSideNodeScope, leftSideNode.get(Attributes.name)))
                {
                    IntermediateOllirRepresentation representation = getOllirRepresentation(rightSideNode, returnType, rightSideNode.getKind().equals(NodeNames.objectProperty));
                    ollirBuilder.add(representation.getBefore());
                    ollirBuilder.addPutField(symbol, representation.getCurrent());
                }
                else
                {
                    IntermediateOllirRepresentation representation = getOllirRepresentation(rightSideNode, returnType, true);
                    ollirBuilder.add(representation.getBefore());
                    if (symbol != null)
                        ollirBuilder.add(ollirBuilder.getAssignmentCustom(symbol, representation.getCurrent()));
                    else {
                        assert leftSideRepresentation != null;
                        ollirBuilder.add(leftSideRepresentation.getBefore());
                        ollirBuilder.add(ollirBuilder.getAssignmentCustom(leftSideRepresentation.getCurrent(), returnType, representation.getCurrent()));
                    }
                }

                return;
            }
            case NodeNames.objectProperty, NodeNames.newAlloc -> {
                Type returnType = typeInterpreter.getNodeType(node);
                IntermediateOllirRepresentation representation = getOllirRepresentation(node, returnType, false);

                ollirBuilder.add(representation.getBefore());
                ollirBuilder.add(representation.getCurrent());

                return;
            }

            case NodeNames.whileLoop -> {
                JmmNode conditionExp = children.get(0).getChildren().get(0);
                Type returnType = typeInterpreter.getNodeType(conditionExp);
                JmmNode body = children.get(1);

                IntermediateOllirRepresentation conditionRepresentation = getOllirRepresentation(conditionExp, returnType, true);

                int whileCount = ollirBuilder.addLoop(conditionRepresentation);
                for (JmmNode n : body.getChildren()) visitNode(n);
                ollirBuilder.addLoopEnd(whileCount);

                List<JmmNode> parentChildren = node.getParent().getChildren();
                if (parentChildren.get(parentChildren.size() - 1) == node) {  // means that this is the last node in the method
                    ollirBuilder.add("\t\tret.V\n");
                }

                return;
            }

            case NodeNames.returnStatement -> {
                JmmNode returnIdentifier = node.getChildren().get(0);
                Scope nodeScope = new ScopeVisitor(symbolTable).visit(returnIdentifier);
                Type returnType = symbolTable.getReturnType(methodIdBuilder.buildMethodId(nodeScope.getMethodScope()));

                IntermediateOllirRepresentation representation = getOllirRepresentation(returnIdentifier, returnType, false, true);

                ollirBuilder.add(representation.getBefore());
                ollirBuilder.addReturn(representation.getCurrent(), returnType);

                return;
            }

            case NodeNames.ifElse -> {
                JmmNode conditionExp = children.get(0).getChildren().get(0);
                Type returnType = typeInterpreter.getNodeType(conditionExp);

                JmmNode ifStatement = children.get(1);
                JmmNode elseStatement = children.get(2);

                IntermediateOllirRepresentation conditionRepresentation = getOllirRepresentation(conditionExp, returnType, true);
                if (!conditionRepresentation.getBefore().equals("")) ollirBuilder.add("\t\t" + conditionRepresentation.getBefore());

                int ifCount = ollirBuilder.addIf(conditionRepresentation.getCurrent(), false);
                for (JmmNode ifChild : elseStatement.getChildren()) visitNode(ifChild);
                ollirBuilder.addIfTransition(ifCount);
                for (JmmNode elseChild : ifStatement.getChildren()) visitNode(elseChild);
                ollirBuilder.addIfEnd(ifCount);

                return;
            }
        }

        for (JmmNode child : children) visitNode(child);
    }

    private IntermediateOllirRepresentation handleObjectProperty(JmmNode node, Type type, boolean inline, boolean isReturn) {
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
                for (int i = 0; i < children.size(); i++) {
                    if (children.get(i) == node) {
                        List<Symbol> parameters = symbolTable.getParameters(typeInterpreter.buildMethodCallId(parent));
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
            return handleObjectMethod(node, type, expectedType, inline || isReturn);
        } else if (property.getKind().equals(NodeNames.length)) {
            IntermediateOllirRepresentation arrayLengthCall = ollirBuilder.getArrayLengthCall(node.getChildren().get(0));
            if (!inline) {
                return new IntermediateOllirRepresentation("\t\t" + arrayLengthCall.getCurrent() + "\n", arrayLengthCall.getBefore());
            } else {
                String auxName = ollirBuilder.getNextAuxName();
                String before = arrayLengthCall.getBefore() + ollirBuilder.getAssignmentCustom(new BasicSymbol(type, auxName), arrayLengthCall.getCurrent());
                String current = auxName + ollirBuilder.typeToCode(type);

                return new IntermediateOllirRepresentation(current, before);
            }
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

    public boolean isArrayAccess(JmmNode node) {
        return node.getKind().equals(NodeNames.arrayAccessResult);
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

            IntermediateOllirRepresentation representation = getOllirRepresentation(parameter, nodeType, !isExpression(parameter) && !isArrayAccess(parameter));
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
            BasicSymbol symbol = typeInterpreter.getIdentifierSymbol(identifier);
            if (symbol == null) { // right after instantiation
                Type instType = new Type(identifier.get(Attributes.name), false);
                IntermediateOllirRepresentation representation = getOllirRepresentation(identifier, instType, true);
                before.append(representation.getBefore());
                symbol = new BasicSymbol(instType, representation.getCurrent().split("\\.")[0]);
            }
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
