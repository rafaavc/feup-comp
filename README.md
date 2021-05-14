# Compilers Project

For this project, you need to [install Gradle](https://gradle.org/install/)

## Project setup

Copy your ``.jjt`` file to the ``javacc`` folder. If you change any of the classes generated by ``jjtree`` or ``javacc``, you also need to copy them to the ``javacc`` folder.

Copy your source files to the ``src`` folder, and your JUnit test files to the ``test`` folder.

## Compile

To compile the program, run ``gradle build``. This will compile your classes to ``classes/main/java`` and copy the JAR file to the root directory. The JAR file will have the same name as the repository folder.

### Run

To run you have two options: Run the ``.class`` files or run the JAR.

### Run ``.class``

To run the ``.class`` files, do the following:

```cmd
java -cp "./build/classes/java/main/" <class_name> <arguments>
```

Where ``<class_name>`` is the name of the class you want to run and ``<arguments>`` are the arguments to be passed to ``main()``.

### Run ``.jar``

To run the JAR, do the following command:

```cmd
java -jar <jar filename> <arguments>
```

Where ``<jar filename>`` is the name of the JAR file that has been copied to the root folder, and ``<arguments>`` are the arguments to be passed to ``main()``.

## Test

To test the program, run ``gradle test``. This will execute the build, and run the JUnit tests in the ``test`` folder. If you want to see output printed during the tests, use the flag ``-i`` (i.e., ``gradle test -i``).
You can also see a test report by opening ``build/reports/tests/test/index.html``.

## Checkpoint 1
For the first checkpoint the following is required:

1. [x] Convert the provided e-BNF grammar into JavaCC grammar format in a .jj file
2. [x] Resolve grammar conflicts (projects with global LOOKAHEAD > 1 will have a penalty)
3. [x] Proceed with error treatment and recovery mechanisms for the while expression
4. [x] Convert the .jj file into a .jjt file
5. [x] Include missing information in nodes (i.e. tree annotation). E.g. include class name in the class Node.
6. [x] Generate a JSON from the AST

### JavaCC to JSON
To help converting the JavaCC nodes into a JSON format, we included in this project the JmmNode interface, which can be seen in ``src-lib/pt/up/fe/comp/jmm/JmmNode.java``. The idea is for you to use this interface along with your SimpleNode class. Then, one can easily convert the JmmNode into a JSON string by invoking the method JmmNode.toJson().

Please check the SimpleNode included in this repository to see an example of how the interface can be implemented, which implements all methods except for the ones related to node attributes. How you should store the attributes in the node is left as an exercise.

### Reports
We also included in this project the class ``src-lib/pt/up/fe/comp/jmm/report/Report.java``. This class is used to generate important reports, including error and warning messages, but also can be used to include debugging and logging information. E.g. When you want to generate an error, create a new Report with the ``Error`` type and provide the stage in which the error occurred.


### Parser Interface

We have included the interface ``src-lib/pt/up/fe/comp/jmm/JmmParser.java``, which you should implement in a class that has a constructor with no parameters (please check ``src/Main.java`` for an example). This class will be used to test your parser. The interface has a single method, ``parse``, which receives a String with the code to parse, and returns a JmmParserResult instance. This instance contains the root node of your AST, as well as a List of Report instances that you collected during parsing.

To configure the name of the class that implements the JmmParser interface, use the file ``parser.properties``.

## Checkpoint 2

### Semantic Analysis
Todas as verificações feitas na análise semantica pedidas devem reportar erro. Outro tipo de verificações (extra) devem reportar warnings e não erro (ou seja, devem permitir continuar a compilação):

- **Symbol Table**
	- [x] global: inclui info de imports e a classe declarada
    - [x] classe-specific: inclui info de extends, fields e methods
    - [x] method-specific: inclui info dos arguments e local variables
    - [x] retorno do SemanticsReport
	    - [x] permite consulta da tabela por parte da análise semantica
		- [x] permite impressão para fins de debug
	- [x] small bonus: permitir method overload (i.e. métodos com mesmo nome mas assinatura de parâmetros diferente)
	
- **Expression Analysis**
	- **Type Verification**
		- [x] verificar se operações são efetuadas com o mesmo tipo (e.g. int + boolean tem de dar erro)
		- [x] não é possível utilizar arrays diretamente para operações aritmeticas (e.g. array1 + array2)
		- [x] verificar se um array access é de facto feito sobre um array (e.g. 1[10] não é permitido)
		- [x] verificar se o indice do array access é um inteiro (e.g. a[true] não é permitido)
		- [x] verificar se valor do assignee é igual ao do assigned (a_int = b_boolean não é permitido!)
		- [x] verificar se operação booleana (&&, < ou !) é efetuada só com booleanos
		- [x] verificar se conditional expressions (if e while) resulta num booleano
	- **Method Verification**
		- [x] verificar se o "target" do método existe, e se este contém o método (e.g. a.foo, ver se 'a' existe e se tem um método 'foo')
			- caso seja do tipo da classe declarada (e.g. a usar o this), se não existir declaração na própria classe: se não tiver extends retorna erro, se tiver extends assumir que é da classe super.
		- [x] caso o método não seja da classe declarada, isto é uma classe importada, assumir como existente e assumir tipos esperados. (e.g. a = Foo.b(), se a é um inteiro, e Foo é uma classe importada, assumir que o método b é estático (pois estamos a aceder a uma método diretamente da classe), que não tem argumentos e que retorna um inteiro)
		- [x] verificar se o número de argumentos na invocação é igual ao número de parâmetros da declaração
		- [x] verificar se o tipo dos parâmetros coincide com o tipo dos argumentos

### OLLIR Generation

- Conversão da AST completa para OLLIR
- Ver documento para mais instruções: link TBD

### Jasmin Generation

- estrutura básica de classe (incluindo construtor <init>)
- estrutura básica de fields
- estrutura básica de métodos (podem desconsiderar os limites neste checkpoint: limit_stack 99, limit_locals 99)
- assignments
- operações aritméticas (com prioridade de operações correta)
    - neste checkpoint não é necessário a seleção das operações mais eficientes mas isto será considerado no CP3 e versão final
- invocação de métodos


## Checkpoint 3

8. Generate JVM code accepted by jasmin for conditional instructions (if and if-else)
e.g.
```
// jmm
if(a < b){
    m = b;
}else{ m = a;}

//jasmin
iload_1
iload_2
if_icmpge else_0
iload_2
istore_3
goto endif_0
else_0:
iload_1
istore_3
endif_0:
```

NOTE: The label name can be anything, just be careful not to repeat the same label!

9. Generate JVM code accepted by jasmin for loops;
    - similar to if statement, but with extra jumps
    
10. Generate JVM code accepted by jasmin to deal with arrays;
    - array initialization
    - array store (astore)
    - attay access (aload)
    - array position store
    - array position access

11. Complete the compiler and test it using a set of Java-- classes
    - have all given tests executing and passing
    - have at least 5 own tests comprising the overall project
    - have 3 to 5 top-notch examples (different from the ones provided!) that demonstrate the potential of your project!
        * These examples should show how good your project is
        * At least 3 of those examples have to be fully compatible with the project specification
        * More information will be given regarding the use of these 3 examples
        * The other 2 can use "extras" that you have in the project (e.g. constructors accepting arguments, method overloading,...)
        * NOTE: these examples can also be used in the final project to demonstrate the implemented optimizations

Another point that will be evaluated in CP3 is the stack limit and local limit correctly calculated (local limit is not considering the -r option, that is just for the final submission, for now we just want the limit based on: this + #arguments + "local vars"

As specified in the project description: AFTER CHECKPOINT 3 "Proceed with the optimizations related to the code generation,related to the register al-location (“-r”option) and the optimizations related to the “-o”option. [this task is necessary for students intending to be eligible for final project grades greater or equal than 18 (out of 20)

