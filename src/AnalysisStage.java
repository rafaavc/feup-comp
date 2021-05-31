import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import visitor.VisitorController;

public class AnalysisStage implements JmmAnalysis {

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        VisitorController controller = new VisitorController(parserResult.getRootNode());
        controller.start();

        return new JmmSemanticsResult(parserResult, controller.getTable(), controller.getReports());
    }
}