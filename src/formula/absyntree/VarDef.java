package formula.absyntree;


public class VarDef {
	public String strVar;
	public PromelaType type;

	@Override
	public String toString()
	{
		String ret = "";
		switch (type)
		{
		case pSHORT: ret = "short " + strVar; break;
		case pINT:   ret = "int " + strVar; break;
		default: break;
		}
		return ret + ";\n";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((strVar == null) ? 0 : strVar.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VarDef other = (VarDef) obj;
		if (strVar == null) {
			if (other.strVar != null)
				return false;
		} else if (!strVar.equals(other.strVar))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
