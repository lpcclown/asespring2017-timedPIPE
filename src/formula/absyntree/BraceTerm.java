package formula.absyntree;

import formula.parser.Visitor;

public class BraceTerm extends SetExp{
	public Term t;
	public String varName;
	public boolean isUserVariable = false;

	public BraceTerm(int p, Term t){
		this.pos = p;
		this.t = t;
	}
	public void accept(Visitor v){
		v.visit(this);
	}
}
