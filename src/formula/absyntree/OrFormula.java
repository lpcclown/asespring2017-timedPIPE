package formula.absyntree;
import formula.parser.Visitor;
public class OrFormula extends CompoundFormula{
	public Formula f1,f2;

	public OrFormula(int p, Formula of1, Formula of2){
		pos = p;
		f1=of1;
		f2=of2;
	}
	public void accept(Visitor v){
		v.visit(this);
	}
}
