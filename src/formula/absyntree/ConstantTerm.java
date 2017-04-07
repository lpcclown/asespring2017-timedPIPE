package formula.absyntree;

import formula.parser.Visitor;
import pipe.dataLayer.Token;

public class ConstantTerm extends Term{
	public Constant c;
	public String var_key;

	//z3
	public String z3str = "";
	public ConstantTerm(int p, Constant c){
		this.pos = p;
		this.c = c;
	}
	public void accept(Visitor v){
		v.visit(this);
	}

	public boolean isValidAsToken() {
		return c.isValidAsToken();
	}

	public Token toToken() {
		return c.toToken();
	}
}
