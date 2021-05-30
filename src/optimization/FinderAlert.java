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

    public FinderAlert(Element element, FinderAlertType type) {
        this.element = element;
        this.type = type;
        this.value = -1;
    }

    public FinderAlert(int value) {
        this.value = value;
        this.type = FinderAlertType.SUCCESSOR;
        this.element = null;
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
}
