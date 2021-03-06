import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;

import java.io.File;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

public class SemanticTest {
    @Test
    public void testArrayIndices() {
        String code = "";
        File f = new File("test/fixtures/public/fail/semantic/arr_index_not_int.jmm");
        try {
            Scanner scanner = new Scanner(f);
            code = scanner.useDelimiter("\\Z").next();
        } catch (Exception e) {
            System.out.println("File not found.");
        }

        JmmSemanticsResult result = TestUtils.analyse(code);
        assertEquals(1, TestUtils.getNumErrors(result.getReports()));
    }

    @Test
    public void testArraySizeInteger() {
        String code = "";
        File f = new File("test/fixtures/public/fail/semantic/arr_size_not_int.jmm");
        try {
            Scanner scanner = new Scanner(f);
            code = scanner.useDelimiter("\\Z").next();
        } catch (Exception e) {
            System.out.println("File not found.");
        }

        JmmSemanticsResult result = TestUtils.analyse(code);


        assertEquals(1, TestUtils.getNumErrors(result.getReports()));
    }

    @Test
    public void testArgumentsPassedToMethods() {
        String code = "";
        File f = new File("test/fixtures/public/fail/semantic/badArguments.jmm");
        try {
            Scanner scanner = new Scanner(f);
            code = scanner.useDelimiter("\\Z").next();
        } catch (Exception e) {
            System.out.println("File not found.");
        }

        JmmSemanticsResult result = TestUtils.analyse(code);
        assertEquals(3, TestUtils.getNumErrors(result.getReports()));
    }

    @Test
    public void testBinaryOperation() {
        String code = "";
        File f = new File("test/fixtures/public/fail/semantic/binop_incomp.jmm");
        try {
            Scanner scanner = new Scanner(f);
            code = scanner.useDelimiter("\\Z").next();
        } catch (Exception e) {
            System.out.println("File not found.");
        }

        JmmSemanticsResult result = TestUtils.analyse(code);
        assertEquals(1, TestUtils.getNumErrors(result.getReports()));
    }

    @Test
    public void testInexistentMethod() {
        String code = "";
        File f = new File("test/fixtures/public/fail/semantic/funcNotFound.jmm");
        try {
            Scanner scanner = new Scanner(f);
            code = scanner.useDelimiter("\\Z").next();
        } catch (Exception e) {
            System.out.println("File not found.");
        }

        JmmSemanticsResult result = TestUtils.analyse(code);
        assertEquals(2, TestUtils.getNumErrors(result.getReports()));
    }

    @Test
    public void testLengthOnSimpleTypes() {
        String code = "";
        File f = new File("test/fixtures/public/fail/semantic/simple_length.jmm");
        try {
            Scanner scanner = new Scanner(f);
            code = scanner.useDelimiter("\\Z").next();
        } catch (Exception e) {
            System.out.println("File not found.");
        }

        JmmSemanticsResult result = TestUtils.analyse(code);
        assertEquals(1, TestUtils.getNumErrors(result.getReports()));
    }

    @Test
    public void testAssignmentRightHandSideExpression() {
        String code = "";
        File f = new File("test/fixtures/public/fail/semantic/var_exp_incomp.jmm");
        try {
            Scanner scanner = new Scanner(f);
            code = scanner.useDelimiter("\\Z").next();
        } catch (Exception e) {
            System.out.println("File not found.");
        }

        JmmSemanticsResult result = TestUtils.analyse(code);
        assertEquals(1, TestUtils.getNumErrors(result.getReports()));
    }

    @Test
    public void testAssignmentRightHandSideLiteral() {
        String code = "";
        File f = new File("test/fixtures/public/fail/semantic/var_lit_incomp.jmm");
        try {
            Scanner scanner = new Scanner(f);
            code = scanner.useDelimiter("\\Z").next();
        } catch (Exception e) {
            System.out.println("File not found.");
        }

        JmmSemanticsResult result = TestUtils.analyse(code);
        assertEquals(1, TestUtils.getNumErrors(result.getReports()));
    }

    @Test
    public void testVariableUndefined() {
        String code = "";
        File f = new File("test/fixtures/public/fail/semantic/var_undef.jmm");
        try {
            Scanner scanner = new Scanner(f);
            code = scanner.useDelimiter("\\Z").next();
        } catch (Exception e) {
            System.out.println("File not found.");
        }

        JmmSemanticsResult result = TestUtils.analyse(code);
        assertEquals(1, TestUtils.getNumErrors(result.getReports()));
    }

    @Test
    public void testVariableNotInitialized() {
        String code = "";
        File f = new File("test/fixtures/public/fail/semantic/extra/var_not_init.jmm");
        try {
            Scanner scanner = new Scanner(f);
            code = scanner.useDelimiter("\\Z").next();
        } catch (Exception e) {
            System.out.println("File not found.");
        }

        JmmSemanticsResult result = TestUtils.analyse(code);

        int errors = 0;
        int warnings = 0;
        for (Report report : result.getReports()) {
            if (report.getType() == ReportType.WARNING) warnings++;
            else if (report.getType() == ReportType.ERROR) errors++;
        }
        assertEquals(1, warnings);
        assertEquals(0, errors);
    }

    @Test
    public void testSimpleVariableNotInitialized() {
        String code = "";
        File f = new File("test/fixtures/public/fail/semantic/extra/simple_var_not_init.jmm");
        try {
            Scanner scanner = new Scanner(f);
            code = scanner.useDelimiter("\\Z").next();
        } catch (Exception e) {
            System.out.println("File not found.");
        }

        JmmSemanticsResult result = TestUtils.analyse(code);

        int errors = 0;
        int warnings = 0;
        for (Report report : result.getReports()) {
            if (report.getType() == ReportType.WARNING) warnings++;
            else if (report.getType() == ReportType.ERROR) errors++;
        }
        assertEquals(2, warnings);
        assertEquals(0, errors);
    }
}
