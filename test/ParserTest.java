import static org.junit.Assert.*;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;
import java.io.StringReader;


import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.TestUtils;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

public class ParserTest {

  @Test
  public void testNoReports() {
    List<String> files = new ArrayList<>();
    files.add("test/fixtures/public/FindMaximum.jmm");
    files.add("test/fixtures/public/HelloWorld.jmm");
    files.add("test/fixtures/public/Lazysort.jmm");
    files.add("test/fixtures/public/Life.jmm");
    files.add("test/fixtures/public/MonteCarloPi.jmm");
    files.add("test/fixtures/public/QuickSort.jmm");
    files.add("test/fixtures/public/Simple.jmm");
    files.add("test/fixtures/public/TicTacToe.jmm");
    files.add("test/fixtures/public/WhileAndIf.jmm");


    String code = "";

    for (String filename : files) {
      File f = new File(filename);

      try {
        Scanner scanner = new Scanner(f);
        code = scanner.useDelimiter("\\Z").next();
      } catch (Exception e) {
        System.out.println("File " + filename + " not found.");
      }

      JmmParserResult result = TestUtils.parse(code);
      TestUtils.noErrors(result.getReports());
    }
  }

  @Test
  public void testBlowUp() {
    String code = "";
    File f = new File("test/fixtures/public/fail/syntactical/BlowUp.jmm");
    try {
      Scanner scanner = new Scanner(f);
      code = scanner.useDelimiter("\\Z").next();
    } catch (Exception e) {
      System.out.println("File not found.");
    }

    JmmParserResult result = TestUtils.parse(code);
    TestUtils.mustFail(result.getReports());
  }

  @Test
  public void testCompleteWhileTest() {
    String code = "";
    File f = new File("test/fixtures/public/fail/syntactical/CompleteWhileTest.jmm");
    try {
      Scanner scanner = new Scanner(f);
      code = scanner.useDelimiter("\\Z").next();
    } catch (Exception e) {
      System.out.println("File not found.");
    }

    JmmParserResult result = TestUtils.parse(code);
    assertEquals(11, TestUtils.getNumErrors(result.getReports()));
  }

  @Test
  public void testLengthError() {
    String code = "";
    File f = new File("test/fixtures/public/fail/syntactical/LengthError.jmm");
    try {
      Scanner scanner = new Scanner(f);
      code = scanner.useDelimiter("\\Z").next();
    } catch (Exception e) {
      System.out.println("File not found.");
    }

    JmmParserResult result = TestUtils.parse(code);
    assertEquals(1, TestUtils.getNumErrors(result.getReports()));
  }
  
  @Test
  public void testMissingRightPar() {
    String code = "";
    File f = new File("test/fixtures/public/fail/syntactical/MissingRightPar.jmm");
    try {
      Scanner scanner = new Scanner(f);
      code = scanner.useDelimiter("\\Z").next();
    } catch (Exception e) {
      System.out.println("File not found.");
    }

    JmmParserResult result = TestUtils.parse(code);
    assertEquals(1, TestUtils.getNumErrors(result.getReports()));
  }

  @Test
  public void testMultipleSequential() {
    String code = "";
    File f = new File("test/fixtures/public/fail/syntactical/MultipleSequential.jmm");
    try {
      Scanner scanner = new Scanner(f);
      code = scanner.useDelimiter("\\Z").next();
    } catch (Exception e) {
      System.out.println("File not found.");
    }

    JmmParserResult result = TestUtils.parse(code);
    assertEquals(2, TestUtils.getNumErrors(result.getReports()));
  }
  
  @Test
  public void testNestedLoop() {
    String code = "";
    File f = new File("test/fixtures/public/fail/syntactical/NestedLoop.jmm");
    try {
      Scanner scanner = new Scanner(f);
      code = scanner.useDelimiter("\\Z").next();
    } catch (Exception e) {
      System.out.println("File not found.");
    }

    JmmParserResult result = TestUtils.parse(code);
    assertEquals(2, TestUtils.getNumErrors(result.getReports()));
  }

  @Test
  public void testSemanticsAnalyse() {
    List<String> files = new ArrayList<>();
    files.add("test/fixtures/public/FindMaximum.jmm");
    files.add("test/fixtures/public/HelloWorld.jmm");
    files.add("test/fixtures/public/Lazysort.jmm");
    files.add("test/fixtures/public/Life.jmm");
    files.add("test/fixtures/public/MonteCarloPi.jmm");
    files.add("test/fixtures/public/QuickSort.jmm");
    files.add("test/fixtures/public/Simple.jmm");
    files.add("test/fixtures/public/TicTacToe.jmm");
    files.add("test/fixtures/public/WhileAndIf.jmm");


    String code = "";

    for (String filename : files) {
      File f = new File(filename);

      try {
        Scanner scanner = new Scanner(f);
        code = scanner.useDelimiter("\\Z").next();
      } catch (Exception e) {
        System.out.println("File " + filename + " not found.");
      }

      JmmSemanticsResult semanticsResult = TestUtils.analyse(code);
      TestUtils.noErrors(semanticsResult.getReports());
    }
  }
}
