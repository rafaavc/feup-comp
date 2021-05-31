package optimization;

import org.specs.comp.ollir.Element;

public class FinderAlert {
    public enum FinderAlertType {
        USE,
        DEF,
        SUCCESSOR
    }

    private final int value;
    private final FinderAlertType type;
    private final Element element;
    private final boolean arrayAccess;

    public FinderAlert(Element element, FinderAlertType type, boolean arrayAccess) {
        this.element = element;
        this.type = type;
        this.value = -1;
        this.arrayAccess = arrayAccess;
    }

    public FinderAlert(Element element, FinderAlertType type) {
        this.element = element;
        this.type = type;
        this.value = -1;
        this.arrayAccess = false;
    }

    public FinderAlert(int value) {
        this.value = value;
        this.type = FinderAlertType.SUCCESSOR;
        this.element = null;
        this.arrayAccess = false;
    }

    public FinderAlertType getType() {
        return type;
    }

    public Element getElement() {
        return element;
    }

    public int getValue() {
        return value;
    }

    public boolean isArrayAccess() {
        return arrayAccess;
    }
}
