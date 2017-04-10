package pipe.dataLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Token implements Comparable<Token> {
	private static final String[] TEMPLATES = new String[] { "%s", "\"%s\"" };
	DataType tokenType;
	public Vector<BasicType> Tlist;
	boolean isDef;
	private String mDisplayString = null;

	public Token() {
		DataType newType = new DataType();
		definetype(newType);
	}

	public Token(DataType input) {
		definetype(input);
	}

	// public Token(boolean[] kind, boolean _isPow)
	// {
	// definetype(kind, _isPow);
	// }

	public void definetype(DataType input) {
		tokenType = input;
		isDef = true;
		Tlist = new Vector<BasicType>();
		mDisplayString = null;
		// System.out.println("The size of Tlist is: "+Tlist.size());
		// defineTlist(tokenType);
	}

	/**
	 * To initialize Tlist when creating new Vector of BasicType, otherwise the
	 * vector is null. Called by Transition.getToken, so that the null token in
	 * symbol table get from arc out places can be used as a token;
	 * 
	 * @param tokenType
	 */
	public void defineTlist(DataType tokenType) {
		for (int i = 0; i < tokenType.getNumofElement(); i++) {
			Tlist.add(new BasicType(tokenType.getTypebyIndex(i)));
		}
		mDisplayString = null;
	}

	// public void definetype(boolean[] kind, boolean _isPow)
	// {
	// tokenType = new DataType();
	// tokenType.NtranslateFrom(kind);
	// tokenType.setPow(_isPow);
	// isDef = true;
	// Tlist = new Vector(kind.length);
	// }

	public boolean checkElementType(int index) {
		if (tokenType.getTypebyIndex(index) != Tlist.get(index).kind)
			return false;
		return true;
	}

	// public boolean add(BasicType[] bt)
	// {
	//
	// if(!isDef)
	// return false;
	//// if(bt.length != tokenType.getNumofElement())
	//// return false;
	// System.out.println(isDef);
	// for(int i = 0; i < bt.length; i ++)
	// {
	//// if(tokenType.getTypebyIndex(i) != bt[i].kind)
	//// return false;
	// Tlist.add(i, bt[i]);
	// }
	// return true;
	// }

	public boolean add(BasicType[] bt) {

		for (int i = 0; i < bt.length; i++) {
			Tlist.add(bt[i]);
		}
		mDisplayString = null;
		return true;
	}

	public void delete(int index) {
		if (index < Tlist.size())
			Tlist.remove(index);
	}

	public void delete() {
		Tlist.clear();
		mDisplayString = null;
	}

	public DataType getTokentype() {
		return tokenType;
	}

	public String displayToken(boolean pSeparated) {
		if (mDisplayString == null) {
			StringBuilder sb = new StringBuilder();
			for (final BasicType basicType : Tlist) {
				sb.append(String.format(TEMPLATES[basicType.kind], basicType.getValueAsString())).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			mDisplayString = sb.toString();
		}

		return pSeparated ? String.format("<%s>", mDisplayString) : mDisplayString;
	}

	public BasicType getBTbyindex(int index) {
		return Tlist.get(index);
	}

	public void UpdateDataTypeByTlist() {
		String[] typesArray = new String[Tlist.size()];
		List<String> types = new ArrayList<>();
		for (BasicType bt : Tlist) {
			types.add(BasicType.TYPES[bt.kind]);
		}
		tokenType.defineType(types.toArray(typesArray));
		mDisplayString = null;
	}

	@Override
	public int hashCode() {
		return 31 * displayToken().hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this)
			return true;
		if (obj == null || !(obj instanceof Token))
			return false;

		Token toTest = (Token) obj;

		return this.displayToken().equals(toTest.displayToken());
	}

	public String displayToken() {
		return displayToken(true);
	}

	@Override
	public int compareTo(Token o) {

		Integer a = new Integer(Tlist.elementAt(1).getValue().toString());
		Integer b = new Integer(o.Tlist.elementAt(1).getValue().toString());
		System.out.println(Tlist.elementAt(0).getValue());

		// Integer c = new Integer(Tlist.elementAt(2).getValue().toString());
		// Integer d = new Integer(o.Tlist.elementAt(2).getValue().toString());
		//
		// int i = a.compareTo(b);
		// if (i != 0) return i;
		//
		// return c.compareTo(d);
		return a.compareTo(b);
	}

	@Override
	public String toString() {
		// return ".......{" + "LowerBound=" +
		// Tlist.elementAt(1).getValue().toString() + "UpperBound="
		// + Tlist.elementAt(2).getValue().toString() + '}';
		return ".......{" + "LowerBound=" + Tlist.elementAt(1).getValue().toString() + '}';
	}

}
