package visitor;

import constants.NodeNames;
import ollir.OllirBuilder;
import table.BasicSymbolTable;
import table.BasicSymbol;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;

public class OllirVisitor extends Visitor {
    private final OllirBuilder ollirBuilder;

    private static class IntermediateOllirRepresentation {
        private final String before, current;

        public IntermediateOllirRepresentation(String current) {
            this.current = current;
            this.before = null;
        }

        public IntermediateOllirRepresentation(String current, String before) {
            this.current = current;
            this.before = before;
        }

        public String getCurrent() {
            return current;
        }

        public String getBefore() {
            return before;
        }
    }

    public OllirVisitor(BasicSymbolTable table, OllirBuilder ollirBuilder) {
        super(table);
        this.ollirBuilder = ollirBuilder;
    }

    private IntermediateOllirRepresentation getOllirRepresentation(JmmNode node, Type type, boolean assignment) {
        switch (node.getKind()) {
            case NodeNames.sum, NodeNames.mul, NodeNames.sub, NodeNames.div, NodeNames.lessThan, NodeNames.and -> {
                IntermediateOllirRepresentation leftChild = getOllirRepresentation(node.getChildren().get(0), type, false);
                IntermediateOllirRepresentation rightChild = getOllirRepresentation(node.getChildren().get(1), type, false);

                String before = leftChild.before + rightChild.before;
                String current = leftChild.current + ollirBuilder.operatorNameToSymbol(node.getKind()) + rightChild.current;

                if (!assignment) {
                    String name = ollirBuilder.getNextAuxName();
                    before += ollirBuilder.getAssignmentCustom(new BasicSymbol(type, name), current);
                    current = name + ollirBuilder.typeToCode(type);
                }

                return new IntermediateOllirRepresentation(current, before);
            }
            case NodeNames.not -> {

            }
        }
        return new IntermediateOllirRepresentation(ollirBuilder.getOperandOllirRepresentation(node, getNodeType(node)), "");
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
            case NodeNames.method, NodeNames.mainMethod -> {
                ollirBuilder.addMethod(node);
            }
            case NodeNames.assignment -> {
                BasicSymbol symbol = getIdentifierSymbol(node.getChildren().get(0));
                JmmNode rightSide = node.getChildren().get(1);

                IntermediateOllirRepresentation representation = getOllirRepresentation(rightSide, symbol.getType(), true);

                ollirBuilder.add(representation.before);
                ollirBuilder.add(ollirBuilder.getAssignmentCustom(symbol, representation.getCurrent()));

                return;
            }
            case NodeNames.objectProperty -> {
                //IntermediateOllirRepresentation representation1 = getOllirRepresentation(rightSide, symbol.getType(), false);
                //IntermediateOllirRepresentation representation2 = getOllirRepresentation(rightSide, symbol.getType(), false);
                //IntermediateOllirRepresentation representation3 = getOllirRepresentation(rightSide, symbol.getType(), false);

                //ollirBuilder.add(representation1.before + representation2.before + representation3.before);

            }
        }

        for (JmmNode child : children) recursiveVisit(child);
    }
}
