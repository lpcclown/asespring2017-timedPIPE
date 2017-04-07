package formula.absyntree;

import formula.parser.Visitor;
import org.apache.commons.lang.builder.ToStringBuilder;

public class VariableTerm extends Term{
  public static final int ID = 0;
  public static final int INDEX = 1;

	public Variable v;
	public String var_key;
	//IMPORTANT:: think three times before changing the initialization of the member variable index.
	// Do not do it unless you are sure what you are doing and what its implication will be
	public int index = 1;
	public final int kind; //0 is id_variabe; 1 is index_variable
	public boolean postcond = false;
	public boolean isPowerSet = false;
	public boolean isUserVariable = false;
	public int size;
	public String pVarName;
//
	//z3
	public String z3str = "";
	public boolean isPostCond;

	public VariableTerm(int p, Variable v){
		this.pos = p;
		this.v = v;
		kind = v instanceof IdVariable ? ID : INDEX;
	}
	public void accept(Visitor v){
		v.visit(this);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("Variable", v)
				.append("var_key", var_key)
				.append("kind", kind)
				.append("isUserVariable", isUserVariable)
				.append("varName", pVarName).toString();
	}
}
