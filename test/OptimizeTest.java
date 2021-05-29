
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
import optimization.LivenessResult;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.*;

public class OptimizeTest {

    @Test
    public void testHelloWorld() {
        //var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
        //TestUtils.noErrors(result.getReports());
    }

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
    }
}
