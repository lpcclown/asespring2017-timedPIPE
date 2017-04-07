package formula.absyntree;

import formula.parser.Visitor;

public class CpxFormula extends Formula{

	public ComplexFormula cpf;
	public int treeLevel; // start from 1

	public CpxFormula(int p, ComplexFormula cpf){
		this.pos = p;
		this.cpf = cpf;
	}
	
	public void accept(Visitor v){
		v.visit(this);
	}

}
