
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
    	
    		return new JmmParserResult(root, new ArrayList<>());
		} catch(ParseException e) {
			throw new RuntimeException("Error while parsing", e);
		}
	}

    public static void main(String[] args) {
        System.out.println("Executing with args: " + Arrays.toString(args));
		String jmmCode = SpecsIo.read(args[0]);
		JmmParser parser = new Main();
		parser.parse(jmmCode);
    }
}