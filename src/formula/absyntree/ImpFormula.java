package formula.absyntree;
import formula.parser.Visitor;
public class ImpFormula extends CompoundFormula{

	public Formula f1,f2;

	public ImpFormula(int p, Formula if1, Formula if2){
		pos = p;
		f1=if1;
		f2=if2;
	}
	public void accept(Visitor v){
		v.visit(this);
	}
}
