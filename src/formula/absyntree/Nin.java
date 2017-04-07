package formula.absyntree;
import formula.parser.Visitor;
public class Nin extends Domain{

	public Nin(int p){
		this.pos = p;
	}
	public void accept(Visitor v){
		v.visit(this);
	}
}
