
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
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

import java.util.*;

import static org.junit.Assert.*;

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
        JasminResult jasmin = TestUtils.backend(optimizationStage.optimize(ollirResult, 1));

        TestUtils.noErrors(ollirResult.getReports());

        String output = jasmin.run();
        assertEquals("1\n2\n2\n5\n5\n", output);
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

        JasminResult jasmin = TestUtils.backend(optimizationStage.optimize(ollirResult, 1));

        var output = jasmin.run();

        assertEquals("Result: 28", output.trim());
    }
}
