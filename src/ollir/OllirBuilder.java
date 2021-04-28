package ollir;

import constants.Attributes;
import constants.NodeNames;
import constants.Types;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import table.BasicSymbol;
import table.BasicSymbolTable;

import java.util.List;

public class OllirBuilder {
    private final StringBuilder code = new StringBuilder();
    private final BasicSymbolTable table;
    private int nextAuxNumber = 1;

    public OllirBuilder(BasicSymbolTable table) {
        this.table = table;
    }

    public String getNextAuxName() {
        String name = "aux" + nextAuxNumber;
        nextAuxNumber++;
        return name;
    }

    public void add(String code) {
        this.code.append(code);
    }

    public void addConstructor() {
        code.append("\t.contruct ").append(table.getClassName());
        code.append("().V {\n");
        code.append("\t\tinvokespecial(this, \"<init>\").V;\n");
        code.append("\t}\n");
    }

    public void addMethod(JmmNode node) {
        code.append("\t.method public ");
        String methodName = node.getOptional(Attributes.name).orElse(null);
        if (methodName == null) return;

        Type returnType = table.getReturnType(methodName);
        String parameters = parseParameters(table.getParameters(methodName));

        code.append(methodName).append("(").append(parameters);
        code.append(")").append(typeToCode(returnType)).append("\n");
    }

    public String addField(JmmNode node) {
        code.append("\t.field private ");
        String fieldName = node.getOptional(Attributes.name).orElse(null);
        if (fieldName == null) return "";

        Type fieldType = table.getField(fieldName).getType();
        code.append(fieldName).append(".").append(typeToCode(fieldType)).append(";\n");
        return "";
    }

    public String addMethodCall(String methodName, BasicSymbol identifier, List<BasicSymbol> parameters, boolean isImportedClass) {
        if (isImportedClass) {
            code.append("\t\tinvokestatic(").append(identifier.getName()).append(", \"");
            code.append(methodName).append("\", ");
        } else {
            code.append("\t\tinvokevirtual(");
        }
        return "";
    }

    public String addAssignment(JmmNode node, Type nodeType) {

        return "";
    }

    public String getAssignmentWithExpression(BasicSymbol symbol, String operatorName, String op1, String op2) {
        // TODO check if identifiers used are in parameters (to add $1, $2, etc)

        return symbol.getName() +
                typeToCode(symbol.getType()) +
                equalsSign(symbol.getType()) +
                op1 +
                " " +
                typeToCode(symbol.getType()) +
                operatorNameToSymbol(operatorName) +
                " " +
                op2 +
                typeToCode(symbol.getType()) +
                "\n";
    }

    /**
     * Gets the ollir representation of array access, integer, bool, identifier
     *
     * @param operand - JmmNode to represent
     * @return the ollir representation
     */
    public String getOperandOllirRepresentation(JmmNode operand, Type type) {
        return switch (operand.getKind()) {
            case NodeNames.integer -> operand.get(Attributes.value) + ".i32";
            case NodeNames.bool -> (operand.get(Attributes.value).equals("true") ? "1" : "0") + ".bool";
            case NodeNames.identifier -> operand.get(Attributes.name) + typeToCode(type);
            // case NodeNames.arrayAccessResult ->
            default -> null;
        };
    }

    public String getAssignmentCustom(BasicSymbol symbol, String rightSide) {
        return "\t\t" + symbol.getName() +
                typeToCode(symbol.getType()) +
                equalsSign(symbol.getType()) +
                rightSide + ";\n";

    }


    public String getCode() {
        return code.toString();
    }

    public String operatorNameToSymbol(String operatorName) {
        return switch (operatorName) {
            case NodeNames.sum -> "+";
            case NodeNames.sub -> "-";
            case NodeNames.mul -> "*";
            case NodeNames.div -> "/";
            case NodeNames.and -> "&&";
            case NodeNames.not -> "!";
            case NodeNames.lessThan -> "<";
            default -> null;
        };
    }

    private String parseParameters(List<Symbol> parameters) {
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < parameters.size(); i++) {
            Symbol parameter = parameters.get(i);
            code.append(parameter.getName());
            code.append(typeToCode(parameter.getType()));
            if (i + 1 < parameters.size()) code.append(", ");
        }

        return code.toString();
    }

    private String parseMethodCallParameters() {

        return "";
    }

    private String equalsSign(Type type) {
        return " :=" + typeToCode(type) + " ";
    }

    public String typeToCode(Type type) {
        if (type == null) return ".V";

        String code = "";
        if (type.isArray()) code += ".array";

        if (type.getName().equals(Types.integer))
            code += ".i32";
        else if (type.getName().equals(Types.bool))
            code += ".bool";
        else code += "." + type.getName();

        return code;
    }
}
