package pipe.dataLayer;

import java.util.UUID;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

public class DataType {
	private static final String PLAIN_TEMPLATE = "<%s>";
	private static final String POW_TEMPLATE = "P(" + PLAIN_TEMPLATE + ")";
	private String ID;
	private String name;
	public int Ntype;
	private Vector<String> types;
	private String mJoinedTypes;
	private boolean isPow;

	private int NumofElement;
	private boolean isDef;

	private Vector<DataType> group;

	public DataType() {
		ID = UUID.randomUUID().toString();
		types = new Vector<String>();
		Ntype = 0;
		group = null;
		isPow = false;
		isDef = false;
	}

	public DataType(String _name, String[] _types, boolean _isPow, Vector<DataType> _group) {
		ID = UUID.randomUUID().toString();
		name = _name;
		group = _group;
		isPow = _isPow;
		types = new Vector<String>();
		defineType(_types);
	}

	public void defineType(String[] t) {
		int num = 0;
		types = new Vector<String>();
		for (int i = 0; i < t.length; i++) {
			if (t[i].equals(BasicType.TYPES[BasicType.NUMBER]) || t[i].equals(BasicType.TYPES[BasicType.STRING])
					|| t[i].equals(BasicType.TYPES[BasicType.TIMELOWERBOUND])) {
				num++;
			} else {
				if (group != null)
					return;
				for (int j = 0; j < group.size(); j++) {
					if (group.get(j).getName().equals(t[i])) {
						num += group.get(j).getNumofElement();
						break;
					}
				}
			}
			types.add(t[i]);
		}

		NtranslateFrom(num);
		isDef = true;
		mJoinedTypes = "";
	}

	public int NtranslateFrom(int num) {
		Ntype = 0;
		for (int i = 0; i < types.size(); i++) {
			if (types.get(i).equals(BasicType.TYPES[1])) {
				Ntype += Math.pow(2, num - i - 1);
				// num --;
			} else if (!types.get(i).equals(BasicType.TYPES[BasicType.NUMBER])) {
				for (int j = 0; j < group.size(); j++) {
					if (group.get(j).ID.equals(types.get(i))) {
						Ntype += group.get(i).Ntype * Math.pow(2, num - i - group.get(i).getNumofElement());
						// num -= group.get(i).getNumofElement();
						break;
					}
				}
			}
		}
		NumofElement = num;
		return Ntype;
	}

	public int getTypebyIndex(int index) {
		// String binary = Integer.toBinaryString(Ntype);
		//
		// return Integer.parseInt(binary.substring(index, index+1));
		int type = BasicType.STRING;
		String typeStr = types.get(index);
		if (BasicType.TYPES[BasicType.NUMBER].equals(typeStr)) {
			type = BasicType.NUMBER;
		}
		return type;
	}

	public void setNumofElement(int num) {
		NumofElement = num;
	}

	public int getNumofElement() {
		return NumofElement;
	}

	public void setPow(boolean _ispow) {
		isPow = _ispow;
	}

	public boolean getPow() {
		return isPow;
	}

	public void setName(String n) {
		name = n;
	}

	public String getName() {
		return name;
	}

	public void setGroup(Vector<DataType> _group) {
		group = _group;
	}

	public Vector<DataType> getGroup() {
		return this.group;
	}

	public void setTypes(Vector<String> _types) {
		types = _types;
		mJoinedTypes = "";
	}

	public Vector<String> getTypes() {
		return types;
	}

	public void setDef(boolean _isDef) {
		isDef = _isDef;
	}

	public boolean getDef() {
		return isDef;
	}

	public void setNtype(int _Ntype) {
		Ntype = _Ntype;
	}

	public int getNtype() {
		return Ntype;
	}

	public String getStringRepresentation() {
		String template = PLAIN_TEMPLATE;
		if (isPow) {
			template = POW_TEMPLATE;
		}

		return String.format(template, getJoinedTypes());
	}

	public String getJoinedTypes() {
		if (StringUtils.isBlank(mJoinedTypes)) {
			mJoinedTypes = StringUtils.join(types, ", ");
		}

		return mJoinedTypes;
	}

	public Token buildTokens(final String[] pTokens) {
		try {
			BasicType[] tokens = new BasicType[types.size()];
			for (int i = 0; i < tokens.length; i++) {
				BasicType basicType = new BasicType(getTypebyIndex(i), pTokens[i].trim());
				tokens[i] = basicType;
			}
			Token token = new Token(this);
			token.add(tokens);
			return token;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
