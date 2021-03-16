
import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.Arrays;
import java.util.ArrayList;
import java.io.StringReader;

import pt.up.fe.specs.util.SpecsIo;

public class Main implements JmmParser {


	public JmmParserResult parse(String jmmCode) {

		try {
			// Replace with parser class
		    Jmm parser = new Jmm(new StringReader(jmmCode));
    		SimpleNode root = parser.Program(); // returns reference to root node
            	
    		root.dump(""); // prints the tree on the screen
    	
    		return new JmmParserResult(root, new ArrayList<Report>());
		} catch(ParseException e) {
			throw new RuntimeException("Error while parsing", e);
		}
	}

    public static void main(String[] args) {
        System.out.println("Executing with args: " + Arrays.toString(args));

		String jmmCode = "import ioPlus;\n" +
				"class FindMaximum {\n" +
				"\tint[] test_arr;\n" +
				"\n" +
				"\tpublic int find_maximum(int[] arr) {\n" +
				"\t\tint i;\n" +
				"\t\tint maximum;\n" +
				"\t\tint value;\n" +
				"\n" +
				"\t\ti = 1;\n" +
				"\t\tmaximum = arr[0];\n" +
				"\t\twhile (i < arr.length) {\n" +
				"\t\t\tvalue = arr[i];\n" +
				"\t\t\tif (maximum < value) {\n" +
				"\t\t\t\tmaximum = value;\n" +
				"\t\t\t} else {\n" +
				"\t\t\t}\n" +
				"\t\t\ti = i + 1;\n" +
				"\t\t}\n" +
				"\n" +
				"\t\treturn maximum;\n" +
				"\t}\n" +
				"\n" +
				"\tpublic int build_test_arr() {\n" +
				"\t\ttest_arr = new int[5];\n" +
				"\t\ttest_arr[0] = 14;\n" +
				"\t\ttest_arr[1] = 28;\n" +
				"\t\ttest_arr[2] = 0;\n" +
				"\t\ttest_arr[3] = 0-5; // No unary minus in Java--\n" +
				"\t\ttest_arr[4] = 12;\n" +
				"\n" +
				"\t\treturn 0;\n" +
				"\t}\n" +
				"\n" +
				"\tpublic int[] get_array() {\n" +
				"\t\treturn test_arr;\n" +
				"\t}\n" +
				"\n" +
				"\tpublic static void main(String[] args) {\n" +
				"\t\tFindMaximum fm;\n" +
				"\t\tint max;\n" +
				"\t\tfm = new FindMaximum();\n" +
				"\t\tfm.build_test_arr();\n" +
				"\n" +
				"\t\tmax = fm.find_maximum(fm.get_array());\n" +
				"\t\tioPlus.printResult(max);\n" +
				"\t}\n" +
				"}";
		JmmParser parser = new Main();
		parser.parse(jmmCode);
    }


}