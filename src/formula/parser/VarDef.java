package formula.parser;


public class VarDef {
	public String strVar;
	public PromelaType type;

	@Override
	public String toString()
	{
		return String.format("%s %s;%n", type.toString().toLowerCase(), strVar);
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
		if (obj == null || getClass() != obj.getClass())
			return false;

		VarDef other = (VarDef) obj;

		boolean isStrEquals = strVar == null && other.strVar == null || strVar != null && strVar.equals(other.strVar);
		boolean isTypeEquals = type == other.type;

		return isStrEquals && isTypeEquals;
	}
}
