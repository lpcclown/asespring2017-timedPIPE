package formula.absyntree;

import formula.parser.Visitor;

public class CpFormula extends Formula{

	public CompoundFormula cf;
	public int treeLevel;
	
	public CpFormula(int p, CompoundFormula cf){
		this.pos = p;
		this.cf = cf;
	}
	
	public void accept(Visitor v){
		v.visit(this);
	}

}
