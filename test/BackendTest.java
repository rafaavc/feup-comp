
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class BackendTest {
    @Test
    public void testHelloWorld() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        assertEquals("Hello, World!", output.trim());
    }

    @Test
    public void testSimple() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/Simple.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        assertEquals("30", output.trim());
    }

    @Test
    public void testMethodCalls() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/created/MethodCalls.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        assertEquals("520", output.trim());
    }

    @Test
    public void testComplexExpressions() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/created/ComplexExpressions.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        assertEquals("10971", output.trim());
    }

    @Test
    public void testArrayAccess() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/created/ArrayAccess.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        assertEquals("4\n12\n1\n5", output.trim());
    }

    @Test
    public void testFac() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/Fac.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        assertEquals("3628800", output.trim());
    }

    @Test
    public void testSimpleIf() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/created/SimpleIf.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        assertEquals("1234", output.trim());
    }

    @Test
    public void testNestedIf() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/created/NestedIf.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        assertEquals("2", output.trim());
    }

    @Test
    public void testComplexExpressionIf() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/created/ComplexExpressionIf.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        assertEquals("1", output.trim());
    }

    @Test
    public void testSimpleWhile() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/created/SimpleWhile.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        assertEquals("10", output.trim());
    }

    @Test
    public void testMultipleWhile() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/created/MultipleWhile.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        assertEquals("20", output.trim());
    }

    @Test
    public void testNestedWhile() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/created/NestedWhile.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        assertEquals("100", output.trim());
    }

    @Test
    public void testFindMaximum() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        assertEquals("Result: 28", output.trim());
    }

    @Test
    public void testLazysort() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/Lazysort.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run().split("\n");
        assertEquals(output.length, 10);
    }

    @Test
    public void testLife() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/Life.jmm"));
        TestUtils.noErrors(result.getReports());

        // is entering an infinite loop?
//        var output = result.run("1\n1\n1\n1\n");
//        assertEquals("100", output.trim());
    }

    @Test
    public void testModifiedLife() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/created/ModifiedLife.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run(SpecsIo.getResource("fixtures/public/created/ModifiedLife.input"));
        assertEquals(SpecsIo.getResource("fixtures/public/created/ModifiedLife.txt"), output.trim());
    }

    @Test
    public void testMonteCarloPi() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/MonteCarloPi.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run("100000\n").trim();

        assertEquals(314, Integer.parseInt(output.substring(output.length() - 3)), 5);
    }

    @Test
    public void testQuickSort() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/QuickSort.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        assertEquals(SpecsIo.getResource("fixtures/public/QuickSort.txt"), output.trim());
    }

    @Test
    public void testTuring() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/private/Turing.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run(SpecsIo.getResource("fixtures/private/Turing.input"));
        assertEquals(SpecsIo.getResource("fixtures/private/Turing.txt"), output.trim());
    }

    @Test
    public void testTicTacToe() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/TicTacToe.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run(SpecsIo.getResource("fixtures/public/TicTacToe.input"));
        assertEquals(SpecsIo.getResource("fixtures/public/TicTacToe.txt")
                        .replace("\n", "").replace(" ", ""),
                        output.trim().replace("\n", "").replace(" ", ""));
    }

    @Test
    public void testWhileAndIf() {
        var result = TestUtils.backend(SpecsIo.getResource("fixtures/public/WhileAndIF.jmm"));
        TestUtils.noErrors(result.getReports());

        var output = result.run();
        assertEquals(SpecsIo.getResource("fixtures/public/WhileAndIF.txt"), output.trim());
    }
}
