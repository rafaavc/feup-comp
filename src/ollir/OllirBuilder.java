package ollir;

import constants.Attributes;
import constants.NodeNames;
import constants.Types;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import table.BasicSymbol;
import table.BasicSymbolTable;
import table.MethodIdBuilder;
import typeInterpreter.TypeInterpreter;
import utils.Logger;
import visitor.scopes.Scope;
import visitor.scopes.ScopeVisitor;

import java.util.List;

public class OllirBuilder {
    private final StringBuilder code = new StringBuilder();
    private final BasicSymbolTable table;
    private int nextAuxNumber = 1;
    private boolean firstMethod = true;
    protected TypeInterpreter typeInterpreter;
    protected MethodIdBuilder methodIdBuilder = new MethodIdBuilder();

    public OllirBuilder(BasicSymbolTable table) {
        this.table = table;
        this.typeInterpreter = new TypeInterpreter(table, new ScopeVisitor(table));

        for (String importName : table.getImports()) {
            code.append("import ").append(importName).append("\n");
        }

        code.append(table.getClassName());
        if (table.getSuper() != null) {
            code.append(" extends ").append(table.getSuper());
        }
        code.append(" {\n");
        addFields();
        addConstructor();
    }

    public String getNextAuxName() {
        String name = "aux" + nextAuxNumber;
        nextAuxNumber++;
        return name;
    }

    public void add(String code) {
        this.code.append(code);
    }

    private void addFields() {
        List<Symbol> fields = table.getFields();
        for (Symbol field : fields) {
            code.append("\t.field private ");
            code.append(field.getName()).append(typeToCode(field.getType())).append("\n");
        }
    }

    private void addConstructor() {
        code.append("\t.construct ").append(table.getClassName());
        code.append("().V {\n");
        code.append("\t\tinvokespecial(this, \"<init>\").V\n");
        code.append("\t}\n");
    }

    public void addMethod(JmmNode node) {
        if (!firstMethod) code.append("\t}\n");
        else firstMethod = false;

        code.append("\t.method public ");
        if (node.getKind().equals(NodeNames.mainMethod))
            code.append("static ");

        String methodName = node.getOptional(Attributes.name).orElse(null);
        if (methodName == null) return;
        String methodId = methodIdBuilder.buildMethodId(node);

        Type returnType = table.getReturnType(methodId);
        String parameters = parseParameters(table.getParameters(methodId));

        code.append(methodName).append("(").append(parameters);
        code.append(")").append(typeToCode(returnType)).append(" {\n");
    }

    public String getClassInstantiation(String name) {
        return "new(" + name + ")." + name;
    }

    public String getArrayInstantiation(String length) {
        return "new(array, " + length + ").array.i32";
    }

    public String getArrayLengthCall(JmmNode identifier) {
        String current = getOperandOllirRepresentation(identifier, new ScopeVisitor(table).visit(identifier), typeInterpreter.getNodeType(identifier)).getCurrent();
        return "arraylength(" + current + ").i32";
    }

    public String getClassInitCall(String varName, String className) {
        return "\t\tinvokespecial(" + varName + "." + className + ",\"<init>\").V\n";
    }

    public String getStaticMethodCall(String identifier, JmmNode method, Type returnType, Type expected, List<String> parameters) {

        return "invokestatic(" + identifier +
                getMethodCall(method, returnType, expected, parameters);
    }

    public String getVirtualMethodCall(String identifier, Type identifierType, JmmNode method, Type returnType, Type expected, List<String> parameters) {

        return "invokevirtual(" + identifier + typeToCode(identifierType) +
                getMethodCall(method, returnType, expected, parameters);
    }

    public String getVirtualMethodCall(String identifier, JmmNode method, Type returnType, Type expected, List<String> parameters) {

        return "invokevirtual(" + identifier +
                getMethodCall(method, returnType, expected, parameters);
    }

    private String getMethodCall(JmmNode method, Type returnType, Type expected, List<String> parameters) {
        String methodName = method.getOptional(Attributes.name).orElse(null);
        if (methodName == null) return "";

        StringBuilder methodCode = new StringBuilder();

        methodCode.append(", \"");
        methodCode.append(methodName).append("\"");
        if (parameters.size() != 0) methodCode.append(", ");

        methodCode.append(String.join(", ", parameters)).append(")").append(typeToCode(returnType, expected));
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
    public IntermediateOllirRepresentation getOperandOllirRepresentation(JmmNode operand, Scope scope, Type type) {
        return switch (operand.getKind()) {
            case NodeNames.integer -> new IntermediateOllirRepresentation(operand.get(Attributes.value) + ".i32", "");
            case NodeNames.bool -> new IntermediateOllirRepresentation((operand.get(Attributes.value).equals("true") ? "1" : "0") + ".bool", "");
            case NodeNames.identifier -> {
                List<Symbol> parameters = table.getParameters(methodIdBuilder.buildMethodId(scope.getMethodScope()));
                String operandName = operand.get(Attributes.name);
                int inParameter = 0;
                for (int i = 0; i < parameters.size(); i++) {
                    Symbol parameter = parameters.get(i);
                    if (parameter.getName().equals(operandName)) {
                        inParameter = i + 1;
                    }
                }

                String current;
                String before = "";
                if (inParameter != 0)
                    current = "$" + inParameter + "." + operand.get(Attributes.name) + typeToCode(type);
                else if (isField(operandName, type)) {
                    String auxName = getNextAuxName();
                    String typeCode = typeToCode(type);
                    String rightSide = "";
                    rightSide += "getfield(this, " + operandName;
                    rightSide += typeCode + ")" + typeCode;

                    before = getAssignmentCustom(new BasicSymbol(type, auxName), rightSide);
                    current = auxName + typeCode;
                }
                else
                    current = operand.get(Attributes.name) + typeToCode(type);

                yield new IntermediateOllirRepresentation(current, before);
            }
            case NodeNames.arrayAccessResult -> null;
            default -> null;
        };
    }

    public boolean isField(Symbol symbol) {
        if (symbol == null) return false;
        return isField(symbol.getName(), symbol.getType());
    }

    private boolean isField(String name, Type type) {
        List<Symbol> fields = table.getFields();

        for (Symbol field : fields) {
            if (field.getName().equals(name) &&
                field.getType().equals(type))
                return true;
        }
        return false;
    }

    public String getAssignmentCustom(BasicSymbol symbol, String rightSide) {
        return "\t\t" + symbol.getName() +
                typeToCode(symbol.getType()) +
                equalsSign(symbol.getType()) +
                rightSide + "\n";
    }

    public void addPutField(BasicSymbol symbol, String rightSide) {
        code.append("\t\tputfield(this, ").append(symbol.getName());
        code.append(typeToCode(symbol.getType())).append(", ").append(rightSide);
        code.append(").V\n");
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

        switch (type.getName()) {
            case Types.integer:
                code += ".i32";
                break;
            case Types.bool:
                code += ".bool";
                break;
            case Types.expected:
                return expected == null ? ".V" : typeToCode(expected, null);
            default:
                code += "." + type.getName();
                break;
        }

        return code;
    }
}
