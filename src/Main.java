
import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.Arrays;
import java.util.ArrayList;
import java.io.StringReader;
import java.util.List;

import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsIo;

public class Main implements JmmParser {


	public JmmParserResult parse(String jmmCode) {
		try {
			// Replace with parser class
		    Jmm parser = new Jmm(new StringReader(jmmCode));
    		SimpleNode root = parser.Program(); // returns reference to root node
            	
    		root.dump(""); // prints the tree on the screen

    		return new JmmParserResult(root, parser.getReports());

		} catch(Exception e) {  //  Only for the rare case when an exception isn't a ParseException (parse exceptions are caught before)
			List<Report> reports = new ArrayList<>();
			reports.add(new Report(ReportType.ERROR, Stage.SYNTATIC, 0, e.getMessage()));
			return new JmmParserResult(null, reports);
		}
	}

    public static void main(String[] args) {
        System.out.println("Executing with args: " + Arrays.toString(args));
		String jmmCode = SpecsIo.read(args[0]);
		JmmParser parser = new Main();
		JmmParserResult result = parser.parse(jmmCode);

		List<Report> reports = result.getReports();
		for (Report report : reports.subList(0, Math.min(reports.size(), 10))) {
			System.out.println(report.getMessage());
		}
    }
}