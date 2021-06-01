import org.specs.comp.ollir.Instruction;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.jasmin.JasminUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsIo;
import utils.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.*;

public class Main implements JmmParser {

	private final static Map<ReportType, Integer> importance = new HashMap<>();
	static {
		importance.put(ReportType.ERROR, 4);
		importance.put(ReportType.WARNING, 3);
		importance.put(ReportType.DEBUG, 2);
		importance.put(ReportType.LOG, 1);
	}

	@Override
	public JmmParserResult parse(String jmmCode) {
		try {
			// Replace with parser class
			Jmm parser = new Jmm(new StringReader(jmmCode));
			SimpleNode root = parser.Program(); // returns reference to root node

			System.out.println("\n### DUMPING TREE ###");
			root.dump(""); // prints the tree on the screen

			return new JmmParserResult(root, parser.getReports());

		} catch(Exception e) {  //  Only for the rare case when an exception isn't a ParseException (parse exceptions are caught before)
			List<Report> reports = new ArrayList<>();
			reports.add(new Report(ReportType.ERROR, Stage.SYNTATIC, 0, e.getMessage()));
			return new JmmParserResult(null, reports);
		}
	}

	private static boolean containsErrorReport(List<Report> reports) {
		for (Report report : reports) if (report.getType() == ReportType.ERROR) return true;
		return false;
	}

	private static void logReports(List<Report> reports, int limit) {
		if (reports.size() == 0) {
			System.out.println("Nothing to report.");
			return;
		}
		int count = 0;

		reports.sort((Report r1, Report r2) -> importance.get(r2.getType()) - importance.get(r1.getType()));

		for (Report report: reports) {
			if (count >= limit) return;
			System.out.println(report);
			count++;
		}
	}

	public static void main(String[] args) {
		ArgsParser argsParser;
		try {
			argsParser = new ArgsParser(args);
		} catch(Exception e) {
			argsParser = null;
        	Logger.err(e.getMessage());
        	System.exit(1);
		}

		int maxErrNo = argsParser.getMaxReports();

		String[] pathParts = argsParser.getFilePath().split("[/\\\\]");
		String fileName = pathParts[pathParts.length - 1].split("\\.")[0];

		String folder = argsParser.toDir() ? "./compiled/" + fileName + "/" : "./";

		if (argsParser.isRun()) {
			String jasminCode = SpecsIo.read(argsParser.getFilePath());
			JasminResult result = new JasminResult(fileName, jasminCode, new ArrayList<>());
			logReports(result.getReports(), maxErrNo);
			if (!containsErrorReport(result.getReports())) {
				if (argsParser.hasStdin()) result.run(argsParser.getStdin());
				else result.run();
			}
			return;
		}

		String jmmCode = SpecsIo.read(argsParser.getFilePath());

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(byteArrayOutputStream);
		PrintStream old = System.out;
		System.setOut(ps);

		Main main = new Main();
		JmmParserResult parserResult = main.parse(jmmCode);

		File output = new File(folder + fileName + ".ast.json");
		SpecsIo.write(output, parserResult.toJson());

		List<Report> globalReports = parserResult.getReports();

		boolean success = false;
		if (!containsErrorReport(parserResult.getReports())) {
			AnalysisStage analysis = new AnalysisStage();
			JmmSemanticsResult semanticsResult = analysis.semanticAnalysis(parserResult);
			globalReports = semanticsResult.getReports();

			File symbolTableOutput = new File(folder + fileName + ".symbols.txt");
			SpecsIo.write(symbolTableOutput, semanticsResult.getSymbolTable().print());

			if (!containsErrorReport(semanticsResult.getReports())) {
				OptimizationStage optimizationStage = new OptimizationStage();
				OllirResult ollirResult = optimizationStage.toOllir(semanticsResult);

				// Optimization
				if (argsParser.isOptimizeO())
					ollirResult = optimizationStage.optimizeO(semanticsResult, ollirResult);

				if (argsParser.isOptimizeR())
					ollirResult = optimizationStage.optimize(ollirResult, argsParser.getOptimizeR());

				globalReports = ollirResult.getReports();

				if (!containsErrorReport(ollirResult.getReports())) {
					File ollirOutput = new File(folder + fileName + ".ollir");
					SpecsIo.write(ollirOutput, ollirResult.getOllirCode());

					JasminResult jasminResult = new BackendStage().toJasmin(ollirResult);
					globalReports = jasminResult.getReports();

					if (!containsErrorReport(jasminResult.getReports())) {
						File jasminOutput = new File(folder + fileName + ".j");
						SpecsIo.write(jasminOutput, jasminResult.getJasminCode());

						success = true;
					}
				}
			}
		}


		System.out.flush();
		System.setOut(old);
		if (argsParser.toDir()) {
			File logOutput = new File(folder + fileName + ".log");
			SpecsIo.write(logOutput, byteArrayOutputStream.toString());
		}

		logReports(globalReports, maxErrNo);
		if (success) {
			System.out.println("Jasmin code generated with success!");
			File jasminFile = new File(folder + fileName + ".j");
			File classOutput = new File(folder);
			JasminUtils.assemble(jasminFile, classOutput);
		}
    }
}