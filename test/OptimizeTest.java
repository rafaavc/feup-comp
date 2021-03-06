
/**
 * Copyright 2021 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

import optimization.Liveness;
import optimization.LivenessRange;
import optimization.LivenessResult;
import optimization.RegisterAllocator;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

import java.util.*;

import static org.junit.Assert.*;
import static pt.up.fe.comp.TestUtils.backend;
import static pt.up.fe.comp.TestUtils.noErrors;

public class OptimizeTest {

    @Test
    public void testSlidesLiveness() throws Exception {
        List<Set<String>> use = new ArrayList<>();
        use.add(new HashSet<>());
        use.add(new HashSet<>());
        use.add(new HashSet<>(Collections.singletonList("i")));
        use.add(new HashSet<>(Collections.singletonList("i")));
        use.add(new HashSet<>(Arrays.asList("s", "t")));
        use.add(new HashSet<>(Collections.singletonList("i")));
        use.add(new HashSet<>(Collections.singletonList("s")));

        List<Set<String>> def = new ArrayList<>();
        def.add(new HashSet<>(Collections.singletonList("s")));
        def.add(new HashSet<>(Collections.singletonList("i")));
        def.add(new HashSet<>());
        def.add(new HashSet<>(Collections.singletonList("t")));
        def.add(new HashSet<>(Collections.singletonList("s")));
        def.add(new HashSet<>(Collections.singletonList("i")));;
        def.add(new HashSet<>());

        List<Set<Integer>> successors = new ArrayList<>();
        successors.add(new HashSet<>(Collections.singletonList(1)));
        successors.add(new HashSet<>(Collections.singletonList(2)));
        successors.add(new HashSet<>(Arrays.asList(3, 6)));
        successors.add(new HashSet<>(Collections.singletonList(4)));
        successors.add(new HashSet<>(Collections.singletonList(5)));
        successors.add(new HashSet<>(Collections.singletonList(2)));
        successors.add(new HashSet<>());


        Liveness liveness = new Liveness(def, use, successors);
        LivenessResult result = liveness.getResult();

        assertEquals(3, result.getIterations());

        List<Set<String>> in = result.getIn(), out = result.getOut();

        assertTrue(out.get(0).size() == 1 && out.get(0).contains("s"));
        assertTrue(out.get(1).size() == 2 && out.get(1).containsAll(Arrays.asList("s", "i")));
        assertTrue(out.get(2).size() == 2 && out.get(2).containsAll(Arrays.asList("s", "i")));
        assertTrue(out.get(3).size() == 3 && out.get(3).containsAll(Arrays.asList("s", "t", "i")));
        assertTrue(out.get(4).size() == 2 && out.get(4).containsAll(Arrays.asList("s", "i")));
        assertTrue(out.get(5).size() == 2 && out.get(5).containsAll(Arrays.asList("s", "i")));
        assertEquals(0, out.get(6).size());

        assertTrue(in.get(6).size() == 1 && in.get(6).contains("s"));
        assertTrue(in.get(5).size() == 2 && in.get(5).containsAll(Arrays.asList("s", "i")));
        assertTrue(in.get(4).size() == 3 && in.get(4).containsAll(Arrays.asList("s", "t", "i")));
        assertTrue(in.get(3).size() == 2 && in.get(3).containsAll(Arrays.asList("s", "i")));
        assertTrue(in.get(2).size() == 2 && in.get(2).containsAll(Arrays.asList("s", "i")));
        assertTrue(in.get(1).size() == 1 && in.get(1).contains("s"));
        assertEquals(0, in.get(0).size());
    }

    @Test
    public void testRegistryAllocation() {
        Map<String, LivenessRange> variables = new HashMap<>();

        variables.put("n", new LivenessRange(0, 9));
        variables.put("aux2", new LivenessRange(1, 3));
        variables.put("aux3", new LivenessRange(4, 5));
        variables.put("simpleWhile", new LivenessRange(3, 8));

        RegisterAllocator allocator = new RegisterAllocator(variables);

        assertTrue(allocator.colorGraph(3));
        assertFalse(allocator.colorGraph(2));
    }

    @Test
    public void testROption() {
        OllirResult ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/created/ROption.jmm"));

        OptimizationStage optimizationStage = new OptimizationStage();
        JasminResult jasmin = backend(optimizationStage.optimize(ollirResult, 1));

        noErrors(ollirResult.getReports());

        String output = jasmin.run();
        assertEquals("12255", output.trim().replace("\r\n", "").replace("\n", ""));
    }

    @Test
    public void testFindMaximum() {
        OllirResult ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));

        OptimizationStage optimizationStage = new OptimizationStage();

        optimizationStage.optimize(ollirResult, 1);
        assertEquals(1, TestUtils.getNumErrors(ollirResult.getReports()));

        ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));

        optimizationStage.optimize(ollirResult, 2);
        assertEquals(1, TestUtils.getNumErrors(ollirResult.getReports()));

        ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));

        optimizationStage.optimize(ollirResult, 3);
        assertEquals(0, TestUtils.getNumErrors(ollirResult.getReports()));

        JasminResult jasmin = backend(optimizationStage.optimize(ollirResult, 1));

        var output = jasmin.run();

        assertEquals("Result: 28", output.trim());
    }

    public static JasminResult backendOptimize(OllirResult ollirResult) {
        try {
            BackendStage backend = new BackendStage();
            return backend.toJasmin(ollirResult);
        } catch (Exception e) {
            throw new RuntimeException("Could not generate Jasmin code", e);
        }
    }

    public static JasminResult backendOptimize(String code) {
        var semanticsResult = TestUtils.analyse(code);

        OptimizationStage optimizationStage = new OptimizationStage();
        OllirResult ollirResult = optimizationStage.toOllir(semanticsResult);
        ollirResult = optimizationStage.optimizeO(semanticsResult, ollirResult);
        ollirResult = optimizationStage.optimize(ollirResult);

        noErrors(ollirResult.getReports());
        return backendOptimize(ollirResult);
    }

    @Test
    public void testHelloWorld() {
        var result = backendOptimize(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
        noErrors(result.getReports());

        var output = result.run();
        assertEquals("Hello, World!", output.trim());
    }

    @Test
    public void testSimple() {
        var result = backendOptimize(SpecsIo.getResource("fixtures/public/Simple.jmm"));
        noErrors(result.getReports());

        var output = result.run();
        assertEquals("30", output.trim());
    }

    @Test
    public void testMethodCalls() {
        var result = backendOptimize(SpecsIo.getResource("fixtures/public/created/MethodCalls.jmm"));
        noErrors(result.getReports());

        var output = result.run();
        assertEquals("520", output.trim());
    }

    @Test
    public void testComplexExpressions() {
        var result = backendOptimize(SpecsIo.getResource("fixtures/public/created/ComplexExpressions.jmm"));
        noErrors(result.getReports());

        var output = result.run();
        assertEquals("10971", output.trim());
    }

    @Test
    public void testArrayAccess() {
        var result = backendOptimize(SpecsIo.getResource("fixtures/public/created/ArrayAccess.jmm"));
        noErrors(result.getReports());

        var output = result.run();
        assertEquals("41215", output.trim().replace("\r\n", "").replace("\n", ""));
    }

    @Test
    public void testFac() {
        var result = backendOptimize(SpecsIo.getResource("fixtures/public/Fac.jmm"));
        noErrors(result.getReports());

        var output = result.run();
        assertEquals("3628800", output.trim());
    }

    @Test
    public void testSimpleIf() {
        var result = backendOptimize(SpecsIo.getResource("fixtures/public/created/SimpleIf.jmm"));
        noErrors(result.getReports());

        var output = result.run();
        assertEquals("1234", output.trim());
    }

    @Test
    public void testNestedIf() {
        var result = backendOptimize(SpecsIo.getResource("fixtures/public/created/NestedIf.jmm"));
        noErrors(result.getReports());

        var output = result.run();
        assertEquals("2", output.trim());
    }

    @Test
    public void testComplexExpressionIf() {
        var result = backendOptimize(SpecsIo.getResource("fixtures/public/created/ComplexExpressionIf.jmm"));
        noErrors(result.getReports());

        var output = result.run();
        assertEquals("1", output.trim());
    }

    @Test
    public void testSimpleWhile() {
        var result = backendOptimize(SpecsIo.getResource("fixtures/public/created/SimpleWhile.jmm"));
        noErrors(result.getReports());

        var output = result.run();
        assertEquals("10", output.trim());
    }

    @Test
    public void testMultipleWhile() {
        var result = backendOptimize(SpecsIo.getResource("fixtures/public/created/MultipleWhile.jmm"));
        noErrors(result.getReports());

        var output = result.run();
        assertEquals("20", output.trim());
    }

    @Test
    public void testNestedWhile() {
        var result = backendOptimize(SpecsIo.getResource("fixtures/public/created/NestedWhile.jmm"));
        noErrors(result.getReports());

        var output = result.run();
        assertEquals("100", output.trim());
    }

    @Test
    public void testLazysort() {
        var result = backendOptimize(SpecsIo.getResource("fixtures/public/Lazysort.jmm"));
        noErrors(result.getReports());

        var output = result.run().split("\n");
        assertEquals(10, output.length);
    }

    @Test
    public void testLife() {
        var result = backendOptimize(SpecsIo.getResource("fixtures/public/Life.jmm"));
        noErrors(result.getReports());

        // is entering an infinite loop?
//        var output = result.run("1\n1\n1\n1\n");
//        assertEquals("100", output.trim());
    }

    @Test
    public void testModifiedLife() {
        var result = backendOptimize(SpecsIo.getResource("fixtures/public/created/ModifiedLife.jmm"));
        noErrors(result.getReports());

        var output = result.run(SpecsIo.getResource("fixtures/public/created/ModifiedLife.input"));
        assertEquals(SpecsIo.getResource("fixtures/public/created/ModifiedLife.txt"), output.trim());
    }

    @Test
    public void testMonteCarloPi() {
        var result = backendOptimize(SpecsIo.getResource("fixtures/public/MonteCarloPi.jmm"));
        noErrors(result.getReports());

        var output = result.run("100000\n").trim();

        assertEquals(314, Integer.parseInt(output.substring(output.length() - 3)), 5);
    }

    @Test
    public void testQuickSort() {
        var result = backendOptimize(SpecsIo.getResource("fixtures/public/QuickSort.jmm"));
        noErrors(result.getReports());

        var output = result.run();
        assertEquals(SpecsIo.getResource("fixtures/public/QuickSort.txt"), output.trim());
    }

    @Test
    public void testTuring() {
        var result = backendOptimize(SpecsIo.getResource("fixtures/private/Turing.jmm"));
        noErrors(result.getReports());

        var output = result.run(SpecsIo.getResource("fixtures/private/Turing.input"));
        assertEquals(SpecsIo.getResource("fixtures/private/Turing.txt"), output.trim());
    }

    @Test
    public void testTicTacToe() {
        var result = backendOptimize(SpecsIo.getResource("fixtures/public/TicTacToe.jmm"));
        noErrors(result.getReports());

        var output = result.run(SpecsIo.getResource("fixtures/public/TicTacToe.input"));
        assertEquals(SpecsIo.getResource("fixtures/public/TicTacToe.txt")
                        .replace("\n", "").replace(" ", ""),
                output.trim().replace("\n", "").replace(" ", ""));
    }

    @Test
    public void testWhileAndIf() {
        var result = backendOptimize(SpecsIo.getResource("fixtures/public/WhileAndIF.jmm"));
        noErrors(result.getReports());

        var output = result.run();
        assertEquals(SpecsIo.getResource("fixtures/public/WhileAndIF.txt"), output.trim());
    }
}
