package formula.absyntree;

import formula.parser.Visitor;

public class AtFormula extends Formula{

	public AtomicFormula af;
	public String str = "";
	public int treeLevel;
	//z3
	public boolean isPostCond;
	public boolean isValidClause;
	
	public AtFormula(int p, AtomicFormula af){
		this.pos = p;
		this.af = af;
	}
	
	public void accept(Visitor v){
		v.visit(this);
	}
}
