
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
}
