
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.StringReader;
import java.util.List;

import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsIo;
import visitor.*;
import utils.Logger;

public class Main implements JmmParser, JmmAnalysis {

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

	@Override
	public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
		// TODO change
		JmmNode root = parserResult.getRootNode();
		VisitorController controller = new VisitorController(root);
		controller.start();

		return new JmmSemanticsResult(parserResult, controller.getTable(), controller.getReports());
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
		for (Report report: reports) {
			if (count >= limit) return;
			System.out.println(report);
			count++;
		}
	}

	public static void main(String[] args) {
        System.out.println("Executing with args: " + Arrays.toString(args));
        if (args.length < 1) {
			System.err.println("I need at least the path of the file you want to parse. If you want, you can also specify the maximum number of errors you want me to report in the second argument.");
		}

        int maxErrNo = args.length > 1 ? Integer.parseInt(args[1]) : 10;
		String jmmCode = SpecsIo.read(args[0]);

		Main main = new Main();
		JmmParserResult parserResult = main.parse(jmmCode);

		File output = new File("tree.json");
		SpecsIo.write(output, parserResult.toJson());

		List<Report> globalReports = new ArrayList<>(parserResult.getReports());

		boolean success = false;
		if (!containsErrorReport(parserResult.getReports())) {
			JmmSemanticsResult semanticsResult = main.semanticAnalysis(parserResult);
			globalReports.addAll(semanticsResult.getReports());

			if (!containsErrorReport(semanticsResult.getReports())) {
				OllirResult ollirResult = new OptimizationStage().toOllir(semanticsResult);
				globalReports.addAll(ollirResult.getReports());

				if (!containsErrorReport(ollirResult.getReports())) {
					JasminResult jasminResult = new BackendStage().toJasmin(ollirResult);
					globalReports.addAll(jasminResult.getReports());

					if (!containsErrorReport(jasminResult.getReports())) success = true;
				}
			}
		}

		logReports(globalReports, maxErrNo);

		if (success) System.out.println("Jasmin code generated with success!");
    }
}