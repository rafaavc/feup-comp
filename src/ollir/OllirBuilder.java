package ollir;

import constants.Attributes;
import constants.NodeNames;
import constants.Types;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import table.BasicSymbol;
import table.BasicSymbolTable;
import visitor.scopes.Scope;

import java.util.List;

public class OllirBuilder {
    private final StringBuilder code = new StringBuilder();
    private final BasicSymbolTable table;
    private int nextAuxNumber = 1;
    private boolean firstMethod = true;

    public OllirBuilder(BasicSymbolTable table) {
        this.table = table;

        code.append(table.getClassName()).append(" {\n");
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
        code.append("\t\tinvokespecial(this, \"<init>\").V\n");
        code.append("\t}\n");
    }

    public void addMethod(JmmNode node) {
        if (!firstMethod) code.append("\t}\n");
        else firstMethod = false;

        code.append("\t.method public ");
        String methodName = node.getOptional(Attributes.name).orElse(null);
        if (methodName == null) return;

        Type returnType = table.getReturnType(methodName);
        String parameters = parseParameters(table.getParameters(methodName));

        code.append(methodName).append("(").append(parameters);
        code.append(")").append(typeToCode(returnType)).append(" {\n");
    }

    public String addField(JmmNode node) {
        code.append("\t.field private ");
        String fieldName = node.getOptional(Attributes.name).orElse(null);
        if (fieldName == null) return "";

        Type fieldType = table.getField(fieldName).getType();
        code.append(fieldName).append(".").append(typeToCode(fieldType)).append("\n");
        return "";
    }

    public String getClassInstantiation(String name) {
        return "new(" + name + ")." + name;
    }

    public String getClassInitCall(String varName, String className) {
        return "\t\tinvokespecial(" + varName + "." + className + ",\"<init>\").V\n";
    }

    public String getStaticMethodCall(String identifier, JmmNode method, Type returnType, List<String> parameters) {
        StringBuilder methodCode = new StringBuilder();

        methodCode.append("invokestatic(").append(identifier);
        methodCode.append(getMethodCall(method, returnType, parameters));

        return methodCode.toString();
    }

    public String getVirtualMethodCall(String identifier, Type identifierType, JmmNode method, Type returnType, List<String> parameters) {
        StringBuilder methodCode = new StringBuilder();

        methodCode.append("invokevirtual(").append(identifier).append(typeToCode(identifierType));
        methodCode.append(getMethodCall(method, returnType, parameters));

        return methodCode.toString();
    }

    public String getVirtualMethodCall(String identifier, JmmNode method, Type returnType, List<String> parameters) {
        StringBuilder methodCode = new StringBuilder();

        methodCode.append("invokevirtual(").append(identifier);
        methodCode.append(getMethodCall(method, returnType, parameters));

        return methodCode.toString();
    }

    private String getMethodCall(JmmNode method, Type returnType, List<String> parameters) {
        String methodName = method.getOptional(Attributes.name).orElse(null);
        if (methodName == null) return "";

        StringBuilder methodCode = new StringBuilder();

        methodCode.append(", \"");
        methodCode.append(methodName).append("\", ");
        System.out.println("RETURN TYPE = " + returnType);
        methodCode.append(String.join(", ", parameters)).append(")").append(typeToCode(returnType));

        return methodCode.toString();
    }

    public void addReturn(String returnOllirRep, Type returnType) {
        String returnTypeCode = typeToCode(returnType);

        code.append("\t\tret").append(returnTypeCode);
        code.append(" ").append(returnOllirRep).append("\n");
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
    public String getOperandOllirRepresentation(JmmNode operand, Scope scope, Type type) {
        return switch (operand.getKind()) {
            case NodeNames.integer -> operand.get(Attributes.value) + ".i32";
            case NodeNames.bool -> (operand.get(Attributes.value).equals("true") ? "1" : "0") + ".bool";
            case NodeNames.identifier -> {
                List<Symbol> parameters = table.getParameters(scope.getMethodScope().get(Attributes.name));
                String operandName = operand.get(Attributes.name);
                int inParameter = 0;
                for (int i = 0; i < parameters.size(); i++) {
                    Symbol parameter = parameters.get(i);
                    if (parameter.getName().equals(operandName)) {
                        inParameter = i + 1;
                    }
                }
                yield (inParameter != 0 ? "$" + inParameter + "." : "") + operand.get(Attributes.name) + typeToCode(type);
            }
            case NodeNames.arrayAccessResult -> "Not needed in this checkpoint";
            default -> null;
        };
    }

    public String getAssignmentCustom(BasicSymbol symbol, String rightSide) {
        return "\t\t" + symbol.getName() +
                typeToCode(symbol.getType()) +
                equalsSign(symbol.getType()) +
                rightSide + "\n";
    }


    public String getCode() {
        String code = this.code.toString();
        return code.replaceAll("(?<!})(?<!\\{)\n", ";\n") + "\t}\n}";
    }

    public String operatorNameToSymbol(String operatorName) {
        return switch (operatorName) {
            case NodeNames.sum -> " +.i32 ";
            case NodeNames.sub -> " -.i32 ";
            case NodeNames.mul -> " *.i32 ";
            case NodeNames.div -> " /.i32 ";
            case NodeNames.and -> " &&.bool ";
            case NodeNames.not -> "!.bool ";
            case NodeNames.lessThan -> " <.i32 ";
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

    private String equalsSign(Type type) {
        return " :=" + typeToCode(type) + " ";
    }

    public String typeToCode(Type type) {
        return typeToCode(type, null);
    }

    public String typeToCode(Type type, Type expected) {
        if (type == null) return ".V";

        String code = "";
        if (type.isArray()) code += ".array";

        if (type.getName().equals(Types.integer))
            code += ".i32";
        else if (type.getName().equals(Types.bool))
            code += ".bool";
        else if (type.getName().equals(Types.expected))
            return expected == null ? ".V" : typeToCode(expected, null);
        else code += "." + type.getName();

        return code;
    }
}
