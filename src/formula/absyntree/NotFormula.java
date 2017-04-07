package formula.absyntree;
import formula.parser.Visitor;
public class NotFormula extends AtomicFormula{
	public Formula f;
	public String str = "";
	
	public NotFormula(int p, Formula f){
		pos = p;
		this.f  = f;
	}
	public void accept(Visitor v){
		v.visit(this);
	}
}
