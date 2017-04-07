package formula.absyntree;
import formula.parser.Visitor;
public class Num {
	public String n;
	public double d;
	public Num(String n){
		this.n = n;
	}
	public void accept(Visitor v){
		v.visit(this);
	}
}
