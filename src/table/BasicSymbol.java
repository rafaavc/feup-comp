package table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class BasicSymbol extends Symbol {
    private boolean init;

    public BasicSymbol(Type type, String name) {
        super(type, name);
        init = false;
    }

    public boolean isInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }
}
