package pipe.dataLayer.converter;

import java.util.HashMap;

public enum PTNElement {
	PLACE("place"),
	TRANSITION("transition"),
	ARC("arc"),
	LABELS("labels"),
	DEFINITION("definitions");

	private String mValue;

	PTNElement(final String pValue) {
		this.mValue = pValue;
	}
	
	public String getValue() {
		return mValue;
	}
	
	@Override
	public String toString() {
		return mValue;
	}

	private static final HashMap<String, PTNElement> sUniverse = new HashMap<>();
	static {
		for (PTNElement element : PTNElement.values()) {
			sUniverse.put(element.getValue(), element);
		}
	}

	public static PTNElement valueFrom(final String pValue) {
		return sUniverse.get(pValue);
	}
}
