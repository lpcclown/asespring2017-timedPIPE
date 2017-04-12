package pipe.dataLayer;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class BasicType {
	public static final int NUMBER = 0;
	public static final int STRING = 1;
	public static final int TIMELOWERBOUND = 2;
	// public static final int TIMEUPPERBOUND = 3;
	public static final String[] TYPES = new String[] { "number", "string", "arrivingTime" };

	public final int kind;// 0 is int, 1 is string
	private Object mValue;

	public BasicType(final int pKind) {
		this(pKind, "");
	}

	public BasicType(final int pKind, final String pValue) {
		kind = pKind;
		setValue(pValue);
	}

	public BasicType(int pKind, int pInt, String pString) {
		this.kind = pKind;
		if (kind == 0) {
			setValue(pInt);
		} else {
			setValue(pString);
		}
	}

	public Object getValue() {
		return mValue;
	}

	public String getValueAsString() {
		return mValue.toString();
	}

	public int getValueAsInt() {
		return kind == NUMBER ? ((BigDecimal) mValue).intValue() : -1;
	}

	public double getValueAsDouble() {
		return kind == NUMBER ? ((BigDecimal) mValue).doubleValue() : -1.0;
	}

	public void setValue(final int pIntValue) {
		// if (kind != NUMBER) {
		// throw new IllegalArgumentException("Type mismatch: cannot accept
		// value other than int type");
		// }

		mValue = new BigDecimal(pIntValue);
	}

	public void setValue(final String pStringValue) {
		if (kind == NUMBER) {
			mValue = new BigDecimal(pStringValue.length() == 0 ? "0" : pStringValue);
		} else {
			mValue = pStringValue;
		}
	}

	public void setValue(final Number pValue) {
		mValue = kind == NUMBER ? pValue : pValue.toString();
	}

	public void setValue(final Object pObject) {
		setValue(pObject.toString());
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31).append(mValue).append(kind).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}

		if (obj == null || !(obj instanceof BasicType)) {
			return false;
		}

		BasicType toTest = (BasicType) obj;
		return kind == toTest.kind && mValue.equals(toTest.mValue);
	}
}
