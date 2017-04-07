package formula.absyntree;
import formula.parser.Visitor;
public class Nexists extends Quantifier{
//	public int quant_type; // 0 is ForAll, 1 is Exists, 2 is Nexists
	public Nexists(int p){
		this.pos = p;
	}
	public void accept(Visitor v){
		v.visit(this);
	}
}
