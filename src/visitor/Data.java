package visitor;

import pt.up.fe.comp.jmm.report.Report;
import table.scopes.Scoped;

import java.util.List;

public class Data {
    Scoped scope;
    List<Report> reports;

    public Data(Scoped scope, List<Report> reports) {
        this.scope = scope;
        this.reports = reports;
    }

    public Scoped getScope() {
        return scope;
    }

    public List<Report> getReports() {
        return reports;
    }
}
